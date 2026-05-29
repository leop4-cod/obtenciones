package senadi.gob.ec.adminob.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.primefaces.PrimeFaces;
import senadi.gob.ec.adminob.dao.ParametroSistemaDAO;
import senadi.gob.ec.adminob.model.ParametroSistema;
import senadi.gob.ec.adminob.util.Controller;
import senadi.gob.ec.adminob.util.Operations;

/**
 * Bean de sesión para la gestión de parámetros de negocio configurables.
 * Vista: parametros/configuracion.xhtml
 */
@ManagedBean(name = "parametroBean")
@SessionScoped
public class ParametroBean implements Serializable {

    private List<ParametroSistema> parametros = new ArrayList<>();
    private ParametroSistema parametroEnEdicion;

    @PostConstruct
    public void init() {
        cargar();
    }

    public void cargar() {
        try {
            parametros = new ParametroSistemaDAO(null).buscarTodos();
        } catch (Exception e) {
            parametros = new ArrayList<>();
            Operations.message(Operations.ERROR, "Error al cargar parámetros: " + e.getMessage());
        }
    }

    public void prepararEdicion(ParametroSistema p) {
        // Copiar para no modificar el original hasta confirmar
        parametroEnEdicion = new ParametroSistema();
        parametroEnEdicion.setId(p.getId());
        parametroEnEdicion.setClave(p.getClave());
        parametroEnEdicion.setValor(p.getValor());
        parametroEnEdicion.setDescripcion(p.getDescripcion());
        parametroEnEdicion.setTipo(p.getTipo());
    }

    public void guardarParametro() {
        if (parametroEnEdicion == null) return;
        if (parametroEnEdicion.getValor() == null || parametroEnEdicion.getValor().trim().isEmpty()) {
            Operations.message(Operations.ERROR, "El valor no puede estar vacío.");
            return;
        }
        // Validación de tipo
        if (!validarValorSegunTipo(parametroEnEdicion)) return;

        try {
            parametroEnEdicion.setUsuarioMod(obtenerUsuario());
            new ParametroSistemaDAO(null).actualizarParametro(parametroEnEdicion);
            cargar();
            PrimeFaces.current().executeScript("PF('dlgParam').hide();");
            Operations.message(Operations.INFORMACION, "Parámetro '"
                + parametroEnEdicion.getClave() + "' actualizado correctamente.");
        } catch (Exception e) {
            Operations.message(Operations.ERROR, "Error al guardar: " + e.getMessage());
        }
    }

    private boolean validarValorSegunTipo(ParametroSistema p) {
        try {
            String tipo = p.getTipo();
            String val  = p.getValor().trim();
            if ("ENTERO".equals(tipo)) {
                Integer.parseInt(val);
            } else if ("DECIMAL".equals(tipo)) {
                new java.math.BigDecimal(val);
            } else if ("BOOLEANO".equals(tipo) && !"true".equalsIgnoreCase(val) && !"false".equalsIgnoreCase(val)) {
                Operations.message(Operations.ERROR, "El valor debe ser 'true' o 'false'.");
                return false;
            }
        } catch (NumberFormatException e) {
            Operations.message(Operations.ERROR,
                "Valor inválido para tipo " + p.getTipo() + ": " + p.getValor());
            return false;
        }
        return true;
    }

    private String obtenerUsuario() {
        try {
            LoginBean login = new Controller().getLogin();
            return login != null && login.getLogin() != null ? login.getLogin() : "sistema";
        } catch (Exception e) {
            return "sistema";
        }
    }

    // ── getters / setters ──

    public List<ParametroSistema> getParametros() { return parametros; }

    public ParametroSistema getParametroEnEdicion() { return parametroEnEdicion; }
    public void setParametroEnEdicion(ParametroSistema p) { this.parametroEnEdicion = p; }
}
