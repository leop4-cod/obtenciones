package senadi.gob.ec.adminob.dao;

import java.util.List;
import javax.persistence.Query;
import senadi.gob.ec.adminob.model.CertificadoObtentor;

public class CertificadoObtentorDAO extends DAOAbstractM<CertificadoObtentor> {

    public CertificadoObtentorDAO(CertificadoObtentor t) { super(t); }

    @Override
    public List<CertificadoObtentor> buscarTodos() {
        try {
            Query q = getEntityManager().createQuery("SELECT c FROM CertificadoObtentor c ORDER BY c.id DESC");
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return q.getResultList();
        } finally { getEntityManager().close(); }
    }

    public CertificadoObtentor getByVegetableFormId(Integer vegetableFormId) {
        try {
            Query q = getEntityManager().createQuery(
                "SELECT c FROM CertificadoObtentor c WHERE c.vegetableFormId = :vfid ORDER BY c.id DESC");
            q.setParameter("vfid", vegetableFormId);
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            List<CertificadoObtentor> r = q.setMaxResults(1).getResultList();
            return r.isEmpty() ? null : r.get(0);
        } finally { getEntityManager().close(); }
    }

    public long countByYear(int year) {
        try {
            Query q = getEntityManager().createQuery(
                "SELECT COUNT(c) FROM CertificadoObtentor c WHERE c.numCertificado LIKE :p");
            q.setParameter("p", "COB-" + year + "-%");
            return (long) q.getSingleResult();
        } finally { getEntityManager().close(); }
    }
}
