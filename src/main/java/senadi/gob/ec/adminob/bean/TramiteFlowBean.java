package senadi.gob.ec.adminob.bean;

import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import senadi.gob.ec.adminob.enums.FlowPhase;
import senadi.gob.ec.adminob.model.Expediente;
import senadi.gob.ec.adminob.model.VegetableForms;
import senadi.gob.ec.adminob.service.TramiteFlowService;
import senadi.gob.ec.adminob.util.Controller;

@ManagedBean(name = "tramiteFlowBean")
@ViewScoped
public class TramiteFlowBean implements Serializable {

    private VegetableForms selectedForm;
    private Expediente expediente;
    private String tecnico;
    private String observaciones;
    private boolean cumpleRequisitos;

    private final TramiteFlowService service = new TramiteFlowService();
    private final Controller controller = new Controller();

    public void cargarTramite(Integer formId) {
        selectedForm = controller.getVegetableFormsById(formId);
        if (selectedForm != null && selectedForm.getId() != null) {
            expediente = service.getExpedienteByFormId(selectedForm.getId());
        }
    }

    public void asignarTecnico() {
        if (selectedForm == null || tecnico == null || tecnico.trim().isEmpty()) {
            addError("Debe seleccionar un trámite y especificar el técnico.");
            return;
        }
        String usuario = getUsuarioActual();
        boolean ok = service.asignarTecnico(selectedForm, tecnico.trim(), usuario);
        if (ok) {
            addInfo("Trámite asignado correctamente al técnico: " + tecnico);
        } else {
            addError("No se pudo asignar el trámite. Intente nuevamente.");
        }
    }

    public void validarRequisitos() {
        if (selectedForm == null) {
            addError("Debe seleccionar un trámite.");
            return;
        }
        if (!FlowPhase.ASSIGNED.equals(selectedForm.getFlowPhase())
                && !FlowPhase.FORM_REVIEW.equals(selectedForm.getFlowPhase())) {
            addError("El trámite no está en estado de revisión de forma.");
            return;
        }
        String usuario = getUsuarioActual();
        boolean ok = service.validarRequisitosForma(selectedForm, cumpleRequisitos, observaciones, usuario);
        if (ok) {
            if (cumpleRequisitos) {
                expediente = service.getExpedienteByFormId(selectedForm.getId());
                addInfo("Providencia emitida. Expediente N°: " + expediente.getExpedienteNumber());
            } else {
                addInfo("Expediente archivado.");
            }
            selectedForm = controller.getVegetableFormsById(selectedForm.getId());
        } else {
            addError("Error al procesar la validación.");
        }
    }

    public void iniciarRevisionFondo() {
        if (selectedForm == null) {
            addError("Debe seleccionar un trámite.");
            return;
        }
        if (!FlowPhase.ADMITTED.equals(selectedForm.getFlowPhase())) {
            addError("El trámite debe estar admitido para iniciar la revisión de fondo.");
            return;
        }
        String usuario = getUsuarioActual();
        boolean ok = service.registrarRevisionFondo(selectedForm, observaciones, usuario);
        if (ok) {
            addInfo("Revisión de forma y fondo registrada.");
            selectedForm = controller.getVegetableFormsById(selectedForm.getId());
            expediente = service.getExpedienteByFormId(selectedForm.getId());
        } else {
            addError("Error al registrar la revisión.");
        }
    }

    private String getUsuarioActual() {
        try {
            LoginBean lb = controller.getLogin();
            return lb != null ? lb.getLogin() : "SISTEMA";
        } catch (Exception e) {
            return "SISTEMA";
        }
    }

    private void addInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    public VegetableForms getSelectedForm() { return selectedForm; }
    public void setSelectedForm(VegetableForms selectedForm) { this.selectedForm = selectedForm; }

    public Expediente getExpediente() { return expediente; }
    public void setExpediente(Expediente expediente) { this.expediente = expediente; }

    public String getTecnico() { return tecnico; }
    public void setTecnico(String tecnico) { this.tecnico = tecnico; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public boolean isCumpleRequisitos() { return cumpleRequisitos; }
    public void setCumpleRequisitos(boolean cumpleRequisitos) { this.cumpleRequisitos = cumpleRequisitos; }
}
