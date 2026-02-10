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

    //private List<Country>
    public VegetableBean() {
        loadVegetables();
    }

    private void loadVegetables() {
        Controller c = new Controller();
        login = c.getLogin();
        radioOption = "Todos";
        vegetables = c.buscarTodos();
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
        vegetables = c.buscarTodosByType(radioOption);
        cleanDate();
    }

    public void searchVegetables(ActionEvent ae) {
        if (Operations.validateDate(startDate) && Operations.validateDate(endDate)) {
            Controller c = new Controller();
            vegetables = c.buscarTodosByTypeAndDate(radioOption, startDate, endDate);
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

            if (vegetableForms.getAssignedUser() != null && vegetableForms.getAssignedUser().equals(login.getLogin())) {
                Operations.message(Operations.AVISO, "EL TRAMITE " + vegetableForms.getApplicationNumber() + " YA ESTA ASIGNADO AL USUARIO ACTUAL");
                return;
            }
            vegetableForms.setAssignedUser(login.getLogin());
            vegetableForms.setAssignedDate(new Date());
            vegetableForms.setStatusFlow(StatusFlow.PENDING);
            Controller c = new Controller();
            if (c.updateVegetableForms(vegetableForms)) {
                History history = new History();
                history.setApplicationNumber(vegetableForms.getApplicationNumber());
                history.setFecha(new Timestamp(System.currentTimeMillis()));
                history.setHistoryUser(login.getLogin());
                history.setDescription("Tramite " + vegetableForms.getApplicationNumber() + " asignado a " + login.getLogin());

                if (c.saveHistory(history)) {
                    onRadioSelected();
                    Operations.message(Operations.INFORMACION, "SE HA ASIGNADO CORRECTAMENTE EL TRAMITE " + vegetableForms.getApplicationNumber() + " AL USUARIO " + login.getLogin());
                    System.out.println("assign " + vegetableForms.getApplicationNumber() + " to " + login.getLogin() + " completed");
                } else {
                    Operations.message(Operations.AVISO, "SE HA ASIGNADO CORRECTAMENTE EL TRAMITE " + vegetableForms.getApplicationNumber()
                            + " AL USUARIO " + login.getLogin() + " PERO NO SE GUARDO EL HISTORIAL");
                }
            } else {
                Operations.message(Operations.AVISO, "NO SE PUDO ASIGNAR EL TRAMITE " + vegetableForms.getApplicationNumber() + " al usuario " + login.getLogin());
            }
        }
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
}
