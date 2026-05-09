package senadi.gob.ec.adminob.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fábrica de EntityManager para uso con RESOURCE_LOCAL.
 *
 * Reglas:
 *  - EMF se crea UNA sola vez al arrancar la aplicación (static initializer).
 *    EntityManagerFactory es thread-safe y costoso de construir.
 *  - getEntityManager() devuelve un EM NUEVO en cada llamada.
 *    EntityManager NO es thread-safe; cada operación de DAO debe usar
 *    el suyo propio y cerrarlo en el bloque finally.
 */
public final class EntityManagerM {

    private static final Logger LOG = Logger.getLogger(EntityManagerM.class.getName());

    private static final EntityManagerFactory EMF;

    static {
        try {
            EMF = Persistence.createEntityManagerFactory("vegetablePU");
            LOG.info("EntityManagerFactory inicializado correctamente para 'vegetablePU'.");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "No se pudo crear EntityManagerFactory para 'vegetablePU'.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private EntityManagerM() {
    }

    /**
     * Devuelve un EntityManager nuevo.
     * El llamador es responsable de cerrarlo en un bloque finally.
     */
    public static EntityManager getEntityManager() {
        return EMF.createEntityManager();
    }
}
