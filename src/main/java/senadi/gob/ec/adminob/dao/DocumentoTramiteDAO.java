package senadi.gob.ec.adminob.dao;

import java.util.List;
import javax.persistence.EntityManager;
import senadi.gob.ec.adminob.model.DocumentoTramite;

public class DocumentoTramiteDAO extends DAOAbstractM<DocumentoTramite> {

    public DocumentoTramiteDAO(DocumentoTramite t) {
        super(t);
    }

    @Override
    public List<DocumentoTramite> buscarTodos() {
        try {
            return getEntityManager()
                .createQuery("SELECT d FROM DocumentoTramite d ORDER BY d.fechaCarga DESC",
                             DocumentoTramite.class)
                .getResultList();
        } finally {
            getEntityManager().close();
        }
    }

    public List<DocumentoTramite> buscarPorTramite(Integer tramiteId) {
        try {
            return getEntityManager()
                .createQuery(
                    "SELECT d FROM DocumentoTramite d WHERE d.tramiteId = :tid ORDER BY d.fechaCarga DESC",
                    DocumentoTramite.class)
                .setParameter("tid", tramiteId)
                .setHint("javax.persistence.cache.storeMode", "REFRESH")
                .getResultList();
        } finally {
            getEntityManager().close();
        }
    }

    public long contarPorTramite(Integer tramiteId) {
        try {
            return getEntityManager()
                .createQuery(
                    "SELECT COUNT(d) FROM DocumentoTramite d WHERE d.tramiteId = :tid",
                    Long.class)
                .setParameter("tid", tramiteId)
                .getSingleResult();
        } finally {
            getEntityManager().close();
        }
    }

    public DocumentoTramite buscarPorId(Integer id) {
        try {
            return getEntityManager()
                .createQuery("SELECT d FROM DocumentoTramite d WHERE d.id = :id",
                             DocumentoTramite.class)
                .setParameter("id", id)
                .getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            return null;
        } finally {
            getEntityManager().close();
        }
    }

    public void deleteById(Integer id) throws Exception {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        try {
            DocumentoTramite d = em.find(DocumentoTramite.class, id);
            if (d != null) em.remove(d);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
