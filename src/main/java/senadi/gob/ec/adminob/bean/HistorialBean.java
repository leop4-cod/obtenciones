package senadi.gob.ec.adminob.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import senadi.gob.ec.adminob.dao.HistoryDAO;
import senadi.gob.ec.adminob.model.History;
import senadi.gob.ec.adminob.util.Operations;

/**
 * Bean de vista para la pantalla de historial/auditoría.
 * Vista: historial/lista.xhtml
 *
 * Parámetros de URL aceptados:
 *   ?appNumber=SOL-2025-001   → historial por número de solicitud del portal
 *   ?tipoEntidad=Tramite&idEntidad=42 → historial por tipo e ID de entidad
 */
@ManagedBean(name = "historialBean")
@ViewScoped
public class HistorialBean implements Serializable {

    private List<History> entradas = new ArrayList<>();

    // ── Parámetros de filtro (recibidos por URL o desde otros beans) ──
    private String appNumber;
    private String tipoEntidad;
    private String idEntidad;

    // ── Filtros de pantalla ──
    private String filtroUsuario = "";
    private String filtroAccion  = "";
    private java.util.Date fechaDesde;
    private java.util.Date fechaHasta;

    @PostConstruct
    public void init() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx != null) {
            java.util.Map<String, String> params =
                ctx.getExternalContext().getRequestParameterMap();
            appNumber  = params.get("appNumber");
            tipoEntidad = params.get("tipoEntidad");
            idEntidad   = params.get("idEntidad");
        }
        cargar();
    }

    public void cargar() {
        try {
            HistoryDAO dao = new HistoryDAO(null);
            if (appNumber != null && !appNumber.trim().isEmpty()) {
                entradas = dao.getHistoriesByAppNumber(appNumber.trim());
            } else if (tipoEntidad != null && idEntidad != null
                    && !tipoEntidad.trim().isEmpty() && !idEntidad.trim().isEmpty()) {
                entradas = dao.getByEntidad(tipoEntidad.trim(), idEntidad.trim());
            } else {
                // Sin filtro: carga los 200 eventos más recientes del sistema
                entradas = dao.getUltimosEventos(200);
            }
        } catch (Exception e) {
            entradas = new ArrayList<>();
            Operations.message(Operations.ERROR, "Error al cargar historial: " + e.getMessage());
        }
        aplicarFiltros();
    }

    // ── Filtrado local ──────────────────────────────────────────────────────

    private List<History> entradasFiltradas = new ArrayList<>();

    public void aplicarFiltros() {
        if (entradas == null) { entradasFiltradas = new ArrayList<>(); return; }
        java.util.Date hastaFin = null;
        if (fechaHasta != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(fechaHasta);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
            cal.set(java.util.Calendar.MINUTE, 59);
            cal.set(java.util.Calendar.SECOND, 59);
            hastaFin = cal.getTime();
        }
        List<History> resultado = new ArrayList<>();
        for (History h : entradas) {
            if (!filtroUsuario.isEmpty()) {
                String usr = h.getHistoryUser() == null ? "" : h.getHistoryUser().toLowerCase();
                if (!usr.contains(filtroUsuario.toLowerCase())) continue;
            }
            if (!filtroAccion.isEmpty()) {
                String acc = h.getTipoAccion() == null ? "" : h.getTipoAccion();
                if (!filtroAccion.equals(acc)) continue;
            }
            if (fechaDesde != null && h.getFecha() != null && h.getFecha().before(fechaDesde)) continue;
            if (hastaFin   != null && h.getFecha() != null && h.getFecha().after(hastaFin))    continue;
            resultado.add(h);
        }
        entradasFiltradas = resultado;
    }

    public void limpiarFiltros() {
        filtroUsuario = "";
        filtroAccion  = "";
        fechaDesde    = null;
        fechaHasta    = null;
        aplicarFiltros();
    }

    // ── Estadísticas rápidas ────────────────────────────────────────────────

    public long contarPorAccion(String tipoAccion) {
        if (entradas == null) return 0;
        return entradas.stream()
            .filter(h -> tipoAccion.equals(h.getTipoAccion()))
            .count();
    }

    // ── Getters / setters ───────────────────────────────────────────────────

    public List<History> getEntradas() { return entradas; }

    public List<History> getEntradasFiltradas() {
        return entradasFiltradas != null ? entradasFiltradas : new ArrayList<>();
    }

    public String getAppNumber() { return appNumber; }
    public void setAppNumber(String appNumber) {
        this.appNumber = appNumber;
    }

    public String getTipoEntidad() { return tipoEntidad; }
    public void setTipoEntidad(String tipoEntidad) { this.tipoEntidad = tipoEntidad; }

    public String getIdEntidad() { return idEntidad; }
    public void setIdEntidad(String idEntidad) { this.idEntidad = idEntidad; }

    public String getFiltroUsuario() { return filtroUsuario; }
    public void setFiltroUsuario(String v) {
        this.filtroUsuario = v != null ? v : "";
        aplicarFiltros();
    }

    public String getFiltroAccion() { return filtroAccion; }
    public void setFiltroAccion(String v) {
        this.filtroAccion = v != null ? v : "";
        aplicarFiltros();
    }

    public java.util.Date getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(java.util.Date fechaDesde) { this.fechaDesde = fechaDesde; }

    public java.util.Date getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(java.util.Date fechaHasta) { this.fechaHasta = fechaHasta; }

    /** Título de la página según el contexto de filtrado. */
    public String getTituloHistorial() {
        if (appNumber != null && !appNumber.trim().isEmpty())
            return "Historial de la Solicitud: " + appNumber;
        if (tipoEntidad != null && idEntidad != null)
            return "Historial de " + tipoEntidad + " #" + idEntidad;
        return "Historial General del Sistema";
    }
}
