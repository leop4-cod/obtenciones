package senadi.gob.ec.adminob.dao;

import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import senadi.gob.ec.adminob.enums.Status;
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
                    // 🔥 AQUÍ ESTÁ LA CLAVE → SIEMPRE TRAE TODO
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
            m.setAssignedUser(src.getAssignedUser());
            m.setApplicationDate(src.getApplicationDate());
            m.setApplicationNumber(src.getApplicationNumber());
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
    public List<VegetableForms> buscarTodosByTypeAndDate(String type, Date start, Date end) {
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

            query.setParameter("start", start, javax.persistence.TemporalType.DATE);
            query.setParameter("end", end, javax.persistence.TemporalType.DATE);

            return query.getResultList();

        } finally {
            em.close();
        }
    }
}
