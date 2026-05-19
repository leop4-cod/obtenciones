package senadi.gob.ec.adminob.dao;

import java.util.List;
import javax.persistence.Query;
import senadi.gob.ec.adminob.model.CertificadoDeposito;

public class CertificadoDepositoDAO extends DAOAbstractM<CertificadoDeposito> {

    public CertificadoDepositoDAO(CertificadoDeposito t) { super(t); }

    @Override
    public List<CertificadoDeposito> buscarTodos() {
        try {
            Query q = getEntityManager().createQuery("SELECT c FROM CertificadoDeposito c ORDER BY c.id DESC");
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return q.getResultList();
        } finally { getEntityManager().close(); }
    }

    public CertificadoDeposito getByVegetableFormId(Integer vegetableFormId) {
        try {
            Query q = getEntityManager().createQuery(
                "SELECT c FROM CertificadoDeposito c WHERE c.vegetableFormId = :vfid ORDER BY c.id DESC");
            q.setParameter("vfid", vegetableFormId);
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            List<CertificadoDeposito> r = q.setMaxResults(1).getResultList();
            return r.isEmpty() ? null : r.get(0);
        } finally { getEntityManager().close(); }
    }

    public long countByYear(int year) {
        try {
            Query q = getEntityManager().createQuery(
                "SELECT COUNT(c) FROM CertificadoDeposito c WHERE c.numCertificado LIKE :p");
            q.setParameter("p", "CD-" + year + "-%");
            return (long) q.getSingleResult();
        } finally { getEntityManager().close(); }
    }
}
