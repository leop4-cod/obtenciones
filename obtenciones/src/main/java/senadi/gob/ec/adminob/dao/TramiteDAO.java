package senadi.gob.ec.adminob.dao;

import java.util.List;
import javax.persistence.EntityManager;
import senadi.gob.ec.adminob.model.Tramite;

public class TramiteDAO extends DAOAbstractM<Tramite> {

    public TramiteDAO(Tramite t) {
        super(t);
    }

    @Override
    public List<Tramite> buscarTodos() {
        try {
            return getEntityManager()
                .createQuery("SELECT t FROM Tramite t ORDER BY t.fechaCreacion DESC", Tramite.class)
                .getResultList();
        } finally {
            getEntityManager().close();
        }
    }

    public List<Tramite> buscarPorEstado(String estado) {
        try {
            return getEntityManager()
                .createQuery(
                    "SELECT t FROM Tramite t WHERE t.estadoActual = :estado ORDER BY t.fechaCreacion DESC",
                    Tramite.class)
                .setParameter("estado", estado)
                .getResultList();
        } finally {
            getEntityManager().close();
        }
    }

    public Tramite buscarPorId(Integer id) {
        try {
            return getEntityManager()
                .createQuery("SELECT t FROM Tramite t WHERE t.id = :id", Tramite.class)
                .setParameter("id", id)
                .getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            return null;
        } finally {
            getEntityManager().close();
        }
    }

    /** Verifica si ya existe un trámite con esa numeración interna (excluyendo el ID dado). */
    public boolean existeNumeracionInterna(String numeracion, Integer excluirId) {
        try {
            String jpql = "SELECT COUNT(t) FROM Tramite t WHERE t.numeracionInterna = :num"
                         + (excluirId != null ? " AND t.id <> :excId" : "");
            var q = getEntityManager()
                .createQuery(jpql, Long.class)
                .setParameter("num", numeracion);
            if (excluirId != null) q.setParameter("excId", excluirId);
            return q.getSingleResult() > 0;
        } finally {
            getEntityManager().close();
        }
    }

    public void actualizarCampos(Tramite tramite) throws Exception {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        try {
            em.merge(tramite);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteById(Integer id) throws Exception {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        try {
            Tramite t = em.find(Tramite.class, id);
            if (t != null) em.remove(t);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
