package senadi.gob.ec.adminob.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import senadi.gob.ec.adminob.dao.DocumentoTramiteDAO;
import senadi.gob.ec.adminob.dao.TramiteDAO;
import senadi.gob.ec.adminob.model.DocumentoTramite;
import senadi.gob.ec.adminob.model.Tramite;
import senadi.gob.ec.adminob.util.AppConfig;
import java.io.File;

/**
 * Bean de sesión para la página de lista de trámites (tramites/lista.xhtml).
 * Gestiona: listado, filtro por estado, eliminación y documentos subidos.
 */
@ManagedBean(name = "tramiteListBean")
@SessionScoped
public class TramiteListBean implements Serializable {

    /** Cuando false muestra solo DELIVERED; cuando true muestra todos los estados. */
    private boolean mostrarTodos = false;

    private List<Tramite> tramites = new ArrayList<>();

    /** Trámite seleccionado para ver sus documentos en el diálogo. */
    private Tramite tramiteSeleccionado;

    private List<DocumentoTramite> documentosSeleccionados = new ArrayList<>();

    @PostConstruct
    public void init() {
        cargarLista();
    }

    public void cargarLista() {
        try {
            TramiteDAO dao = new TramiteDAO(null);
            tramites = mostrarTodos ? dao.buscarTodos() : dao.buscarPorEstado("DELIVERED");
        } catch (Exception e) {
            tramites = new ArrayList<>();
            addError("Error al cargar trámites: " + e.getMessage());
        }
    }

    public void onFiltroChanged() {
        cargarLista();
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    public void eliminarTramite(Tramite t) {
        if (t == null) return;
        if (!puedeEliminar(t)) {
            addError("No se puede eliminar: el trámite tiene más de 48 h y posee documentos adjuntos.");
            return;
        }
        try {
            eliminarDocumentosFisicos(t.getId());
            new TramiteDAO(null).deleteById(t.getId());
            cargarLista();
            addInfo("Trámite eliminado correctamente.");
        } catch (Exception e) {
            addError("Error al eliminar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Regla: se puede eliminar si tiene < 48 h de antigüedad O si no tiene documentos. */
    public boolean puedeEliminar(Tramite t) {
        if (t == null || t.getFechaCreacion() == null) return false;
        long horas = (System.currentTimeMillis() - t.getFechaCreacion().getTime()) / (1000L * 60 * 60);
        if (horas < 48) return true;
        long docs = new DocumentoTramiteDAO(null).contarPorTramite(t.getId());
        return docs == 0;
    }

    private void eliminarDocumentosFisicos(Integer tramiteId) {
        try {
            List<DocumentoTramite> docs = new DocumentoTramiteDAO(null).buscarPorTramite(tramiteId);
            for (DocumentoTramite d : docs) {
                if (d.getRutaArchivo() != null) {
                    File f = new File(d.getRutaArchivo());
                    if (f.exists()) f.delete();
                }
            }
        } catch (Exception e) {
            System.err.println("[TramiteListBean] Error eliminando archivos físicos: " + e.getMessage());
        }
    }

    // ── Documentos ────────────────────────────────────────────────────────────

    public void prepararDocumentos(Tramite t) {
        tramiteSeleccionado = t;
        try {
            documentosSeleccionados = new DocumentoTramiteDAO(null).buscarPorTramite(t.getId());
        } catch (Exception e) {
            documentosSeleccionados = new ArrayList<>();
            addError("Error al cargar documentos: " + e.getMessage());
        }
    }

    public void eliminarDocumento(DocumentoTramite doc) {
        if (doc == null) return;
        try {
            if (doc.getRutaArchivo() != null) {
                File f = new File(doc.getRutaArchivo());
                if (f.exists()) f.delete();
            }
            new DocumentoTramiteDAO(null).deleteById(doc.getId());
            if (tramiteSeleccionado != null) {
                documentosSeleccionados = new DocumentoTramiteDAO(null).buscarPorTramite(tramiteSeleccionado.getId());
            }
            cargarLista();
            addInfo("Documento eliminado.");
        } catch (Exception e) {
            addError("Error al eliminar documento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public long contarDocumentos(Integer tramiteId) {
        try {
            return new DocumentoTramiteDAO(null).contarPorTramite(tramiteId);
        } catch (Exception e) {
            return 0;
        }
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    public String etiquetaEstado(String estado) {
        if (estado == null) return "—";
        switch (estado.toUpperCase().trim()) {
            case "DELIVERED":                    return "EN PROCESO";
            case "ABANDONADA":                   return "ABANDONADA";
            case "EN TRÁMITE":                   return "EN TRÁMITE";
            case "EN TRÁMITE_PUBLICADA":         return "EN TRÁMITE PUBLICADA";
            case "PENDIENTE":                    return "PENDIENTE";
            case "ATENDIDO":                     return "ATENDIDO";
            case "NEGADO":                       return "NEGADO";
            case "CADUCADO":                     return "CADUCADO";
            case "ACEPTADO":                     return "ACEPTADO";
            case "DESISTIMIENTO DE OFICIO":      return "DESISTIMIENTO OFICIO";
            case "DESISTIMIENTO VOLUNTARIO":     return "DESISTIMIENTO VOLUNTARIO";
            case "DOMINIO PÚBLICO":              return "DOMINIO PÚBLICO";
            case "CADUCIDAD DEL TRÁMITE CQA":    return "CADUCIDAD CQA";
            default: return estado;
        }
    }

    public String claseEstado(String estado) {
        if (estado == null) return "tr-badge-otros";
        if ("DELIVERED".equalsIgnoreCase(estado)) return "tr-badge-delivered";
        if ("ACEPTADO".equalsIgnoreCase(estado)) return "tr-badge-aceptado";
        if ("NEGADO".equalsIgnoreCase(estado) || "CADUCADO".equalsIgnoreCase(estado)) return "tr-badge-negado";
        if ("ATENDIDO".equalsIgnoreCase(estado)) return "tr-badge-atendido";
        return "tr-badge-otros";
    }

    private void addInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    // ── Getters/Setters ───────────────────────────────────────────────────────

    public boolean isMostrarTodos() { return mostrarTodos; }
    public void setMostrarTodos(boolean mostrarTodos) { this.mostrarTodos = mostrarTodos; }

    public List<Tramite> getTramites() { return tramites; }

    public Tramite getTramiteSeleccionado() { return tramiteSeleccionado; }
    public void setTramiteSeleccionado(Tramite tramiteSeleccionado) { this.tramiteSeleccionado = tramiteSeleccionado; }

    public List<DocumentoTramite> getDocumentosSeleccionados() { return documentosSeleccionados; }
}
