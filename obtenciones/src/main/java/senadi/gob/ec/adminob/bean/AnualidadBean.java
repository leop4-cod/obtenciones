package senadi.gob.ec.adminob.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.primefaces.PrimeFaces;
import senadi.gob.ec.adminob.dao.AnualidadDAO;
import senadi.gob.ec.adminob.dao.VegetableFormsDAO;
import senadi.gob.ec.adminob.enums.EstadoAnualidad;
import senadi.gob.ec.adminob.model.Anualidad;
import senadi.gob.ec.adminob.model.VegetableForms;
import senadi.gob.ec.adminob.util.Controller;
import senadi.gob.ec.adminob.util.Operations;

@ManagedBean(name = "anualidadBean")
@SessionScoped
public class AnualidadBean implements Serializable {

    private List<Anualidad>      todasLasAnualidades    = new ArrayList<>();
    private List<Anualidad>      alertas                = new ArrayList<>();
    private List<VegetableForms> tramites               = new ArrayList<>();
    private Set<Integer>         tramitesConAnualidades = new HashSet<>();

    private Anualidad        anualidadEnEdicion;
    private EstadoAnualidad  estadoOriginal;
    private Integer          tramiteSeleccionadoId;
    private boolean          completado          = false;
    private int              cantidadAnualidades = 20;

    private String filtroTramite = "";
    private String filtroEstado  = "";

    @PostConstruct
    public void init() {
        cargar();
    }

    public void cargar() {
        try {
            AnualidadDAO dao = new AnualidadDAO(null);
            dao.marcarVencidas();
            todasLasAnualidades    = dao.buscarTodos();
            alertas                = dao.buscarConAlerta(15);
            tramitesConAnualidades = new HashSet<>(dao.idsConAnualidades());
        } catch (Exception e) {
            todasLasAnualidades = new ArrayList<>();
            alertas             = new ArrayList<>();
            System.err.println("[AnualidadBean] Error al cargar: " + e.getMessage());
        }
        try {
            // Solo los trámites ACEPTADOS pueden tener anualidades
            tramites = new VegetableFormsDAO(null).buscarTodosByType("Aceptados");
        } catch (Exception e) {
            tramites = new ArrayList<>();
            System.err.println("[AnualidadBean] Error al cargar trámites: " + e.getMessage());
        }
    }

    public List<Anualidad> getAnualidadesFiltradas() {
        if (todasLasAnualidades == null) return new ArrayList<>();
        List<Anualidad> result = new ArrayList<>();
        for (Anualidad a : todasLasAnualidades) {
            if (!filtroEstado.isEmpty() && (a.getEstado() == null || !filtroEstado.equals(a.getEstado().name()))) continue;
            if (!filtroTramite.isEmpty()) {
                VegetableForms vf = getVegetableForm(a.getVegetableFormId());
                if (vf == null) continue;
                String appNum = vf.getApplicationNumber() == null ? "" : vf.getApplicationNumber().toLowerCase();
                String numInt = vf.getNumeracionInterna()  == null ? "" : vf.getNumeracionInterna().toLowerCase();
                String busq   = filtroTramite.toLowerCase();
                if (!appNum.contains(busq) && !numInt.contains(busq)) continue;
            }
            result.add(a);
        }
        return result;
    }

