package senadi.gob.ec.adminob.dao;

import java.util.List;
import javax.persistence.EntityManager;

/**
 * DAO base para transacciones RESOURCE_LOCAL manuales.
 *
 * Cada instancia obtiene su propio EntityManager en el constructor y
 * lo cierra en el bloque finally de cada operación, garantizando que
 * no haya fugas de conexión aunque ocurra una excepción.
 *
 * @param <T> tipo de entidad JPA
 */
public abstract class DAOAbstractM<T> {

    private T instancia;
    private final EntityManager entityManager;

    public DAOAbstractM(T t) {
        instancia = t;
        entityManager = EntityManagerM.getEntityManager();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public T getInstancia() {
        return instancia;
    }

    public void setInstancia(T instancia) {
        this.instancia = instancia;
    }

    /** Persiste la entidad en una transacción atómica. */
    public void persist() throws Exception {
        entityManager.getTransaction().begin();
        try {
            entityManager.persist(instancia);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        } finally {
            entityManager.close();
        }
    }

    /** Hace merge (INSERT o UPDATE) en una transacción atómica. */
    public void update() throws Exception {
        entityManager.getTransaction().begin();
        try {
            instancia = entityManager.merge(instancia);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        } finally {
            entityManager.close();
        }
    }

    /** Elimina la entidad en una transacción atómica. */
    public void remove() throws Exception {
        entityManager.getTransaction().begin();
        try {
            T managed = entityManager.merge(instancia);
            entityManager.remove(managed);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        } finally {
            entityManager.close();
        }
    }

    /** Busca por clave primaria Long. Cierra el EM internamente. */
    @SuppressWarnings("unchecked")
    public T findById(Long id) {
        try {
            return (T) entityManager.find(instancia.getClass(), id);
        } finally {
            entityManager.close();
        }
    }

    public abstract List<T> buscarTodos();
}
