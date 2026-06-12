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
import senadi.gob.ec.adminob.integration.ExternalAppRestClient;
import senadi.gob.ec.adminob.integration.dto.ExternalVarietyDataDTO;
import senadi.gob.ec.adminob.integration.dto.TechnicalInfoDTO;

@ManagedBean(name = "registroBean")
@ViewScoped
public class RegistroBean implements Serializable {

    private Integer editId;
    private boolean editMode;
    private VegetableForms form;
    private LoginBean login;
    private List<ComprobantePago> archivosSubidos;
    private String originalObservacionTecnica;

    private static final List<String> OBSERVACIONES_ORDENADAS = java.util.Arrays.asList(
        "Aceptada a trámite y revisión de requisitos de fondo",
        "Para publicar en gaceta",
        "Para consulta de resultados de examen DHE",
        "Para realización de examen DHE",
        "En espera de resultados de examen DHE",
        "Para dictamen técnico",
        "Para resolución"
    );

    private UploadedFile archivoFormulario;
    private UploadedFiles archivosFotos;
    private UploadedFile archivoPago;
    private String nombrePersonalizado;

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
        originalObservacionTecnica = null;
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
                originalObservacionTecnica = loaded.getObservacionTecnica();
                try {
                    archivosSubidos = new ComprobantePagoDAO(null).getTodosLosArchivosPorVegetableFormId(form.getId());
                } catch (Exception e) {
                    archivosSubidos = new ArrayList<>();
                }
                autoJalarDatosExternos();
            } else {
                addError("No se encontró el registro con ID: " + editId);
            }
        }
    }

    public void autoJalarDatosExternos() {
        if (form == null || form.getApplicationNumber() == null || form.getApplicationNumber().trim().isEmpty()) {
            return;
        }
        boolean isFresh = (form.getGenealogy() == null || form.getGenealogy().trim().isEmpty())
                       && (form.getReproductionMechanism() == null || form.getReproductionMechanism().trim().isEmpty());
        if (!isFresh) {
            return;
        }
        try {
            ExternalAppRestClient client = new ExternalAppRestClient();
            ExternalVarietyDataDTO extData = client.obtenerDatos(form.getApplicationNumber());
            if (extData != null) {
                if (extData.getEnTerritorio() != null) {
                    form.setInTerritory(true);
                }
                if (extData.getFueraTerritorio() != null) {
                    form.setOutTerritory(true);
                }
                if (extData.getInformacionTecnica() != null) {
                    TechnicalInfoDTO tech = extData.getInformacionTecnica();
                    if (tech.getTaxon() != null && !tech.getTaxon().trim().isEmpty()) {
                        form.setBotanicalTaxon(tech.getTaxon().trim());
                    }
                    if (tech.getNombreComun() != null && !tech.getNombreComun().trim().isEmpty()) {
                        form.setCommonName(tech.getNombreComun().trim());
                    }
                    if (tech.getMecanismoReproducccion() != null && !tech.getMecanismoReproducccion().trim().isEmpty()) {
                        form.setReproductionMechanism(tech.getMecanismoReproducccion().trim());
                    }
                    if (tech.getOrigenGeografico() != null && !tech.getOrigenGeografico().trim().isEmpty()) {
                        form.setGeographicalMaterialOrigin(tech.getOrigenGeografico().trim());
                    }
                    if (tech.getGenealogy() != null && !tech.getGenealogy().trim().isEmpty()) {
                        form.setGenealogy(tech.getGenealogy().trim());
                    }
                    if (tech.getObservaciones() != null && !tech.getObservaciones().trim().isEmpty()) {
                        form.setAdditionalInformation(tech.getObservaciones().trim());
                    }
                }
            }
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.WARNING, 
                "[RegistroBean] Error al auto-sincronizar con el aplicativo externo: " + e.getMessage());
        }
    }

    public void sincronizarDatosExternos() {
        if (form == null || form.getApplicationNumber() == null || form.getApplicationNumber().trim().isEmpty()) {
            addError("No se puede sincronizar: el número de trámite está vacío.");
            return;
        }
        try {
            ExternalAppRestClient client = new ExternalAppRestClient();
            ExternalVarietyDataDTO extData = client.obtenerDatos(form.getApplicationNumber());
            if (extData == null) {
                addError("No se encontraron datos en el aplicativo externo para el trámite: " + form.getApplicationNumber());
                return;
            }

            int count = 0;
            if (extData.getEnTerritorio() != null) {
                form.setInTerritory(true);
                count++;
            } else {
                form.setInTerritory(false);
            }
            if (extData.getFueraTerritorio() != null) {
                form.setOutTerritory(true);
                count++;
            } else {
                form.setOutTerritory(false);
            }

            if (extData.getInformacionTecnica() != null) {
                TechnicalInfoDTO tech = extData.getInformacionTecnica();
                if (tech.getTaxon() != null && !tech.getTaxon().trim().isEmpty()) {
                    form.setBotanicalTaxon(tech.getTaxon().trim());
                    count++;
                }
                if (tech.getNombreComun() != null && !tech.getNombreComun().trim().isEmpty()) {
                    form.setCommonName(tech.getNombreComun().trim());
                    count++;
                }
                if (tech.getMecanismoReproducccion() != null && !tech.getMecanismoReproducccion().trim().isEmpty()) {
                    form.setReproductionMechanism(tech.getMecanismoReproducccion().trim());
                    count++;
                }
                if (tech.getOrigenGeografico() != null && !tech.getOrigenGeografico().trim().isEmpty()) {
                    form.setGeographicalMaterialOrigin(tech.getOrigenGeografico().trim());
                    count++;
                }
                if (tech.getGenealogy() != null && !tech.getGenealogy().trim().isEmpty()) {
                    form.setGenealogy(tech.getGenealogy().trim());
                    count++;
                }
                if (tech.getObservaciones() != null && !tech.getObservaciones().trim().isEmpty()) {
                    form.setAdditionalInformation(tech.getObservaciones().trim());
                    count++;
                }
            }

            if (count > 0) {
                addInfo("Sincronización exitosa. Se actualizaron los campos con datos del aplicativo externo.");
            } else {
                addInfo("El trámite existe en el aplicativo externo pero no contiene datos técnicos adicionales.");
            }
        } catch (Exception e) {
            addError("Error al conectar con el aplicativo externo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String saveRegistro() {
        if (!validarPublicacionGaceta()) return null;
        if (isObservacionTecnicaOptionDisabled(form.getObservacionTecnica())) {
            addError("Transición de Observación Técnica no permitida. Debe avanzar secuencialmente.");
            return null;
        }
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

            originalObservacionTecnica = form.getObservacionTecnica();
            registrarHistorial(form, "Registro creado");
            return "index?faces-redirect=true";
        } catch (Exception e) {
            addError("ERROR AL GUARDAR: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String saveRegistroAndStay() {
        if (!validarPublicacionGaceta()) return null;
        if (isObservacionTecnicaOptionDisabled(form.getObservacionTecnica())) {
            addError("Transición de Observación Técnica no permitida. Debe avanzar secuencialmente.");
            return null;
        }
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
            originalObservacionTecnica = form.getObservacionTecnica();
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
        if (!validarPublicacionGaceta()) return null;
        if (isObservacionTecnicaOptionDisabled(form.getObservacionTecnica())) {
            addError("Transición de Observación Técnica no permitida. Debe avanzar secuencialmente y guardar cada estado.");
            return null;
        }

        try {
            if (!"Para resolución".equals(form.getObservacionTecnica())) {
                form.setFechaResolucion(null);
            }
            if ("Para resolución".equals(form.getObservacionTecnica()) && form.getFechaResolucion() == null) {
                addError("La fecha de resolución es requerida para el estado 'Para resolución'.");
                return null;
            }
            VegetableForms anterior = new VegetableFormsDAO(null).getVegetableFormsById(form.getId());

            boolean transitioningToAccepted = false;
            if (anterior != null && anterior.getStatus() == Status.EN_PROCESO) {
                boolean esAccionPermitida = form.getObservacionTecnica() != null 
                    && "Aceptada a trámite y revisión de requisitos de fondo".equals(form.getObservacionTecnica().trim());
                if (!esAccionPermitida) {
                    addError("El expediente está en estado EN PROCESO y la edición está bloqueada.");
                    return null;
                } else {
                    form.setStatus(Status.ACCEPTED);
                    transitioningToAccepted = true;
                }
            }

            form.setCreateDate(new Timestamp(System.currentTimeMillis()));
            new VegetableFormsDAO(null).actualizarCamposEditables(form);
            originalObservacionTecnica = form.getObservacionTecnica();

            // Generación automática de anualidades al pasar a "Para resolución" con fecha de resolución
            if ("Para resolución".equals(form.getObservacionTecnica()) && form.getFechaResolucion() != null) {
                try {
                    senadi.gob.ec.adminob.dao.AnualidadDAO aDao = new senadi.gob.ec.adminob.dao.AnualidadDAO(null);
                    if (!aDao.existenAnualidades(form.getId())) {
                        String user = (login != null ? login.getLogin() : "sistema");
                        aDao.generarAnualidadesAutomaticas(form.getId(), form.getFechaResolucion(), user);
                        System.out.println("[RegistroBean] Anualidades generadas automáticamente tras guardar trámite: " + form.getId());
                    }
                } catch (Exception ex) {
                    System.err.println("[RegistroBean] Error al generar anualidades automáticas: " + ex.getMessage());
                }
            }

            if (transitioningToAccepted) {
                try {
                    senadi.gob.ec.adminob.dao.TramiteDAO tDao = new senadi.gob.ec.adminob.dao.TramiteDAO(null);
                    senadi.gob.ec.adminob.model.Tramite t = tDao.buscarPorVegetableFormId(form.getId());
                    if (t != null) {
                        t.setEstadoActual("ACCEPTED");
                        t.setPuedeEditar(true);
                        tDao.actualizarCampos(t);
                    }
                } catch (Exception ex) {
                    System.err.println("[RegistroBean] Error al actualizar tramite a ACEPTADO: " + ex.getMessage());
                }
            }

            StringBuilder desc = new StringBuilder("Registro actualizado");
            if (transitioningToAccepted) {
                desc.append(". Trámite Aceptado automáticamente por cumplimiento de requisitos de fondo");
            }
            if (anterior != null && anterior.getStatusFlow() != form.getStatusFlow()) {
                String estadoAnterior = anterior.getManagementStatus();
                String estadoNuevo = form.getManagementStatus();
                desc.append(". Estado cambiado de '").append(estadoAnterior)
                    .append("' a '").append(estadoNuevo).append("'");
            }
            if (anterior != null) {
                String appNum = form.getApplicationNumber();
                String idEnt = form.getApplicationNumber();
                
                // 1. Observación Técnica
                verificarYRegistrarCambio(appNum, idEnt, "Observación Técnica", 
                    anterior.getObservacionTecnica(), form.getObservacionTecnica(), 
                    "Observación técnica actualizada", "ACTUALIZAR");

                // 2. Fecha de Resolución
                java.util.Date fecAnt = anterior.getFechaResolucion();
                java.util.Date fecNue = form.getFechaResolucion();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                String strFecAnt = fecAnt != null ? sdf.format(fecAnt) : null;
                String strFecNue = fecNue != null ? sdf.format(fecNue) : null;
                verificarYRegistrarCambio(appNum, idEnt, "Fecha de Resolución", 
                    strFecAnt, strFecNue, 
                    "Fecha de resolución actualizada", "ACTUALIZAR");

                // 3. Estado (Status)
                verificarYRegistrarCambio(appNum, idEnt, "Estado del Registro", 
                    getStatusLabel(anterior.getStatus()), getStatusLabel(form.getStatus()), 
                    "Estado del registro actualizado", "CAMBIO_ESTADO");

                // 4. Publicación en Gaceta
                verificarYRegistrarCambio(appNum, idEnt, "Publicación en Gaceta", 
                    anterior.getPublicacionGaceta(), form.getPublicacionGaceta(), 
                    "Publicación en gaceta actualizada", "ACTUALIZAR");

                // 5. Estado de Gestión (StatusFlow)
                StatusFlow sfAnt = anterior.getStatusFlow();
                StatusFlow sfNue = form.getStatusFlow();
                verificarYRegistrarCambio(appNum, idEnt, "Estado de Gestión", 
                    sfAnt != null ? sfAnt.name() : null, sfNue != null ? sfNue.name() : null, 
                    "Estado de gestión actualizado", "CAMBIO_ESTADO");

                // 6. Etapa Actual
                verificarYRegistrarCambio(appNum, idEnt, "Etapa Actual", 
                    anterior.getEtapaActual(), form.getEtapaActual(), 
                    "Etapa actual actualizada", "ACTUALIZAR");

                // 7. Estado de Expediente
                verificarYRegistrarCambio(appNum, idEnt, "Estado de Expediente", 
                    anterior.getEstadoExpediente(), form.getEstadoExpediente(), 
                    "Estado de expediente actualizado", "ACTUALIZAR");

                // 8. Nombre Común
                verificarYRegistrarCambio(appNum, idEnt, "Nombre Común", 
                    anterior.getCommonName(), form.getCommonName(), 
                    "Nombre común actualizado", "ACTUALIZAR");

                // 9. Taxón Botánico
                verificarYRegistrarCambio(appNum, idEnt, "Taxón Botánico", 
                    anterior.getBotanicalTaxon(), form.getBotanicalTaxon(), 
                    "Taxón botánico actualizado", "ACTUALIZAR");

                // 10. Denominación Genérica
                verificarYRegistrarCambio(appNum, idEnt, "Denominación Genérica", 
                    anterior.getGenericDenomination(), form.getGenericDenomination(), 
                    "Denominación genérica actualizada", "ACTUALIZAR");

                // 11. Designación Provisional
                verificarYRegistrarCambio(appNum, idEnt, "Designación Provisional", 
                    anterior.getProvitionalDesignation(), form.getProvitionalDesignation(), 
                    "Designación provisional actualizada", "ACTUALIZAR");
            }

            registrarHistorial(form, desc.toString());

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
        if (form != null && form.getStatus() == Status.EN_PROCESO) {
            addError("No se pueden modificar documentos cuando el trámite está EN PROCESO.");
            return;
        }
        try {
            UploadedFile file = event.getFile();
            if (file == null) return;

            if (guardarArchivo(file, nombrePersonalizado)) {
                archivosSubidos = new ComprobantePagoDAO(null).getTodosLosArchivosPorVegetableFormId(form.getId());
                addInfo("ARCHIVO SUBIDO: " + (nombrePersonalizado != null && !nombrePersonalizado.trim().isEmpty()
                        ? nombrePersonalizado.trim() : file.getFileName()));
                nombrePersonalizado = null;
            }
        } catch (Exception e) {
            addError("ERROR AL SUBIR ARCHIVO: " + e.getMessage());
        }
    }

    public void subirTodosLosArchivos() {
        if (form != null && form.getStatus() == Status.EN_PROCESO) {
            addError("No se pueden modificar documentos cuando el trámite está EN PROCESO.");
            return;
        }
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
                archivosSubidos = new ComprobantePagoDAO(null).getTodosLosArchivosPorVegetableFormId(form.getId());
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

    private void verificarYRegistrarCambio(String appNum, String idEnt, String campo, String valAnt, String valNue, String desc, String accion) {
        if ((valAnt != null && !valAnt.equals(valNue)) || (valAnt == null && valNue != null)) {
            try {
                String user = (login != null ? login.getLogin() : "sistema");
                new senadi.gob.ec.adminob.dao.HistoryDAO(null).registrar(
                    appNum,
                    "Solicitud",
                    idEnt,
                    accion != null ? accion : "ACTUALIZAR",
                    campo,
                    valAnt != null ? valAnt : "Ninguno",
                    valNue != null ? valNue : "Ninguno",
                    desc,
                    user
                );
            } catch (Exception ex) {
                System.err.println("[RegistroBean] Error al registrar cambio en " + campo + ": " + ex.getMessage());
            }
        }
    }

    public String getStatusLabel(Status s) {
        if (s == null) return "—";
        switch (s) {
            case SAVED: return "Guardado";
            case ACCEPTED: return "Aceptado";
            case EN_PROCESO: return "En Proceso";
            case PREVIEW: return "Vista Previa";
            case FINISHED: return "Finalizado";
            default: return s.name();
        }
    }

    public void eliminarArchivo(ComprobantePago archivo) {
        if (form != null && form.getStatus() == Status.EN_PROCESO) {
            addError("No se pueden modificar documentos cuando el trámite está EN PROCESO.");
            return;
        }
        if (archivo != null && archivo.getId() < 0) {
            addError("No se pueden eliminar los documentos cargados en el portal público ni del trámite original.");
            return;
        }
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
            if (archivo.getId() <= -1000000 && archivo.getId() >= -1999999) {
                // Archivo histórico en disco - ya fue eliminado físicamente arriba, no requiere borrado en base de datos
            } else if (archivo.getId() <= -1000000000) {
                // VegetableAnnexesData (Anexo del inicio)
                int temp = -1000000000 - archivo.getId();
                int annexId = temp % 100;
                int formId = temp / 100;
                javax.persistence.EntityManager em = null;
                try {
                    em = new ComprobantePagoDAO(null).getEntityManager();
                    em.getTransaction().begin();
                    javax.persistence.Query q = em.createQuery(
                        "DELETE FROM VegetableAnnexesData d WHERE d.id.vegetableFormId = :fid AND d.id.vegetableAnnexesId = :aid");
                    q.setParameter("fid", formId);
                    q.setParameter("aid", annexId);
                    q.executeUpdate();
                    em.getTransaction().commit();
                } catch (Exception e) {
                    if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
                    throw e;
                } finally {
                    if (em != null) em.close();
                }
            } else if (archivo.getId() < 0) {
                // Documento de trámite
                int realDocId = -archivo.getId();
                new senadi.gob.ec.adminob.dao.DocumentoTramiteDAO(null).deleteById(realDocId);
            } else {
                // Comprobante de pago normal
                new ComprobantePagoDAO(null).delete(archivo.getId());
            }

            // RECARGAR TABLA
            archivosSubidos = new ComprobantePagoDAO(null)
                    .getTodosLosArchivosPorVegetableFormId(form.getId());

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

    private boolean validarPublicacionGaceta() {
        if (form != null && form.getPublicacionGaceta() != null && !form.getPublicacionGaceta().trim().isEmpty()) {
            if (!form.getPublicacionGaceta().trim().matches("^[0-9]+$")) {
                addError("La publicación en gaceta debe contener únicamente dígitos numéricos (se rechazan letras, símbolos y espacios).");
                return false;
            }
        }
        return true;
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

    public boolean isObservacionTecnicaOptionDisabled(String optionValue) {
        String original = originalObservacionTecnica;
        
        if (original != null && original.trim().isEmpty()) {
            original = null;
        }
        String normalizedOption = optionValue;
        if (normalizedOption != null && normalizedOption.trim().isEmpty()) {
            normalizedOption = null;
        }
        
        if (original == null && normalizedOption == null) {
            return false;
        }
        if (original != null && original.equals(normalizedOption)) {
            return false;
        }
        
        int originalIdx = -1;
        if (original != null) {
            originalIdx = OBSERVACIONES_ORDENADAS.indexOf(original);
        }
        
        int optionIdx = -1;
        if (normalizedOption != null) {
            optionIdx = OBSERVACIONES_ORDENADAS.indexOf(normalizedOption);
        }
        
        if (optionIdx == originalIdx + 1) {
            return false;
        }
        
        return true;
    }
}