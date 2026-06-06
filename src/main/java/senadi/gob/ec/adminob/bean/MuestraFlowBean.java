package senadi.gob.ec.adminob.bean;

import java.io.Serializable;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import senadi.gob.ec.adminob.enums.EstadoOposicion;
import senadi.gob.ec.adminob.enums.FlowPhase;
import senadi.gob.ec.adminob.enums.ResultadoDHE;
import senadi.gob.ec.adminob.enums.TipoExamenDHE;
import senadi.gob.ec.adminob.enums.TipoResolucion;
import senadi.gob.ec.adminob.model.CertificadoDeposito;
import senadi.gob.ec.adminob.model.CertificadoObtentor;
import senadi.gob.ec.adminob.model.DepositoMuestra;
import senadi.gob.ec.adminob.model.DictamenTecnico;
import senadi.gob.ec.adminob.model.ExamenDHE;
import senadi.gob.ec.adminob.model.Oposicion;
import senadi.gob.ec.adminob.model.PublicacionGaceta;
import senadi.gob.ec.adminob.model.Resolucion;
import senadi.gob.ec.adminob.model.VegetableForms;
import senadi.gob.ec.adminob.service.TramiteFlowService;
import senadi.gob.ec.adminob.util.Controller;

@ManagedBean(name = "muestraFlowBean")
@ViewScoped
public class MuestraFlowBean implements Serializable {

    private VegetableForms selectedForm;

    // Depósito muestra
    private String lugarDeposito;
    private String responsable;
    private String numActa;

    // Certificado depósito
    private String emitidoPor;

    // Publicación gaceta
    private String denominacionGenerica;
    private boolean denominacionValida;
    private String numGaceta;

    // Oposición
    private String oponente;
    private String motivo;
    private Oposicion oposicionSeleccionada;
    private EstadoOposicion estadoOposicion;
    private boolean tieneOposicion;

    // DHE
    private TipoExamenDHE tipoExamenDHE;
    private String entidadExaminadora;
    private ResultadoDHE resultadoDHE;

    // Dictamen
    private String tecnicoDictamen;
    private String dictamen;
    private String recomendacion;

    // Resolución
    private TipoResolucion tipoResolucion;
    private String fundamento;

    // Certificado Obtentor
    private int vigenciaYears = 20;

    // Observaciones compartidas
    private String observaciones;

    // Datos cargados
    private DepositoMuestra depositoMuestra;
    private CertificadoDeposito certificadoDeposito;
    private PublicacionGaceta publicacionGaceta;
    private List<Oposicion> oposiciones;
    private ExamenDHE examenDHE;
    private DictamenTecnico dictamenTecnico;
    private Resolucion resolucion;
    private CertificadoObtentor certificadoObtentor;

    private final TramiteFlowService service = new TramiteFlowService();
    private final Controller controller = new Controller();

    public void cargarTramite(Integer formId) {
        selectedForm = controller.getVegetableFormsById(formId);
        if (selectedForm == null || selectedForm.getId() == null) return;
        Integer id = selectedForm.getId();
        depositoMuestra = service.getDepositoMuestraByFormId(id);
        certificadoDeposito = service.getCertificadoDepositoByFormId(id);
        publicacionGaceta = service.getPublicacionGacetaByFormId(id);
        oposiciones = service.getOposicionesByFormId(id);
        examenDHE = service.getExamenDHEByFormId(id);
        dictamenTecnico = service.getDictamenTecnicoByFormId(id);
        resolucion = service.getResolucionByFormId(id);
        certificadoObtentor = service.getCertificadoObtentorByFormId(id);
        tieneOposicion = publicacionGaceta != null
            && Boolean.TRUE.equals(publicacionGaceta.getTieneOposicion());
    }

    public void registrarDepositoMuestra() {
        if (selectedForm == null) { addError("Seleccione un trámite."); return; }
        boolean ok = service.registrarDepositoMuestra(selectedForm, lugarDeposito,
            responsable, numActa, observaciones, getUsuario());
        if (ok) {
            depositoMuestra = service.getDepositoMuestraByFormId(selectedForm.getId());
            selectedForm = controller.getVegetableFormsById(selectedForm.getId());
            addInfo("Depósito de muestra viva registrado. Acta N°: " + numActa);
        } else {
            addError("Error al registrar el depósito.");
        }
    }

    public void emitirCertificadoDeposito() {
        if (selectedForm == null || depositoMuestra == null) {
            addError("Debe registrar primero el depósito de muestra."); return;
        }
        boolean ok = service.emitirCertificadoDeposito(selectedForm,
            depositoMuestra.getId(), emitidoPor, observaciones, getUsuario());
        if (ok) {
            certificadoDeposito = service.getCertificadoDepositoByFormId(selectedForm.getId());
            selectedForm = controller.getVegetableFormsById(selectedForm.getId());
            addInfo("Certificado de depósito emitido N°: " + certificadoDeposito.getNumCertificado());
        } else {
            addError("Error al emitir certificado de depósito.");
        }
    }

