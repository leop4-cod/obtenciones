package senadi.gob.ec.adminob.bean;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import senadi.gob.ec.adminob.dao.ComprobantePagoDAO;
import senadi.gob.ec.adminob.dao.VegetableFormsDAO;
import senadi.gob.ec.adminob.enums.DenominationType;
import senadi.gob.ec.adminob.enums.FlowPhase;
import senadi.gob.ec.adminob.enums.Status;
import senadi.gob.ec.adminob.enums.StatusFlow;
import senadi.gob.ec.adminob.enums.VarietyTransferType;
import senadi.gob.ec.adminob.model.ComprobantePago;
import senadi.gob.ec.adminob.model.History;
import senadi.gob.ec.adminob.model.VegetableForms;
import senadi.gob.ec.adminob.util.AppConfig;
import senadi.gob.ec.adminob.util.Controller;

@ManagedBean(name = "registroBean")
@ViewScoped
public class RegistroBean implements Serializable {

    private Integer editId;
    private boolean editMode;
    private VegetableForms form;
    private LoginBean login;
    private List<ComprobantePago> archivosSubidos;

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    @PostConstruct
    public void init() {
        form = new VegetableForms();
        form.setStatus(Status.SAVED);
        form.setFlowPhase(FlowPhase.INITIAL);
        form.setStatusFlow(StatusFlow.PENDING);
        form.setCreateDate(new Timestamp(System.currentTimeMillis()));
        archivosSubidos = new ArrayList<>();
        editMode = false;
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx != null) {
            login = (LoginBean) ctx.getExternalContext().getSessionMap().get("loginBean");
        }
    }

    public void cargarRegistro() {
        if (editId != null) {
            editMode = true;
            Controller c = new Controller();
            VegetableForms loaded = c.getVegetableFormsById(editId);
            if (loaded != null) {
                form = loaded;
                try {
                    archivosSubidos = new ComprobantePagoDAO(null).getByVegetableFormId(form.getId());
                } catch (Exception e) {
                    archivosSubidos = new ArrayList<>();
                }
            } else {
                addError("No se encontró el registro con ID: " + editId);
            }
        }
    }

    public String saveRegistro() {
        try {
            if (form.getStatus() == null) form.setStatus(Status.SAVED);
            if (form.getFlowPhase() == null) form.setFlowPhase(FlowPhase.INITIAL);
            if (form.getStatusFlow() == null) form.setStatusFlow(StatusFlow.PENDING);
            form.setCreateDate(new Timestamp(System.currentTimeMillis()));
            if (form.getApplicationDate() == null) {
                form.setApplicationDate(new Timestamp(System.currentTimeMillis()));
            }

            VegetableFormsDAO dao = new VegetableFormsDAO(form);
            dao.persist();

            registrarHistorial(form, "Registro creado");
            return "index?faces-redirect=true";
        } catch (Exception e) {
            addError("ERROR AL GUARDAR: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String updateRegistro() {
        if (form == null || form.getId() == null) {
            addError("No hay registro seleccionado para actualizar.");
            return null;
        }
        try {
            // Usa actualizarCamposEditables: carga entidad MANAGED en EM nuevo,
            // copia solo campos escalares → evita LazyInitializationException
            // sobre las colecciones @OneToMany detachadas.
            new VegetableFormsDAO(null).actualizarCamposEditables(form);
            registrarHistorial(form, "Registro actualizado");
            addInfo("CAMBIOS GUARDADOS CORRECTAMENTE");
            return "index?faces-redirect=true";
        } catch (Exception e) {
            addError("ERROR AL ACTUALIZAR: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String cancelar() {
        return "index?faces-redirect=true";
    }

    public void uploadArchivo(FileUploadEvent event) {
        try {
            UploadedFile file = event.getFile();
            if (file == null) return;

            if (!editMode || form == null || form.getId() == null) {
                addError("Guarde el registro antes de subir archivos.");
                return;
            }

            Integer tramiteId = form.getId();
            String nombreOriginal = file.getFileName();
            String nombreMin = nombreOriginal.toLowerCase();
            long tamano = file.getSize();

            if (tamano > MAX_FILE_SIZE) {
                addError("EL ARCHIVO SUPERA EL LÍMITE DE 10 MB: " + nombreOriginal);
                return;
            }

            String baseDir = AppConfig.get("upload.base.path", "UPLOAD_BASE_PATH",
                "C:" + File.separator + "uploads");
            String rutaDestino = baseDir + File.separator + tramiteId + File.separator;

            File carpeta = new File(rutaDestino);
            if (!carpeta.exists() && !carpeta.mkdirs()) {
                addError("NO SE PUDO CREAR EL DIRECTORIO: " + rutaDestino);
                return;
            }

            String ext = nombreMin.contains(".") ? nombreMin.substring(nombreMin.lastIndexOf('.')) : ".bin";
            String tipo = nombreMin.endsWith(".pdf") ? "doc"
                : (nombreMin.endsWith(".jpg") || nombreMin.endsWith(".jpeg") || nombreMin.endsWith(".png")) ? "foto"
                : "archivo";
            String nombreUnico = tipo + "_" + tramiteId + "_" + System.currentTimeMillis() + ext;
            File destino = new File(rutaDestino + nombreUnico);

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, destino.toPath());
            }

            if (!destino.exists()) {
                addError("ERROR AL GUARDAR EN DISCO: " + nombreOriginal);
                return;
            }

            ComprobantePago cp = new ComprobantePago();
            cp.setVegetableFormId(tramiteId);
            cp.setNombreArchivo(nombreOriginal);
            cp.setRutaArchivo(destino.getAbsolutePath());
            cp.setFechaCarga(new Timestamp(System.currentTimeMillis()));
            cp.setCargadoPor(login != null ? login.getLogin() : "SISTEMA");
            cp.setTamanoBytes(tamano);
            new ComprobantePagoDAO(cp).persist();

            archivosSubidos = new ComprobantePagoDAO(null).getByVegetableFormId(tramiteId);
            addInfo("ARCHIVO SUBIDO: " + nombreOriginal);

        } catch (Exception e) {
            addError("ERROR AL SUBIR ARCHIVO: " + e.getMessage());
        }
    }

    private void registrarHistorial(VegetableForms v, String descripcion) {
        if (v == null || v.getApplicationNumber() == null) return;
        try {
            History h = new History();
            h.setApplicationNumber(v.getApplicationNumber());
            h.setDescription(descripcion + " por " + (login != null ? login.getLogin() : "sistema"));
            h.setFecha(new Timestamp(System.currentTimeMillis()));
            h.setHistoryUser(login != null ? login.getLogin() : "sistema");
            new Controller().saveHistory(h);
        } catch (Exception e) {
            System.err.println("[RegistroBean] No se pudo guardar historial: " + e.getMessage());
        }
    }

    public String getStatusLabel(Status s) {
        if (s == null) return "—";
        switch (s) {
            case SAVED: return "Guardado";
            case DELIVERED: return "En Proceso";
            case PREVIEW: return "Vista Previa";
            case FINISHED: return "Finalizado";
            default: return s.name();
        }
    }

    public String getFlowPhaseLabel(FlowPhase fp) {
        if (fp == null) return "—";
        switch (fp) {
            case INITIAL: return "Inicial";
            case ASSIGNED: return "Asignado";
            case FORM_REVIEW: return "Revisión de Forma";
            case ADMITTED: return "Admitido";
            case SUBSTANCE_REVIEW: return "Revisión de Fondo";
            case LIVE_SAMPLE_DEPOSIT: return "Depósito de Muestra";
            case DEPOSIT_CERTIFICATE: return "Certificado de Depósito";
            case OPPOSITION_PERIOD: return "Período de Oposición";
            case OPPOSITION_RESOLUTION: return "Resolución de Oposición";
            case DHE_EXAM: return "Examen DHE";
            case TECHNICAL_OPINION: return "Dictamen Técnico";
            case RESOLUTION: return "Resolución";
            case CERTIFICATE_ISSUED: return "Certificado Emitido";
            case ARCHIVED: return "Archivado";
            default: return fp.name();
        }
    }

    public List<Status> getStatusList() { return Arrays.asList(Status.values()); }
    public List<DenominationType> getDenominationTypes() { return Arrays.asList(DenominationType.values()); }
    public List<VarietyTransferType> getVarietyTransferTypes() { return Arrays.asList(VarietyTransferType.values()); }
    public List<FlowPhase> getFlowPhases() { return Arrays.asList(FlowPhase.values()); }
    public List<StatusFlow> getStatusFlows() { return Arrays.asList(StatusFlow.values()); }

    private void addInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    public Integer getEditId() { return editId; }
    public void setEditId(Integer editId) { this.editId = editId; }
    public boolean isEditMode() { return editMode; }
    public VegetableForms getForm() { return form; }
    public void setForm(VegetableForms form) { this.form = form; }
    public List<ComprobantePago> getArchivosSubidos() { return archivosSubidos; }
    public LoginBean getLogin() { return login; }
}
