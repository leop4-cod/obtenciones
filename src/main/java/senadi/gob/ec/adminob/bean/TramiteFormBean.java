package senadi.gob.ec.adminob.bean;

import java.io.File;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.primefaces.model.file.UploadedFile;
import senadi.gob.ec.adminob.dao.DocumentoTramiteDAO;
import senadi.gob.ec.adminob.dao.TramiteDAO;
import senadi.gob.ec.adminob.dao.VegetableFormsDAO;
import senadi.gob.ec.adminob.model.DocumentoTramite;
import senadi.gob.ec.adminob.model.PersonVegetable;
import senadi.gob.ec.adminob.model.Tramite;
import senadi.gob.ec.adminob.model.VegetableForms;
import senadi.gob.ec.adminob.solicitudes.Owners;
import senadi.gob.ec.adminob.solicitudes.OwnersDAO;
import senadi.gob.ec.adminob.util.AppConfig;
import java.nio.file.Files;

/**
 * Bean de vista para el formulario de trámites (tramites/formulario.xhtml).
 * Maneja creación y edición, incluyendo upload de documentos previo al guardado.
 */
@ManagedBean(name = "tramiteFormBean")
@ViewScoped
public class TramiteFormBean implements Serializable {

    // ── Estado del formulario ─────────────────────────────────────────────────

    private Integer editId;
    private boolean editMode = false;
    private Tramite tramite;
    private LoginBean login;

    // ── Documentos ────────────────────────────────────────────────────────────

    /** Documentos ya guardados en BD (solo en modo edición). */
    private List<DocumentoTramite> documentosGuardados = new ArrayList<>();

    /**
     * Cola de archivos pendientes de guardar al momento del submit.
     * Se usa en modo creación para poder subir docs antes de tener el ID.
     * Los bytes se copian a memoria para sobrevivir entre requests.
     */
    private List<ArchivoEnCola> archivosPendientes = new ArrayList<>();

    /** Archivo seleccionado en el input de la UI (se asigna en cada submit). */
    private UploadedFile archivoActual;

    /** Nombre personalizado para el archivoActual. */
    private String nombreActual;

    // ── Portal: tramites en proceso y solicitantes ────────────────────────────

    /** Solicitudes del portal que ya tienen gestión iniciada (flowPhase != INITIAL). */
    private List<VegetableForms> tramitesPortalEnProceso = new ArrayList<>();

    /** Solicitantes auto-completados al vincular con una solicitud del portal. */
    private List<SolicitanteDTO> solicitantes = new ArrayList<>();

    // ── Catálogos ────────────────────────────────────────────────────────────

    public static final List<String> ESTADOS = Arrays.asList(
        "DELIVERED",
        "EN TRÁMITE",
        "EN TRÁMITE_PUBLICADA",
        "PENDIENTE",
        "ATENDIDO",
        "NEGADO",
        "CADUCADO",
        "ACEPTADO",
        "ABANDONADA",
        "DESISTIMIENTO DE OFICIO",
        "DESISTIMIENTO VOLUNTARIO",
        "DOMINIO PÚBLICO",
        "CADUCIDAD DEL TRÁMITE CQA"
    );

