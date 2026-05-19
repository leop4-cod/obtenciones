package senadi.gob.ec.adminob.service;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import senadi.gob.ec.adminob.dao.CertificadoDepositoDAO;
import senadi.gob.ec.adminob.dao.CertificadoObtentorDAO;
import senadi.gob.ec.adminob.dao.DepositoMuestraDAO;
import senadi.gob.ec.adminob.dao.DictamenTecnicoDAO;
import senadi.gob.ec.adminob.dao.EntityManagerM;
import senadi.gob.ec.adminob.dao.ExamenDHEDAO;
import senadi.gob.ec.adminob.dao.ExpedienteDAO;
import senadi.gob.ec.adminob.dao.HistoryDAO;
import senadi.gob.ec.adminob.dao.OposicionDAO;
import senadi.gob.ec.adminob.dao.PublicacionGacetaDAO;
import senadi.gob.ec.adminob.dao.ResolucionDAO;
import senadi.gob.ec.adminob.dao.VegetableFormsDAO;
import senadi.gob.ec.adminob.enums.EstadoOposicion;
import senadi.gob.ec.adminob.enums.FlowPhase;
import senadi.gob.ec.adminob.enums.ResultadoDHE;
import senadi.gob.ec.adminob.enums.StatusFlow;
import senadi.gob.ec.adminob.enums.TipoExamenDHE;
import senadi.gob.ec.adminob.enums.TipoResolucion;
import senadi.gob.ec.adminob.model.CertificadoDeposito;
import senadi.gob.ec.adminob.model.CertificadoObtentor;
import senadi.gob.ec.adminob.model.DepositoMuestra;
import senadi.gob.ec.adminob.model.DictamenTecnico;
import senadi.gob.ec.adminob.model.ExamenDHE;
import senadi.gob.ec.adminob.model.Expediente;
import senadi.gob.ec.adminob.model.History;
import senadi.gob.ec.adminob.model.Oposicion;
import senadi.gob.ec.adminob.model.PublicacionGaceta;
import senadi.gob.ec.adminob.model.Resolucion;
import senadi.gob.ec.adminob.model.VegetableForms;

public class TramiteFlowService {

    private static final Logger LOG = Logger.getLogger(TramiteFlowService.class.getName());

    // ── VALIDACIÓN CENTRALIZADA DE TRANSICIONES ───────────────────────────────

    /**
     * Verifica que el trámite esté en alguna de las fases permitidas.
     * Lanza IllegalStateException si la fase actual no corresponde.
     * Este es el único punto de control de transiciones del sistema.
     */
    private void requirePhase(VegetableForms form, FlowPhase... allowed) {
        FlowPhase current = form.getFlowPhase();
        for (FlowPhase p : allowed) {
            if (p == current) return;
        }
        throw new IllegalStateException(
            "El trámite " + form.getApplicationNumber()
            + " está en fase [" + current + "]. "
            + "Para esta operación se requiere: " + Arrays.toString(allowed));
    }

    // ── NUMERACIÓN SECUENCIAL ATÓMICA (sin race condition) ────────────────────