    public void publicarEnGaceta() {
        if (selectedForm == null) { addError("Seleccione un trámite."); return; }
        if (numGaceta == null || numGaceta.trim().isEmpty()) {
            addError("El número de gaceta es obligatorio.");
            return;
        }
        if (!numGaceta.trim().matches("^[0-9]+$")) {
            addError("El número de gaceta debe contener únicamente dígitos numéricos (se rechazan letras, símbolos y espacios).");
            return;
        }
        boolean ok = service.publicarEnGaceta(selectedForm, denominacionGenerica,
            denominacionValida, numGaceta.trim(), observaciones, getUsuario());
        if (ok) {
            publicacionGaceta = service.getPublicacionGacetaByFormId(selectedForm.getId());
            selectedForm = controller.getVegetableFormsById(selectedForm.getId());
            addInfo("Publicación en gaceta registrada N°: " + numGaceta
                + ". Período de oposición: 30 días.");
        } else {
            addError("Error al registrar la publicación.");
        }
    }

    public void registrarOposicion() {
        if (selectedForm == null) { addError("Seleccione un trámite."); return; }
        boolean ok = service.registrarOposicion(selectedForm, oponente, motivo, getUsuario());
        if (ok) {
            oposiciones = service.getOposicionesByFormId(selectedForm.getId());
            publicacionGaceta = service.getPublicacionGacetaByFormId(selectedForm.getId());
            tieneOposicion = true;
            selectedForm = controller.getVegetableFormsById(selectedForm.getId());
            addInfo("Oposición registrada para: " + oponente);
        } else {
            addError("Error al registrar oposición.");
        }
    }

    public void resolverOposicion() {
        if (oposicionSeleccionada == null) { addError("Seleccione una oposición."); return; }
        boolean ok = service.resolverOposicion(oposicionSeleccionada, estadoOposicion,
            observaciones, getUsuario(), selectedForm, getUsuario());
        if (ok) {
            oposiciones = service.getOposicionesByFormId(selectedForm.getId());
            selectedForm = controller.getVegetableFormsById(selectedForm.getId());
            addInfo("Oposición resuelta: " + estadoOposicion.name());
        } else {
            addError("Error al resolver oposición.");
        }
    }

    public void continuarSinOposicion() {
        if (selectedForm == null) { addError("Seleccione un trámite."); return; }
        boolean ok = service.continuarSinOposicion(selectedForm, getUsuario());
        if (ok) {
            selectedForm = controller.getVegetableFormsById(selectedForm.getId());
            addInfo("Trámite continúa al examen DHE.");
        } else {
            addError("Error al avanzar el trámite.");
        }
    }

    public void registrarExamenDHE() {
        if (selectedForm == null) { addError("Seleccione un trámite."); return; }
        boolean ok = service.registrarExamenDHE(selectedForm, tipoExamenDHE,
            entidadExaminadora, observaciones, getUsuario());
        if (ok) {
            examenDHE = service.getExamenDHEByFormId(selectedForm.getId());
            selectedForm = controller.getVegetableFormsById(selectedForm.getId());
            addInfo("Examen DHE registrado.");
        } else {
            addError("Error al registrar examen DHE.");
        }
    }

    public void actualizarResultadoDHE() {
        if (examenDHE == null) { addError("Primero registre el examen DHE."); return; }
        boolean ok = service.actualizarResultadoDHE(examenDHE, resultadoDHE,
            observaciones, selectedForm, getUsuario());
        if (ok) {
            examenDHE = service.getExamenDHEByFormId(selectedForm.getId());
            addInfo("Resultado DHE actualizado: " + resultadoDHE.name());
        } else {
            addError("Error al actualizar resultado DHE.");
        }
    }

    public void emitirDictamenTecnico() {
        if (selectedForm == null) { addError("Seleccione un trámite."); return; }
        Integer examenId = examenDHE != null ? examenDHE.getId() : null;
        boolean ok = service.emitirDictamenTecnico(selectedForm, examenId,
            tecnicoDictamen, dictamen, recomendacion, observaciones, getUsuario());
        if (ok) {
            dictamenTecnico = service.getDictamenTecnicoByFormId(selectedForm.getId());
            selectedForm = controller.getVegetableFormsById(selectedForm.getId());
            addInfo("Dictamen técnico emitido.");
        } else {
            addError("Error al emitir dictamen técnico.");
        }
    }

    public void emitirResolucion() {
        if (selectedForm == null) { addError("Seleccione un trámite."); return; }
        boolean ok = service.emitirResolucion(selectedForm, tipoResolucion,
            fundamento, emitidoPor, getUsuario());
        if (ok) {
            resolucion = service.getResolucionByFormId(selectedForm.getId());
            selectedForm = controller.getVegetableFormsById(selectedForm.getId());
            addInfo("Resolución emitida: " + tipoResolucion.name()
                + " N°: " + resolucion.getNumResolucion());
        } else {
            addError("Error al emitir resolución.");
        }
    }

