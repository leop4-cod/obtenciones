package senadi.gob.ec.adminob.dao;

import java.util.List;
import javax.persistence.Query;
import senadi.gob.ec.adminob.model.DictamenTecnico;

public class DictamenTecnicoDAO extends DAOAbstractM<DictamenTecnico> {

    public DictamenTecnicoDAO(DictamenTecnico t) { super(t); }

    @Override
    public List<DictamenTecnico> buscarTodos() {
        try {
            Query q = getEntityManager().createQuery("SELECT d FROM DictamenTecnico d ORDER BY d.id DESC");
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return q.getResultList();
        } finally { getEntityManager().close(); }
    }

    public DictamenTecnico getByVegetableFormId(Integer vegetableFormId) {
        try {
            Query q = getEntityManager().createQuery(
                "SELECT d FROM DictamenTecnico d WHERE d.vegetableFormId = :vfid ORDER BY d.id DESC");
            q.setParameter("vfid", vegetableFormId);
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            List<DictamenTecnico> r = q.setMaxResults(1).getResultList();
            return r.isEmpty() ? null : r.get(0);
        } finally { getEntityManager().close(); }
    }
}
