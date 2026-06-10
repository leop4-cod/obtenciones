package senadi.gob.ec.adminob.resources;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import senadi.gob.ec.adminob.model.History;

public class HistoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String fecha;
    private String historyUser;
    private String tipoAccion;
    private String tipoAccionLabel;
    private String tipoAccionBadgeClass;
    private String tipoEntidad;
    private String idEntidad;
    private String applicationNumber;
    private String campoModificado;
    private String valorAnterior;
    private String valorNuevo;
    private String description;

    public HistoryDTO() {
    }

    public HistoryDTO(History h) {
        this.id = h.getId();
        if (h.getFecha() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            this.fecha = sdf.format(h.getFecha());
        } else {
            this.fecha = "";
        }
        this.historyUser = h.getHistoryUser();
        this.tipoAccion = h.getTipoAccion();
        this.tipoAccionLabel = h.getTipoAccionLabel();
        this.tipoAccionBadgeClass = h.getTipoAccionBadgeClass();
        this.tipoEntidad = h.getTipoEntidad();
        this.idEntidad = h.getIdEntidad();
        this.applicationNumber = h.getApplicationNumber();
        this.campoModificado = h.getCampoModificado();
        this.valorAnterior = h.getValorAnterior();
        this.valorNuevo = h.getValorNuevo();
        this.description = h.getDescription();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHistoryUser() {
        return historyUser;
    }

    public void setHistoryUser(String historyUser) {
        this.historyUser = historyUser;
    }

    public String getTipoAccion() {
        return tipoAccion;
    }

    public void setTipoAccion(String tipoAccion) {
        this.tipoAccion = tipoAccion;
    }

    public String getTipoAccionLabel() {
        return tipoAccionLabel;
    }

    public void setTipoAccionLabel(String tipoAccionLabel) {
        this.tipoAccionLabel = tipoAccionLabel;
    }

    public String getTipoAccionBadgeClass() {
        return tipoAccionBadgeClass;
    }

    public void setTipoAccionBadgeClass(String tipoAccionBadgeClass) {
        this.tipoAccionBadgeClass = tipoAccionBadgeClass;
    }

    public String getTipoEntidad() {
        return tipoEntidad;
    }

    public void setTipoEntidad(String tipoEntidad) {
        this.tipoEntidad = tipoEntidad;
    }

    public String getIdEntidad() {
        return idEntidad;
    }

    public void setIdEntidad(String idEntidad) {
        this.idEntidad = idEntidad;
    }

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public String getCampoModificado() {
        return campoModificado;
    }

    public void setCampoModificado(String campoModificado) {
        this.campoModificado = campoModificado;
    }

    public String getValorAnterior() {
        return valorAnterior;
    }

    public void setValorAnterior(String valorAnterior) {
        this.valorAnterior = valorAnterior;
    }

    public String getValorNuevo() {
        return valorNuevo;
    }

    public void setValorNuevo(String valorNuevo) {
        this.valorNuevo = valorNuevo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
