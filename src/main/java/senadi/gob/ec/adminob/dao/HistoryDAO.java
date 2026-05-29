package senadi.gob.ec.adminob.dao;

import java.sql.Timestamp;
import java.util.List;
import javax.persistence.EntityManager;
import senadi.gob.ec.adminob.model.History;

public class HistoryDAO extends DAOAbstractM<History> {

    public HistoryDAO(History t) {
        super(t);
    }

    @Override
    public List<History> buscarTodos() {
        EntityManager em = EntityManagerM.getEntityManager();
        try {
            return em.createQuery(
                "SELECT h FROM History h ORDER BY h.fecha DESC",
                History.class).getResultList();
        } finally {
            em.close();
        }
    }

    /** Historial de una solicitud por applicationNumber (orden cronológico inverso). */
    public List<History> getHistoriesByAppNumber(String applicationNumber) {
        EntityManager em = EntityManagerM.getEntityManager();
        try {
            return em.createQuery(
                "SELECT h FROM History h WHERE h.applicationNumber = :appnum ORDER BY h.fecha DESC",
                History.class)
                .setParameter("appnum", applicationNumber)
                .getResultList();
        } finally {
            em.close();
        }
    }

    /** Historial de cualquier entidad por tipo + ID (para Tramite, Anualidad, etc.). */
    public List<History> getByEntidad(String tipoEntidad, String idEntidad) {
        EntityManager em = EntityManagerM.getEntityManager();
        try {
            return em.createQuery(
                "SELECT h FROM History h " +
                "WHERE h.tipoEntidad = :tipo AND h.idEntidad = :id " +
                "ORDER BY h.fecha DESC",
                History.class)
                .setParameter("tipo", tipoEntidad)
                .setParameter("id",   idEntidad)
                .getResultList();
        } finally {
            em.close();
        }
    }

    /** Historial de un trámite SENADI por su número de trámite. */
    public List<History> getByNumeroTramite(String numeroTramite) {
        EntityManager em = EntityManagerM.getEntityManager();
        try {
            return em.createQuery(
                "SELECT h FROM History h " +
                "WHERE h.applicationNumber = :num OR " +
                "      (h.tipoEntidad = 'Tramite' AND h.idEntidad = :num) " +
                "ORDER BY h.fecha DESC",
                History.class)
                .setParameter("num", numeroTramite)
                .getResultList();
        } finally {
            em.close();
        }
    }

    /** Últimos N eventos del sistema (para dashboard). */
    public List<History> getUltimosEventos(int maxResults) {
        EntityManager em = EntityManagerM.getEntityManager();
        try {
            return em.createQuery(
                "SELECT h FROM History h ORDER BY h.fecha DESC",
                History.class)
                .setMaxResults(maxResults)
                .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Registra un evento de auditoría en un solo paso.
     * Método de conveniencia para no repetir el bloque try/commit en los beans.
     */
    public void registrar(String applicationNumber, String tipoEntidad, String idEntidad,
                          String tipoAccion, String campoModificado,
                          String valorAnterior, String valorNuevo,
                          String descripcion, String usuario) throws Exception {
        History h = new History();
        h.setApplicationNumber(applicationNumber);
        h.setTipoEntidad(tipoEntidad);
        h.setIdEntidad(idEntidad);
        h.setTipoAccion(tipoAccion);
        h.setCampoModificado(campoModificado);
        h.setValorAnterior(truncar(valorAnterior, 2000));
        h.setValorNuevo(truncar(valorNuevo, 2000));
        h.setDescription(truncar(descripcion, 2000));
        h.setHistoryUser(usuario != null ? usuario : "sistema");
        h.setFecha(new Timestamp(System.currentTimeMillis()));
        setInstancia(h);
        persist();
    }

    private String truncar(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}
