package senadi.gob.ec.adminob.bean;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import senadi.gob.ec.adminob.dao.DocumentoTramiteDAO;
import senadi.gob.ec.adminob.dao.HistoryDAO;
import senadi.gob.ec.adminob.dao.TramiteDAO;
import senadi.gob.ec.adminob.dao.VegetableFormsDAO;
import senadi.gob.ec.adminob.model.DocumentoTramite;
import senadi.gob.ec.adminob.model.History;
import senadi.gob.ec.adminob.model.Tramite;
import senadi.gob.ec.adminob.model.VegetableForms;
import senadi.gob.ec.adminob.solicitudes.Owners;
import senadi.gob.ec.adminob.solicitudes.OwnersDAO;
import senadi.gob.ec.adminob.util.AppConfig;
import senadi.gob.ec.adminob.util.Controller;
import senadi.gob.ec.adminob.util.Operations;
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

    /** Historial del trámite seleccionado. */
    private List<History> historialEntradas = new ArrayList<>();
    private Tramite tramiteParaHistorial;

    /** Lista de solicitudes del portal disponibles para importar (DELIVERED sin tramite asociado). */
    private List<VegetableForms> tramitesPortalDisponibles = new ArrayList<>();

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

    // ── Aceptar trámite ───────────────────────────────────────────────────────

    public void aceptarTramite(Tramite t) {
        if (t == null) return;
        if (!"DELIVERED".equalsIgnoreCase(t.getEstadoActual())) {
            addError("Solo se pueden aceptar trámites en estado DELIVERED.");
            return;
        }
        try {
            String usuario = obtenerUsuario();
            t.setEstadoActual("ACEPTADO");
            t.setUsuarioModificacion(usuario);
            t.setFechaModificacion(new Timestamp(System.currentTimeMillis()));
            new TramiteDAO(null).actualizarCampos(t);
            guardarHistoria(t.getNumeroTramite(),
                "Trámite ACEPTADO por " + usuario, null);
            cargarLista();
            addInfo("Trámite " + t.getNumeroTramite() + " aceptado correctamente por " + usuario + ".");
        } catch (Exception e) {
            addError("Error al aceptar el trámite: " + e.getMessage());
        }
    }

    // ── Importar desde portal ─────────────────────────────────────────────────

    public void prepararImportacion() {
        try {
            tramitesPortalDisponibles = new VegetableFormsDAO(null).buscarDeliveredSinImportar();
        } catch (Exception e) {
            tramitesPortalDisponibles = new ArrayList<>();
            addError("Error al cargar solicitudes del portal: " + e.getMessage());
        }
    }

    public void importarDesdePortal(VegetableForms vf) {
        if (vf == null) return;
        try {
            String usuario = obtenerUsuario();
            Tramite t = new Tramite();
            t.setNumeroTramite(vf.getApplicationNumber() != null ? vf.getApplicationNumber().trim() : "SIN-NUMERO");
            t.setDenominacion(vf.getCommonName() != null ? vf.getCommonName().trim() : null);
            t.setVegetableFormId(vf.getId());
            t.setEstadoActual("DELIVERED");
            t.setPuedeEditar(true);
            t.setFechaCreacion(new Timestamp(System.currentTimeMillis()));
            t.setFechaModificacion(new Timestamp(System.currentTimeMillis()));
            t.setUsuarioCreacion(usuario);

            if (vf.getApplicationDate() != null) {
                t.setFechaPresentacion(new Date(vf.getApplicationDate().getTime()));
            }

            if (vf.getOwnerId() != null) {
                try {
                    Owners owner = new OwnersDAO().getOwnerById(vf.getOwnerId());
                    if (owner != null) {
                        String nombre = ((owner.getFirsName() != null ? owner.getFirsName() : "") + " "
                                       + (owner.getLastName() != null ? owner.getLastName() : "")).trim();
                        if (!nombre.isEmpty()) t.setTitular(nombre);
                    }
                } catch (Exception ex) {
                    System.err.println("[TramiteListBean] No se pudo cargar owner al importar: " + ex.getMessage());
                }
            }

            new TramiteDAO(t).persist();
            guardarHistoria(t.getNumeroTramite(), "Trámite importado desde portal por " + usuario, null);
            tramitesPortalDisponibles.remove(vf);
            cargarLista();
            addInfo("Solicitud " + t.getNumeroTramite() + " importada correctamente como nuevo trámite.");
        } catch (Exception e) {
            addError("Error al importar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Historial ─────────────────────────────────────────────────────────────

    public void prepararHistorial(Tramite t) {
        tramiteParaHistorial = t;
        try {
            historialEntradas = new HistoryDAO(null).getHistoriesByAppNumber(t.getNumeroTramite());
        } catch (Exception e) {
            historialEntradas = new ArrayList<>();
            addError("Error al cargar historial: " + e.getMessage());
        }
    }

    private void guardarHistoria(String numeroTramite, String descripcion, String observacion) {
        try {
            String usuario = obtenerUsuario();
            History h = new History();
            h.setApplicationNumber(numeroTramite);
            h.setFecha(new Timestamp(System.currentTimeMillis()));
            h.setHistoryUser(usuario);
            String desc = descripcion;
            if (observacion != null && !observacion.trim().isEmpty()) {
                desc += ". Obs: " + observacion.trim();
            }
            h.setDescription(desc);
            new HistoryDAO(h).persist();
        } catch (Exception e) {
            System.err.println("[TramiteListBean] No se pudo guardar historial: " + e.getMessage());
        }
    }

    private String obtenerUsuario() {
        try {
            LoginBean login = new Controller().getLogin();
            return (login != null && login.getLogin() != null) ? login.getLogin() : "sistema";
        } catch (Exception e) {
            return "sistema";
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

    /** Retorna solo los trámites en estado DELIVERED del listado actual. */
    public List<Tramite> getTramitesPorAceptar() {
        if (tramites == null) return new ArrayList<>();
        if (!mostrarTodos) return tramites; // Ya son solo DELIVERED
        List<Tramite> result = new ArrayList<>();
        for (Tramite t : tramites) {
            if ("DELIVERED".equals(t.getEstadoActual())) result.add(t);
        }
        return result;
    }

    public Tramite getTramiteSeleccionado() { return tramiteSeleccionado; }
    public void setTramiteSeleccionado(Tramite tramiteSeleccionado) { this.tramiteSeleccionado = tramiteSeleccionado; }

    public List<DocumentoTramite> getDocumentosSeleccionados() { return documentosSeleccionados; }

    public List<History> getHistorialEntradas() { return historialEntradas; }
    public Tramite getTramiteParaHistorial() { return tramiteParaHistorial; }

    public List<VegetableForms> getTramitesPortalDisponibles() { return tramitesPortalDisponibles; }
}
