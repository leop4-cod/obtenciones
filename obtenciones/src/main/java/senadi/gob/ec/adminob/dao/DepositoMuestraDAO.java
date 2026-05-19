package senadi.gob.ec.adminob.dao;

import java.util.List;
import javax.persistence.Query;
import senadi.gob.ec.adminob.model.DepositoMuestra;

public class DepositoMuestraDAO extends DAOAbstractM<DepositoMuestra> {

    public DepositoMuestraDAO(DepositoMuestra t) { super(t); }

    @Override
    public List<DepositoMuestra> buscarTodos() {
        try {
            Query q = getEntityManager().createQuery("SELECT d FROM DepositoMuestra d ORDER BY d.id DESC");
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return q.getResultList();
        } finally { getEntityManager().close(); }
    }

    public DepositoMuestra getByVegetableFormId(Integer vegetableFormId) {
        try {
            Query q = getEntityManager().createQuery(
                "SELECT d FROM DepositoMuestra d WHERE d.vegetableFormId = :vfid ORDER BY d.id DESC");
            q.setParameter("vfid", vegetableFormId);
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            List<DepositoMuestra> r = q.setMaxResults(1).getResultList();
            return r.isEmpty() ? null : r.get(0);
        } finally { getEntityManager().close(); }
    }
}
