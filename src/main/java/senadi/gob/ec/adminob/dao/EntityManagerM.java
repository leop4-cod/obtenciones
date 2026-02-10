/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package senadi.gob.ec.adminob.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


/**
 *
 * @author Michael Y.
 */
public class EntityManagerM {

    private static EntityManager em = null;

    public static EntityManager getEntityManager() {
        if (em == null || !em.isOpen()) {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("vegetablePU");
            em = emf.createEntityManager();
        }
        return em;
    }
}
