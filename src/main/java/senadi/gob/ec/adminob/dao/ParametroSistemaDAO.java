package senadi.gob.ec.adminob.dao;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import senadi.gob.ec.adminob.model.ParametroSistema;

/**
 * DAO para leer y actualizar parámetros de negocio configurables.
 * Todos los valores defaults reflejan el comportamiento original
 * para garantizar retro-compatibilidad si la tabla no existe aún.
 */
public class ParametroSistemaDAO extends DAOAbstractM<ParametroSistema> {

    public ParametroSistemaDAO(ParametroSistema p) {
        super(p);
    }

    @Override
    public List<ParametroSistema> buscarTodos() {
        EntityManager em = EntityManagerM.getEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM ParametroSistema p WHERE p.activo = true ORDER BY p.clave",
                ParametroSistema.class).getResultList();
        } finally {
            em.close();
        }
    }

    public ParametroSistema buscarPorClave(String clave) {
        EntityManager em = EntityManagerM.getEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM ParametroSistema p WHERE p.clave = :clave AND p.activo = true",
                ParametroSistema.class)
                .setParameter("clave", clave)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    /** Retorna el valor como String o el default si el parámetro no existe. */
    public String getValor(String clave, String valorDefault) {
        try {
            ParametroSistema p = buscarPorClave(clave);
            return p != null ? p.getValor() : valorDefault;
        } catch (Exception e) {
            return valorDefault;
        }
    }

    public int getValorEntero(String clave, int valorDefault) {
        try {
            return Integer.parseInt(getValor(clave, String.valueOf(valorDefault)).trim());
        } catch (NumberFormatException e) {
            return valorDefault;
        }
    }

    public BigDecimal getValorDecimal(String clave, BigDecimal valorDefault) {
        try {
            return new BigDecimal(getValor(clave, valorDefault.toPlainString()).trim());
        } catch (NumberFormatException e) {
            return valorDefault;
        }
    }

    /** Actualiza solo el valor de un parámetro existente. */
    public void actualizarParametro(ParametroSistema src) throws Exception {
        EntityManager em = EntityManagerM.getEntityManager();
        em.getTransaction().begin();
        try {
            ParametroSistema managed = em.find(ParametroSistema.class, src.getId());
            if (managed == null) throw new Exception("Parámetro no encontrado: id=" + src.getId());
            managed.setValor(src.getValor() != null ? src.getValor().trim() : "");
            managed.setDescripcion(src.getDescripcion());
            managed.setUsuarioMod(src.getUsuarioMod());
            managed.setFechaMod(new Timestamp(System.currentTimeMillis()));
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
