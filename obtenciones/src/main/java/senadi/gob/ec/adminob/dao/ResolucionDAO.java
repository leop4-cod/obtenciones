package senadi.gob.ec.adminob.dao;

import java.util.List;
import javax.persistence.Query;
import senadi.gob.ec.adminob.model.Resolucion;

public class ResolucionDAO extends DAOAbstractM<Resolucion> {

    public ResolucionDAO(Resolucion t) { super(t); }

    @Override
    public List<Resolucion> buscarTodos() {
        try {
            Query q = getEntityManager().createQuery("SELECT r FROM Resolucion r ORDER BY r.id DESC");
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return q.getResultList();
        } finally { getEntityManager().close(); }
    }

    public Resolucion getByVegetableFormId(Integer vegetableFormId) {
        try {
            Query q = getEntityManager().createQuery(
                "SELECT r FROM Resolucion r WHERE r.vegetableFormId = :vfid ORDER BY r.id DESC");
            q.setParameter("vfid", vegetableFormId);
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            List<Resolucion> r = q.setMaxResults(1).getResultList();
            return r.isEmpty() ? null : r.get(0);
        } finally { getEntityManager().close(); }
    }

    public long countByYear(int year) {
        try {
            Query q = getEntityManager().createQuery(
                "SELECT COUNT(r) FROM Resolucion r WHERE r.numResolucion LIKE :p");
            q.setParameter("p", "RES-" + year + "-%");
            return (long) q.getSingleResult();
        } finally { getEntityManager().close(); }
    }
}
