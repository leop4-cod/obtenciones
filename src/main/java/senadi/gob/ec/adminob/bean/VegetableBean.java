package senadi.gob.ec.adminob.bean;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIData;
import javax.faces.event.ActionEvent;
import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import senadi.gob.ec.adminob.dao.ComprobantePagoDAO;
import senadi.gob.ec.adminob.enums.FlowPhase;
import senadi.gob.ec.adminob.enums.StatusFlow;
import senadi.gob.ec.adminob.model.ComprobantePago;
import senadi.gob.ec.adminob.model.History;
import senadi.gob.ec.adminob.model.VegetableForms;
import senadi.gob.ec.adminob.service.TramiteFlowService;
import senadi.gob.ec.adminob.util.AppConfig;
import senadi.gob.ec.adminob.util.Controller;
import senadi.gob.ec.adminob.util.Operations;
import senadi.gob.ec.adminob.util.Parameter;

/**
 * Managed Bean para la gestión de formularios de obtenciones vegetales.
 * Incluye corrección de carga de archivos en rutas absolutas de Windows.
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

    private List<ComprobantePago> archivosSubidos;

    private static final long   MAX_FORM_PDF_SIZE    = 10L * 1024 * 1024;
    private static final long   MAX_PAYMENT_PDF_SIZE =  5L * 1024 * 1024;
    private static final long   MAX_PHOTO_SIZE       =  5L * 1024 * 1024;
    private static final double A4_RATIO             = 297.0 / 210.0;
    private static final double A4_TOLERANCE         = 0.15;

    private final TramiteFlowService tramiteService = new TramiteFlowService();

    public VegetableBean() {
        loadVegetables();
    }

    private void loadVegetables() {
        Controller c = new Controller();
        login = c.getLogin();
        radioOption = "Todos";
        try {
            vegetables = c.buscarTodos();
            c.precargarLockers(vegetables);
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
            c.precargarLockers(vegetables);
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
                c.precargarLockers(vegetables);
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

    /**
     * Asigna el trámite al usuario actual mediante TramiteFlowService,
     * que establece correctamente FlowPhase.ASSIGNED y StatusFlow.PENDING.
     * Punto único de asignación — no duplicar lógica en otros beans.
     */
    public void assignApplication(ActionEvent ae) {
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
        if (current.getAssignedUser() != null && !current.getAssignedUser().trim().isEmpty()) {
            if (current.getAssignedUser().equalsIgnoreCase(login.getLogin())) {
                Operations.message(Operations.AVISO,
                    "EL TRAMITE " + current.getApplicationNumber() + " YA ESTA ASIGNADO AL USUARIO ACTUAL");
            } else {
                Operations.message(Operations.ERROR,
                    "EL TRAMITE " + current.getApplicationNumber()
                    + " YA ESTA ASIGNADO AL USUARIO " + current.getAssignedUser());
            }
            return;
        }

        // Delegar en el servicio para garantizar FlowPhase.ASSIGNED + historial
        boolean ok = tramiteService.asignarTecnico(current, login.getLogin(), login.getLogin());
        if (ok) {
            onRadioSelected();
            Operations.message(Operations.INFORMACION,
                "SE HA ASIGNADO CORRECTAMENTE EL TRAMITE "
                + current.getApplicationNumber() + " AL USUARIO " + login.getLogin());
        } else {
            Operations.message(Operations.AVISO,
                "NO SE PUDO ASIGNAR EL TRAMITE " + current.getApplicationNumber());
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

    // 1. Esto llena el combo box con las opciones del Enum
    public List<StatusFlow> getStatusFlows() {
        return java.util.Arrays.asList(StatusFlow.values());
    }

    // 2. Esto cambia lo que el usuario VE en el combo box
    public String getStatusFlowLabel(StatusFlow statusFlow) {
        if (statusFlow == null) return "SIN GESTION";
        
        switch (statusFlow) {
            case ATTENDED: return "FINISHED"; 
            case PENDING:  return "SAVED";    
            case DENIED:   return "REJECTED"; 
            case EXPIRED:  return "EXPIRED";  
            default: return statusFlow.name();
        }
    }

    public String getPendingStatusFlowLabel() {
        return getStatusFlowLabel(pendingStatusFlow);
    }

    public String getPreviousStatusFlowLabel() {
        return getStatusFlowLabel(previousStatusFlow);
    }

    public void prepararHistorial(ActionEvent ae) {
        // No necesitas getRowData() porque f:setPropertyActionListener ya hizo el trabajo
        if (this.vegetableForms != null && this.vegetableForms.getApplicationNumber() != null) {
            try {
                Controller c = new Controller();
                List<History> hists = c.getHistoriesByAppNumber(this.vegetableForms.getApplicationNumber());
                
                if (hists != null && !hists.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (History h : hists) {
                        sb.append(h.toString()).append("\n");
                    }
                    this.historial = sb.toString();
                } else {
                    this.historial = "Estado Actual: " + this.vegetableForms.getStatus() 
                                    + "\n(No hay eventos previos registrados)";
                }
            } catch (Exception e) {
                this.historial = "Error al conectar con el servidor de historial.";
                e.printStackTrace();
            }
        } else {
            this.historial = "Error: No hay un trámite seleccionado.";
        }
    }   
    // ── UPLOAD DE COMPROBANTES DE PAGO ────────────────────────────────────────

    /**
     * Carga un comprobante de pago (PDF) asociado al trámite seleccionado.
     *
     * Reglas:
     * - Solo acepta PDF para comprobantes (JPG/PNG se manejan por otro canal).
     * - Genera nombre de archivo único basado en timestamp — nunca sobrescribe.
     * - Registra el archivo en la tabla comprobante_pago (relación 1:N).
     * - La ruta base se configura via propiedad de sistema "upload.base.path".
     */
    public void upload(FileUploadEvent event) {
        try {
            UploadedFile file = event.getFile();
            if (file == null) return;

            if (this.vegetableForms == null || this.vegetableForms.getId() == null) {
                Operations.message(Operations.ERROR, "SELECCIONE UN TRÁMITE PRIMERO");
                return;
            }

            Integer tramiteId = this.vegetableForms.getId();
            String nombreOriginal = file.getFileName();
            String nombreMin      = nombreOriginal.toLowerCase();
            long   tamano         = file.getSize();

            boolean esPhoto = nombreMin.endsWith(".jpg") || nombreMin.endsWith(".jpeg")
                           || nombreMin.endsWith(".png");
            boolean esPdf   = nombreMin.endsWith(".pdf");

            if (!esPhoto && !esPdf) {
                Operations.message(Operations.ERROR,
                    "TIPO NO PERMITIDO: " + nombreOriginal
                    + ". Use PDF para comprobantes o JPG/PNG para fotografías.");
                return;
            }

            // Validar tamaño
            long limite = esPhoto ? MAX_PHOTO_SIZE : MAX_PAYMENT_PDF_SIZE;
            if (tamano > limite) {
                Operations.message(Operations.ERROR,
                    "EL ARCHIVO SUPERA EL LÍMITE DE " + (limite / (1024 * 1024)) + "MB: " + nombreOriginal);
                return;
            }

            // Validar proporción A4 solo para fotografías
            if (esPhoto) {
                byte[] contenido = file.getContent();
                try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(contenido)) {
                    java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(bais);
                    if (img != null) {
                        double relacion = (double) img.getHeight() / img.getWidth();
                        if (Math.abs(relacion - A4_RATIO) > A4_TOLERANCE) {
                            Operations.message(Operations.ERROR,
                                "FOTOGRAFÍA SIN PROPORCIÓN A4 (1:1.41): " + nombreOriginal
                                + " [alto/ancho=" + String.format("%.2f", relacion) + "]");
                            return;
                        }
                    }
                } catch (Exception ex) {
                    // Si no se puede leer la imagen, se advierte pero no se bloquea
                    Operations.message(Operations.AVISO,
                        "No se pudo verificar dimensiones de: " + nombreOriginal);
                }
            }

            // Ruta configurable via System Property "upload.base.path"
            String baseDir = AppConfig.get("upload.base.path", "UPLOAD_BASE_PATH",
                "C:" + File.separator + "uploads");
            String rutaDestino = baseDir + File.separator + tramiteId + File.separator;

            File carpeta = new File(rutaDestino);
            if (!carpeta.exists() && !carpeta.mkdirs()) {
                Operations.message(Operations.ERROR,
                    "NO SE PUDO CREAR EL DIRECTORIO DE DESTINO: " + rutaDestino);
                return;
            }

            // Nombre único — nunca sobrescribe archivos anteriores
            String extension  = nombreMin.substring(nombreMin.lastIndexOf('.'));
            String nombreUnico = (esPdf ? "comprobante" : "foto")
                + "_" + tramiteId + "_" + System.currentTimeMillis() + extension;
            File archivoDestino = new File(rutaDestino + nombreUnico);

            try (InputStream in = file.getInputStream()) {
                // Sin REPLACE_EXISTING: si ya existe el nombre único, es un error real
                Files.copy(in, archivoDestino.toPath());
            }

            if (!archivoDestino.exists()) {
                Operations.message(Operations.ERROR, "ERROR AL GUARDAR EN DISCO: " + nombreOriginal);
                return;
            }

            // Registrar en BD todos los archivos subidos (PDF y fotos)
            ComprobantePago cp = new ComprobantePago();
            cp.setVegetableFormId(tramiteId);
            cp.setNombreArchivo(nombreOriginal);
            cp.setRutaArchivo(archivoDestino.getAbsolutePath());
            cp.setFechaCarga(new java.sql.Timestamp(System.currentTimeMillis()));
            cp.setCargadoPor(login != null ? login.getLogin() : "SISTEMA");
            cp.setTamanoBytes(tamano);
            new ComprobantePagoDAO(cp).persist();

            Operations.message(Operations.INFORMACION,
                "ARCHIVO CARGADO CORRECTAMENTE: " + nombreOriginal);

        } catch (Exception e) {
            java.util.logging.Logger.getLogger(getClass().getName())
                .log(java.util.logging.Level.SEVERE, "Error en upload de archivo", e);
            Operations.message(Operations.ERROR, "ERROR AL GUARDAR ARCHIVO: " + e.getMessage());
        }
    }

    public void prepareViewUploaded(ActionEvent ae) {
        if (vegetableForms == null || vegetableForms.getId() == null) {
            Operations.message(Operations.ERROR, "NO SE ENCONTRO EL TRAMITE SELECCIONADO");
            return;
        }
        try {
            archivosSubidos = new ComprobantePagoDAO(null).getByVegetableFormId(vegetableForms.getId());
        } catch (Exception e) {
            archivosSubidos = new java.util.ArrayList<>();
            Operations.message(Operations.ERROR, "NO SE PUDO CARGAR LOS ARCHIVOS SUBIDOS");
        }
    }

    public List<ComprobantePago> getArchivosSubidos() { return archivosSubidos; }

    /** Cantidad de comprobantes de pago registrados para un trámite. */
    public int getUploadedFileCount(Integer tramiteId) {
        if (tramiteId == null) return 0;
        try {
            return new ComprobantePagoDAO(null).getByVegetableFormId(tramiteId).size();
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean hasUploadedFiles(Integer tramiteId) {
        return getUploadedFileCount(tramiteId) > 0;
    }

    public void onRowSelect(org.primefaces.event.SelectEvent<VegetableForms> event) {
        this.vegetableForms = event.getObject();
        System.out.println("SELECCIONADO: " + vegetableForms.getApplicationNumber());
    }

        // --- GETTERS Y SETTERS COMPLETOS ---
        public LoginBean getLogin() { return login; }
        public void setLogin(LoginBean login) { this.login = login; }
        public List<VegetableForms> getVegetables() { return vegetables; }
        public void setVegetables(List<VegetableForms> vegetables) { this.vegetables = vegetables; }
        public List<VegetableForms> getVegetablesFilter() { return vegetablesFilter; }
        public void setVegetablesFilter(List<VegetableForms> vegetablesFilter) { this.vegetablesFilter = vegetablesFilter; }
        public UIData getVegetableTable() { return vegetableTable; }
        public void setVegetableTable(UIData vegetableTable) { this.vegetableTable = vegetableTable; }
        public VegetableForms getVegetableForms() { return vegetableForms; }
        public void setVegetableForms(VegetableForms vegetableForms) { this.vegetableForms = vegetableForms; }
        public String getPreviewPath() { return previewPath; }
        public void setPreviewPath(String previewPath) { this.previewPath = previewPath; }
        public String getRadioOption() { return radioOption; }
        public void setRadioOption(String radioOption) { this.radioOption = radioOption; }
        public Date getStartDate() { return startDate; }
        public void setStartDate(Date startDate) { this.startDate = startDate; }
        public Date getEndDate() { return endDate; }
        public void setEndDate(Date endDate) { this.endDate = endDate; }
        public boolean isByDate() { return byDate; }
        public void setByDate(boolean byDate) { this.byDate = byDate; }
        public String getHistorial() { return historial; }
        public void setHistorial(String historial) { this.historial = historial; }
        public String getNewAssignedUser() { return newAssignedUser; }
        public void setNewAssignedUser(String newAssignedUser) { this.newAssignedUser = newAssignedUser; }
        public String getStatusObservation() { return statusObservation; }
        public void setStatusObservation(String statusObservation) { this.statusObservation = statusObservation; }
        public StatusFlow getPendingStatusFlow() { return pendingStatusFlow; }
        public void setPendingStatusFlow(StatusFlow pendingStatusFlow) { this.pendingStatusFlow = pendingStatusFlow; }
        public StatusFlow getPreviousStatusFlow() { return previousStatusFlow; }
        public void setPreviousStatusFlow(StatusFlow previousStatusFlow) { this.previousStatusFlow = previousStatusFlow; }
    }