/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package senadi.gob.ec.adminob.util;

import java.util.Date;
import java.util.List;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import senadi.gob.ec.adminob.bean.LoginBean;
import senadi.gob.ec.adminob.dao.HistoryDAO;
import senadi.gob.ec.adminob.dao.VegetableFormsDAO;
import senadi.gob.ec.adminob.model.History;
import senadi.gob.ec.adminob.model.VegetableForms;
import senadi.gob.ec.adminob.solicitudes.Owners;
import senadi.gob.ec.adminob.solicitudes.OwnersDAO;

/**
 *
 * @author michael
 */
public class Controller {

    public LoginBean getLogin() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        LoginBean loginB = (LoginBean) session.getAttribute("loginBean");
        return loginB;
    }

    public List<VegetableForms> buscarTodos() {
        VegetableFormsDAO vf = new VegetableFormsDAO(null);
        return vf.buscarTodos();
    }

    public Owners getOwnerById(Integer ownerId) {
        OwnersDAO od = new OwnersDAO();
        return od.getOwnerById(ownerId);
    }

    public List<VegetableForms> buscarTodosByType(String type) {
        VegetableFormsDAO vd = new VegetableFormsDAO(null);
        return vd.buscarTodosByType(type);
    }

    public List<VegetableForms> buscarTodosByTypeAndDate(String type, Date start, Date end) {
        VegetableFormsDAO vd = new VegetableFormsDAO(null);
        return vd.buscarTodosByTypeAndDate(type, start, end);
    }
    
    public boolean saveHistory(History history){
        HistoryDAO hd = new HistoryDAO(history);
        try {
            hd.persist();
            return true;
        } catch (Exception ex) {
            System.err.println("Error al guardar history "+history.getDescription()+": "+ex);
            return false;
        }
    }
    
    public boolean updateVegetableForms(VegetableForms vegetable){
        VegetableFormsDAO vd = new VegetableFormsDAO(vegetable);
        try {
            vd.update();
            return true;
        } catch (Exception ex) {
            System.err.println("No se pudo actualizar la obtencion vegetal "+vegetable.getApplicationNumber()+": "+ex);
            return false;
        }
    }

    public VegetableForms getVegetableFormsById(Integer id) {
        VegetableFormsDAO vd = new VegetableFormsDAO(null);
        return vd.getVegetableFormsById(id);
    }

    public void precargarLockers(List<VegetableForms> vegetables) {
        if (vegetables == null || vegetables.isEmpty()) return;
        List<Integer> ids = new java.util.ArrayList<>();
        for (VegetableForms vf : vegetables) {
            if (vf.getOwnerId() != null) ids.add(vf.getOwnerId());
        }
        if (ids.isEmpty()) return;
        OwnersDAO od = new OwnersDAO();
        java.util.Map<Integer, String> lockerMap = od.getLockersByOwnerIds(ids);
        for (VegetableForms vf : vegetables) {
            if (vf.getOwnerId() == null) {
                vf.setLocker("SIN CASILLERO");
            } else {
                String locker = lockerMap.get(vf.getOwnerId());
                vf.setLocker(locker != null ? locker : "SIN CASILLERO");
            }
        }
    }
    
    public List<History> getHistoriesByAppNumber(String applicationNumber){
        HistoryDAO hd = new HistoryDAO(null);
        return hd.getHistoriesByAppNumber(applicationNumber);
    }
}
