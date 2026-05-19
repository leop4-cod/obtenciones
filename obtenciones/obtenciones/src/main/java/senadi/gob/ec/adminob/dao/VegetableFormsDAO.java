package senadi.gob.ec.adminob.dao;

import java.sql.Timestamp;
import java.util.List;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
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

            String prevAssigned = m.getAssignedUser();
            String newAssigned = src.getAssignedUser();

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
            m.setNumeracionInterna(src.getNumeracionInterna());
            // La entidad es managed → Hibernate dirty-check genera UPDATE automáticamente
            em.getTransaction().commit();

            String prevNorm = prevAssigned == null ? "" : prevAssigned.trim();
            String newNorm = newAssigned == null ? "" : newAssigned.trim();
            if (!prevNorm.equals(newNorm) && m.getApplicationNumber() != null) {
                try {
                    String desde = prevNorm.isEmpty() ? "(sin asignar)" : prevNorm;
                    String hasta = newNorm.isEmpty() ? "(sin asignar)" : newNorm;
                    String descripcion = "Se cambió de usuario asignado de " + desde + " a " + hasta;
                    String quien = obtenerUsuarioSesion();
                    EntityManager em2 = getEntityManager();
                    Timestamp limite = new Timestamp(System.currentTimeMillis() - 10000);
                    Long existentes = em2.createQuery(
                            "SELECT COUNT(h) FROM History h WHERE h.applicationNumber = :app "
                            + "AND h.description = :desc AND h.fecha >= :limite", Long.class)
                        .setParameter("app", m.getApplicationNumber())
                        .setParameter("desc", descripcion)
                        .setParameter("limite", limite)
                        .getSingleResult();
                    if (existentes == null || existentes == 0) {
                        History h = new History();
                        h.setApplicationNumber(m.getApplicationNumber());
                        h.setDescription(descripcion);
                        h.setFecha(new Timestamp(System.currentTimeMillis()));
                        h.setHistoryUser(quien);
                        em2.getTransaction().begin();
                        em2.persist(h);
                        em2.getTransaction().commit();
                    }
                    em2.close();
                } catch (Exception logEx) {
                    System.err.println("[VegetableFormsDAO] No se pudo registrar cambio de asignación: " + logEx.getMessage());
                }
            }
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
}
