package senadi.gob.ec.adminob.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.primefaces.PrimeFaces;
import org.primefaces.model.file.UploadedFile;
import senadi.gob.ec.adminob.dao.AnualidadDAO;
import senadi.gob.ec.adminob.dao.ParametroSistemaDAO;
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

    private List<Anualidad>      anualidadesFiltradas   = new ArrayList<>();
    private List<VegetableForms> tramitesFiltradosConAnualidades = new ArrayList<>();
    private java.util.Map<Integer, VegetableForms> cacheTramites = new java.util.HashMap<>();

    private Anualidad        anualidadEnEdicion;
    private EstadoAnualidad  estadoOriginal;
    private Integer          tramiteSeleccionadoId;
    private boolean          completado          = false;
    private int              cantidadAnualidades = 20;

    private String filtroTramite = "";
    private String filtroEstado  = "";

    // ── Parámetros de negocio configurables ──
    /** Porcentaje de recargo durante el período de gracia (default 10%). */
    private BigDecimal porcentajeRecargoDefecto    = new BigDecimal("10.00");
    /** Porcentaje de incremento cada 5 años (default 5%). */
    private BigDecimal porcentajeIncremento5Anios  = new BigDecimal("5.00");
    /** Monto base de la anualidad (usado al crear anualidades con monto). */
    private BigDecimal montoBaseCreacion;

    // ── Importación anualidades desde Excel ──
    private UploadedFile     archivoExcel;
    private List<String>     resultadosImportacion  = new ArrayList<>();
    private int              insertadosImportacion  = 0;
    private int              actualizadosImportacion = 0;
    private int              erroresImportacion     = 0;
    private boolean          mostrarResultados      = false;

    // ── Importación trámites faltantes desde Excel ──
    private UploadedFile     archivoExcelTramites;
    private List<String>     resultadosTramites      = new ArrayList<>();
    private int              insertadosTramites      = 0;
    private int              existentesTramites      = 0;
    private int              erroresTramites         = 0;
    private boolean          mostrarResultadosTramites = false;

    @PostConstruct
    public void init() {
        // Cargar valores configurables desde BD (fallback a defaults originales)
        try {
            ParametroSistemaDAO pDao = new ParametroSistemaDAO(null);
            porcentajeRecargoDefecto   = pDao.getValorDecimal("PORCENTAJE_RECARGO",
                new BigDecimal("10.00"));
            porcentajeIncremento5Anios = pDao.getValorDecimal("PORCENTAJE_INCREMENTO",
                new BigDecimal("5.00"));
            int diasAlerta = pDao.getValorEntero("DIAS_ALERTA_ANUALIDAD", 15);
            alertas = new AnualidadDAO(null).buscarConAlerta(diasAlerta);
        } catch (Exception e) {
            alertas = new ArrayList<>();
        }
    }

    public void cargar() {
        try {
            AnualidadDAO dao = new AnualidadDAO(null);
            // Transición automática: PENDIENTE→EN_GRACIA→PROCESO_EXPIRADO
            dao.marcarEstadosAutomaticos();
            todasLasAnualidades    = dao.buscarTodos();
            alertas                = dao.buscarConAlerta(15);
            tramitesConAnualidades = new HashSet<>(dao.idsConAnualidades());
        } catch (Exception e) {
            todasLasAnualidades = new ArrayList<>();
            alertas             = new ArrayList<>();
            System.err.println("[AnualidadBean] Error al cargar: " + e.getMessage());
        }
        try {
            tramites = new VegetableFormsDAO(null).buscarTodosByType("Aceptados");
        } catch (Exception e) {
            tramites = new ArrayList<>();
            System.err.println("[AnualidadBean] Error al cargar trámites: " + e.getMessage());
        }

        cacheTramites = new java.util.HashMap<>();
        if (tramites != null) {
            for (VegetableForms vf : tramites) {
                if (vf.getId() != null) cacheTramites.put(vf.getId(), vf);
            }
        }
        if (todasLasAnualidades != null) {
            List<Integer> idsALoad = new ArrayList<>();
            for (Anualidad a : todasLasAnualidades) {
                Integer formId = a.getVegetableFormId();
                if (formId != null && !cacheTramites.containsKey(formId)) idsALoad.add(formId);
            }
            if (!idsALoad.isEmpty()) {
                VegetableFormsDAO vfDao = new VegetableFormsDAO(null);
                for (Integer formId : idsALoad) {
                    if (!cacheTramites.containsKey(formId)) {
                        try {
                            VegetableForms vf = vfDao.getVegetableFormsById(formId);
                            if (vf != null) cacheTramites.put(formId, vf);
                        } catch (Exception ignored) {}
                    }
                }
            }
        }

        aplicarFiltros();
    }

    public void aplicarFiltros() {
        List<Anualidad> aFiltradas = new ArrayList<>();
        if (todasLasAnualidades != null) {
            for (Anualidad a : todasLasAnualidades) {
                if (!filtroEstado.isEmpty() && (a.getEstado() == null || !filtroEstado.equals(a.getEstado().name()))) continue;
                if (!filtroTramite.isEmpty()) {
                    VegetableForms vf = getVegetableForm(a.getVegetableFormId());
                    if (vf == null) continue;
                    String appNum = vf.getApplicationNumber() == null ? "" : vf.getApplicationNumber().toLowerCase();
                    String numInt = vf.getNumeroDeTramite()   == null ? "" : vf.getNumeroDeTramite().toLowerCase();
                    String busq   = filtroTramite.toLowerCase();
                    if (!appNum.contains(busq) && !numInt.contains(busq)) continue;
                }
                aFiltradas.add(a);
            }
        }
        this.anualidadesFiltradas = aFiltradas;

        java.util.LinkedHashSet<Integer> ids = new java.util.LinkedHashSet<>();
        for (Anualidad a : aFiltradas) {
            if (a.getVegetableFormId() != null) ids.add(a.getVegetableFormId());
        }
        List<VegetableForms> tFiltrados = new ArrayList<>();
        for (Integer id : ids) {
            VegetableForms vf = getVegetableForm(id);
            if (vf != null) tFiltrados.add(vf);
        }
        this.tramitesFiltradosConAnualidades = tFiltrados;
    }

    public List<Anualidad> getAnualidadesFiltradas() {
        if (anualidadesFiltradas == null) anualidadesFiltradas = new ArrayList<>();
        return anualidadesFiltradas;
    }

    public VegetableForms getVegetableForm(Integer id) {
        if (id == null) return null;
        if (cacheTramites == null) cacheTramites = new java.util.HashMap<>();
        if (cacheTramites.containsKey(id)) return cacheTramites.get(id);
        try {
            VegetableForms vf = new VegetableFormsDAO(null).getVegetableFormsById(id);
            cacheTramites.put(id, vf);
            return vf;
        } catch (Exception e) { return null; }
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
            new AnualidadDAO(null).crearAnualidadesVacias(
                tramiteSeleccionadoId, usuario, cantidadAnualidades,
                montoBaseCreacion, porcentajeRecargoDefecto, porcentajeIncremento5Anios);
            cargar();
            Operations.message(Operations.INFORMACION,
                "Se generaron " + cantidadAnualidades + " anualidades para el trámite.");
            tramiteSeleccionadoId = null;
            cantidadAnualidades   = 20;
            montoBaseCreacion     = null;
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
        anualidadEnEdicion.setMontoOriginal(a.getMontoOriginal());
        anualidadEnEdicion.setPorcentajeRecargo(
            a.getPorcentajeRecargo() != null ? a.getPorcentajeRecargo() : porcentajeRecargoDefecto);
        anualidadEnEdicion.setFechaLimitePago(a.getFechaLimitePago());
        anualidadEnEdicion.setFechaLimiteGracia(a.getFechaLimiteGracia());
        anualidadEnEdicion.setIncrementoAplicado(a.getIncrementoAplicado());
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
            // Determinar el estado correcto según el período actual
            anualidadEnEdicion.setEstado(calcularEstadoActual(anualidadEnEdicion));
            anualidadEnEdicion.setFechaPago(null);
        }

        // Si se ingresó un monto pero no hay montoOriginal, usar el monto como base
        if (anualidadEnEdicion.getMontoOriginal() == null && anualidadEnEdicion.getMonto() != null) {
            anualidadEnEdicion.setMontoOriginal(anualidadEnEdicion.getMonto());
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

    /** Determina el estado correcto según las fechas límite de la anualidad. */
    private EstadoAnualidad calcularEstadoActual(Anualidad a) {
        long now = System.currentTimeMillis();
        if (a.getFechaLimiteGracia() != null && now > a.getFechaLimiteGracia().getTime()) {
            return EstadoAnualidad.PROCESO_EXPIRADO;
        }
        if (a.getFechaLimitePago() != null && now > a.getFechaLimitePago().getTime()) {
            return EstadoAnualidad.EN_GRACIA;
        }
        return EstadoAnualidad.PENDIENTE;
    }

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
        montoBaseCreacion     = null;
        cargar();
    }

    // ── Contadores para la UI ──

    public int getTotalExpiradas() {
        if (todasLasAnualidades == null) return 0;
        int c = 0;
        for (Anualidad a : todasLasAnualidades) {
            if (a.getEstado() == EstadoAnualidad.PROCESO_EXPIRADO) c++;
        }
        return c;
    }

    public int getTotalEnGracia() {
        if (todasLasAnualidades == null) return 0;
        int c = 0;
        for (Anualidad a : todasLasAnualidades) {
            if (a.getEstado() == EstadoAnualidad.EN_GRACIA) c++;
        }
        return c;
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
            if (EstadoAnualidad.VENCIDO == a.getEstado()
                    || EstadoAnualidad.PROCESO_EXPIRADO == a.getEstado()) n++;
        }
        return n;
    }

    public long contarEnGraciaPorTramite(Integer vegetableFormId) {
        long n = 0;
        for (Anualidad a : getAnualidadesFiltradasPorTramite(vegetableFormId)) {
            if (EstadoAnualidad.EN_GRACIA == a.getEstado()) n++;
        }
        return n;
    }

    // ── Importaciones ──

    public void importarExcel() {
        if (archivoExcel == null || archivoExcel.getSize() == 0) {
            Operations.message(Operations.ERROR, "Seleccione un archivo Excel (.xls o .xlsx) antes de importar.");
            return;
        }
        try {
            AnualidadDAO dao = new AnualidadDAO(null);
            AnualidadDAO.ImportResult res = dao.importarDesdeExcel(
                archivoExcel.getInputStream(), obtenerUsuario());

            insertadosImportacion   = res.insertados;
            actualizadosImportacion = res.actualizados;
            erroresImportacion      = res.errores;
            resultadosImportacion   = res.mensajes;
            mostrarResultados       = true;
            cargar();

            if (res.errores == 0) {
                Operations.message(Operations.INFORMACION,
                    "Importación completada: " + res.insertados + " nuevas, "
                    + res.actualizados + " actualizadas.");
            } else {
                Operations.message(Operations.AVISO,
                    "Importación con advertencias: " + res.insertados + " nuevas, "
                    + res.actualizados + " actualizadas, " + res.errores + " filas con error.");
            }
        } catch (Exception e) {
            Operations.message(Operations.ERROR, "Error al importar el archivo: " + e.getMessage());
            System.err.println("[AnualidadBean] importarExcel: " + e.getMessage());
        } finally {
            archivoExcel = null;
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

    /** Texto del período actual para mostrar en el diálogo de edición. */
    public String getPeriodoEdicionLabel() {
        if (anualidadEnEdicion == null) return "";
        return anualidadEnEdicion.getPeriodoActualLabel();
    }

    /** Estilo CSS del período en el diálogo. */
    public String getPeriodoEdicionStyle() {
        if (anualidadEnEdicion == null) return "";
        String periodo = anualidadEnEdicion.getPeriodoActualLabel();
        if ("Período de Gracia".equals(periodo)) return "color:#92400e; font-weight:800;";
        if ("Expirado".equals(periodo) || "Proceso Expirado".equals(periodo))
            return "color:#be123c; font-weight:800;";
        if ("Plazo Normal".equals(periodo)) return "color:#065f46; font-weight:800;";
        return "color:#6b7280;";
    }

    public List<VegetableForms> getTramitesFiltradosConAnualidades() {
        if (tramitesFiltradosConAnualidades == null) tramitesFiltradosConAnualidades = new ArrayList<>();
        return tramitesFiltradosConAnualidades;
    }

    public List<Anualidad> getAnualidadesFiltradasPorTramite(Integer vegetableFormId) {
        List<Anualidad> result = new ArrayList<>();
        if (vegetableFormId == null || anualidadesFiltradas == null) return result;
        for (Anualidad a : anualidadesFiltradas) {
            if (vegetableFormId.equals(a.getVegetableFormId())) result.add(a);
        }
        return result;
    }

    public List<Anualidad>      getTodasLasAnualidades() { return todasLasAnualidades; }
    public List<Anualidad>      getAlertas()             { return alertas; }
    public List<VegetableForms> getTramites()            { return tramites; }

    public Anualidad getAnualidadEnEdicion()            { return anualidadEnEdicion; }
    public void      setAnualidadEnEdicion(Anualidad a) { this.anualidadEnEdicion = a; }

    public Integer getTramiteSeleccionadoId()           { return tramiteSeleccionadoId; }
    public void    setTramiteSeleccionadoId(Integer id) { this.tramiteSeleccionadoId = id; }

    public String getFiltroTramite()         { return filtroTramite; }
    public void   setFiltroTramite(String v) { this.filtroTramite = v != null ? v : ""; aplicarFiltros(); }

    public String getFiltroEstado()          { return filtroEstado; }
    public void   setFiltroEstado(String v)  { this.filtroEstado = v != null ? v : ""; aplicarFiltros(); }

    public boolean isCompletado()            { return completado; }
    public void    setCompletado(boolean v)  { this.completado = v; }

    public int  getCantidadAnualidades()      { return cantidadAnualidades; }
    public void setCantidadAnualidades(int v) { this.cantidadAnualidades = (v < 1 ? 1 : (v > 20 ? 20 : v)); }

    public BigDecimal getPorcentajeRecargoDefecto()            { return porcentajeRecargoDefecto; }
    public void       setPorcentajeRecargoDefecto(BigDecimal v){ this.porcentajeRecargoDefecto = v; }

    public BigDecimal getPorcentajeIncremento5Anios()            { return porcentajeIncremento5Anios; }
    public void       setPorcentajeIncremento5Anios(BigDecimal v){ this.porcentajeIncremento5Anios = v; }

    public BigDecimal getMontoBaseCreacion()            { return montoBaseCreacion; }
    public void       setMontoBaseCreacion(BigDecimal v){ this.montoBaseCreacion = v; }

    public UploadedFile getArchivoExcel()            { return archivoExcel; }
    public void         setArchivoExcel(UploadedFile f) { this.archivoExcel = f; }

    public List<String> getResultadosImportacion()   { return resultadosImportacion; }
    public int  getInsertadosImportacion()            { return insertadosImportacion; }
    public int  getActualizadosImportacion()          { return actualizadosImportacion; }
    public int  getErroresImportacion()               { return erroresImportacion; }
    public boolean isMostrarResultados()              { return mostrarResultados; }

    public void limpiarResultados() {
        mostrarResultados       = false;
        resultadosImportacion   = new ArrayList<>();
        insertadosImportacion   = 0;
        actualizadosImportacion = 0;
        erroresImportacion      = 0;
        archivoExcel            = null;
    }

    public void importarTramitesExcel() {
        if (archivoExcelTramites == null || archivoExcelTramites.getSize() == 0) {
            Operations.message(Operations.ERROR, "Seleccione un archivo Excel antes de importar.");
            return;
        }
        try {
            VegetableFormsDAO.ImportTramiteResult res =
                new VegetableFormsDAO(null).importarTramitesDesdeExcel(archivoExcelTramites.getInputStream());

            insertadosTramites        = res.insertados;
            existentesTramites        = res.existentes;
            erroresTramites           = res.errores;
            resultadosTramites        = res.mensajes;
            mostrarResultadosTramites = true;
            cargar();

            if (res.errores == 0) {
                Operations.message(Operations.INFORMACION,
                    "Trámites registrados: " + res.insertados + " nuevos, " + res.existentes + " ya existían.");
            } else {
                Operations.message(Operations.AVISO,
                    "Importación con advertencias: " + res.insertados + " nuevos, "
                    + res.existentes + " existían, " + res.errores + " errores.");
            }
        } catch (Exception e) {
            Operations.message(Operations.ERROR, "Error al importar trámites: " + e.getMessage());
        } finally {
            archivoExcelTramites = null;
        }
    }

    public void limpiarResultadosTramites() {
        mostrarResultadosTramites = false;
        resultadosTramites        = new ArrayList<>();
        insertadosTramites        = 0;
        existentesTramites        = 0;
        erroresTramites           = 0;
        archivoExcelTramites      = null;
    }

    public UploadedFile getArchivoExcelTramites()               { return archivoExcelTramites; }
    public void         setArchivoExcelTramites(UploadedFile f) { this.archivoExcelTramites = f; }
    public List<String> getResultadosTramites()                 { return resultadosTramites; }
    public int          getInsertadosTramites()                 { return insertadosTramites; }
    public int          getExistentesTramites()                 { return existentesTramites; }
    public int          getErroresTramites()                    { return erroresTramites; }
    public boolean      isMostrarResultadosTramites()           { return mostrarResultadosTramites; }
}
