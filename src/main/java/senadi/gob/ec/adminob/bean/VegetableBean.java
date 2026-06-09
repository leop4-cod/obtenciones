package senadi.gob.ec.adminob.bean;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIData;
import javax.faces.event.ActionEvent;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import senadi.gob.ec.adminob.dao.ComprobantePagoDAO;
import senadi.gob.ec.adminob.dao.TramiteDAO;
import senadi.gob.ec.adminob.dao.VegetableFormsDAO;
import senadi.gob.ec.adminob.enums.FlowPhase;
import senadi.gob.ec.adminob.enums.Status;
import senadi.gob.ec.adminob.enums.StatusFlow;
import senadi.gob.ec.adminob.dao.HistoryDAO;
import senadi.gob.ec.adminob.model.ComprobantePago;
import senadi.gob.ec.adminob.model.History;
import senadi.gob.ec.adminob.model.Tramite;
import senadi.gob.ec.adminob.model.VegetableForms;
import senadi.gob.ec.adminob.service.TramiteFlowService;
import senadi.gob.ec.adminob.solicitudes.Owners;
import senadi.gob.ec.adminob.solicitudes.OwnersDAO;
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
    private UIData vegetableTable;

    private String previewPath;
    private String radioOption;

    private Date startDate;
    private Date endDate;

    private boolean byDate;

    private List<History> historialList = new java.util.ArrayList<>();
    private String statusObservation;
    private StatusFlow pendingStatusFlow;
    private StatusFlow previousStatusFlow;

    private List<ComprobantePago> archivosSubidos;
    private Set<Integer> idsConArchivos = new HashSet<>();

    private static final long   MAX_FORM_PDF_SIZE    = 10L * 1024 * 1024;
    private static final long   MAX_PAYMENT_PDF_SIZE =  5L * 1024 * 1024;
    private static final long   MAX_PHOTO_SIZE       =  5L * 1024 * 1024;

    private final TramiteFlowService tramiteService = new TramiteFlowService();

    public VegetableBean() {
        loadVegetables();
    }

    private void loadVegetables() {
        Controller c = new Controller();
        radioOption = "Aceptados";
        try {
            login = c.getLogin();
            vegetables = c.buscarTodosByType("Aceptados");
        } catch (Exception ex) {
            vegetables = new java.util.ArrayList<>();
            System.err.println("[VegetableBean] Error al cargar obtenciones: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            ex.printStackTrace();
            return;
        }
        try {
            c.precargarLockers(vegetables);
        } catch (Exception ex) {
            System.err.println("[VegetableBean] No se pudieron cargar casilleros: " + ex.getMessage());
        }
        try {
            idsConArchivos = new ComprobantePagoDAO(null).getDistinctVegetableFormIds();
        } catch (Exception ex) {
            idsConArchivos = new HashSet<>();
            System.err.println("[VegetableBean] No se pudieron cargar IDs con archivos: " + ex.getMessage());
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
            Operations.message(Operations.ERROR, "Hay un problema con el registro seleccionado.");
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
            Operations.message(Operations.ERROR, "Hay un problema con el registro seleccionado.");
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
            Operations.message(Operations.ERROR, "No se pudo consultar la base de datos local.");
        }
        try {
            idsConArchivos = new ComprobantePagoDAO(null).getDistinctVegetableFormIds();
        } catch (Exception ex) {
            idsConArchivos = new HashSet<>();
        }
        cleanDate();
    }

    public void searchVegetables(ActionEvent ae) {
        if (Operations.validateDate(startDate) && Operations.validateDate(endDate)) {
            Controller c = new Controller();
            try {
                vegetables = c.buscarTodosByTypeAndDate(radioOption,
                        startDate != null ? new java.sql.Timestamp(startDate.getTime()) : null,
                        endDate   != null ? new java.sql.Timestamp(endDate.getTime())   : null);
                c.precargarLockers(vegetables);
            } catch (Exception ex) {
                vegetables = new java.util.ArrayList<>();
                Operations.message(Operations.ERROR, "No se pudo consultar la base de datos local.");
            }
        } else {
            Operations.message(Operations.ERROR, "Ingrese un rango de fechas correcto.");
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
            Operations.message(Operations.ERROR, "No se encontró el trámite seleccionado.");
            return;
        }
        Controller c = new Controller();
        VegetableForms current = c.getVegetableFormsById(vegetableForms.getId());

        if (current == null || current.getId() == null) {
            Operations.message(Operations.ERROR, "No se encontró el trámite seleccionado.");
            return;
        }
        // Delegar en el servicio para garantizar FlowPhase.ASSIGNED + historial
        boolean ok = tramiteService.asignarTecnico(current, login.getLogin(), login.getLogin());
        if (ok) {
            onRadioSelected();
            Operations.message(Operations.INFORMACION,
                "Se asignó correctamente el trámite"
                + current.getApplicationNumber() + " AL USUARIO " + login.getLogin());
        } else {
            Operations.message(Operations.AVISO,
                "No se pudo asignar el trámite " + current.getApplicationNumber() + ".");
        }
    }

    public void prepareStatusFlowChange() {
        vegetableForms = (VegetableForms) vegetableTable.getRowData();
        if (vegetableForms == null || vegetableForms.getId() == null) {
            Operations.message(Operations.ERROR, "No se encontró el trámite seleccionado.");
            return;
        }

        Controller c = new Controller();
        VegetableForms current = c.getVegetableFormsById(vegetableForms.getId());
        if (current == null || current.getId() == null) {
            Operations.message(Operations.ERROR, "No se encontró el trámite seleccionado.");
            return;
        }
        previousStatusFlow = current.getStatusFlow();
        pendingStatusFlow = vegetableForms.getStatusFlow();
        vegetableForms.setStatusFlow(previousStatusFlow);

        if (pendingStatusFlow == null) {
            Operations.message(Operations.AVISO, "Seleccione un estado de gestión válido.");
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
            Operations.message(Operations.ERROR, "No se encontró el trámite seleccionado.");
            return;
        }
        if (statusObservation == null || statusObservation.trim().isEmpty()) {
            Operations.message(Operations.ERROR, "Debe ingresar una observación para cambiar el estado de gestión.");
            return;
        }

        Controller c = new Controller();
        VegetableForms current = c.getVegetableFormsById(vegetableForms.getId());
        if (current == null || current.getId() == null) {
            Operations.message(Operations.ERROR, "No se encontró el trámite seleccionado.");
            return;
        }
        if (pendingStatusFlow == null) {
            Operations.message(Operations.AVISO, "No existe un nuevo estado de gestión para guardar.");
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
            Operations.message(Operations.INFORMACION, "Se actualizó el estado de gestión del trámite " + current.getApplicationNumber() + ".");
        } else {
            Operations.message(Operations.ERROR, "No se pudo actualizar el estado de gestión.");
        }
    }

    public void aceptarObtencion(ActionEvent ae) {
        if (vegetableForms == null || vegetableForms.getId() == null) {
            Operations.message(Operations.ERROR, "No se encontró el trámite seleccionado.");
            return;
        }
        Controller c = new Controller();
        VegetableForms current = c.getVegetableFormsById(vegetableForms.getId());
        if (current == null) {
            Operations.message(Operations.ERROR, "No se encontró el trámite seleccionado.");
            return;
        }
        if (current.getStatus() != Status.EN_PROCESO) {
            Operations.message(Operations.AVISO, "Solo se pueden aceptar trámites en estado EN PROCESO.");
            return;
        }
        current.setStatus(Status.ACCEPTED);
        if (c.updateVegetableForms(current)) {
            saveHistoryEntry(current.getApplicationNumber(),
                "Tramite ACEPTADO por " + login.getLogin());
            onRadioSelected();
            Operations.message(Operations.INFORMACION,
                "Trámite " + current.getApplicationNumber() + " aceptado correctamente por " + login.getLogin() + ".");
        } else {
            Operations.message(Operations.ERROR, "No se pudo aceptar el trámite.");
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

    // 1. Esto llena el combo box con las opciones del Enum
    public List<StatusFlow> getStatusFlows() {
        return java.util.Arrays.asList(StatusFlow.values());
    }

    // 2. Esto cambia lo que el usuario VE en el combo box
    public String getStatusFlowLabel(StatusFlow statusFlow) {
        if (statusFlow == null) return "SIN GESTIÓN";
        switch (statusFlow) {
            case ATTENDED: return "ATENDIDO";
            case PENDING:  return "PENDIENTE";
            case DENIED:   return "NEGADO";
            case EXPIRED:  return "CADUCADO";
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
        historialList = new java.util.ArrayList<>();
        if (this.vegetableForms == null || this.vegetableForms.getApplicationNumber() == null) {
            Operations.message(Operations.ERROR, "No hay un trámite seleccionado.");
            return;
        }
        try {
            Controller c = new Controller();
            List<History> hists = c.getHistoriesByAppNumber(this.vegetableForms.getApplicationNumber());
            if (hists != null) {
                historialList = hists;
            }
        } catch (Exception e) {
            Operations.message(Operations.ERROR, "Error al cargar historial: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void prepararHistorial(VegetableForms v) {
        historialList = new java.util.ArrayList<>();
        if (v == null || v.getApplicationNumber() == null) {
            Operations.message(Operations.ERROR, "No hay un trámite seleccionado.");
            return;
        }
        this.vegetableForms = v;
        try {
            Controller c = new Controller();
            List<History> hists = c.getHistoriesByAppNumber(v.getApplicationNumber());
            if (hists != null) {
                historialList = hists;
            }
        } catch (Exception e) {
            Operations.message(Operations.ERROR, "Error al cargar historial: " + e.getMessage());
            e.printStackTrace();
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
    public void importarExcel(FileUploadEvent event) {
        UploadedFile file = event.getFile();
        if (file == null) {
            Operations.message(Operations.ERROR, "No se recibió el archivo Excel.");
            return;
        }

        String fileName = file.getFileName() == null ? "" : file.getFileName().toLowerCase(Locale.ROOT);
        if (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls")) {
            Operations.message(Operations.ERROR, "Formato no permitido. Suba un archivo .xlsx o .xls.");
            return;
        }

        int imported = 0;
        int skipped = 0;
        int duplicated = 0;
        int failed = 0;

        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            DataFormatter formatter = new DataFormatter(new Locale("es", "EC"));
            Sheet sheet = findBaseDatosSheet(workbook);
            if (sheet == null) {
                Operations.message(Operations.AVISO, "No se encontró la hoja 'BASE DE DATOS' en el Excel.");
                return;
            }

            Row headerRow = findHeaderRow(sheet, formatter);
            if (headerRow == null) {
                Operations.message(Operations.AVISO, "No se encontró la fila de encabezados. Use una columna llamada TRAMITE.");
                return;
            }

            Map<String, Integer> columns = buildHeaderMap(headerRow, formatter);
            Set<String> existingNumbers = getExistingApplicationNumbers();

            for (int i = headerRow.getRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isBlankRow(row, formatter)) {
                    skipped++;
                    continue;
                }

                String applicationNumber = firstValue(row, columns, formatter,
                        "tramite nro", "tramite no", "tramite numero", "tramite", "numero tramite",
                        "application number", "applicationnumber", "solicitud");
                if (applicationNumber == null || applicationNumber.trim().isEmpty()) {
                    skipped++;
                    continue;
                }

                String key = applicationNumber.trim().toUpperCase(Locale.ROOT);
                if (existingNumbers.contains(key)) {
                    duplicated++;
                    continue;
                }

                try {
                    VegetableForms form = new VegetableForms();
                    form.setApplicationNumber(limit(applicationNumber.trim(), 80));
                    form.setCreateDate(toTimestamp(firstCell(row, columns, "fecha admision de solicitud", "f creacion", "fecha creacion", "create date")));
                    form.setApplicationDate(toTimestamp(firstCell(row, columns, "fecha admision de solicitud", "f presentacion", "fecha presentacion", "application date")));
                    form.setBotanicalTaxon(firstValue(row, columns, formatter, "taxon botanico", "taxon", "botanical taxon"));
                    form.setCommonName(firstValue(row, columns, formatter, "nombre comun", "common name"));
                    // Campos de denominacion se dejan fuera en la carga masiva inicial
                    // porque en algunas bases historicas tienen restricciones cortas.
                    form.setGenericDenomination(null);
                    form.setProvitionalDesignation(null);
                    form.setVarietalGroup(limit(firstValue(row, columns, formatter, "tipo de cultivo", "grupo varietal", "varietal group"), 80));
                    String etapaActual = firstValue(row, columns, formatter, "etapa actual", "estado", "status");
                    String estadoExpediente = firstValue(row, columns, formatter, "estado del expediente");
                    String combinedStatus = (etapaActual != null ? etapaActual : "") + " " + (estadoExpediente != null ? estadoExpediente : "");
                    form.setStatus(parseStatus(combinedStatus));
                    form.setStatusFlow(parseStatusFlow(combinedStatus));
                    // No se guarda la observacion consolidada en BD porque algunas
                    // instalaciones tienen additional_information con longitud menor.
                    form.setAdditionalInformation(null);
                    form.setFlowPhase(FlowPhase.INITIAL);

                    if (form.getCreateDate() == null) {
                        form.setCreateDate(new Timestamp(System.currentTimeMillis()));
                    }
                    if (form.getApplicationDate() == null) {
                        form.setApplicationDate(new Timestamp(System.currentTimeMillis()));
                    }

                    new VegetableFormsDAO(form).persist();
                    existingNumbers.add(key);
                    imported++;
                } catch (Exception rowError) {
                    failed++;
                    System.err.println("[IMPORT EXCEL] Fila " + (i + 1) + " omitida (" + applicationNumber + "): "
                            + rowError.getClass().getSimpleName() + " - " + rowError.getMessage());
                }
            }

            try {
                onRadioSelected();
            } catch (Exception refreshError) {
                System.err.println("[IMPORT EXCEL] Importacion realizada, pero no se pudo refrescar la tabla: "
                        + refreshError.getMessage());
            }
            Operations.message(Operations.INFORMACION,
                    "Importación completada. Nuevos: " + imported
                    + " | Duplicados: " + duplicated
                    + " | Omitidos: " + skipped
                    + " | Fallidos: " + failed);
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(getClass().getName())
                .log(java.util.logging.Level.SEVERE, "Error importando Excel", e);
            Operations.message(Operations.ERROR, "Error general en importación: " + e.getMessage());
        }
    }

    private Map<String, Integer> buildHeaderMap(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> headers = new HashMap<>();
        if (headerRow == null) return headers;
        for (Cell cell : headerRow) {
            String key = normalize(formatter.formatCellValue(cell));
            if (!key.isEmpty()) headers.put(key, cell.getColumnIndex());
        }
        return headers;
    }

    private Sheet findBaseDatosSheet(Workbook workbook) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (sheet != null && "base de datos".equals(normalize(sheet.getSheetName()))) {
                return sheet;
            }
        }
        return null;
    }

    private Row findHeaderRow(Sheet sheet, DataFormatter formatter) {
        if (sheet == null) return null;
        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isBlankRow(row, formatter)) continue;

            Map<String, Integer> headers = buildHeaderMap(row, formatter);
            if (hasAnyHeader(headers, "tramite nro", "tramite no", "tramite numero", "tramite", "numero tramite", "application number", "applicationnumber", "solicitud")) {
                return row;
            }
        }
        return null;
    }

    private boolean hasAnyHeader(Map<String, Integer> headers, String... names) {
        for (String name : names) {
            if (headers.containsKey(normalize(name))) return true;
        }
        return false;
    }

    private Set<String> getExistingApplicationNumbers() {
        Set<String> result = new HashSet<>();
        Controller c = new Controller();
        List<VegetableForms> all = c.buscarTodos();
        if (all != null) {
            for (VegetableForms vf : all) {
                if (vf.getApplicationNumber() != null && !vf.getApplicationNumber().trim().isEmpty()) {
                    result.add(vf.getApplicationNumber().trim().toUpperCase(Locale.ROOT));
                }
            }
        }
        return result;
    }

    private boolean isBlankRow(Row row, DataFormatter formatter) {
        for (Cell cell : row) {
            String value = formatter.formatCellValue(cell);
            if (value != null && !value.trim().isEmpty()) return false;
        }
        return true;
    }

    private String firstValue(Row row, Map<String, Integer> columns, DataFormatter formatter, String... names) {
        Cell cell = firstCell(row, columns, names);
        if (cell == null) return null;
        String value = formatter.formatCellValue(cell);
        return value == null ? null : value.trim();
    }

    private Cell firstCell(Row row, Map<String, Integer> columns, String... names) {
        for (String name : names) {
            Integer index = columns.get(normalize(name));
            if (index != null) return row.getCell(index);
        }
        return null;
    }

    private String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replace("#", "")
                .replace(".", " ")
                .replace("_", " ")
                .replace("-", " ")
                .replace("/", " ")
                .replace("(", " ")
                .replace(")", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private Timestamp toTimestamp(Cell cell) {
        Date date = toDate(cell);
        return date == null ? null : new Timestamp(date.getTime());
    }

    private Date toDate(Cell cell) {
        if (cell == null) return null;
        try {
            if (DateUtil.isCellDateFormatted(cell)) return cell.getDateCellValue();
            try {
                double numericValue = cell.getNumericCellValue();
                if (numericValue > 20000 && DateUtil.isValidExcelDate(numericValue)) {
                    return DateUtil.getJavaDate(numericValue);
                }
            } catch (Exception ignored) {
            }
            String raw = new DataFormatter(new Locale("es", "EC")).formatCellValue(cell);
            if (raw == null || raw.trim().isEmpty()) return null;
            String[] patterns = {"yyyy-MM-dd", "dd/MM/yyyy", "d/M/yyyy", "dd-MM-yyyy", "d-M-yyyy"};
            for (String pattern : patterns) {
                try {
                    return new SimpleDateFormat(pattern).parse(raw.trim());
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private Status parseStatus(String value) {
        String text = normalize(value);
        if (text.contains("pagado") || text.contains("finished") || text.contains("dominio publico")
                || text.contains("concesion") || text.contains("renuncia") || text.contains("vencimiento")) {
            return Status.FINISHED;
        }
        if (text.contains("aceptado") || text.contains("accepted") || text.contains("aprobado")) {
            return Status.ACCEPTED;
        }
        if (text.contains("proceso") || text.contains("iniciado") || text.contains("delivered")
                || text.contains("tramite") || text.contains("en tramite") || text.contains("ingresado")
                || text.contains("entregado") || text.contains("recibido") || text.contains("presentado")
                || text.contains("en_proceso")) {
            return Status.EN_PROCESO;
        }
        if (text.contains("vista") || text.contains("preview")) return Status.PREVIEW;
        if (text.contains("guardado") || text.contains("saved") || text.contains("borrador")) return Status.SAVED;
        // Los registros importados desde Excel son trámites activos por defecto
        return Status.EN_PROCESO;
    }

    private StatusFlow parseStatusFlow(String value) {
        String text = normalize(value);
        if (text.contains("finished") || text.contains("attended") || text.contains("atendido")
                || text.contains("dominio publico") || text.contains("concesion")
                || text.contains("renuncia") || text.contains("vencimiento")) return StatusFlow.ATTENDED;
        if (text.contains("reject") || text.contains("denied") || text.contains("negado")) return StatusFlow.DENIED;
        if (text.contains("expired") || text.contains("expirado")) return StatusFlow.EXPIRED;
        return StatusFlow.PENDING;
    }

    private String limit(String value, int maxLength) {
        if (value == null) return null;
        String text = value.trim();
        if (text.isEmpty()) return null;
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    // ── BÚSQUEDA E IMPORTACIÓN DESDE PORTAL ──────────────────────────────────

    private String numeroBusquedaPortal = "";
    private VegetableForms tramitePortalEncontrado;
    private String mensajeBusquedaPortal;
    private boolean tramiteYaImportado;

    public void buscarEnPortal() {
        tramitePortalEncontrado = null;
        tramiteYaImportado = false;
        mensajeBusquedaPortal = null;

        if (numeroBusquedaPortal == null || numeroBusquedaPortal.trim().isEmpty()) {
            mensajeBusquedaPortal = "Ingrese un número de trámite.";
            return;
        }
        String numero = numeroBusquedaPortal.trim();
        try {
            VegetableForms vf = new VegetableFormsDAO(null).buscarPorNumero(numero);
            if (vf == null) {
                mensajeBusquedaPortal = "No se encontró ningún trámite con el número: " + numero;
                return;
            }
            if (vf.getStatus() != Status.EN_PROCESO) {
                mensajeBusquedaPortal = "El trámite " + numero + " no está en estado EN PROCESO (estado: "
                    + (vf.getStatus() != null ? vf.getStatus().name() : "desconocido") + ").";
                return;
            }
            if (new TramiteDAO(null).getDistinctVegetableFormIds().contains(vf.getId())) {
                tramiteYaImportado = true;
                mensajeBusquedaPortal = "Este trámite ya fue importado anteriormente.";
                tramitePortalEncontrado = vf;
                return;
            }
            tramitePortalEncontrado = vf;
        } catch (Exception e) {
            mensajeBusquedaPortal = "Error al buscar: " + e.getMessage();
        }
    }

    public void importarTramitePortal() {
        if (tramitePortalEncontrado == null) {
            Operations.message(Operations.ERROR, "No hay un trámite seleccionado para importar.");
            return;
        }
        VegetableForms vf = tramitePortalEncontrado;
        try {
            String usuario = (login != null && login.getLogin() != null) ? login.getLogin() : "sistema";

            Tramite t = new Tramite();
            t.setNumeroTramite(vf.getApplicationNumber() != null ? vf.getApplicationNumber().trim() : "SIN-NUMERO");
            t.setDenominacion(vf.getCommonName() != null ? vf.getCommonName().trim() : null);
            t.setVegetableFormId(vf.getId());
            t.setEstadoActual("EN_PROCESO");
            t.setPuedeEditar(false);
            t.setFechaCreacion(new java.sql.Timestamp(System.currentTimeMillis()));
            t.setFechaModificacion(new java.sql.Timestamp(System.currentTimeMillis()));
            t.setUsuarioCreacion(usuario);

            if (vf.getApplicationDate() != null) {
                t.setFechaPresentacion(new java.sql.Date(vf.getApplicationDate().getTime()));
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
                    System.err.println("[VegetableBean] No se pudo cargar owner: " + ex.getMessage());
                }
            }

            new TramiteDAO(t).persist();

            History h = new History();
            h.setApplicationNumber(t.getNumeroTramite());
            h.setFecha(new java.sql.Timestamp(System.currentTimeMillis()));
            h.setHistoryUser(usuario);
            h.setDescription("Trámite importado desde portal por " + usuario);
            new HistoryDAO(h).persist();

            Operations.message(Operations.INFORMACION,
                "Solicitud " + t.getNumeroTramite() + " importada como nuevo trámite EN PROCESO.");

            tramitePortalEncontrado = null;
            numeroBusquedaPortal = "";
            mensajeBusquedaPortal = null;
            tramiteYaImportado = false;

        } catch (Exception e) {
            Operations.message(Operations.ERROR, "Error al importar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void limpiarBusquedaPortal() {
        tramitePortalEncontrado = null;
        numeroBusquedaPortal = "";
        mensajeBusquedaPortal = null;
        tramiteYaImportado = false;
    }

    public String getNumeroBusquedaPortal() { return numeroBusquedaPortal; }
    public void setNumeroBusquedaPortal(String n) { this.numeroBusquedaPortal = n; }
    public VegetableForms getTramitePortalEncontrado() { return tramitePortalEncontrado; }
    public String getMensajeBusquedaPortal() { return mensajeBusquedaPortal; }
    public boolean isTramiteYaImportado() { return tramiteYaImportado; }

    public void prepareViewUploaded(ActionEvent ae) {
        if (vegetableForms == null || vegetableForms.getId() == null) {
            Operations.message(Operations.ERROR, "No se encontró el trámite seleccionado.");
            return;
        }
        try {
            archivosSubidos = new ComprobantePagoDAO(null).getTodosLosArchivosPorVegetableFormId(vegetableForms.getId());
        } catch (Exception e) {
            archivosSubidos = new java.util.ArrayList<>();
            Operations.message(Operations.ERROR, "No se pudieron cargar los archivos subidos.");
        }
    }

    public List<ComprobantePago> getArchivosSubidos() { return archivosSubidos; }

    public boolean hasUploadedFiles(Integer tramiteId) {
        return tramiteId != null && idsConArchivos != null && idsConArchivos.contains(tramiteId);
    }


        // --- GETTERS Y SETTERS COMPLETOS ---
        public LoginBean getLogin() { return login; }
        public void setLogin(LoginBean login) { this.login = login; }
        public List<VegetableForms> getVegetables() { return vegetables; }
        public void setVegetables(List<VegetableForms> vegetables) { this.vegetables = vegetables; }
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
        public List<History> getHistorialList() { return historialList; }
        public String getStatusObservation() { return statusObservation; }
        public void setStatusObservation(String statusObservation) { this.statusObservation = statusObservation; }
        public StatusFlow getPendingStatusFlow() { return pendingStatusFlow; }
        public void setPendingStatusFlow(StatusFlow pendingStatusFlow) { this.pendingStatusFlow = pendingStatusFlow; }
        public StatusFlow getPreviousStatusFlow() { return previousStatusFlow; }
        public void setPreviousStatusFlow(StatusFlow previousStatusFlow) { this.previousStatusFlow = previousStatusFlow; }
    }
