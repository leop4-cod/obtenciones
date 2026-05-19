package senadi.gob.ec.adminob.dao;

import java.util.List;
import javax.persistence.Query;
import senadi.gob.ec.adminob.model.Expediente;

public class ExpedienteDAO extends DAOAbstractM<Expediente> {

    public ExpedienteDAO(Expediente t) {
        super(t);
    }

    @Override
    public List<Expediente> buscarTodos() {
        try {
            Query query = this.getEntityManager().createQuery("SELECT e FROM Expediente e ORDER BY e.id DESC");
            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return query.getResultList();
        } finally {
            this.getEntityManager().close();
        }
    }

    public Expediente getByVegetableFormId(Integer vegetableFormId) {
        try {
            Query query = this.getEntityManager().createQuery(
                "SELECT e FROM Expediente e WHERE e.vegetableFormId = :vfid ORDER BY e.id DESC");
            query.setParameter("vfid", vegetableFormId);
            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            List<Expediente> result = query.setMaxResults(1).getResultList();
            return result.isEmpty() ? null : result.get(0);
        } finally {
            this.getEntityManager().close();
        }
    }

    public long countByYear(int year) {
        try {
            Query query = this.getEntityManager().createQuery(
                "SELECT COUNT(e) FROM Expediente e WHERE e.expedienteNumber LIKE :pattern");
            query.setParameter("pattern", "OV-" + year + "-%");
            return (long) query.getSingleResult();
        } finally {
            this.getEntityManager().close();
        }
    }
}