    /**
     * Obtiene el siguiente número de secuencia para el tipo y año dados.
     * Usa INSERT ... ON DUPLICATE KEY UPDATE para que sea atómico incluso
     * con múltiples hilos concurrentes en MySQL 5.7.
     *
     * Formato resultante: "OV-2025-0001"
     */
    private String nextNumber(String prefix) throws Exception {
        int anio = Calendar.getInstance().get(Calendar.YEAR);
        EntityManager em = EntityManagerM.getEntityManager();
        try {
            em.getTransaction().begin();

            em.createNativeQuery(
                "INSERT INTO secuencia_numeracion (tipo, anio, ultimo) " +
                "VALUES (?, ?, 1) " +
                "ON DUPLICATE KEY UPDATE ultimo = ultimo + 1")
                .setParameter(1, prefix)
                .setParameter(2, anio)
                .executeUpdate();

            Number ultimo = (Number) em.createNativeQuery(
                "SELECT ultimo FROM secuencia_numeracion WHERE tipo = ? AND anio = ?")
                .setParameter(1, prefix)
                .setParameter(2, anio)
                .getSingleResult();

            em.getTransaction().commit();
            return String.format("%s-%d-%04d", prefix, anio, ultimo.longValue());

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // ── ASIGNACIÓN DE TÉCNICO ─────────────────────────────────────────────────

    public boolean asignarTecnico(VegetableForms form, String tecnico, String usuarioSesion) {
        try {
            requirePhase(form, FlowPhase.INITIAL, null);
            form.setAssignedUser(tecnico);
            form.setAssignedDate(new java.sql.Timestamp(System.currentTimeMillis()));
            form.setFlowPhase(FlowPhase.ASSIGNED);
            form.setStatusFlow(StatusFlow.PENDING);
            new VegetableFormsDAO(form).update();
            registrarHistoria(form.getApplicationNumber(),
                "Trámite asignado al técnico: " + tecnico, usuarioSesion);
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al asignar técnico al trámite " + form.getApplicationNumber(), e);
            return false;
        }
    }

    // ── REVISIÓN DE REQUISITOS DE FORMA ──────────────────────────────────────

    public boolean validarRequisitosForma(VegetableForms form, boolean cumple,
            String observaciones, String usuarioSesion) {
        try {
            requirePhase(form, FlowPhase.ASSIGNED, FlowPhase.FORM_REVIEW);

            if (cumple) {
                form.setFlowPhase(FlowPhase.ADMITTED);
                form.setStatusFlow(StatusFlow.PENDING);
                new VegetableFormsDAO(form).update();

                Expediente exp = buildExpediente(form, observaciones);
                new ExpedienteDAO(exp).persist();

                form.setApplicationNumber(exp.getExpedienteNumber());
                new VegetableFormsDAO(form).update();

                registrarHistoria(exp.getExpedienteNumber(),
                    "Providencia emitida. Expediente N°: " + exp.getExpedienteNumber(),
                    usuarioSesion);
            } else {
                form.setFlowPhase(FlowPhase.ARCHIVED);
                form.setStatusFlow(StatusFlow.DENIED);
                new VegetableFormsDAO(form).update();

                Expediente exp = new Expediente();
                exp.setVegetableFormId(form.getId());
                exp.setTecnico(form.getAssignedUser());
                exp.setArchivedDate(new Timestamp(System.currentTimeMillis()));
                exp.setArchivedReason(observaciones);
                new ExpedienteDAO(exp).persist();

                registrarHistoria(form.getApplicationNumber(),
                    "Expediente archivado. Motivo: " + observaciones, usuarioSesion);
            }
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al validar requisitos de forma: " + form.getApplicationNumber(), e);
            return false;
        }
    }

    // ── REVISIÓN DE FONDO ─────────────────────────────────────────────────────

    public boolean registrarRevisionFondo(VegetableForms form, String observaciones,
            String usuarioSesion) {
        try {
            requirePhase(form, FlowPhase.ADMITTED);

            form.setFlowPhase(FlowPhase.SUBSTANCE_REVIEW);
            new VegetableFormsDAO(form).update();

            Expediente exp = new ExpedienteDAO(null).getByVegetableFormId(form.getId());
            if (exp != null) {
                exp.setSubstanceReviewDate(new Timestamp(System.currentTimeMillis()));
                exp.setSubstanceObservations(observaciones);
                new ExpedienteDAO(exp).update();
            }

            registrarHistoria(form.getApplicationNumber(),
                "Revisión de fondo iniciada. Observaciones: " + observaciones, usuarioSesion);
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al registrar revisión de fondo: " + form.getApplicationNumber(), e);
            return false;
        }
    }

    public Expediente getExpedienteByFormId(Integer vegetableFormId) {
        return new ExpedienteDAO(null).getByVegetableFormId(vegetableFormId);
    }

    // ── DEPÓSITO DE MUESTRA VIVA ──────────────────────────────────────────────

    public boolean registrarDepositoMuestra(VegetableForms form, String lugarDeposito,
            String responsable, String numActa, String observaciones, String usuarioSesion) {
        try {
            requirePhase(form, FlowPhase.SUBSTANCE_REVIEW);

            DepositoMuestra dm = new DepositoMuestra();
            dm.setVegetableFormId(form.getId());
            dm.setFechaDeposito(new Timestamp(System.currentTimeMillis()));
            dm.setFechaActa(new Timestamp(System.currentTimeMillis()));
            dm.setLugarDeposito(lugarDeposito);
            dm.setResponsable(responsable);
            dm.setNumActa(numActa);
            dm.setObservaciones(observaciones);
            new DepositoMuestraDAO(dm).persist();

            form.setFlowPhase(FlowPhase.LIVE_SAMPLE_DEPOSIT);
            new VegetableFormsDAO(form).update();

            registrarHistoria(form.getApplicationNumber(),
                "Depósito de muestra viva registrado. Acta N°: " + numActa, usuarioSesion);
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al registrar depósito de muestra: " + form.getApplicationNumber(), e);
            return false;
        }
    }

    public DepositoMuestra getDepositoMuestraByFormId(Integer formId) {
        return new DepositoMuestraDAO(null).getByVegetableFormId(formId);
    }

    // ── CERTIFICADO DE DEPÓSITO ───────────────────────────────────────────────

    public boolean emitirCertificadoDeposito(VegetableForms form, Integer depositoMuestraId,
            String emitidoPor, String observaciones, String usuarioSesion) {
        try {
            requirePhase(form, FlowPhase.LIVE_SAMPLE_DEPOSIT);

            String numCert = nextNumber("CD");

            CertificadoDeposito cd = new CertificadoDeposito();
            cd.setVegetableFormId(form.getId());
            cd.setDepositoMuestraId(depositoMuestraId);
            cd.setNumCertificado(numCert);
            cd.setFechaEmision(new Timestamp(System.currentTimeMillis()));
            cd.setEmitidoPor(emitidoPor);
            cd.setObservaciones(observaciones);
            new CertificadoDepositoDAO(cd).persist();

            form.setFlowPhase(FlowPhase.DEPOSIT_CERTIFICATE);
            new VegetableFormsDAO(form).update();

            registrarHistoria(form.getApplicationNumber(),
                "Certificado de depósito de muestra viva emitido N°: " + numCert, usuarioSesion);
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al emitir certificado de depósito: " + form.getApplicationNumber(), e);
            return false;
        }
    }

    public CertificadoDeposito getCertificadoDepositoByFormId(Integer formId) {
        return new CertificadoDepositoDAO(null).getByVegetableFormId(formId);
    }

    // ── PUBLICACIÓN EN GACETA (inicia período de oposición de 30 días) ────────

    public boolean publicarEnGaceta(VegetableForms form, String denominacionGenerica,
            boolean denominacionValida, String numGaceta, String observaciones,
            String usuarioSesion) {
        try {
            requirePhase(form, FlowPhase.DEPOSIT_CERTIFICATE);

            Timestamp ahora = new Timestamp(System.currentTimeMillis());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 30);
            Timestamp finOposicion = new Timestamp(cal.getTimeInMillis());

            PublicacionGaceta pg = new PublicacionGaceta();
            pg.setVegetableFormId(form.getId());
            pg.setDenominacionGenerica(denominacionGenerica);
            pg.setDenominacionValida(denominacionValida);
            pg.setNumGaceta(numGaceta);
            pg.setFechaPublicacion(ahora);
            pg.setFechaFinOposicion(finOposicion);
            pg.setObservaciones(observaciones);
            pg.setTieneOposicion(false);
            new PublicacionGacetaDAO(pg).persist();

            // La fase pasa a OPPOSITION_PERIOD desde la publicación (no cuando llega una oposición)
            form.setFlowPhase(FlowPhase.OPPOSITION_PERIOD);
            new VegetableFormsDAO(form).update();

            registrarHistoria(form.getApplicationNumber(),
                "Publicación en gaceta N°: " + numGaceta
                + ". Plazo de oposición hasta: " + finOposicion, usuarioSesion);
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al publicar en gaceta: " + form.getApplicationNumber(), e);
            return false;
        }
    }

    public PublicacionGaceta getPublicacionGacetaByFormId(Integer formId) {
        return new PublicacionGacetaDAO(null).getByVegetableFormId(formId);
    }

    // ── GESTIÓN DE OPOSICIÓN ──────────────────────────────────────────────────

    public boolean registrarOposicion(VegetableForms form, String oponente,
            String motivo, String usuarioSesion) {
        try {
            requirePhase(form, FlowPhase.OPPOSITION_PERIOD);

            Oposicion op = new Oposicion();
            op.setVegetableFormId(form.getId());
            op.setOponente(oponente);
            op.setMotivo(motivo);
            op.setFechaPresentacion(new Timestamp(System.currentTimeMillis()));
            op.setEstado(EstadoOposicion.PRESENTADA);
            new OposicionDAO(op).persist();

            PublicacionGaceta pg = new PublicacionGacetaDAO(null).getByVegetableFormId(form.getId());
            if (pg != null) {
                pg.setTieneOposicion(true);
                new PublicacionGacetaDAO(pg).update();
            }

            // La fase permanece en OPPOSITION_PERIOD; el estado indica que hay oposición activa
            registrarHistoria(form.getApplicationNumber(),
                "Oposición registrada. Oponente: " + oponente, usuarioSesion);
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al registrar oposición: " + form.getApplicationNumber(), e);
            return false;
        }
    }

    public boolean resolverOposicion(Oposicion oposicion, EstadoOposicion estado,
            String resolucionDetalle, String resueltoPor,
            VegetableForms form, String usuarioSesion) {
        try {
            requirePhase(form, FlowPhase.OPPOSITION_PERIOD);

            oposicion.setEstado(estado);
            oposicion.setResolucionDetalle(resolucionDetalle);
            oposicion.setResueltoPor(resueltoPor);
            oposicion.setFechaResolucion(new Timestamp(System.currentTimeMillis()));
            new OposicionDAO(oposicion).update();

            form.setFlowPhase(FlowPhase.OPPOSITION_RESOLUTION);
            new VegetableFormsDAO(form).update();

            registrarHistoria(form.getApplicationNumber(),
                "Oposición resuelta. Estado: " + estado.name(), usuarioSesion);
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al resolver oposición: " + form.getApplicationNumber(), e);
            return false;
        }
    }

    /**
     * Avanza al examen DHE cuando el período de 30 días ha vencido sin oposiciones.
     * Valida que la fecha de fin del período haya pasado antes de permitir el avance.
     */
    public boolean continuarSinOposicion(VegetableForms form, String usuarioSesion) {
        try {
            requirePhase(form, FlowPhase.OPPOSITION_PERIOD);

            PublicacionGaceta pg = new PublicacionGacetaDAO(null).getByVegetableFormId(form.getId());
            if (pg != null && pg.getFechaFinOposicion() != null) {
                Timestamp ahora = new Timestamp(System.currentTimeMillis());
                if (ahora.before(pg.getFechaFinOposicion())) {
                    throw new IllegalStateException(
                        "El plazo de 30 días para oposición no ha vencido. "
                        + "Vence el: " + pg.getFechaFinOposicion()
                        + ". No se puede avanzar al examen DHE.");
                }
            }

            form.setFlowPhase(FlowPhase.DHE_EXAM);
            new VegetableFormsDAO(form).update();

            registrarHistoria(form.getApplicationNumber(),
                "Período de oposición cerrado sin oposiciones. Trámite avanza al examen DHE.",
                usuarioSesion);
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al cerrar período de oposición: " + form.getApplicationNumber(), e);
            return false;
        }
    }

    public List<Oposicion> getOposicionesByFormId(Integer formId) {
        return new OposicionDAO(null).getByVegetableFormId(formId);
    }

    // ── EXAMEN DHE ────────────────────────────────────────────────────────────

    public boolean registrarExamenDHE(VegetableForms form, TipoExamenDHE tipoExamen,
            String entidadExaminadora, String observaciones, String usuarioSesion) {
        try {
            requirePhase(form, FlowPhase.DHE_EXAM, FlowPhase.OPPOSITION_RESOLUTION);

            ExamenDHE dhe = new ExamenDHE();
            dhe.setVegetableFormId(form.getId());
            dhe.setTipoExamen(tipoExamen);
            dhe.setFechaSolicitud(new Timestamp(System.currentTimeMillis()));
            dhe.setResultado(ResultadoDHE.EN_PROCESO);
            dhe.setEntidadExaminadora(entidadExaminadora);
            dhe.setObservaciones(observaciones);
            dhe.setSolicitadoPor(usuarioSesion);
            new ExamenDHEDAO(dhe).persist();

            form.setFlowPhase(FlowPhase.DHE_EXAM);
            new VegetableFormsDAO(form).update();

            registrarHistoria(form.getApplicationNumber(),
                "Examen DHE solicitado. Tipo: " + tipoExamen.name()
                + ". Entidad: " + entidadExaminadora, usuarioSesion);
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al registrar examen DHE: " + form.getApplicationNumber(), e);
            return false;
        }
    }

    public boolean actualizarResultadoDHE(ExamenDHE examen, ResultadoDHE resultado,
            String observaciones, VegetableForms form, String usuarioSesion) {
        try {
            requirePhase(form, FlowPhase.DHE_EXAM);

            examen.setResultado(resultado);
            examen.setFechaResultado(new Timestamp(System.currentTimeMillis()));
            examen.setObservaciones(observaciones);
            new ExamenDHEDAO(examen).update();

            registrarHistoria(form.getApplicationNumber(),
                "Resultado DHE actualizado: " + resultado.name(), usuarioSesion);
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al actualizar resultado DHE: " + form.getApplicationNumber(), e);
            return false;
        }
    }

    public ExamenDHE getExamenDHEByFormId(Integer formId) {
        return new ExamenDHEDAO(null).getByVegetableFormId(formId);
    }

    // ── DICTAMEN TÉCNICO ──────────────────────────────────────────────────────

    public boolean emitirDictamenTecnico(VegetableForms form, Integer examenDheId,
            String tecnico, String dictamen, String recomendacion,
            String observaciones, String usuarioSesion) {
        try {
            requirePhase(form, FlowPhase.DHE_EXAM);

            DictamenTecnico dt = new DictamenTecnico();
            dt.setVegetableFormId(form.getId());
            dt.setExamenDheId(examenDheId);
            dt.setFechaDictamen(new Timestamp(System.currentTimeMillis()));
            dt.setTecnico(tecnico);
            dt.setDictamen(dictamen);
            dt.setRecomendacion(recomendacion);
            dt.setObservaciones(observaciones);
            new DictamenTecnicoDAO(dt).persist();

            form.setFlowPhase(FlowPhase.TECHNICAL_OPINION);
            new VegetableFormsDAO(form).update();

            registrarHistoria(form.getApplicationNumber(),
                "Dictamen técnico emitido por: " + tecnico, usuarioSesion);
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al emitir dictamen técnico: " + form.getApplicationNumber(), e);
            return false;
        }
    }

    public DictamenTecnico getDictamenTecnicoByFormId(Integer formId) {
        return new DictamenTecnicoDAO(null).getByVegetableFormId(formId);
    }

    // ── RESOLUCIÓN ────────────────────────────────────────────────────────────

    public boolean emitirResolucion(VegetableForms form, TipoResolucion tipo,
            String fundamento, String emitidoPor, String usuarioSesion) {
        try {
            requirePhase(form, FlowPhase.TECHNICAL_OPINION);

            String numRes = nextNumber("RES");

            Resolucion res = new Resolucion();
            res.setVegetableFormId(form.getId());
            res.setNumResolucion(numRes);
            res.setFechaResolucion(new Timestamp(System.currentTimeMillis()));
            res.setTipo(tipo);
            res.setFundamento(fundamento);
            res.setEmitidoPor(emitidoPor);
            new ResolucionDAO(res).persist();

            form.setFlowPhase(FlowPhase.RESOLUTION);
            form.setStatusFlow(tipo == TipoResolucion.CONCESION
                ? StatusFlow.ATTENDED : StatusFlow.DENIED);
            new VegetableFormsDAO(form).update();

            registrarHistoria(form.getApplicationNumber(),
                "Resolución " + numRes + " emitida: " + tipo.name(), usuarioSesion);
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al emitir resolución: " + form.getApplicationNumber(), e);
            return false;
        }
    }

    public Resolucion getResolucionByFormId(Integer formId) {
        return new ResolucionDAO(null).getByVegetableFormId(formId);
    }

    // ── CERTIFICADO DE OBTENTOR ───────────────────────────────────────────────

    public boolean emitirCertificadoObtentor(VegetableForms form, Resolucion resolucion,
            int vigenciaYears, String emitidoPor, String observaciones,
            String usuarioSesion) {
        try {
            requirePhase(form, FlowPhase.RESOLUTION);

            if (resolucion == null || resolucion.getTipo() != TipoResolucion.CONCESION) {
                throw new IllegalStateException(
                    "Solo se puede emitir el certificado de obtentor si la resolución es CONCESIÓN.");
            }

            String numCert = nextNumber("COB");

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, vigenciaYears);

            CertificadoObtentor co = new CertificadoObtentor();
            co.setVegetableFormId(form.getId());
            co.setResolucionId(resolucion.getId());
            co.setNumCertificado(numCert);
            co.setFechaEmision(new Timestamp(System.currentTimeMillis()));
            co.setVigenciaYears(vigenciaYears);
            co.setFechaVencimiento(new Timestamp(cal.getTimeInMillis()));
            co.setEmitidoPor(emitidoPor);
            co.setObservaciones(observaciones);
            new CertificadoObtentorDAO(co).persist();

            form.setFlowPhase(FlowPhase.CERTIFICATE_ISSUED);
            form.setStatusFlow(StatusFlow.ATTENDED);
            new VegetableFormsDAO(form).update();

            registrarHistoria(form.getApplicationNumber(),
                "Certificado de Obtentor emitido N°: " + numCert
                + ". Vigencia: " + vigenciaYears + " años", usuarioSesion);
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al emitir Certificado de Obtentor: " + form.getApplicationNumber(), e);
            return false;
        }
    }

