package senadi.gob.ec.adminob.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.hibernate.jpa.HibernatePersistenceProvider;
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
            // Propiedades JDBC explícitas para que Hibernate use MySQL directamente,
            // ignorando cualquier datasource JNDI que WildFly pudiera asignar.
            java.util.Map<String, Object> props = new java.util.HashMap<>();
            props.put("javax.persistence.jdbc.driver",   "com.mysql.jdbc.Driver");
            props.put("javax.persistence.jdbc.url",      "jdbc:mysql://localhost:3306/vegetable_prod?useSSL=false&serverTimezone=UTC");
            props.put("javax.persistence.jdbc.user",     "root");
            props.put("javax.persistence.jdbc.password", "root");
            props.put("hibernate.dialect",               "org.hibernate.dialect.MySQL57Dialect");
            props.put("hibernate.hbm2ddl.auto",          "none");
            props.put("hibernate.show_sql",              "false");
            props.put("hibernate.format_sql",            "false");
            props.put("hibernate.connection.handling_mode", "DELAYED_ACQUISITION_AND_HOLD");
            // Usar HibernatePersistenceProvider directamente — WildFly no intercepta esta llamada
            EMF = new HibernatePersistenceProvider().createEntityManagerFactory("vegetablePU", props);
            LOG.info("EntityManagerFactory inicializado correctamente para 'vegetablePU' (HibernatePersistenceProvider).");
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