    public VegetableForms getVegetableForm(Integer id) {
        if (id == null || tramites == null) return null;
        for (VegetableForms vf : tramites) {
            if (id.equals(vf.getId())) return vf;
        }
        // Fallback: buscar en BD si no está en la lista (p. ej. estado cambiado)
        try {
            return new VegetableFormsDAO(null).getVegetableFormsById(id);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean tieneAnualidades(Integer vegetableFormId) {
        return tramitesConAnualidades != null && tramitesConAnualidades.contains(vegetableFormId);
    }

    public void crearAnualidades() {
        if (tramiteSeleccionadoId == null) {
            Operations.message(Operations.ERROR, "Seleccione un trámite válido.");
            return;
        }
        if (tieneAnualidades(tramiteSeleccionadoId)) {
            Operations.message(Operations.AVISO, "Este trámite ya tiene anualidades generadas.");
            return;
        }
        try {
            String usuario = obtenerUsuario();
            new AnualidadDAO(null).crearAnualidadesVacias(tramiteSeleccionadoId, usuario, cantidadAnualidades);
            cargar();
            Operations.message(Operations.INFORMACION,
                "Se generaron " + cantidadAnualidades + " anualidades para el trámite.");
            tramiteSeleccionadoId = null;
            cantidadAnualidades   = 20;
        } catch (Exception e) {
            Operations.message(Operations.ERROR, "Error al crear anualidades: " + e.getMessage());
        }
    }

    public void prepararEdicion(Anualidad a) {
        anualidadEnEdicion = new Anualidad();
        anualidadEnEdicion.setId(a.getId());
        anualidadEnEdicion.setVegetableFormId(a.getVegetableFormId());
        anualidadEnEdicion.setAnio(a.getAnio());
        anualidadEnEdicion.setEtiqueta(a.getEtiqueta());
        anualidadEnEdicion.setFechaVencimiento(a.getFechaVencimiento());
        anualidadEnEdicion.setFechaPago(a.getFechaPago());
        anualidadEnEdicion.setMonto(a.getMonto());
        anualidadEnEdicion.setNumeroComprobante(a.getNumeroComprobante());
        anualidadEnEdicion.setValorPagadoRecargo(a.getValorPagadoRecargo());
        anualidadEnEdicion.setNumeroComprobanteRecargo(a.getNumeroComprobanteRecargo());
        anualidadEnEdicion.setObservaciones(a.getObservaciones());
        estadoOriginal = a.getEstado() != null ? a.getEstado() : EstadoAnualidad.PENDIENTE;
        anualidadEnEdicion.setEstado(estadoOriginal);
        completado = (estadoOriginal == EstadoAnualidad.PAGADO);
    }

    public void guardarEdicion() {
        if (anualidadEnEdicion == null) return;

        AnualidadDAO dao = new AnualidadDAO(null);
        boolean esPagoNuevo = completado && estadoOriginal != EstadoAnualidad.PAGADO;

        if (completado) {
            anualidadEnEdicion.setEstado(EstadoAnualidad.PAGADO);
            if (esPagoNuevo) {
                anualidadEnEdicion.setFechaPago(new java.sql.Date(System.currentTimeMillis()));
            }
        } else {
            anualidadEnEdicion.setEstado(EstadoAnualidad.PENDIENTE);
            anualidadEnEdicion.setFechaPago(null);
        }

        try {
            dao.actualizarAnualidad(anualidadEnEdicion);

            if (esPagoNuevo && anualidadEnEdicion.getAnio() < maxAnioParaTramite(anualidadEnEdicion.getVegetableFormId())) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(anualidadEnEdicion.getFechaPago());
                cal.add(java.util.Calendar.YEAR, 1);
                java.sql.Date proximoVenc = new java.sql.Date(cal.getTimeInMillis());
                dao.actualizarFechaVencimientoSiguiente(
                    anualidadEnEdicion.getVegetableFormId(),
                    anualidadEnEdicion.getAnio(),
                    proximoVenc);
                String fmtFecha = new java.text.SimpleDateFormat("dd/MM/yyyy").format(proximoVenc);
                cargar();
                PrimeFaces.current().executeScript("PF('dlgEdit').hide();");
                Operations.message(Operations.INFORMACION,
                    "Anualidad completada. Próximo vencimiento (Año "
                    + (anualidadEnEdicion.getAnio() + 1) + "): " + fmtFecha);
            } else {
                cargar();
                PrimeFaces.current().executeScript("PF('dlgEdit').hide();");
                Operations.message(Operations.INFORMACION, "Anualidad actualizada correctamente.");
            }
        } catch (Exception e) {
            Operations.message(Operations.ERROR, "Error al guardar: " + e.getMessage());
        }
    }

    /** Retorna el año más alto registrado para el trámite (total de anualidades creadas). */
    private int maxAnioParaTramite(Integer vegFormId) {
        if (vegFormId == null || todasLasAnualidades == null) return 20;
        int max = 0;
        for (Anualidad a : todasLasAnualidades) {
            if (vegFormId.equals(a.getVegetableFormId()) && a.getAnio() != null && a.getAnio() > max) {
                max = a.getAnio();
            }
        }
        return max > 0 ? max : 20;
    }

    public void limpiarFiltros() {
        filtroTramite         = "";
        filtroEstado          = "";
        anualidadEnEdicion    = null;
        estadoOriginal        = null;
        tramiteSeleccionadoId = null;
        completado            = false;
        cantidadAnualidades   = 20;
        cargar();
    }

    public List<VegetableForms> getTramitesFiltradosConAnualidades() {
        List<Anualidad> filtradas = getAnualidadesFiltradas();
        java.util.LinkedHashSet<Integer> ids = new java.util.LinkedHashSet<>();
        for (Anualidad a : filtradas) ids.add(a.getVegetableFormId());
        List<VegetableForms> result = new ArrayList<>();
        for (Integer id : ids) {
            VegetableForms vf = getVegetableForm(id);
            if (vf != null) result.add(vf);
        }
        return result;
    }

    public List<Anualidad> getAnualidadesFiltradasPorTramite(Integer vegetableFormId) {
        List<Anualidad> result = new ArrayList<>();
        if (vegetableFormId == null) return result;
        for (Anualidad a : getAnualidadesFiltradas()) {
            if (vegetableFormId.equals(a.getVegetableFormId())) result.add(a);
        }
        return result;
    }

    public long contarPagadasPorTramite(Integer vegetableFormId) {
        long n = 0;
        for (Anualidad a : getAnualidadesFiltradasPorTramite(vegetableFormId)) {
            if (EstadoAnualidad.PAGADO == a.getEstado()) n++;
        }
        return n;
    }

    public long contarVencidasPorTramite(Integer vegetableFormId) {
        long n = 0;
        for (Anualidad a : getAnualidadesFiltradasPorTramite(vegetableFormId)) {
            if (EstadoAnualidad.VENCIDO == a.getEstado()) n++;
        }
        return n;
    }

    private String obtenerUsuario() {
        try {
            LoginBean login = new Controller().getLogin();
            return (login != null && login.getLogin() != null) ? login.getLogin() : "sistema";
        } catch (Exception e) {
            return "sistema";
        }
    }

    // ── Getters / setters ──

    public int getTotalAlertas()     { return alertas == null ? 0 : alertas.size(); }
    public int getTotalAnualidades() { return todasLasAnualidades == null ? 0 : todasLasAnualidades.size(); }

    public int getTotalCompletadas() {
        if (todasLasAnualidades == null) return 0;
        int count = 0;
        for (Anualidad a : todasLasAnualidades) {
            if (a.getEstado() == EstadoAnualidad.PAGADO) count++;
        }
        return count;
    }

    public int getAnioSiguiente() {
        if (anualidadEnEdicion == null || anualidadEnEdicion.getAnio() == null) return 0;
        return anualidadEnEdicion.getAnio() + 1;
    }

    public List<Anualidad>      getTodasLasAnualidades() { return todasLasAnualidades; }
    public List<Anualidad>      getAlertas()             { return alertas; }
    public List<VegetableForms> getTramites()            { return tramites; }

    public Anualidad getAnualidadEnEdicion()            { return anualidadEnEdicion; }
    public void      setAnualidadEnEdicion(Anualidad a) { this.anualidadEnEdicion = a; }

    public Integer getTramiteSeleccionadoId()           { return tramiteSeleccionadoId; }
    public void    setTramiteSeleccionadoId(Integer id) { this.tramiteSeleccionadoId = id; }

    public String getFiltroTramite()         { return filtroTramite; }
    public void   setFiltroTramite(String v) { this.filtroTramite = v; }

    public String getFiltroEstado()          { return filtroEstado; }
    public void   setFiltroEstado(String v)  { this.filtroEstado = v; }

    public boolean isCompletado()            { return completado; }
    public void    setCompletado(boolean v)  { this.completado = v; }

    public int  getCantidadAnualidades()      { return cantidadAnualidades; }
    public void setCantidadAnualidades(int v) { this.cantidadAnualidades = (v < 1 ? 1 : (v > 20 ? 20 : v)); }
}
