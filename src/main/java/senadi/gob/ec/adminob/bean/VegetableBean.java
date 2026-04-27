/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package senadi.gob.ec.adminob.bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIData;
import javax.faces.event.ActionEvent;
import org.primefaces.PrimeFaces;
import senadi.gob.ec.adminob.enums.StatusFlow;
import senadi.gob.ec.adminob.model.History;
import senadi.gob.ec.adminob.model.VegetableForms;
import senadi.gob.ec.adminob.util.Controller;
import senadi.gob.ec.adminob.util.Operations;
import senadi.gob.ec.adminob.util.Parameter;

/**
 *
 * @author michael
 */
@ManagedBean(name = "vegetableBean")
@ViewScoped
public class VegetableBean implements Serializable {

    private LoginBean login;

    private VegetableForms vegetableForms;
    private List<VegetableForms> vegetables;
    private List<VegetableForms> vegetablesFilter;
    private UIData vegetableTable;

    private String previewPath;
    private String radioOption;

    private Date startDate;
    private Date endDate;

    private boolean byDate;

    private String historial;
    private String newAssignedUser;
    private String statusObservation;
    private StatusFlow pendingStatusFlow;
    private StatusFlow previousStatusFlow;

    //private List<Country>
    public VegetableBean() {
        loadVegetables();
    }

    private void loadVegetables() {
        Controller c = new Controller();
        login = c.getLogin();
        radioOption = "Todos";
        try {
            vegetables = c.buscarTodos();
        } catch (Exception ex) {
            vegetables = new java.util.ArrayList<>();
            System.err.println("No se pudo cargar el listado de obtenciones: " + ex);
        }
    }

    public void viewFormulario(ActionEvent ae) {
        vegetableForms = (VegetableForms) vegetableTable.getRowData();
        if (vegetableForms != null && vegetableForms.getId() != null) {
            previewPath = Parameter.RUTA_URL + vegetableForms.getId() + "/pdf_breederfrm_" + vegetableForms.getId() + ".pdf";
            System.out.println("preview path: " + previewPath);
            //Operations.mensaje(Operations.INFORMACION, "ENVIADO A IMPRIMIR");
            PrimeFaces.current().ajax().addCallbackParam("url", previewPath);
            PrimeFaces.current().ajax().addCallbackParam("doit", true);
        } else {
            Operations.message(Operations.ERROR, "HAY UN PROBLEMA CON EL REGISTRO SELECCIONADO");
        }
    }

    public void viewVoucher(ActionEvent ae) {
        vegetableForms = (VegetableForms) vegetableTable.getRowData();
        if (vegetableForms != null && vegetableForms.getId() != null) {
            previewPath = Parameter.RUTA_URL + vegetableForms.getId() + "/pdf_voucher_breederfrm_" + vegetableForms.getId() + ".pdf";
            System.out.println("preview path: " + previewPath);
            //Operations.mensaje(Operations.INFORMACION, "ENVIADO A IMPRIMIR");
            PrimeFaces.current().ajax().addCallbackParam("url", previewPath);
            PrimeFaces.current().ajax().addCallbackParam("doit", true);
        } else {
            Operations.message(Operations.ERROR, "HAY UN PROBLEMA CON EL REGISTRO SELECCIONADO");
        }
    }

    public void cleanDate() {
        byDate = false;
    }

    public void onRadioSelected() {
        Controller c = new Controller();
        try {
            vegetables = c.buscarTodosByType(radioOption);
        } catch (Exception ex) {
            vegetables = new java.util.ArrayList<>();
            Operations.message(Operations.ERROR, "NO SE PUDO CONSULTAR LA BASE LOCAL");
        }
        cleanDate();
    }

    public void searchVegetables(ActionEvent ae) {
        if (Operations.validateDate(startDate) && Operations.validateDate(endDate)) {
            Controller c = new Controller();
            try {
                vegetables = c.buscarTodosByTypeAndDate(radioOption, startDate, endDate);
            } catch (Exception ex) {
                vegetables = new java.util.ArrayList<>();
                Operations.message(Operations.ERROR, "NO SE PUDO CONSULTAR LA BASE LOCAL");
            }
        } else {
            Operations.message(Operations.ERROR, "INGRESE UN RANGO DE FECHAS CORRECTO");
        }

    }

    public void onCheckSelected() {
        System.out.println("by date: " + byDate);
    }

