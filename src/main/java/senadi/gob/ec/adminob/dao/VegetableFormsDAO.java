/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package senadi.gob.ec.adminob.dao;

import java.util.Date;
import java.util.List;
import javax.persistence.Query;
import senadi.gob.ec.adminob.enums.Status;
import senadi.gob.ec.adminob.model.VegetableForms;
import senadi.gob.ec.adminob.util.Operations;

/**
 *
 * @author michael
 */
public class VegetableFormsDAO extends DAOAbstractM<VegetableForms> {

    public VegetableFormsDAO(VegetableForms t) {
        super(t);
    }

    @Override
    public List<VegetableForms> buscarTodos() {
        try {
            Query query = this.getEntityManager().createQuery("Select v from VegetableForms v order by v.id");
            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return query.setMaxResults(300).getResultList();
        } finally {
            this.getEntityManager().close();
        }
    }
    
    public List<VegetableForms> buscarTodosByType(String type) {
        String parameter = "";
        switch (type) {
            case "Iniciados":
                parameter = "where v.status = 'DELIVERED'";
                break;
            case "Pagados":
                parameter = "where v.status = 'FINISHED' and v.paymentReceiptId is not null";
                break;
            case "Vista":
                parameter = "where v.status = 'PREVIEW'";
                break;
            default:
                parameter = "";
                break;
        }
        try {
            Query query = this.getEntityManager().createQuery("Select v from VegetableForms v "+parameter+" order by v.id");            
            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return query.setMaxResults(300).getResultList();
        } finally {
            this.getEntityManager().close();
        }
    }
    
    public List<VegetableForms> buscarTodosByTypeAndDate(String type, Date start, Date end) {
        
        String ini = Operations.formatDate(start);
        String fin = Operations.formatDate(end);
        
        
        String fecha = " v.applicationDate BETWEEN '"+ini+"' and '"+fin+"'";
        String parameter = "";
        switch (type) {
            case "Iniciados":
                parameter = "where v.status = 'DELIVERED' and "+fecha;
                break;
            case "Pagados":
                parameter = "where v.status = 'FINISHED' and v.paymentReceiptId is not null and "+fecha;
                break;
            case "Vista":
                parameter = "where v.status = 'PREVIEW' and" + fecha;
                break;
            default:
                parameter = "where "+fecha;
                break;
        }
        try {
            Query query = this.getEntityManager().createQuery("Select v from VegetableForms v "+parameter+" order by v.id");            
            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return query.setMaxResults(300).getResultList();
        } finally {
            this.getEntityManager().close();
        }
    }

    public List<VegetableForms> getVegetableFormsByOwnerId(Integer id) {
        try {
            Query query = this.getEntityManager().createQuery("Select v from VegetableForms v where v.ownerId = :id order by v.id desc");
            query.setParameter("id", id);
            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return query.getResultList();
        } finally {
            this.getEntityManager().close();
        }
    }

    public VegetableForms getVegetableFormsById(Integer id) {
        try {
            Query query = this.getEntityManager().createQuery("Select v from VegetableForms v where v.id = :id");
            query.setParameter("id", id);
            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            List<VegetableForms> vfs = query.getResultList();
            if (vfs.isEmpty()) {
                return new VegetableForms();
            } else {
                return vfs.get(0);
            }
        } finally {
            this.getEntityManager().close();
        }
    }

    public VegetableForms getVegetableFormsByApplicationNumber(String applicationNumber) {
        try {
            Query query = this.getEntityManager().createQuery("Select v from VegetableForms v where v.applicationNumber = :appnumber");
            query.setParameter("appnumber", applicationNumber);
            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            List<VegetableForms> vegs = query.getResultList();
            if (vegs.isEmpty()) {
                return new VegetableForms();
            } else {
                return vegs.get(0);
            }
        } finally {
            this.getEntityManager().close();
        }
    }

    public List<VegetableForms> getVegetableFormsPaymentByOwnerId(Integer ownerId) {
        try {
            Query query = this.getEntityManager().createQuery("Select v from VegetableForms v where v.ownerId = :ownerid and v.paymentReceiptId is not null and v.status = :status");
            query.setParameter("ownerid", ownerId);
            query.setParameter("status", Status.FINISHED);
            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return query.getResultList();
        } finally {
            this.getEntityManager().close();
        }
    }
}
