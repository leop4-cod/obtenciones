package senadi.gob.ec.adminob.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import senadi.gob.ec.adminob.enums.EstadoAnualidad;
import senadi.gob.ec.adminob.model.Anualidad;

public class AnualidadDAO extends DAOAbstractM<Anualidad> {

    public AnualidadDAO(Anualidad t) {
        super(t);
    }

    @Override
    public List<Anualidad> buscarTodos() {
        EntityManager em = EntityManagerM.getEntityManager();
        try {
            return em.createQuery(
                "SELECT a FROM Anualidad a ORDER BY a.vegetableFormId, a.anio",
                Anualidad.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Anualidad> buscarPorVegetableFormId(Integer vegetableFormId) {
        EntityManager em = EntityManagerM.getEntityManager();
        try {
            return em.createQuery(
                "SELECT a FROM Anualidad a WHERE a.vegetableFormId = :vid ORDER BY a.anio",
                Anualidad.class)
                .setParameter("vid", vegetableFormId)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public boolean existenAnualidades(Integer vegetableFormId) {
        EntityManager em = EntityManagerM.getEntityManager();
        try {
            Long count = em.createQuery(
                "SELECT COUNT(a) FROM Anualidad a WHERE a.vegetableFormId = :vid",
                Long.class)
                .setParameter("vid", vegetableFormId)
                .getSingleResult();
            return count != null && count > 0;
        } finally {
            em.close();
        }
    }

    public void crearAnualidadesVacias(Integer vegetableFormId, String usuarioRegistro) throws Exception {
        if (existenAnualidades(vegetableFormId)) return;
        EntityManager em = EntityManagerM.getEntityManager();
        em.getTransaction().begin();
        try {
            Timestamp ahora = new Timestamp(System.currentTimeMillis());
            for (int i = 1; i <= 20; i++) {
                Anualidad a = new Anualidad();
                a.setVegetableFormId(vegetableFormId);
                a.setAnio(i);
                a.setEtiqueta("Año" + i);
                a.setEstado(EstadoAnualidad.PENDIENTE);
                a.setUsuarioRegistro(usuarioRegistro);
                a.setFechaCreacion(ahora);
                a.setFechaModificacion(ahora);
                em.persist(a);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void actualizarAnualidad(Anualidad src) throws Exception {
        EntityManager em = EntityManagerM.getEntityManager();
        em.getTransaction().begin();
        try {
            Anualidad m = em.find(Anualidad.class, src.getId());
            if (m == null) throw new Exception("Anualidad no encontrada id=" + src.getId());
            m.setFechaVencimiento(src.getFechaVencimiento());
            m.setFechaPago(src.getFechaPago());
            m.setMonto(src.getMonto());
            m.setNumeroComprobante(src.getNumeroComprobante());
            m.setValorPagadoRecargo(src.getValorPagadoRecargo());
            m.setNumeroComprobanteRecargo(src.getNumeroComprobanteRecargo());
            m.setEstado(src.getEstado());
            m.setObservaciones(src.getObservaciones());
            m.setFechaModificacion(new Timestamp(System.currentTimeMillis()));
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Anualidad> buscarConAlerta(int diasAnticipacion) {
        EntityManager em = EntityManagerM.getEntityManager();
        try {
            java.sql.Date hoy = new java.sql.Date(System.currentTimeMillis());
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DAY_OF_MONTH, diasAnticipacion);
            java.sql.Date limite = new java.sql.Date(cal.getTimeInMillis());
            return em.createQuery(
                "SELECT a FROM Anualidad a " +
                "WHERE a.fechaVencimiento IS NOT NULL " +
                "AND a.fechaVencimiento BETWEEN :hoy AND :limite " +
                "AND a.estado <> :pagado " +
                "ORDER BY a.fechaVencimiento, a.vegetableFormId",
                Anualidad.class)
                .setParameter("hoy",    hoy)
                .setParameter("limite", limite)
                .setParameter("pagado", EstadoAnualidad.PAGADO)
                .getResultList();
        } finally {
            em.close();
        }
    }

    /** Asigna la fecha de vencimiento a la siguiente anualidad al registrar un pago. */
    public void actualizarFechaVencimientoSiguiente(Integer vegetableFormId, Integer anioActual,
                                                     java.sql.Date fechaVencimiento) throws Exception {
        EntityManager em = EntityManagerM.getEntityManager();
        em.getTransaction().begin();
        try {
            em.createQuery(
                "UPDATE Anualidad a SET a.fechaVencimiento = :fecha, " +
                "a.fechaModificacion = :ahora " +
                "WHERE a.vegetableFormId = :vid AND a.anio = :anio")
                .setParameter("fecha", fechaVencimiento)
                .setParameter("ahora", new java.sql.Timestamp(System.currentTimeMillis()))
                .setParameter("vid",   vegetableFormId)
                .setParameter("anio",  anioActual + 1)
                .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public int marcarVencidas() throws Exception {
        EntityManager em = EntityManagerM.getEntityManager();
        em.getTransaction().begin();
        try {
            java.sql.Date hoy = new java.sql.Date(System.currentTimeMillis());
            int n = em.createQuery(
                "UPDATE Anualidad a SET a.estado = :vencido " +
                "WHERE a.fechaVencimiento IS NOT NULL " +
                "AND a.fechaVencimiento < :hoy " +
                "AND a.estado = :pendiente")
                .setParameter("vencido",   EstadoAnualidad.VENCIDO)
                .setParameter("hoy",       hoy)
                .setParameter("pendiente", EstadoAnualidad.PENDIENTE)
                .executeUpdate();
            em.getTransaction().commit();
            return n;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Integer> idsConAnualidades() {
        EntityManager em = EntityManagerM.getEntityManager();
        try {
            return em.createQuery(
                "SELECT DISTINCT a.vegetableFormId FROM Anualidad a",
                Integer.class).getResultList();
        } finally {
            em.close();
        }
    }
}
