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
    private boolean          completado = false;

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
            tramites = new VegetableFormsDAO(null).buscarTodos();
        } catch (Exception e) {
            tramites = new ArrayList<>();
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
                String numInt = vf.getNumeracionInterna() == null ? "" : vf.getNumeracionInterna().toLowerCase();
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
        return null;
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
            new AnualidadDAO(null).crearAnualidadesVacias(tramiteSeleccionadoId, usuario);
            cargar();
            tramiteSeleccionadoId = null;
            Operations.message(Operations.INFORMACION, "Se generaron 20 anualidades para el trámite.");
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
                // Auto-asignar fecha de pago = hoy
                anualidadEnEdicion.setFechaPago(new java.sql.Date(System.currentTimeMillis()));
            }
        } else {
            anualidadEnEdicion.setEstado(EstadoAnualidad.PENDIENTE);
            anualidadEnEdicion.setFechaPago(null);
        }

        try {
            dao.actualizarAnualidad(anualidadEnEdicion);

            if (esPagoNuevo && anualidadEnEdicion.getAnio() < 20) {
                // Auto-calcular próximo vencimiento = fechaPago + 1 año
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

    public void limpiarFiltros() {
        filtroTramite         = "";
        filtroEstado          = "";
        anualidadEnEdicion    = null;
        estadoOriginal        = null;
        tramiteSeleccionadoId = null;
        completado            = false;
        cargar();
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

    /** Número del año siguiente al que está en edición (para mostrar en el diálogo). */
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
}
