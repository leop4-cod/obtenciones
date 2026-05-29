package senadi.gob.ec.adminob.dao;

import java.util.List;
import javax.persistence.Query;
import senadi.gob.ec.adminob.model.ExamenDHE;

public class ExamenDHEDAO extends DAOAbstractM<ExamenDHE> {

    public ExamenDHEDAO(ExamenDHE t) { super(t); }

    @Override
    public List<ExamenDHE> buscarTodos() {
        try {
            Query q = getEntityManager().createQuery("SELECT e FROM ExamenDHE e ORDER BY e.id DESC");
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return q.getResultList();
        } finally { getEntityManager().close(); }
    }

    public ExamenDHE getByVegetableFormId(Integer vegetableFormId) {
        try {
            Query q = getEntityManager().createQuery(
                "SELECT e FROM ExamenDHE e WHERE e.vegetableFormId = :vfid ORDER BY e.id DESC");
            q.setParameter("vfid", vegetableFormId);
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            List<ExamenDHE> r = q.setMaxResults(1).getResultList();
            return r.isEmpty() ? null : r.get(0);
        } finally { getEntityManager().close(); }
    }
}
