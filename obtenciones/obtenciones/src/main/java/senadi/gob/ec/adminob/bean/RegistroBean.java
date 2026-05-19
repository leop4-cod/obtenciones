package senadi.gob.ec.adminob.bean;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;
import senadi.gob.ec.adminob.dao.ComprobantePagoDAO;
import senadi.gob.ec.adminob.dao.VegetableFormsDAO;
import senadi.gob.ec.adminob.enums.FlowPhase;
import senadi.gob.ec.adminob.enums.Status;
import senadi.gob.ec.adminob.enums.StatusFlow;
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

    private UploadedFile archivoFormulario;
    private UploadedFiles archivosFotos;
    private UploadedFile archivoPago;
    private String nombrePersonalizado;

    private String personaAsignar;


    private static final long MAX_FORM_PDF_SIZE = 10L * 1024 * 1024;
    private static final long MAX_PAYMENT_PDF_SIZE = 5L * 1024 * 1024;
    private static final long MAX_PHOTO_SIZE = 5L * 1024 * 1024;
    
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
            } else if (!(form.getApplicationDate() instanceof java.sql.Timestamp)) {
                form.setApplicationDate(new Timestamp(form.getApplicationDate().getTime()));
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

    public String saveRegistroAndStay() {
        try {
            if (form.getStatus() == null) form.setStatus(Status.SAVED);
            if (form.getFlowPhase() == null) form.setFlowPhase(FlowPhase.INITIAL);
            if (form.getStatusFlow() == null) form.setStatusFlow(StatusFlow.PENDING);
            form.setCreateDate(new Timestamp(System.currentTimeMillis()));
            if (form.getApplicationDate() == null) {
                form.setApplicationDate(new Timestamp(System.currentTimeMillis()));
            } else if (!(form.getApplicationDate() instanceof java.sql.Timestamp)) {
                form.setApplicationDate(new Timestamp(form.getApplicationDate().getTime()));
            }

            VegetableFormsDAO dao = new VegetableFormsDAO(form);
            dao.persist();

            editMode = true;
            archivosSubidos = new ArrayList<>();
            registrarHistorial(form, "Registro creado");
            addInfo("Registro guardado. Ahora puede subir los documentos.");
            return null;
        } catch (Exception e) {
            addError("ERROR AL GUARDAR: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    public String updateRegistro() {

        System.out.println("ENTRO A UPDATE");

        try {

            new VegetableFormsDAO(null).actualizarCamposEditables(form);

            addInfo("CAMBIOS GUARDADOS CORRECTAMENTE");
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
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

    private boolean guardarArchivo(UploadedFile file) throws Exception {
        return guardarArchivo(file, null);
    }

    private boolean guardarArchivo(UploadedFile file, String customName) throws Exception {
        if (file == null || file.getSize() == 0) {
            return false;
        }

        if (!editMode || form == null || form.getId() == null) {
            throw new IllegalStateException("Guarde el registro antes de subir archivos.");
        }

        Integer tramiteId = form.getId();
        String nombreOriginal = file.getFileName();
        String nombreMin = nombreOriginal.toLowerCase();
        long tamano = file.getSize();

        boolean esPhoto = nombreMin.endsWith(".jpg") || nombreMin.endsWith(".jpeg")
                || nombreMin.endsWith(".png");
        boolean esPdf = nombreMin.endsWith(".pdf");

        if (!esPhoto && !esPdf) {
            throw new IllegalArgumentException("TIPO NO PERMITIDO: " + nombreOriginal + ". Use PDF, JPG o PNG.");
        }

        long limite = esPhoto ? MAX_PHOTO_SIZE : MAX_FORM_PDF_SIZE;
        if (nombreMin.contains("pago") || nombreMin.contains("voucher") || nombreMin.contains("comprobante")) {
            limite = MAX_PAYMENT_PDF_SIZE;
        }
        if (tamano > limite) {
            throw new IllegalArgumentException("EL ARCHIVO SUPERA EL LIMITE DE " + (limite / (1024 * 1024)) + " MB: " + nombreOriginal);
        }

        String baseDir = AppConfig.get("upload.base.path", "UPLOAD_BASE_PATH",
            "C:" + File.separator + "uploads");
        String rutaDestino = baseDir + File.separator + tramiteId + File.separator;

        File carpeta = new File(rutaDestino);
        if (!carpeta.exists() && !carpeta.mkdirs()) {
            throw new IllegalStateException("NO SE PUDO CREAR EL DIRECTORIO: " + rutaDestino);
        }

        String ext = nombreMin.substring(nombreMin.lastIndexOf('.'));
        String tipo = esPdf ? "comprobante" : "foto";
        String nombreUnico = tipo + "_" + tramiteId + "_" + System.currentTimeMillis() + ext;
        File destino = new File(rutaDestino + nombreUnico);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, destino.toPath());
        }

        if (!destino.exists()) {
            throw new IllegalStateException("ERROR AL GUARDAR EN DISCO: " + nombreOriginal);
        }

        ComprobantePago cp = new ComprobantePago();
        cp.setVegetableFormId(tramiteId);
        cp.setNombreArchivo(nombreOriginal);
        cp.setNombrePersonalizado(customName != null && !customName.trim().isEmpty() ? customName.trim() : null);
        cp.setRutaArchivo(destino.getAbsolutePath());
        cp.setFechaCarga(new Timestamp(System.currentTimeMillis()));
        cp.setCargadoPor(login != null ? login.getLogin() : "SISTEMA");
        cp.setTamanoBytes(tamano);
        new ComprobantePagoDAO(cp).persist();

        return true;
    }

    public void uploadArchivo(FileUploadEvent event) {
        try {
            UploadedFile file = event.getFile();
            if (file == null) return;

            if (guardarArchivo(file, nombrePersonalizado)) {
                archivosSubidos = new ComprobantePagoDAO(null).getByVegetableFormId(form.getId());
                addInfo("ARCHIVO SUBIDO: " + (nombrePersonalizado != null && !nombrePersonalizado.trim().isEmpty()
                        ? nombrePersonalizado.trim() : file.getFileName()));
                nombrePersonalizado = null;
            }
        } catch (Exception e) {
            addError("ERROR AL SUBIR ARCHIVO: " + e.getMessage());
        }
    }

    public void subirTodosLosArchivos() {
        if (!editMode || form == null || form.getId() == null) {
            addError("Guarde el registro antes de subir archivos.");
            return;
        }

        int guardados = 0;
        try {
            if (archivoFormulario != null && archivoFormulario.getSize() > 0) {
                if (guardarArchivo(archivoFormulario, nombrePersonalizado)) guardados++;
            }
            if (archivosFotos != null) {
                for (UploadedFile foto : archivosFotos.getFiles()) {
                    if (foto != null && foto.getSize() > 0) {
                        if (guardarArchivo(foto, null)) guardados++;
                    }
                }
            }
            if (archivoPago != null && archivoPago.getSize() > 0) {
                if (guardarArchivo(archivoPago, null)) guardados++;
            }

            if (guardados > 0) {
                archivosSubidos = new ComprobantePagoDAO(null).getByVegetableFormId(form.getId());
                addInfo("Se guardaron " + guardados + " archivo(s).");
                archivoFormulario = null;
                archivosFotos = null;
                archivoPago = null;
                nombrePersonalizado = null;
            } else {
                addError("No se seleccionó ningún archivo para cargar.");
            }
        } catch (Exception e) {
            addError("ERROR AL SUBIR ARCHIVOS: " + e.getMessage());
        }
    }

    private void registrarHistorial(VegetableForms v, String descripcion) {
        registrarHistorial(v, descripcion, null);
    }

    private void registrarCambioAsignacion(VegetableForms v, String descripcion) {
        if (v == null || v.getApplicationNumber() == null) return;
        try {
            History h = new History();
            h.setApplicationNumber(v.getApplicationNumber());
            h.setDescription(descripcion);
            h.setFecha(new Timestamp(System.currentTimeMillis()));
            h.setHistoryUser(login != null ? login.getLogin() : "sistema");
            new Controller().saveHistory(h);
        } catch (Exception e) {
            System.err.println("[RegistroBean] No se pudo guardar historial de asignación: " + e.getMessage());
        }
    }

    private void registrarHistorial(VegetableForms v, String descripcion, String assignedTo) {
        if (v == null || v.getApplicationNumber() == null) return;
        try {
            History h = new History();
            h.setApplicationNumber(v.getApplicationNumber());
            h.setDescription(descripcion + " por " + (login != null ? login.getLogin() : "sistema"));
            h.setFecha(new Timestamp(System.currentTimeMillis()));
            h.setHistoryUser(login != null ? login.getLogin() : "sistema");
            // El asignado se guarda en la descripción, la columna assigned_to no existe en BD
            new Controller().saveHistory(h);
        } catch (Exception e) {
            System.err.println("[RegistroBean] No se pudo guardar historial: " + e.getMessage());
        }
    }

    public void asignarTramite() {
        if (personaAsignar == null || personaAsignar.trim().isEmpty()) {
            addError("Por favor escriba el nombre de la persona a asignar.");
            return;
        }
        if (!editMode || form == null || form.getApplicationNumber() == null) {
            addError("No hay un trámite válido para asignar.");
            return;
        }
        try {
            String persona = personaAsignar.trim();
            form.setAssignedUser(persona);
            new VegetableFormsDAO(form).actualizarCamposEditables(form);
            registrarHistorial(form, "Trámite asignado a " + persona, persona);
            addInfo("Trámite asignado a " + persona + " correctamente.");
            personaAsignar = null;
        } catch (Exception e) {
            addError("ERROR AL ASIGNAR: " + e.getMessage());
            e.printStackTrace();
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

    public void eliminarArchivo(ComprobantePago archivo) {

        try {

            if (archivo == null) {
                addError("Archivo no válido.");
                return;
            }

            // ELIMINAR ARCHIVO FÍSICO
            if (archivo.getRutaArchivo() != null) {

                File f = new File(archivo.getRutaArchivo());

                if (f.exists()) {
                    f.delete();
                }
            }

            // ELIMINAR DE BASE DE DATOS
            new ComprobantePagoDAO(null).delete(archivo.getId());

            // RECARGAR TABLA
            archivosSubidos = new ComprobantePagoDAO(null)
                    .getByVegetableFormId(form.getId());

            addInfo("ARCHIVO ELIMINADO CORRECTAMENTE");

        } catch (Exception e) {

            addError("ERROR AL ELIMINAR ARCHIVO: " + e.getMessage());
            e.printStackTrace();
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

    public Integer getEditId() { return editId; }
    public void setEditId(Integer editId) { this.editId = editId; }
    public boolean isEditMode() { return editMode; }
    public VegetableForms getForm() { return form; }
    public void setForm(VegetableForms form) { this.form = form; }
    public List<ComprobantePago> getArchivosSubidos() { return archivosSubidos; }
    public LoginBean getLogin() { return login; }
    
    public UploadedFile getArchivoFormulario() { return archivoFormulario; }
    public void setArchivoFormulario(UploadedFile archivoFormulario) { this.archivoFormulario = archivoFormulario; }
    
    public UploadedFiles getArchivosFotos() { return archivosFotos; }
    public void setArchivosFotos(UploadedFiles archivosFotos) { this.archivosFotos = archivosFotos; }

    public UploadedFile getArchivoPago() { return archivoPago; }
    public void setArchivoPago(UploadedFile archivoPago) { this.archivoPago = archivoPago; }

    public String getNombrePersonalizado() { return nombrePersonalizado; }
    public void setNombrePersonalizado(String nombrePersonalizado) { this.nombrePersonalizado = nombrePersonalizado; }

    public String getPersonaAsignar() { return personaAsignar; }
    public void setPersonaAsignar(String personaAsignar) { this.personaAsignar = personaAsignar; }

    /** Elimina el registro actual y redirige a la lista principal. */
    public String eliminarRegistro() {
        if (!editMode || form == null || form.getId() == null) {
            addError("No hay un registro cargado para eliminar.");
            return null;
        }
        try {
            new senadi.gob.ec.adminob.dao.VegetableFormsDAO(null).deleteById(form.getId());
            return "index?faces-redirect=true";
        } catch (Exception e) {
            addError("ERROR AL ELIMINAR: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}