    // ── Ciclo de vida ────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        tramite = new Tramite();
        tramite.setEstadoActual("DELIVERED");
        tramite.setFechaCreacion(new Timestamp(System.currentTimeMillis()));
        tramite.setFechaPresentacion(new Date(System.currentTimeMillis()));
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx != null) {
            login = (LoginBean) ctx.getExternalContext().getSessionMap().get("loginBean");
        }
        cargarTramitesPortalEnProceso();
    }

    /** Invocado por f:viewAction para cargar el trámite cuando editId no es null. */
    public void cargarFormulario() {
        if (editId != null) {
            editMode = true;
            Tramite cargado = new TramiteDAO(null).buscarPorId(editId);
            if (cargado != null) {
                tramite = cargado;
                cargarDocumentosGuardados();
                if (tramite.getVegetableFormId() != null) {
                    cargarSolicitantes(tramite.getVegetableFormId());
                }
            } else {
                addError("No se encontró el trámite con ID: " + editId);
            }
        }
    }

    /** Carga la lista de solicitudes del portal que ya están en proceso (para el selector). */
    private void cargarTramitesPortalEnProceso() {
        try {
            tramitesPortalEnProceso = new VegetableFormsDAO(null).buscarEnProceso();
        } catch (Exception e) {
            tramitesPortalEnProceso = new ArrayList<>();
            System.err.println("[TramiteFormBean] No se pudo cargar tramites del portal: " + e.getMessage());
        }
    }

    /**
     * Llamado por p:ajax cuando el usuario elige una solicitud del portal.
     * Auto-completa titular y denominación, y carga la tabla de solicitantes.
     */
    public void onVegetableFormSeleccionado() {
        solicitantes.clear();
        Integer vfId = tramite.getVegetableFormId();
        if (vfId == null) return;

        VegetableForms vf = new VegetableFormsDAO(null).buscarConPersonasPorId(vfId);
        if (vf == null) return;

        // Auto-completar titular desde el owner del portal
        if (vf.getOwnerId() != null) {
            try {
                Owners owner = new OwnersDAO().getOwnerById(vf.getOwnerId());
                if (owner != null && owner.getId() != null) {
                    String nombre = ((owner.getFirsName() != null ? owner.getFirsName() : "") + " "
                                   + (owner.getLastName() != null ? owner.getLastName() : "")).trim();
                    if (!nombre.isEmpty()) tramite.setTitular(nombre);
                }
            } catch (Exception e) {
                System.err.println("[TramiteFormBean] No se pudo cargar owner: " + e.getMessage());
            }
        }

        // Auto-completar denominación si está vacía
        if ((tramite.getDenominacion() == null || tramite.getDenominacion().trim().isEmpty())
                && vf.getCommonName() != null && !vf.getCommonName().trim().isEmpty()) {
            tramite.setDenominacion(vf.getCommonName().trim());
        }

        poblarSolicitantes(vf);
    }

    private void cargarSolicitantes(Integer vfId) {
        VegetableForms vf = new VegetableFormsDAO(null).buscarConPersonasPorId(vfId);
        if (vf != null) poblarSolicitantes(vf);
    }

    private void poblarSolicitantes(VegetableForms vf) {
        solicitantes.clear();
        for (PersonVegetable pv : vf.getPersonVegetables()) {
            if (pv.getPerson() == null) continue;
            SolicitanteDTO dto = new SolicitanteDTO();
            dto.setNombre(pv.getPerson().getName() != null ? pv.getPerson().getName() : "—");
            dto.setTipo(pv.getPersonType() != null ? pv.getPersonType().name() : "—");
            dto.setDocumento(pv.getPerson().getIdentificationNumber() != null
                    ? pv.getPerson().getIdentificationNumber() : "—");
            solicitantes.add(dto);
        }
    }

    private void cargarDocumentosGuardados() {
        try {
            documentosGuardados = new DocumentoTramiteDAO(null).buscarPorTramite(tramite.getId());
        } catch (Exception e) {
            documentosGuardados = new ArrayList<>();
        }
    }

    // ── Agregar archivo a la cola ────────────────────────────────────────────

    public void agregarArchivo() {
        if (archivoActual == null || archivoActual.getSize() == 0) {
            addError("Seleccione un archivo antes de agregar.");
            return;
        }
        if (nombreActual == null || nombreActual.trim().length() < 5) {
            addError("El nombre personalizado debe tener mínimo 5 caracteres.");
            return;
        }
        try {
            byte[] bytes = archivoActual.getContent();
            ArchivoEnCola a = new ArchivoEnCola();
            a.setNombrePersonalizado(nombreActual.trim());
            a.setNombreOriginal(archivoActual.getFileName());
            a.setContenido(bytes);
            a.setTamanoBytes(archivoActual.getSize());
            archivosPendientes.add(a);
            addInfo("Archivo agregado: \"" + a.getNombrePersonalizado() + "\"");
            archivoActual = null;
            nombreActual = null;
        } catch (Exception e) {
            addError("Error al agregar archivo: " + e.getMessage());
        }
    }

    public void removerArchivoPendiente(ArchivoEnCola a) {
        archivosPendientes.remove(a);
    }

    // ── Guardar (crear) ──────────────────────────────────────────────────────

    public String guardarTramite() {
        if (!validarCampos()) return null;

        try {
            tramite.setFechaCreacion(new Timestamp(System.currentTimeMillis()));
            tramite.setFechaModificacion(new Timestamp(System.currentTimeMillis()));
            tramite.setUsuarioCreacion(usuarioActual());
            tramite.setPuedeEditar("DELIVERED".equals(tramite.getEstadoActual()));

            if (!validarNumeroDeTramite(null)) return null;

            new TramiteDAO(tramite).persist();

            procesarArchivosPendientes(tramite.getId());

            return "lista?faces-redirect=true";
        } catch (Exception e) {
            addError("Error al guardar: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ── Actualizar (editar) ──────────────────────────────────────────────────

    public String actualizarTramite() {
        if (!validarCampos()) return null;

        try {
            tramite.setFechaModificacion(new Timestamp(System.currentTimeMillis()));
            tramite.setUsuarioModificacion(usuarioActual());
            tramite.setPuedeEditar("DELIVERED".equals(tramite.getEstadoActual()));

            if (!validarNumeroDeTramite(tramite.getId())) return null;

            new TramiteDAO(null).actualizarCampos(tramite);

            if (!archivosPendientes.isEmpty()) {
                procesarArchivosPendientes(tramite.getId());
            }

            cargarDocumentosGuardados();
            addInfo("Trámite actualizado correctamente.");
            return null;
        } catch (Exception e) {
            addError("Error al actualizar: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String cancelar() {
        return "lista?faces-redirect=true";
    }

    // ── Eliminar documento existente ─────────────────────────────────────────

    public void eliminarDocumentoGuardado(DocumentoTramite doc) {
        if (doc == null) return;
        try {
            if (doc.getRutaArchivo() != null) {
                File f = new File(doc.getRutaArchivo());
                if (f.exists()) f.delete();
            }
            new DocumentoTramiteDAO(null).deleteById(doc.getId());
            cargarDocumentosGuardados();
            addInfo("Documento eliminado.");
        } catch (Exception e) {
            addError("Error al eliminar documento: " + e.getMessage());
        }
    }

    // ── Lógica interna ───────────────────────────────────────────────────────

    private boolean validarCampos() {
        if (tramite.getNumeroTramite() == null || tramite.getNumeroTramite().trim().isEmpty()) {
            addError("El número de trámite es obligatorio.");
            return false;
        }
        if (tramite.getFechaPresentacion() != null) {
            long hoy = System.currentTimeMillis();
            if (tramite.getFechaPresentacion().getTime() > hoy) {
                addError("La fecha de presentación no puede ser futura.");
                return false;
            }
        }
        return true;
    }

    private boolean validarNumeroDeTramite(Integer excluirId) {
        String num = tramite.getNumeroDeTramite();
        if (num != null && !num.trim().isEmpty()) {
            boolean existe = new TramiteDAO(null).existeNumeroDeTramite(num.trim(), excluirId);
            if (existe) {
                addError("El número de trámite \"" + num + "\" ya está en uso.");
                return false;
            }
        }
        return true;
    }

    /**
     * Escribe los archivos pendientes a disco y crea los registros en BD.
     * Se llama después de persistir el trámite (ya tenemos el ID).
     */
    private void procesarArchivosPendientes(Integer tramiteId) throws Exception {
        if (archivosPendientes.isEmpty()) return;

        String baseDir = AppConfig.get("upload.base.path", "UPLOAD_BASE_PATH",
            "C:" + File.separator + "uploads");
        String dirTramite = baseDir + File.separator + "tramites" + File.separator + tramiteId;
        File carpeta = new File(dirTramite);
        if (!carpeta.exists() && !carpeta.mkdirs()) {
            throw new IllegalStateException("No se pudo crear el directorio: " + dirTramite);
        }

        DocumentoTramiteDAO dao = new DocumentoTramiteDAO(null);
        for (ArchivoEnCola a : archivosPendientes) {
            String nombreMin = a.getNombreOriginal().toLowerCase();
            String ext = nombreMin.contains(".")
                ? nombreMin.substring(nombreMin.lastIndexOf('.')) : "";
            String tipo = ext.isEmpty() ? "otro"
                : (ext.equals(".pdf") ? "pdf"
                : (ext.matches("\\.(jpg|jpeg|png)") ? "imagen" : "otro"));

            String nombreUnico = tipo + "_" + tramiteId + "_" + System.currentTimeMillis()
                + "_" + a.getNombreOriginal().replaceAll("[^a-zA-Z0-9._-]", "_");
            File destino = new File(dirTramite + File.separator + nombreUnico);

            Files.write(destino.toPath(), a.getContenido());

            DocumentoTramite doc = new DocumentoTramite();
            doc.setTramiteId(tramiteId);
            doc.setNombrePersonalizado(a.getNombrePersonalizado());
            doc.setNombreArchivo(a.getNombreOriginal());
            doc.setRutaArchivo(destino.getAbsolutePath());
            doc.setTipoArchivo(tipo);
            doc.setTamanoBytes(a.getTamanoBytes());
            doc.setFechaCarga(new Timestamp(System.currentTimeMillis()));
            doc.setUsuarioCarga(usuarioActual());
            dao.setInstancia(doc);
            dao.persist();

            // Crear DAO fresco para cada inserción
            dao = new DocumentoTramiteDAO(null);
        }
        archivosPendientes.clear();
    }

    private String usuarioActual() {
        return login != null ? login.getLogin() : "SISTEMA";
    }

    private void addInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Integer getEditId() { return editId; }
    public void setEditId(Integer editId) { this.editId = editId; }

    public boolean isEditMode() { return editMode; }

    public Tramite getTramite() { return tramite; }
    public void setTramite(Tramite tramite) { this.tramite = tramite; }

    public List<DocumentoTramite> getDocumentosGuardados() { return documentosGuardados; }

    public List<ArchivoEnCola> getArchivosPendientes() { return archivosPendientes; }

    public UploadedFile getArchivoActual() { return archivoActual; }
    public void setArchivoActual(UploadedFile archivoActual) { this.archivoActual = archivoActual; }

    public String getNombreActual() { return nombreActual; }
    public void setNombreActual(String nombreActual) { this.nombreActual = nombreActual; }

    public List<String> getEstados() { return ESTADOS; }

    public List<VegetableForms> getTramitesPortalEnProceso() { return tramitesPortalEnProceso; }

    public List<SolicitanteDTO> getSolicitantes() { return solicitantes; }

    // ── Clase interna: archivo en cola ───────────────────────────────────────

    /**
     * Wrapper serializable que retiene los bytes del archivo entre requests,
     * ya que UploadedFile no es serializable y el Part subyacente expira.
     */
    public static class ArchivoEnCola implements Serializable {
        private String nombrePersonalizado;
        private String nombreOriginal;
        private byte[] contenido;
        private long tamanoBytes;

        public String getNombrePersonalizado() { return nombrePersonalizado; }
        public void setNombrePersonalizado(String n) { this.nombrePersonalizado = n; }

        public String getNombreOriginal() { return nombreOriginal; }
        public void setNombreOriginal(String n) { this.nombreOriginal = n; }

        public byte[] getContenido() { return contenido; }
        public void setContenido(byte[] c) { this.contenido = c; }

        public long getTamanoBytes() { return tamanoBytes; }
        public void setTamanoBytes(long t) { this.tamanoBytes = t; }

        public String getTamanoFormateado() {
            if (tamanoBytes < 1024) return tamanoBytes + " B";
            if (tamanoBytes < 1024 * 1024) return (tamanoBytes / 1024) + " KB";
            return String.format("%.1f MB", tamanoBytes / (1024.0 * 1024.0));
        }
    }

    // ── Clase interna: solicitante del portal ────────────────────────────────

    public static class SolicitanteDTO implements Serializable {
        private String nombre;
        private String tipo;
        private String documento;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }

        public String getDocumento() { return documento; }
        public void setDocumento(String documento) { this.documento = documento; }
    }
}
