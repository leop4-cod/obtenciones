package senadi.gob.ec.adminob.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import senadi.gob.ec.adminob.model.ComprobantePago;

public class ComprobantePagoDAO extends DAOAbstractM<ComprobantePago> {

    public ComprobantePagoDAO(ComprobantePago t) {
        super(t);
    }

    @Override
    public List<ComprobantePago> buscarTodos() {
        try {
            return getEntityManager()
                .createQuery("SELECT c FROM ComprobantePago c ORDER BY c.fechaCarga DESC",
                             ComprobantePago.class)
                .getResultList();
        } finally {
            getEntityManager().close();
        }
    }

    /** Devuelve todos los comprobantes de un trámite, ordenados del más reciente al más antiguo. */
    public List<ComprobantePago> getByVegetableFormId(Integer vegetableFormId) {
        try {
            return getEntityManager()
                .createQuery(
                    "SELECT c FROM ComprobantePago c WHERE c.vegetableFormId = :vfid " +
                    "ORDER BY c.fechaCarga DESC",
                    ComprobantePago.class)
                .setParameter("vfid", vegetableFormId)
                .setHint("javax.persistence.cache.storeMode", "REFRESH")
                .getResultList();
        } finally {
            getEntityManager().close();
        }
    }

    /** Devuelve un comprobante por su ID primario. */
    public ComprobantePago getById(Integer id) {
        try {
            return getEntityManager()
                .createQuery("SELECT c FROM ComprobantePago c WHERE c.id = :id", ComprobantePago.class)
                .setParameter("id", id)
                .getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            return null;
        } finally {
            getEntityManager().close();
        }
    }

    /** Devuelve true si el trámite tiene al menos un comprobante registrado. */
    public boolean existsByVegetableFormId(Integer vegetableFormId) {
        try {
            Long count = getEntityManager()
                .createQuery(
                    "SELECT COUNT(c) FROM ComprobantePago c WHERE c.vegetableFormId = :vfid",
                    Long.class)
                .setParameter("vfid", vegetableFormId)
                .getSingleResult();
            return count > 0;
        } finally {
            getEntityManager().close();
        }
    }
    
    /** Una sola consulta que devuelve todos los vegetableFormId que tienen al menos un archivo. */
    public java.util.Set<Integer> getDistinctVegetableFormIds() {
        try {
            java.util.List<Integer> ids = getEntityManager()
                .createQuery("SELECT DISTINCT c.vegetableFormId FROM ComprobantePago c", Integer.class)
                .getResultList();
            return new java.util.HashSet<>(ids);
        } finally {
            getEntityManager().close();
        }
    }

    public void delete(Integer id) throws Exception {
        javax.persistence.EntityManager em = getEntityManager();
        em.getTransaction().begin();
        try {
            ComprobantePago cp = em.find(ComprobantePago.class, id);
            if (cp != null) {
                em.remove(cp);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