    public void assignApplication(ActionEvent ae) {
        vegetableForms = (VegetableForms) vegetableTable.getRowData();
        if (vegetableForms != null && vegetableForms.getId() != null) {
            Controller c = new Controller();
            VegetableForms current = c.getVegetableFormsById(vegetableForms.getId());

            if (current == null || current.getId() == null) {
                Operations.message(Operations.ERROR, "NO SE ENCONTRO EL TRAMITE SELECCIONADO");
                return;
            }
            if (current.getAssignedUser() != null && !current.getAssignedUser().trim().isEmpty()) {
                if (current.getAssignedUser().equalsIgnoreCase(login.getLogin())) {
                    Operations.message(Operations.AVISO, "EL TRAMITE " + current.getApplicationNumber() + " YA ESTA ASIGNADO AL USUARIO ACTUAL");
                } else {
                    Operations.message(Operations.ERROR, "EL TRAMITE " + current.getApplicationNumber() + " YA ESTA ASIGNADO AL USUARIO " + current.getAssignedUser());
                }
                return;
            }

            current.setAssignedUser(login.getLogin());
            current.setAssignedDate(new Date());
            current.setStatusFlow(StatusFlow.PENDING);

            if (c.updateVegetableForms(current)) {
                saveHistoryEntry(current.getApplicationNumber(), "Tramite " + current.getApplicationNumber() + " asignado a " + login.getLogin());
                onRadioSelected();
                Operations.message(Operations.INFORMACION, "SE HA ASIGNADO CORRECTAMENTE EL TRAMITE " + current.getApplicationNumber() + " AL USUARIO " + login.getLogin());
            } else {
                Operations.message(Operations.AVISO, "NO SE PUDO ASIGNAR EL TRAMITE " + current.getApplicationNumber() + " AL USUARIO " + login.getLogin());
            }
        }
    }

    public void prepareStatusFlowChange() {
        vegetableForms = (VegetableForms) vegetableTable.getRowData();
        if (vegetableForms == null || vegetableForms.getId() == null) {
            Operations.message(Operations.ERROR, "NO SE ENCONTRO EL TRAMITE SELECCIONADO");
            return;
        }

        Controller c = new Controller();
        VegetableForms current = c.getVegetableFormsById(vegetableForms.getId());
        if (current == null || current.getId() == null) {
            Operations.message(Operations.ERROR, "NO SE ENCONTRO EL TRAMITE SELECCIONADO");
            return;
        }
        if (!isAssignedToCurrentUser(current)) {
            Operations.message(Operations.ERROR, "SOLO EL USUARIO ASIGNADO PUEDE CAMBIAR EL ESTADO DE GESTION");
            onRadioSelected();
            return;
        }

        previousStatusFlow = current.getStatusFlow();
        pendingStatusFlow = vegetableForms.getStatusFlow();
        vegetableForms.setStatusFlow(previousStatusFlow);

        if (pendingStatusFlow == null) {
            Operations.message(Operations.AVISO, "SELECCIONE UN ESTADO DE GESTION VALIDO");
            onRadioSelected();
            return;
        }
        if (previousStatusFlow == pendingStatusFlow) {
            onRadioSelected();
            return;
        }

        statusObservation = "";
        PrimeFaces.current().ajax().addCallbackParam("openStatusDialog", true);
    }

