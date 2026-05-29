package senadi.gob.ec.adminob.bean;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpSession;
import org.primefaces.PrimeFaces;
import senadi.gob.ec.adminob.util.AppConfig;
import senadi.gob.ec.adminob.util.LDAP;
import senadi.gob.ec.adminob.util.Operations;

@ManagedBean(name = "loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = -2152389656664659476L;
    private String login;
    private String clave;
    private boolean logeado = false;

    private boolean shake;

    private boolean various;

    private boolean lectura;
    private String grupoActivo;

    private String dialogText;

    private int avisapagos; //1: caducados, 2: por caducar        

    public LoginBean() {
        shake = true;
    }

    public boolean estaLogeado() {
        return logeado;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public void logueo(ActionEvent actionEvent) throws Exception {
        if (isLocalAccess()) {
            if (login != null && !login.trim().isEmpty() && clave != null && !clave.trim().isEmpty()) {
                grupoActivo = "LOCALHOST";
                shake = false;
                logeado = true;
                Operations.message(Operations.INFORMACION, "Bienvenido " + login + " (modo local)");
                PrimeFaces.current().ajax().addCallbackParam("estaLogeado", logeado);
                PrimeFaces.current().ajax().addCallbackParam("view", "index.xhtml");
            } else {
                Operations.message(Operations.ERROR, "Ingrese usuario y clave");
            }
            return;
        }

        LDAP ldap = new LDAP();
        String grupo = "SC_Obtenciones";

        int n = ldap.validarIngresoLDAPRestringido(login, clave, grupo);
//        int n = ldap.validarIngresoLDAPSinrestrinccion(login, clave) ? 1 : 0;
//        int  n = 1;
        switch (n) {
            case 1:

                grupoActivo = grupo;
                shake = false;
                logeado = true;
                Operations.message(Operations.INFORMACION, "Bienvenido " + login);
                PrimeFaces.current().ajax().addCallbackParam("estaLogeado", logeado);
                PrimeFaces.current().ajax().addCallbackParam("view", "index.xhtml");
                break;
            case -1:
                Operations.message(Operations.ERROR, "No tiene autorización para ingresar");
                break;
            default:
                Operations.message(Operations.ERROR, "Credenciales Incorrectas");
                break;
        }

    }

    private boolean isLocalAccess() {
        String forced = AppConfig.get("adminob.local", "ADMINOB_LOCAL", "");
        if (AppConfig.isTruthy(forced)) {
            return true;
        }
        try {
            String serverName = FacesContext.getCurrentInstance().getExternalContext().getRequestServerName();
            return "localhost".equalsIgnoreCase(serverName)
                    || "127.0.0.1".equals(serverName)
                    || "0:0:0:0:0:0:0:1".equals(serverName)
                    || "::1".equals(serverName);
        } catch (Exception ex) {
            return false;
        }
    }

    public void logout() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        session.invalidate();
        logeado = false;
        shake = false;
    }

    public void prepararVerCaducado() {
        avisapagos = 1;
    }

    public void prepararVerPorCaducar() {
        avisapagos = 2;
    }

    public void prepararVerRevisiones() {
        avisapagos = 3;
    }

    /**
     * @return the shake
     */
    public boolean isShake() {
        return shake;
    }

    /**
     * @param shake the shake to set
     */
    public void setShake(boolean shake) {
        this.shake = shake;
    }

    /**
     * @return the lectura
     */
    public boolean isLectura() {
        return lectura;
    }

    /**
     * @param lectura the lectura to set
     */
    public void setLectura(boolean lectura) {
        this.lectura = lectura;
    }

    /**
     * @return the various
     */
    public boolean isVarious() {
        return various;
    }

    /**
     * @param various the various to set
     */
    public void setVarious(boolean various) {
        this.various = various;
    }

    /**
     * @return the grupoActivo
     */
    public String getGrupoActivo() {
        return grupoActivo;
    }

    /**
     * @param grupoActivo the grupoActivo to set
     */
    public void setGrupoActivo(String grupoActivo) {
        this.grupoActivo = grupoActivo;
    }
   
    /**
     * @return the dialogText
     */
    public String getDialogText() {
        return dialogText;
    }

    /**
     * @param dialogText the dialogText to set
     */
    public void setDialogText(String dialogText) {
        this.dialogText = dialogText;
    }

    /**
     * @return the avisapagos
     */
    public int getAvisapagos() {
        return avisapagos;
    }

    /**
     * @param avisapagos the avisapagos to set
     */
    public void setAvisapagos(int avisapagos) {
        this.avisapagos = avisapagos;
    }

}
