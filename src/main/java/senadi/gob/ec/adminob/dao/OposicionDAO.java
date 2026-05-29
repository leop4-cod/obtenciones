package senadi.gob.ec.adminob.dao;

import java.util.List;
import javax.persistence.Query;
import senadi.gob.ec.adminob.model.Oposicion;

public class OposicionDAO extends DAOAbstractM<Oposicion> {

    public OposicionDAO(Oposicion t) { super(t); }

    @Override
    public List<Oposicion> buscarTodos() {
        try {
            Query q = getEntityManager().createQuery("SELECT o FROM Oposicion o ORDER BY o.id DESC");
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return q.getResultList();
        } finally { getEntityManager().close(); }
    }

    public List<Oposicion> getByVegetableFormId(Integer vegetableFormId) {
        try {
            Query q = getEntityManager().createQuery(
                "SELECT o FROM Oposicion o WHERE o.vegetableFormId = :vfid ORDER BY o.fechaPresentacion DESC");
            q.setParameter("vfid", vegetableFormId);
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return q.getResultList();
        } finally { getEntityManager().close(); }
    }

    public Oposicion getById(Integer id) {
        try {
            return getEntityManager().find(Oposicion.class, id);
        } finally { getEntityManager().close(); }
    }
}