    public void confirmStatusFlowChange() {
        if (vegetableForms == null || vegetableForms.getId() == null) {
            Operations.message(Operations.ERROR, "NO SE ENCONTRO EL TRAMITE SELECCIONADO");
            return;
        }
        if (statusObservation == null || statusObservation.trim().isEmpty()) {
            Operations.message(Operations.ERROR, "DEBE INGRESAR UNA OBSERVACION PARA CAMBIAR EL ESTADO DE GESTION");
            return;
        }

        Controller c = new Controller();
        VegetableForms current = c.getVegetableFormsById(vegetableForms.getId());
        if (current == null || current.getId() == null) {
            Operations.message(Operations.ERROR, "NO SE ENCONTRO EL TRAMITE SELECCIONADO");
            return;
        }
        if (!isAssignedToCurrentUser(current)) {
            Operations.message(Operations.ERROR, "SOLO EL USUARIO ASIGNADO PUEDE CAMBIAR EL ESTADO DE GESTION");
            onRadioSelected();
            return;
        }
        if (pendingStatusFlow == null) {
            Operations.message(Operations.AVISO, "NO EXISTE UN NUEVO ESTADO DE GESTION PARA GUARDAR");
            return;
        }

        current.setStatusFlow(pendingStatusFlow);
        String movement = previousStatusFlow == null
                ? "Estado de gestion establecido en " + getStatusFlowLabel(pendingStatusFlow) + " por " + login.getLogin()
                : "Estado de gestion cambiado de " + getStatusFlowLabel(previousStatusFlow) + " a " + getStatusFlowLabel(pendingStatusFlow) + " por " + login.getLogin();

        if (c.updateVegetableForms(current)) {
            saveHistoryEntry(current.getApplicationNumber(), movement, statusObservation);
            pendingStatusFlow = null;
            previousStatusFlow = null;
            statusObservation = "";
            onRadioSelected();
            PrimeFaces.current().executeScript("PF('dlgStatusFlow').hide();");
            Operations.message(Operations.INFORMACION, "SE ACTUALIZO EL ESTADO DE GESTION DEL TRAMITE " + current.getApplicationNumber());
        } else {
            Operations.message(Operations.ERROR, "NO SE PUDO ACTUALIZAR EL ESTADO DE GESTION");
        }
    }

    public void prepareReassign(ActionEvent ae) {
        vegetableForms = (VegetableForms) vegetableTable.getRowData();
        if (vegetableForms != null) {
            newAssignedUser = vegetableForms.getAssignedUser();
        }
    }

    public void reassignApplication() {
        if (vegetableForms == null || vegetableForms.getId() == null) {
            Operations.message(Operations.ERROR, "NO SE ENCONTRO EL TRAMITE SELECCIONADO");
            return;
        }
        if (newAssignedUser == null || newAssignedUser.trim().isEmpty()) {
            Operations.message(Operations.AVISO, "INGRESE EL USUARIO AL QUE DESEA REASIGNAR EL TRAMITE");
            return;
        }

        Controller c = new Controller();
        VegetableForms current = c.getVegetableFormsById(vegetableForms.getId());
        if (current == null || current.getId() == null) {
            Operations.message(Operations.ERROR, "NO SE ENCONTRO EL TRAMITE SELECCIONADO");
            return;
        }
        if (!isAssignedToCurrentUser(current)) {
            Operations.message(Operations.ERROR, "SOLO EL USUARIO ASIGNADO PUEDE REASIGNAR ESTE TRAMITE");
            return;
        }

        String targetUser = newAssignedUser.trim();
        if (targetUser.equalsIgnoreCase(current.getAssignedUser())) {
            Operations.message(Operations.AVISO, "INGRESE UN USUARIO DIFERENTE PARA REASIGNAR");
            return;
        }

        String previousUser = current.getAssignedUser();
        current.setAssignedUser(targetUser);
        current.setAssignedDate(new Date());
        if (current.getStatusFlow() == null) {
            current.setStatusFlow(StatusFlow.PENDING);
        }

        if (c.updateVegetableForms(current)) {
            saveHistoryEntry(current.getApplicationNumber(), "Tramite " + current.getApplicationNumber() + " reasignado de "
                    + previousUser + " a " + targetUser + " por " + login.getLogin());
            onRadioSelected();
            PrimeFaces.current().executeScript("PF('dlgReassign').hide();");
            Operations.message(Operations.INFORMACION, "SE REASIGNO CORRECTAMENTE EL TRAMITE " + current.getApplicationNumber() + " AL USUARIO " + targetUser);
        } else {
            Operations.message(Operations.ERROR, "NO SE PUDO REASIGNAR EL TRAMITE");
        }
    }

    private void saveHistoryEntry(String applicationNumber, String description) {
        saveHistoryEntry(applicationNumber, description, null);
    }

    private void saveHistoryEntry(String applicationNumber, String description, String observation) {
        Controller c = new Controller();
        History history = new History();
        history.setApplicationNumber(applicationNumber);
        history.setFecha(new Timestamp(System.currentTimeMillis()));
        history.setHistoryUser(login.getLogin());
        if (observation != null && !observation.trim().isEmpty()) {
            history.setDescription(description + ". Observación: " + observation.trim());
        } else {
            history.setDescription(description);
        }
        c.saveHistory(history);
    }