    public CertificadoObtentor getCertificadoObtentorByFormId(Integer formId) {
        return new CertificadoObtentorDAO(null).getByVegetableFormId(formId);
    }

    // ── HELPERS INTERNOS ──────────────────────────────────────────────────────

    private Expediente buildExpediente(VegetableForms form, String observations) throws Exception {
        String numero = nextNumber("OV");
        Expediente exp = new Expediente();
        exp.setVegetableFormId(form.getId());
        exp.setExpedienteNumber(numero);
        exp.setTecnico(form.getAssignedUser());
        exp.setProvidenciaDate(new Timestamp(System.currentTimeMillis()));
        exp.setAdmissionDate(new Timestamp(System.currentTimeMillis()));
        exp.setObservations(observations);
        return exp;
    }

    private void registrarHistoria(String appNumber, String descripcion, String usuario) {
        try {
            History h = new History();
            h.setApplicationNumber(appNumber);
            h.setDescription(descripcion);
            h.setFecha(new Timestamp(System.currentTimeMillis()));
            h.setHistoryUser(usuario);
            new HistoryDAO(h).persist();
        } catch (Exception e) {
            // El fallo en historial no debe abortar la operación principal,
            // pero sí debe registrarse con stack trace completo para diagnóstico.
            LOG.log(Level.WARNING, "No se pudo registrar historia para " + appNumber + ": " + descripcion, e);
        }
    }
}
