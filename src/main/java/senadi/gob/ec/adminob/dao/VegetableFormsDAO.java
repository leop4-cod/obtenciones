package senadi.gob.ec.adminob.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import senadi.gob.ec.adminob.bean.LoginBean;
import senadi.gob.ec.adminob.enums.Status;
import senadi.gob.ec.adminob.model.History;
import senadi.gob.ec.adminob.model.VegetableForms;

public class VegetableFormsDAO extends DAOAbstractM<VegetableForms> {

    public VegetableFormsDAO(VegetableForms t) {
        super(t);
    }

    @Override
    public List<VegetableForms> buscarTodos() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "SELECT v FROM VegetableForms v ORDER BY v.id DESC",
                    VegetableForms.class)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public VegetableForms getVegetableFormsById(Integer id) {
        if (id == null) return null;
        EntityManager em = getEntityManager();
        try {
            return em.find(VegetableForms.class, id);
        } finally {
            em.close();
        }
    }

    // 🔥 MÉTODO PRINCIPAL (ARREGLADO)
    public List<VegetableForms> buscarTodosByType(String type) {
        EntityManager em = getEntityManager();
        try {
            String t = (type == null) ? "" : type.trim();

            System.out.println("TYPE (SIN FECHA): [" + t + "]");

            TypedQuery<VegetableForms> query;

            switch (t) {
                case "Iniciados":
                    query = em.createQuery(
                        "SELECT v FROM VegetableForms v WHERE v.status = :st ORDER BY v.id DESC",
                        VegetableForms.class);
                    query.setParameter("st", Status.DELIVERED);
                    break;

                case "Pagados":
                    query = em.createQuery(
                        "SELECT v FROM VegetableForms v " +
                        "WHERE v.status = :st AND EXISTS " +
                        "(SELECT 1 FROM ComprobantePago cp WHERE cp.vegetableFormId = v.id) " +
                        "ORDER BY v.id DESC",
                        VegetableForms.class);
                    query.setParameter("st", Status.FINISHED);
                    break;

                case "Aceptados":
                    query = em.createQuery(
                        "SELECT v FROM VegetableForms v WHERE v.status = :st ORDER BY v.id DESC",
                        VegetableForms.class);
                    query.setParameter("st", Status.ACCEPTED);
                    break;

                case "Vista":
                    query = em.createQuery(
                        "SELECT v FROM VegetableForms v WHERE v.status = :st ORDER BY v.id DESC",
                        VegetableForms.class);
                    query.setParameter("st", Status.PREVIEW);
                    break;

                case "TODOS":
                case "Todos":
                case "todos":
                default:
                    query = em.createQuery(
                        "SELECT v FROM VegetableForms v ORDER BY v.id DESC",
                        VegetableForms.class);
                    break;
            }

            return query.getResultList();

        } finally {
            em.close();
        }
    }

    /**
     * Actualiza solo los campos escalares editables de un VegetableForms.
     * Estrategia: carga la entidad MANEJADA en un EM nuevo, copia los campos,
     * luego el dirty-check de Hibernate genera el UPDATE automáticamente.
     * No toca las colecciones @OneToMany para evitar LazyInitializationException
     * sobre proxies detachados.
     */
    public void actualizarCamposEditables(VegetableForms src) throws Exception {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        try {
            VegetableForms m = em.find(VegetableForms.class, src.getId());
            if (m == null) throw new Exception("Registro no encontrado id=" + src.getId());

            m.setCommonName(src.getCommonName());
            m.setBotanicalTaxon(src.getBotanicalTaxon());
            m.setGenericDenomination(src.getGenericDenomination());
            m.setProvitionalDesignation(src.getProvitionalDesignation());
            m.setDenominationType(src.getDenominationType());
            m.setVarietalGroup(src.getVarietalGroup());
            m.setGeographicalMaterialOrigin(src.getGeographicalMaterialOrigin());
            m.setGeographicalVarietyOrigin(src.getGeographicalVarietyOrigin());
            m.setVarietyTransfer(src.getVarietyTransfer());
            m.setVarietyTransferType(src.getVarietyTransferType());
            m.setVarietyTransferDescription(src.getVarietyTransferDescription());
            m.setHasOtherApplications(src.getHasOtherApplications());
            m.setPriorityClaim(src.getPriorityClaim());
            m.setInTerritory(src.getInTerritory());
            m.setOutTerritory(src.getOutTerritory());
            m.setGenealogy(src.getGenealogy());
            m.setFeaturesDescription(src.getFeaturesDescription());
            m.setReproductionMechanism(src.getReproductionMechanism());
            m.setProcessHistory(src.getProcessHistory());
            m.setAdditionalInformation(src.getAdditionalInformation());
            m.setMaterialVarietyIdentification(src.getMaterialVarietyIdentification());
            m.setProductVarietyIdentification(src.getProductVarietyIdentification());
            m.setExamPerformed(src.getExamPerformed());
            m.setExamInProcess(src.getExamInProcess());
            m.setNoExamYet(src.getNoExamYet());
            m.setLivingSample(src.getLivingSample());
            m.setSamplePlace(src.getSamplePlace());
            m.setElectronicCommunicationConsent(src.getElectronicCommunicationConsent());
            m.setStatus(src.getStatus());
            m.setStatusFlow(src.getStatusFlow());
            m.setFlowPhase(src.getFlowPhase());
            m.setApplicationDate(src.getApplicationDate());
            m.setApplicationNumber(src.getApplicationNumber());
            m.setNumeroDeTramite(src.getNumeroDeTramite());
            m.setObservacionTecnica(src.getObservacionTecnica());
            m.setPublicacionGaceta(src.getPublicacionGaceta());
            m.setEtapaActual(src.getEtapaActual());
            m.setEstadoExpediente(src.getEstadoExpediente());
            // La entidad es managed → Hibernate dirty-check genera UPDATE automáticamente
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // 🔥 MÉTODO CON FECHAS (ARREGLADO)
    public List<VegetableForms> buscarTodosByTypeAndDate(String type, Timestamp start, Timestamp end) {
        EntityManager em = getEntityManager();
        try {
            String t = (type == null) ? "" : type.trim();

            System.out.println("TYPE (CON FECHA): [" + t + "]");
            System.out.println("FECHAS: " + start + " - " + end);

            // 🔥 SI NO HAY FECHAS → TRAE TODO
            if (start == null || end == null) {
                System.out.println("⚠️ Sin fechas → devolviendo TODOS");
                return buscarTodosByType(t);
            }

            TypedQuery<VegetableForms> query;

            switch (t) {
                case "Iniciados":
                    query = em.createQuery(
                        "SELECT v FROM VegetableForms v " +
                        "WHERE v.status = :st AND v.applicationDate BETWEEN :start AND :end " +
                        "ORDER BY v.id DESC",
                        VegetableForms.class);
                    query.setParameter("st", Status.DELIVERED);
                    break;

                case "Pagados":
                    query = em.createQuery(
                        "SELECT v FROM VegetableForms v " +
                        "WHERE v.status = :st " +
                        "AND EXISTS (SELECT 1 FROM ComprobantePago cp WHERE cp.vegetableFormId = v.id) " +
                        "AND v.applicationDate BETWEEN :start AND :end " +
                        "ORDER BY v.id DESC",
                        VegetableForms.class);
                    query.setParameter("st", Status.FINISHED);
                    break;

                case "Aceptados":
                    query = em.createQuery(
                        "SELECT v FROM VegetableForms v " +
                        "WHERE v.status = :st AND v.applicationDate BETWEEN :start AND :end " +
                        "ORDER BY v.id DESC",
                        VegetableForms.class);
                    query.setParameter("st", Status.ACCEPTED);
                    break;

                case "Vista":
                    query = em.createQuery(
                        "SELECT v FROM VegetableForms v " +
                        "WHERE v.status = :st AND v.applicationDate BETWEEN :start AND :end " +
                        "ORDER BY v.id DESC",
                        VegetableForms.class);
                    query.setParameter("st", Status.PREVIEW);
                    break;

                case "TODOS":
                case "Todos":
                case "todos":
                default:
                    query = em.createQuery(
                        "SELECT v FROM VegetableForms v " +
                        "WHERE v.applicationDate BETWEEN :start AND :end " +
                        "ORDER BY v.id DESC",
                        VegetableForms.class);
                    break;
            }

            query.setParameter("start", start);
            query.setParameter("end", end);

            return query.getResultList();

        } finally {
            em.close();
        }
    }

    public int updateAllSavedToDelivered() throws Exception {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        try {
            int updated = em.createQuery(
                "UPDATE VegetableForms v SET v.status = :delivered WHERE v.status = :saved")
                .setParameter("delivered", Status.DELIVERED)
                .setParameter("saved", Status.SAVED)
                .executeUpdate();
            em.getTransaction().commit();
            return updated;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Elimina un VegetableForms por su ID primario junto con sus relaciones en cascada. */
    public void deleteById(Integer id) throws Exception {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        try {
            VegetableForms entity = em.find(VegetableForms.class, id);
            if (entity != null) {
                em.remove(entity);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    private String obtenerUsuarioSesion() {
        try {
            FacesContext ctx = FacesContext.getCurrentInstance();
            if (ctx != null) {
                Object lb = ctx.getExternalContext().getSessionMap().get("loginBean");
                if (lb instanceof LoginBean) {
                    String login = ((LoginBean) lb).getLogin();
                    if (login != null && !login.trim().isEmpty()) return login;
                }
            }
        } catch (Exception ignore) {
        }
        return "sistema";
    }

    /**
     * Busca un VegetableForms por su application_number o numeracion_interna.
     * Usado para importación masiva desde Excel.
     */
    public VegetableForms buscarPorNumero(String numero) {
        if (numero == null || numero.trim().isEmpty()) return null;
        EntityManager em = getEntityManager();
        try {
            String n = numero.trim();
            List<VegetableForms> result = em.createQuery(
                "SELECT v FROM VegetableForms v " +
                "WHERE v.applicationNumber = :num OR v.numeroDeTramite = :num " +
                "ORDER BY v.id DESC",
                VegetableForms.class)
                .setParameter("num", n)
                .setMaxResults(1)
                .getResultList();
            return result.isEmpty() ? null : result.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * VegetableForms en estado DELIVERED que aún no han sido importados como Tramite.
     * Usado por el diálogo de importación en lista.xhtml.
     */
    public List<VegetableForms> buscarDeliveredSinImportar() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                "SELECT v FROM VegetableForms v " +
                "WHERE v.status = :delivered " +
                "AND NOT EXISTS (SELECT t FROM Tramite t WHERE t.vegetableFormId = v.id) " +
                "ORDER BY v.createDate DESC",
                VegetableForms.class)
                .setParameter("delivered", Status.DELIVERED)
                .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tramites del portal que ya tienen gestión iniciada (flowPhase no nulo y distinto de INITIAL).
     * Estos son los únicos que pueden vincularse a un Tramite administrativo.
     */
    public List<VegetableForms> buscarEnProceso() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                "SELECT v FROM VegetableForms v " +
                "WHERE v.flowPhase IS NOT NULL AND v.flowPhase <> :initial " +
                "ORDER BY v.createDate DESC",
                VegetableForms.class)
                .setParameter("initial", senadi.gob.ec.adminob.enums.FlowPhase.INITIAL)
                .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Carga un VegetableForms con sus PersonVegetable y Person asociados en una sola consulta
     * (JOIN FETCH), evitando LazyInitializationException fuera del EntityManager.
     */
    public VegetableForms buscarConPersonasPorId(Integer id) {
        if (id == null) return null;
        EntityManager em = getEntityManager();
        try {
            List<VegetableForms> result = em.createQuery(
                "SELECT DISTINCT v FROM VegetableForms v " +
                "LEFT JOIN FETCH v.personVegetables pv " +
                "LEFT JOIN FETCH pv.person " +
                "WHERE v.id = :id",
                VegetableForms.class)
                .setParameter("id", id)
                .getResultList();
            return result.isEmpty() ? null : result.get(0);
        } finally {
            em.close();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Importación de trámites faltantes desde Excel
    // ══════════════════════════════════════════════════════════════════════

    public static class ImportTramiteResult {
        public final int insertados;
        public final int existentes;
        public final int errores;
        public final List<String> mensajes;

        public ImportTramiteResult(int ins, int ex, int err, List<String> msg) {
            this.insertados = ins;
            this.existentes = ex;
            this.errores    = err;
            this.mensajes   = msg;
        }
    }

    /**
     * Importa trámites faltantes desde un Excel con el formato:
     *   Columna A: N° Trámite (application_number) — obligatorio
     *   Columna B: Nombre Común (common_name)       — opcional
     *   Columna C: Numeración Interna               — opcional
     * La primera fila se trata como encabezado y se omite.
     */
    public ImportTramiteResult importarTramitesDesdeExcel(java.io.InputStream is) throws Exception {
        int insertados = 0, existentes = 0, errores = 0;
        List<String> mensajes = new ArrayList<>();

        try (Workbook wb = WorkbookFactory.create(is)) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 0) {
                throw new Exception("El archivo está vacío.");
            }

            // Si la primera celda parece un encabezado, empezar en fila 1
            int startRow = 0;
            String primeraCelda = cellStr(sheet.getRow(0), 0);
            if (primeraCelda != null) {
                String lower = primeraCelda.toLowerCase();
                if (lower.contains("tramite") || lower.contains("trámite")
                        || lower.contains("n°") || lower.contains("numero")
                        || lower.contains("número") || lower.equals("#")) {
                    startRow = 1;
                }
            }

            for (int rowIdx = startRow; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) continue;

                String appNumber  = cellStr(row, 0);
                if (appNumber == null || appNumber.isEmpty()) continue;

                String commonName = cellStr(row, 1);
                String numInterna = cellStr(row, 2);

                EntityManager emCheck = getEntityManager();
                try {
                    Long count = emCheck.createQuery(
                        "SELECT COUNT(v) FROM VegetableForms v WHERE v.applicationNumber = :an",
                        Long.class)
                        .setParameter("an", appNumber)
                        .getSingleResult();
                    if (count != null && count > 0) {
                        existentes++;
                        mensajes.add("Fila " + (rowIdx + 1) + " [" + appNumber + "]: ya existe, omitido");
                        continue;
                    }
                } finally {
                    emCheck.close();
                }

                EntityManager em = getEntityManager();
                em.getTransaction().begin();
                try {
                    VegetableForms vf = new VegetableForms();
                    vf.setApplicationNumber(appNumber);
                    vf.setStatus(Status.ACCEPTED);
                    vf.setCreateDate(new Timestamp(System.currentTimeMillis()));
                    if (commonName != null && !commonName.isEmpty()) vf.setCommonName(commonName);
                    if (numInterna != null && !numInterna.isEmpty()) vf.setNumeroDeTramite(numInterna);
                    em.persist(vf);
                    em.getTransaction().commit();
                    insertados++;
                } catch (Exception rowEx) {
                    if (em.getTransaction().isActive()) em.getTransaction().rollback();
                    mensajes.add("Fila " + (rowIdx + 1) + " [" + appNumber + "]: " + rowEx.getMessage());
                    errores++;
                } finally {
                    em.close();
                }
            }
        }

        return new ImportTramiteResult(insertados, existentes, errores, mensajes);
    }

    private String cellStr(Row row, int col) {
        if (row == null || col < 0) return null;
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) {
            double v = cell.getNumericCellValue();
            return (v == Math.floor(v) && !Double.isInfinite(v)) ? String.valueOf((long) v) : String.valueOf(v);
        }
        return null;
    }
}
