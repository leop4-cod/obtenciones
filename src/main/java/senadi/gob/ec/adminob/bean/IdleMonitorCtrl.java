/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package senadi.gob.ec.adminob.bean;

/**
 *
 * @author Michael Yanangómez
 */
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import senadi.gob.ec.adminob.util.BaseController;




@ManagedBean(name = "idleMonitorCtrl")
public class IdleMonitorCtrl extends BaseController {

    private static final long serialVersionUID = -2936502063859756421L;

    public void idleListener() {
        System.err.println("sesión caducada idle obtenciones-web");
        getHttpServletRequest().getSession(false).invalidate();

        try {
            FacesContext contex = FacesContext.getCurrentInstance();
            contex.getExternalContext().redirect("login.xhtml");
        } catch (Exception e) {
            System.err.println("no se pudo terminar la sesion");
        }
    }

}
