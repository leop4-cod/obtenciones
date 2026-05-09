package senadi.gob.ec.adminob.dao;

import java.util.Date;
import java.util.List;
import javax.persistence.TypedQuery;
import senadi.gob.ec.adminob.enums.Status;
import senadi.gob.ec.adminob.model.VegetableForms;

public class VegetableFormsDAO extends DAOAbstractM<VegetableForms> {

    public VegetableFormsDAO(VegetableForms t) {
        super(t);
    }

    @Override
    public List<VegetableForms> buscarTodos() {
        try {
            return getEntityManager()
                .createQuery("SELECT DISTINCT v FROM VegetableForms v ORDER BY v.id",
                             VegetableForms.class)
                .setHint("javax.persistence.cache.storeMode", "REFRESH")
                .setMaxResults(300)
                .getResultList();
        } finally {
            getEntityManager().close();
        }
    }

    /**
     * Filtra por tipo de solicitud.
     * Tipos soportados: "Iniciados", "Pagados", "Vista". Cualquier otro devuelve todos.
     * "Pagados" busca trámites que tengan al menos un ComprobantePago registrado.
     */
    public List<VegetableForms> buscarTodosByType(String type) {
        try {
            TypedQuery<VegetableForms> query;
            switch (type == null ? "" : type) {
                case "Iniciados":
                    query = getEntityManager().createQuery(
                        "SELECT DISTINCT v FROM VegetableForms v WHERE v.status = :st ORDER BY v.id",
                        VegetableForms.class);
                    query.setParameter("st", Status.DELIVERED);
                    break;
                case "Pagados":
                    query = getEntityManager().createQuery(
                        "SELECT DISTINCT v FROM VegetableForms v " +
                        "WHERE v.status = :st AND EXISTS " +
                        "  (SELECT 1 FROM ComprobantePago cp WHERE cp.vegetableFormId = v.id) " +
                        "ORDER BY v.id",
                        VegetableForms.class);
                    query.setParameter("st", Status.FINISHED);
                    break;
                case "Vista":
                    query = getEntityManager().createQuery(
                        "SELECT DISTINCT v FROM VegetableForms v WHERE v.status = :st ORDER BY v.id",
                        VegetableForms.class);
                    query.setParameter("st", Status.PREVIEW);
                    break;
                default:
                    query = getEntityManager().createQuery(
                        "SELECT DISTINCT v FROM VegetableForms v ORDER BY v.id",
                        VegetableForms.class);
                    break;
            }
            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return query.setMaxResults(300).getResultList();
        } finally {
            getEntityManager().close();
        }
    }

    /**
     * Filtra por tipo y rango de fechas.
     * Las fechas se pasan como parámetros tipados — sin concatenación de strings.
     */
    public List<VegetableForms> buscarTodosByTypeAndDate(String type, Date start, Date end) {
        try {
            TypedQuery<VegetableForms> query;
            switch (type == null ? "" : type) {
                case "Iniciados":
                    query = getEntityManager().createQuery(
                        "SELECT DISTINCT v FROM VegetableForms v " +
                        "WHERE v.status = :st AND v.applicationDate BETWEEN :start AND :end " +
                        "ORDER BY v.id",
                        VegetableForms.class);
                    query.setParameter("st", Status.DELIVERED);
                    break;
                case "Pagados":
                    query = getEntityManager().createQuery(
                        "SELECT DISTINCT v FROM VegetableForms v " +
                        "WHERE v.status = :st " +
                        "AND EXISTS (SELECT 1 FROM ComprobantePago cp WHERE cp.vegetableFormId = v.id) " +
                        "AND v.applicationDate BETWEEN :start AND :end " +
                        "ORDER BY v.id",
                        VegetableForms.class);
                    query.setParameter("st", Status.FINISHED);
                    break;
                case "Vista":
                    query = getEntityManager().createQuery(
                        "SELECT DISTINCT v FROM VegetableForms v " +
                        "WHERE v.status = :st AND v.applicationDate BETWEEN :start AND :end " +
                        "ORDER BY v.id",
                        VegetableForms.class);
                    query.setParameter("st", Status.PREVIEW);
                    break;
                default:
                    query = getEntityManager().createQuery(
                        "SELECT DISTINCT v FROM VegetableForms v " +
                        "WHERE v.applicationDate BETWEEN :start AND :end " +
                        "ORDER BY v.id",
                        VegetableForms.class);
                    break;
            }
            // Parámetros de fecha tipados — nunca interpolados en el string JPQL
            query.setParameter("start", start, javax.persistence.TemporalType.DATE);
            query.setParameter("end",   end,   javax.persistence.TemporalType.DATE);
            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return query.setMaxResults(300).getResultList();
        } finally {
            getEntityManager().close();
        }
    }

    public VegetableForms getVegetableFormsById(Integer id) {
        try {
            List<VegetableForms> result = getEntityManager()
                .createQuery(
                    "SELECT DISTINCT v FROM VegetableForms v WHERE v.id = :id",
                    VegetableForms.class)
                .setParameter("id", id)
                .setHint("javax.persistence.cache.storeMode", "REFRESH")
                .setMaxResults(1)
                .getResultList();
            return result.isEmpty() ? new VegetableForms() : result.get(0);
        } finally {
            getEntityManager().close();
        }
    }

    public List<VegetableForms> getVegetableFormsByOwnerId(Integer id) {
        try {
            return getEntityManager()
                .createQuery(
                    "SELECT DISTINCT v FROM VegetableForms v WHERE v.ownerId = :id ORDER BY v.id DESC",
                    VegetableForms.class)
                .setParameter("id", id)
                .setHint("javax.persistence.cache.storeMode", "REFRESH")
                .getResultList();
        } finally {
            getEntityManager().close();
        }
    }

    public VegetableForms getVegetableFormsByApplicationNumber(String applicationNumber) {
        try {
            List<VegetableForms> result = getEntityManager()
                .createQuery(
                    "SELECT DISTINCT v FROM VegetableForms v WHERE v.applicationNumber = :appnumber",
                    VegetableForms.class)
                .setParameter("appnumber", applicationNumber)
                .setHint("javax.persistence.cache.storeMode", "REFRESH")
                .setMaxResults(1)
                .getResultList();
            return result.isEmpty() ? new VegetableForms() : result.get(0);
        } finally {
            getEntityManager().close();
        }
    }

    public List<VegetableForms> getVegetableFormsPaymentByOwnerId(Integer ownerId) {
        try {
            return getEntityManager()
                .createQuery(
                    "SELECT DISTINCT v FROM VegetableForms v " +
                    "WHERE v.ownerId = :ownerid AND v.status = :status " +
                    "AND EXISTS (SELECT 1 FROM ComprobantePago cp WHERE cp.vegetableFormId = v.id)",
                    VegetableForms.class)
                .setParameter("ownerid", ownerId)
                .setParameter("status",  Status.FINISHED)
                .setHint("javax.persistence.cache.storeMode", "REFRESH")
                .getResultList();
        } finally {
            getEntityManager().close();
        }
    }
}
