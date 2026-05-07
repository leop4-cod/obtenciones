package senadi.gob.ec.adminob.dao;

import java.util.List;
import javax.persistence.Query;
import senadi.gob.ec.adminob.model.PublicacionGaceta;

public class PublicacionGacetaDAO extends DAOAbstractM<PublicacionGaceta> {

    public PublicacionGacetaDAO(PublicacionGaceta t) { super(t); }

    @Override
    public List<PublicacionGaceta> buscarTodos() {
        try {
            Query q = getEntityManager().createQuery("SELECT p FROM PublicacionGaceta p ORDER BY p.id DESC");
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return q.getResultList();
        } finally { getEntityManager().close(); }
    }

    public PublicacionGaceta getByVegetableFormId(Integer vegetableFormId) {
        try {
            Query q = getEntityManager().createQuery(
                "SELECT p FROM PublicacionGaceta p WHERE p.vegetableFormId = :vfid ORDER BY p.id DESC");
            q.setParameter("vfid", vegetableFormId);
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            List<PublicacionGaceta> r = q.setMaxResults(1).getResultList();
            return r.isEmpty() ? null : r.get(0);
        } finally { getEntityManager().close(); }
    }
}