    public void emitirCertificadoObtentor() {
        if (resolucion == null || resolucion.getTipo() != TipoResolucion.CONCESION) {
            addError("Solo se puede emitir el certificado si la resolución es de CONCESIÓN."); return;
        }
        boolean ok = service.emitirCertificadoObtentor(selectedForm, resolucion,
            vigenciaYears, emitidoPor, observaciones, getUsuario());
        if (ok) {
            certificadoObtentor = service.getCertificadoObtentorByFormId(selectedForm.getId());
            selectedForm = controller.getVegetableFormsById(selectedForm.getId());
            addInfo("Certificado de Obtentor emitido N°: " + certificadoObtentor.getNumCertificado());
        } else {
            addError("Error al emitir Certificado de Obtentor.");
        }
    }

    private String getUsuario() {
        try {
            LoginBean lb = controller.getLogin();
            return lb != null ? lb.getLogin() : "SISTEMA";
        } catch (Exception e) { return "SISTEMA"; }
    }

    private void addInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public VegetableForms getSelectedForm() { return selectedForm; }
    public void setSelectedForm(VegetableForms selectedForm) { this.selectedForm = selectedForm; }

    public String getLugarDeposito() { return lugarDeposito; }
    public void setLugarDeposito(String lugarDeposito) { this.lugarDeposito = lugarDeposito; }

    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }

    public String getNumActa() { return numActa; }
    public void setNumActa(String numActa) { this.numActa = numActa; }

    public String getEmitidoPor() { return emitidoPor; }
    public void setEmitidoPor(String emitidoPor) { this.emitidoPor = emitidoPor; }

    public String getDenominacionGenerica() { return denominacionGenerica; }
    public void setDenominacionGenerica(String denominacionGenerica) { this.denominacionGenerica = denominacionGenerica; }

    public boolean isDenominacionValida() { return denominacionValida; }
    public void setDenominacionValida(boolean denominacionValida) { this.denominacionValida = denominacionValida; }

    public String getNumGaceta() { return numGaceta; }
    public void setNumGaceta(String numGaceta) { this.numGaceta = numGaceta; }

    public String getOponente() { return oponente; }
    public void setOponente(String oponente) { this.oponente = oponente; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public Oposicion getOposicionSeleccionada() { return oposicionSeleccionada; }
    public void setOposicionSeleccionada(Oposicion oposicionSeleccionada) { this.oposicionSeleccionada = oposicionSeleccionada; }

    public EstadoOposicion getEstadoOposicion() { return estadoOposicion; }
    public void setEstadoOposicion(EstadoOposicion estadoOposicion) { this.estadoOposicion = estadoOposicion; }

    public boolean isTieneOposicion() { return tieneOposicion; }
    public void setTieneOposicion(boolean tieneOposicion) { this.tieneOposicion = tieneOposicion; }

    public TipoExamenDHE getTipoExamenDHE() { return tipoExamenDHE; }
    public void setTipoExamenDHE(TipoExamenDHE tipoExamenDHE) { this.tipoExamenDHE = tipoExamenDHE; }

    public String getEntidadExaminadora() { return entidadExaminadora; }
    public void setEntidadExaminadora(String entidadExaminadora) { this.entidadExaminadora = entidadExaminadora; }

    public ResultadoDHE getResultadoDHE() { return resultadoDHE; }
    public void setResultadoDHE(ResultadoDHE resultadoDHE) { this.resultadoDHE = resultadoDHE; }

    public String getTecnicoDictamen() { return tecnicoDictamen; }
    public void setTecnicoDictamen(String tecnicoDictamen) { this.tecnicoDictamen = tecnicoDictamen; }

    public String getDictamen() { return dictamen; }
    public void setDictamen(String dictamen) { this.dictamen = dictamen; }

    public String getRecomendacion() { return recomendacion; }
    public void setRecomendacion(String recomendacion) { this.recomendacion = recomendacion; }

    public TipoResolucion getTipoResolucion() { return tipoResolucion; }
    public void setTipoResolucion(TipoResolucion tipoResolucion) { this.tipoResolucion = tipoResolucion; }

    public String getFundamento() { return fundamento; }
    public void setFundamento(String fundamento) { this.fundamento = fundamento; }

    public int getVigenciaYears() { return vigenciaYears; }
    public void setVigenciaYears(int vigenciaYears) { this.vigenciaYears = vigenciaYears; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public DepositoMuestra getDepositoMuestra() { return depositoMuestra; }
    public CertificadoDeposito getCertificadoDeposito() { return certificadoDeposito; }
    public PublicacionGaceta getPublicacionGaceta() { return publicacionGaceta; }
    public List<Oposicion> getOposiciones() { return oposiciones; }
    public ExamenDHE getExamenDHE() { return examenDHE; }
    public DictamenTecnico getDictamenTecnico() { return dictamenTecnico; }
    public Resolucion getResolucion() { return resolucion; }
    public CertificadoObtentor getCertificadoObtentor() { return certificadoObtentor; }

    public EstadoOposicion[] getEstadosOposicion() { return EstadoOposicion.values(); }
    public TipoExamenDHE[] getTiposExamenDHE() { return TipoExamenDHE.values(); }
    public ResultadoDHE[] getResultadosDHE() { return ResultadoDHE.values(); }
    public TipoResolucion[] getTiposResolucion() { return TipoResolucion.values(); }
}