    public boolean isAssignedToCurrentUser(VegetableForms vegetable) {
        return vegetable != null
                && vegetable.getAssignedUser() != null
                && login != null
                && login.getLogin() != null
                && vegetable.getAssignedUser().equalsIgnoreCase(login.getLogin());
    }

    public List<StatusFlow> getStatusFlows() {
        return java.util.Arrays.asList(StatusFlow.values());
    }

    public String getStatusFlowLabel(StatusFlow statusFlow) {
        if (statusFlow == null) {
            return "SIN GESTION";
        }
        switch (statusFlow) {
            case ATTENDED:
                return "ATENDIDO";
            case PENDING:
                return "PENDIENTE";
            case DENIED:
                return "NEGADO";
            case EXPIRED:
                return "CADUCADO";
            default:
                return statusFlow.name();
        }
    }

    public String getPendingStatusFlowLabel() {
        return getStatusFlowLabel(pendingStatusFlow);
    }

    public String getPreviousStatusFlowLabel() {
        return getStatusFlowLabel(previousStatusFlow);
    }

    public void prepararHistorial(ActionEvent ae) {
        vegetableForms = (VegetableForms) vegetableTable.getRowData();
        if (vegetableForms != null && vegetableForms.getId() != null) {
            Controller c = new Controller();
            List<History> hists = c.getHistoriesByAppNumber(vegetableForms.getApplicationNumber());
            historial = "";
            for (int i = 0; i < hists.size(); i++) {
                historial += hists.get(i).toString() + "\n";
            }
            if (historial.isEmpty()) {
                historial = "Estado Actual: " + vegetableForms.getStatus();
            }
        }
    }

    /**
     * @return the login
     */
    public LoginBean getLogin() {
        return login;
    }

    /**
     * @param login the login to set
     */
    public void setLogin(LoginBean login) {
        this.login = login;
    }

    /**
     * @return the vegetables
     */
    public List<VegetableForms> getVegetables() {
        return vegetables;
    }

    /**
     * @param vegetables the vegetables to set
     */
    public void setVegetables(List<VegetableForms> vegetables) {
        this.vegetables = vegetables;
    }

    /**
     * @return the vegetablesFilter
     */
    public List<VegetableForms> getVegetablesFilter() {
        return vegetablesFilter;
    }

    /**
     * @param vegetablesFilter the vegetablesFilter to set
     */
    public void setVegetablesFilter(List<VegetableForms> vegetablesFilter) {
        this.vegetablesFilter = vegetablesFilter;
    }

    /**
     * @return the vegetableTable
     */
    public UIData getVegetableTable() {
        return vegetableTable;
    }

    /**
     * @param vegetableTable the vegetableTable to set
     */
    public void setVegetableTable(UIData vegetableTable) {
        this.vegetableTable = vegetableTable;
    }

    /**
     * @return the vegetableForms
     */
    public VegetableForms getVegetableForms() {
        return vegetableForms;
    }

    /**
     * @param vegetableForms the vegetableForms to set
     */
    public void setVegetableForms(VegetableForms vegetableForms) {
        this.vegetableForms = vegetableForms;
    }

    /**
     * @return the previewPath
     */
    public String getPreviewPath() {
        return previewPath;
    }

    /**
     * @param previewPath the previewPath to set
     */
    public void setPreviewPath(String previewPath) {
        this.previewPath = previewPath;
    }

    /**
     * @return the radioOption
     */
    public String getRadioOption() {
        return radioOption;
    }

    /**
     * @param radioOption the radioOption to set
     */
    public void setRadioOption(String radioOption) {
        this.radioOption = radioOption;
    }

    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the endDate
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * @return the byDate
     */
    public boolean isByDate() {
        return byDate;
    }

    /**
     * @param byDate the byDate to set
     */
    public void setByDate(boolean byDate) {
        this.byDate = byDate;
    }

    /**
     * @return the historial
     */
    public String getHistorial() {
        return historial;
    }

    /**
     * @param historial the historial to set
     */
    public void setHistorial(String historial) {
        this.historial = historial;
    }

    public String getNewAssignedUser() {
        return newAssignedUser;
    }

    public void setNewAssignedUser(String newAssignedUser) {
        this.newAssignedUser = newAssignedUser;
    }

    public String getStatusObservation() {
        return statusObservation;
    }

    public void setStatusObservation(String statusObservation) {
        this.statusObservation = statusObservation;
    }
}
