package senadi.gob.ec.adminob.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import senadi.gob.ec.adminob.enums.EstadoAnualidad;
import senadi.gob.ec.adminob.model.Anualidad;
import senadi.gob.ec.adminob.model.VegetableForms;

public class AnualidadDAO extends DAOAbstractM<Anualidad> {

    public AnualidadDAO(Anualidad t) {
        super(t);
    }

    @Override
    public List<Anualidad> buscarTodos() {
        EntityManager em = EntityManagerM.getEntityManager();
        try {
            return em.createQuery(
                "SELECT a FROM Anualidad a ORDER BY a.vegetableFormId, a.anio",
                Anualidad.class).getResultList();
        } finally {
            em.close();
        }
    }

    public boolean existenAnualidades(Integer vegetableFormId) {
        EntityManager em = EntityManagerM.getEntityManager();
        try {
            Long count = em.createQuery(
                "SELECT COUNT(a) FROM Anualidad a WHERE a.vegetableFormId = :vid",
                Long.class)
                .setParameter("vid", vegetableFormId)
                .getSingleResult();
            return count != null && count > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Crea registros vacíos de anualidad para un trámite.
     *
     * @param montoBase              Monto de la anualidad (año 1). Si es null no se asigna.
     * @param porcentajeRecargo      % de recargo en período de gracia (ej: 10.00). Si null usa 10%.
     * @param porcentajeIncremento   % de incremento cada 5 años. Si null no aplica.
     */
    public void crearAnualidadesVacias(Integer vegetableFormId, String usuarioRegistro, int cantidad,
                                        BigDecimal montoBase, BigDecimal porcentajeRecargo,
                                        BigDecimal porcentajeIncremento) throws Exception {
        if (existenAnualidades(vegetableFormId)) return;
        if (cantidad < 1) cantidad = 1;
        if (cantidad > 50) cantidad = 50;
        BigDecimal recargo = (porcentajeRecargo != null) ? porcentajeRecargo : new BigDecimal("10.00");
        EntityManager em = EntityManagerM.getEntityManager();
        em.getTransaction().begin();
        try {
            Timestamp ahora = new Timestamp(System.currentTimeMillis());
            for (int i = 1; i <= cantidad; i++) {
                Anualidad a = new Anualidad();
                a.setVegetableFormId(vegetableFormId);
                a.setAnio(i);
                a.setEtiqueta("Año" + i);
                a.setEstado(EstadoAnualidad.PENDIENTE);
                a.setPorcentajeRecargo(recargo);
                a.setUsuarioRegistro(usuarioRegistro);
                a.setFechaCreacion(ahora);
                a.setFechaModificacion(ahora);
                if (montoBase != null) {
                    BigDecimal montoAnio = calcularMontoConIncremento(montoBase, i, porcentajeIncremento);
                    int grupo = (i - 1) / 5;
                    BigDecimal incAplicado = (grupo > 0 && porcentajeIncremento != null)
                            ? porcentajeIncremento : BigDecimal.ZERO;
                    a.setMontoOriginal(montoAnio);
                    a.setIncrementoAplicado(incAplicado);
                }
                em.persist(a);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Sobrecarga de compatibilidad sin parámetros de monto. */
    public void crearAnualidadesVacias(Integer vegetableFormId, String usuarioRegistro, int cantidad) throws Exception {
        crearAnualidadesVacias(vegetableFormId, usuarioRegistro, cantidad, null, null, null);
    }

    public void actualizarAnualidad(Anualidad src) throws Exception {
        EntityManager em = EntityManagerM.getEntityManager();
        em.getTransaction().begin();
        try {
            Anualidad m = em.find(Anualidad.class, src.getId());
            if (m == null) throw new Exception("Anualidad no encontrada id=" + src.getId());
            m.setFechaVencimiento(src.getFechaVencimiento());
            m.setFechaPago(src.getFechaPago());
            m.setMonto(src.getMonto());
            m.setNumeroComprobante(src.getNumeroComprobante());
            m.setValorPagadoRecargo(src.getValorPagadoRecargo());
            m.setNumeroComprobanteRecargo(src.getNumeroComprobanteRecargo());
            m.setEstado(src.getEstado());
            m.setObservaciones(src.getObservaciones());
            // Nuevos campos de control
            m.setMontoOriginal(src.getMontoOriginal());
            m.setPorcentajeRecargo(src.getPorcentajeRecargo());
            m.setIncrementoAplicado(src.getIncrementoAplicado());
            // Recalcular fechas límite si cambió la fecha de vencimiento
            if (src.getFechaVencimiento() != null) {
                m.setFechaLimitePago(calcularFechaLimitePago(src.getFechaVencimiento()));
                m.setFechaLimiteGracia(calcularFechaLimiteGracia(src.getFechaVencimiento()));
            } else {
                m.setFechaLimitePago(src.getFechaLimitePago());
                m.setFechaLimiteGracia(src.getFechaLimiteGracia());
            }
            m.setFechaModificacion(new Timestamp(System.currentTimeMillis()));
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // ── helpers para leer parámetros de BD con fallback ──

    private static int paramEntero(String clave, int defecto) {
        try { return new ParametroSistemaDAO(null).getValorEntero(clave, defecto); }
        catch (Exception e) { return defecto; }
    }

    // ── fecha_vencimiento + N meses (versión parametrizada) ──

    /**
     * Calcula el límite de pago normal a partir de la fecha de vencimiento.
     * Los meses se leen de la tabla parametros_sistema (clave MESES_PLAZO_PAGO).
     * Fallback: 4 meses (valor original hardcodeado).
     */
    public static java.sql.Date calcularFechaLimitePago(java.sql.Date fechaVencimiento) {
        return calcularFechaLimitePago(fechaVencimiento, paramEntero("MESES_PLAZO_PAGO", 4));
    }

    /** Sobrecarga explícita: usa los meses indicados sin consultar la BD. */
    public static java.sql.Date calcularFechaLimitePago(java.sql.Date fechaVencimiento, int mesesPlazo) {
        if (fechaVencimiento == null) return null;
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(fechaVencimiento);
        cal.add(java.util.Calendar.MONTH, mesesPlazo);
        return new java.sql.Date(cal.getTimeInMillis());
    }

    /**
     * Calcula el límite del período de gracia.
     * Total de meses = MESES_PLAZO_PAGO + MESES_PERIODO_GRACIA (leídos de BD).
     * Fallback: 4 + 6 = 10 meses (valor original).
     */
    public static java.sql.Date calcularFechaLimiteGracia(java.sql.Date fechaVencimiento) {
        int plazo  = paramEntero("MESES_PLAZO_PAGO",      4);
        int gracia = paramEntero("MESES_PERIODO_GRACIA",  6);
        return calcularFechaLimiteGracia(fechaVencimiento, plazo + gracia);
    }

    /** Sobrecarga explícita: usa el total de meses indicado sin consultar la BD. */
    public static java.sql.Date calcularFechaLimiteGracia(java.sql.Date fechaVencimiento, int mesesTotal) {
        if (fechaVencimiento == null) return null;
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(fechaVencimiento);
        cal.add(java.util.Calendar.MONTH, mesesTotal);
        return new java.sql.Date(cal.getTimeInMillis());
    }

    /**
     * Calcula el monto original con el incremento quinquenal.
     * Cada 5 años el monto base sube según porcentajeIncremento5Anios.
     * Grupo 1=años 1-5 (sin incremento), grupo 2=años 6-10, etc.
     */
    public static BigDecimal calcularMontoConIncremento(BigDecimal montoBase, int anio,
                                                         BigDecimal porcentajeIncremento) {
        if (montoBase == null) return BigDecimal.ZERO;
        if (porcentajeIncremento == null || porcentajeIncremento.compareTo(BigDecimal.ZERO) == 0)
            return montoBase;
        int grupo = (anio - 1) / 5; // 0 para años 1-5, 1 para 6-10, etc.
        if (grupo == 0) return montoBase;
        BigDecimal factor = BigDecimal.ONE.add(
            porcentajeIncremento.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
        BigDecimal resultado = montoBase;
        for (int i = 0; i < grupo; i++) {
            resultado = resultado.multiply(factor);
        }
        return resultado.setScale(2, RoundingMode.HALF_UP);
    }

    public List<Anualidad> buscarConAlerta(int diasAnticipacion) {
        EntityManager em = EntityManagerM.getEntityManager();
        try {
            java.sql.Date hoy = new java.sql.Date(System.currentTimeMillis());
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DAY_OF_MONTH, diasAnticipacion);
            java.sql.Date limite = new java.sql.Date(cal.getTimeInMillis());
            return em.createQuery(
                "SELECT a FROM Anualidad a " +
                "WHERE a.fechaVencimiento IS NOT NULL " +
                "AND a.fechaVencimiento BETWEEN :hoy AND :limite " +
                "AND a.estado <> :pagado " +
                "ORDER BY a.fechaVencimiento, a.vegetableFormId",
                Anualidad.class)
                .setParameter("hoy",    hoy)
                .setParameter("limite", limite)
                .setParameter("pagado", EstadoAnualidad.PAGADO)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Anualidad> buscarVencidas() {
        EntityManager em = EntityManagerM.getEntityManager();
        try {
            java.sql.Date hoy = new java.sql.Date(System.currentTimeMillis());
            return em.createQuery(
                "SELECT a FROM Anualidad a " +
                "WHERE a.fechaVencimiento IS NOT NULL " +
                "AND a.fechaVencimiento < :hoy " +
                "AND a.estado <> :pagado " +
                "ORDER BY a.fechaVencimiento DESC, a.vegetableFormId",
                Anualidad.class)
                .setParameter("hoy",    hoy)
                .setParameter("pagado", EstadoAnualidad.PAGADO)
                .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Al registrar un pago, asigna al año siguiente:
     * - fechaVencimiento
     * - fechaLimitePago  (fechaVencimiento + 4 meses)
     * - fechaLimiteGracia (fechaVencimiento + 10 meses)
     */
    public void actualizarFechaVencimientoSiguiente(Integer vegetableFormId, Integer anioActual,
                                                     java.sql.Date fechaVencimiento) throws Exception {
        EntityManager em = EntityManagerM.getEntityManager();
        em.getTransaction().begin();
        try {
            java.sql.Date limitePago   = calcularFechaLimitePago(fechaVencimiento);
            java.sql.Date limiteGracia = calcularFechaLimiteGracia(fechaVencimiento);
            em.createQuery(
                "UPDATE Anualidad a SET " +
                "a.fechaVencimiento  = :fecha, " +
                "a.fechaLimitePago   = :limitePago, " +
                "a.fechaLimiteGracia = :limiteGracia, " +
                "a.estado            = :pendiente, " +
                "a.fechaModificacion = :ahora " +
                "WHERE a.vegetableFormId = :vid AND a.anio = :anio")
                .setParameter("fecha",        fechaVencimiento)
                .setParameter("limitePago",   limitePago)
                .setParameter("limiteGracia", limiteGracia)
                .setParameter("pendiente",    EstadoAnualidad.PENDIENTE)
                .setParameter("ahora",        new java.sql.Timestamp(System.currentTimeMillis()))
                .setParameter("vid",          vegetableFormId)
                .setParameter("anio",         anioActual + 1)
                .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Transición automática de estados según las fechas límite:
     * 1. PENDIENTE/EN_GRACIA → PROCESO_EXPIRADO  (si pasó fechaLimiteGracia)
     * 2. PENDIENTE            → EN_GRACIA         (si pasó fechaLimitePago pero no fechaLimiteGracia)
     * 3. PENDIENTE            → VENCIDO (legacy)  (registros sin fechaLimitePago definida)
     * Retorna el total de filas actualizadas.
     */
    public int marcarEstadosAutomaticos() throws Exception {
        EntityManager em = EntityManagerM.getEntityManager();
        em.getTransaction().begin();
        try {
            java.sql.Date hoy   = new java.sql.Date(System.currentTimeMillis());
            java.sql.Timestamp ahora = new java.sql.Timestamp(System.currentTimeMillis());
            int total = 0;

            // 1. Expirar si pasó el límite de gracia
            total += em.createQuery(
                "UPDATE Anualidad a SET a.estado = :exp, a.fechaModificacion = :ahora " +
                "WHERE a.fechaLimiteGracia IS NOT NULL " +
                "AND a.fechaLimiteGracia < :hoy " +
                "AND (a.estado = :pend OR a.estado = :gracia OR a.estado = :venc)")
                .setParameter("exp",    EstadoAnualidad.PROCESO_EXPIRADO)
                .setParameter("ahora",  ahora)
                .setParameter("hoy",    hoy)
                .setParameter("pend",   EstadoAnualidad.PENDIENTE)
                .setParameter("gracia", EstadoAnualidad.EN_GRACIA)
                .setParameter("venc",   EstadoAnualidad.VENCIDO)
                .executeUpdate();

            // 2. Pasar a período de gracia si venció el plazo normal
            total += em.createQuery(
                "UPDATE Anualidad a SET a.estado = :gracia, a.fechaModificacion = :ahora " +
                "WHERE a.fechaLimitePago IS NOT NULL " +
                "AND a.fechaLimitePago < :hoy " +
                "AND (a.fechaLimiteGracia IS NULL OR a.fechaLimiteGracia >= :hoy) " +
                "AND a.estado = :pend")
                .setParameter("gracia", EstadoAnualidad.EN_GRACIA)
                .setParameter("ahora",  ahora)
                .setParameter("hoy",    hoy)
                .setParameter("pend",   EstadoAnualidad.PENDIENTE)
                .executeUpdate();

            // 3. VENCIDO para registros sin fechaLimitePago (datos históricos/legado)
            total += em.createQuery(
                "UPDATE Anualidad a SET a.estado = :venc, a.fechaModificacion = :ahora " +
                "WHERE a.fechaVencimiento IS NOT NULL " +
                "AND a.fechaLimitePago IS NULL " +
                "AND a.fechaVencimiento < :hoy " +
                "AND a.estado = :pend")
                .setParameter("venc",  EstadoAnualidad.VENCIDO)
                .setParameter("ahora", ahora)
                .setParameter("hoy",   hoy)
                .setParameter("pend",  EstadoAnualidad.PENDIENTE)
                .executeUpdate();

            em.getTransaction().commit();
            return total;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Alias de compatibilidad — delega al nuevo método. */
    public int marcarVencidas() throws Exception {
        return marcarEstadosAutomaticos();
    }

    public List<Integer> idsConAnualidades() {
        EntityManager em = EntityManagerM.getEntityManager();
        try {
            return em.createQuery(
                "SELECT DISTINCT a.vegetableFormId FROM Anualidad a",
                Integer.class).getResultList();
        } finally {
            em.close();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Importación masiva desde Excel (Apache POI)
    // ══════════════════════════════════════════════════════════════════════

    public static class ImportResult {
        public final int insertados;
        public final int actualizados;
        public final int errores;
        public final List<String> mensajes;

        public ImportResult(int ins, int act, int err, List<String> msg) {
            this.insertados  = ins;
            this.actualizados = act;
            this.errores     = err;
            this.mensajes    = msg;
        }
    }

    /**
     * Importa anualidades desde el Excel "BASE DE DATOS..." de SENADI.
     *
     * Formato (hoja "Base de Datos"):
     *   Fila 1: grupos AÑO 1 (col 44), AÑO 2 (col 49) ... AÑO 20 (col 139)
     *   Fila 2: subcolumnas — col 3: Trámite Nro.; por año: +0 fecha_pago,
     *           +1 valor, +2 comprobante, +3 recargo, +4 comp_recargo
     *   Filas 3+: datos
     *
     * Solo crea/actualiza años que tengan fecha de pago o comprobante.
     * Estado: PAGADO si hay fecha_pago o comprobante; PENDIENTE si no.
     * Vencimiento de año N = fecha_pago del año N-1 + 1 año.
     */
    public ImportResult importarDesdeExcel(java.io.InputStream is, String usuario) throws Exception {
        int insertados = 0, actualizados = 0, errores = 0;
        List<String> mensajes = new ArrayList<>();

        final int COL_TRAMITE   = 2;   // "Tramite Nro." (0-based)
        final int COL_CONCESION = 36;  // "Fecha de Concesion del Derecho" (0-based)
        final int COL_ANO_BASE  = 43;  // AÑO 1 empieza en col 44 (1-based) = 43 (0-based)
        final int COLS_POR_ANO  = 5;
        final int MAX_ANOS      = 20;

        try (Workbook wb = WorkbookFactory.create(is)) {
            Sheet sheet = null;
            for (int si = 0; si < wb.getNumberOfSheets(); si++) {
                String nombre = wb.getSheetName(si).toLowerCase().trim();
                if (nombre.contains("base") && nombre.contains("dato")) {
                    sheet = wb.getSheetAt(si);
                    break;
                }
            }
            if (sheet == null) sheet = wb.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 2) {
                throw new Exception("El archivo no tiene datos (se esperan 2 filas de encabezado + datos).");
            }

            // Pre-cargar todos los VegetableForms en un Map ANTES de abrir la transacción
            // principal, para evitar que los EM internos del DAO interfieran con el EM principal.
            Map<String, Integer> mapaTramites = new HashMap<>();
            for (VegetableForms vf : new VegetableFormsDAO(null).buscarTodos()) {
                if (vf.getApplicationNumber() != null && !vf.getApplicationNumber().trim().isEmpty()) {
                    mapaTramites.put(vf.getApplicationNumber().trim(), vf.getId());
                }
                if (vf.getNumeroDeTramite() != null && !vf.getNumeroDeTramite().trim().isEmpty()) {
                    mapaTramites.put(vf.getNumeroDeTramite().trim(), vf.getId());
                }
            }

            Timestamp ahora = new Timestamp(System.currentTimeMillis());

            for (int rowIdx = 2; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (isFilaVacia(row)) continue;

                String numeroTramite = excelString(row, COL_TRAMITE);
                if (numeroTramite == null || numeroTramite.isEmpty()) continue;

                Integer vegFormId = mapaTramites.get(numeroTramite.trim());
                if (vegFormId == null) {
                    mensajes.add("Fila " + (rowIdx + 1) + " [" + numeroTramite + "]: no encontrado en la BD");
                    errores++;
                    continue;
                }

                java.sql.Date fechaConcesion = excelDate(row, COL_CONCESION);

                java.sql.Date[] fechasPago = new java.sql.Date[MAX_ANOS + 1];
                for (int a = 1; a <= MAX_ANOS; a++) {
                    fechasPago[a] = excelDate(row, COL_ANO_BASE + (a - 1) * COLS_POR_ANO);
                }

                // Un EntityManager por año: un año malo no bloquea los demás del mismo trámite
                for (int anio = 1; anio <= MAX_ANOS; anio++) {
                    int base = COL_ANO_BASE + (anio - 1) * COLS_POR_ANO;

                    java.sql.Date fechaPago  = fechasPago[anio];
                    BigDecimal   monto       = excelDecimal(row, base + 1);
                    String       comprobante = truncate(excelString(row, base + 2), 150);
                    BigDecimal   recargo     = excelDecimal(row, base + 3);
                    String       compRec     = truncate(excelString(row, base + 4), 150);

                    boolean tieneDatos = fechaPago != null
                        || (comprobante != null && !comprobante.isEmpty())
                        || monto != null;
                    if (!tieneDatos) continue;

                    EstadoAnualidad estado = (fechaPago != null
                        || (comprobante != null && !comprobante.isEmpty()))
                        ? EstadoAnualidad.PAGADO : EstadoAnualidad.PENDIENTE;

                    java.sql.Date fechaVenc = null;
                    if (anio == 1 && fechaConcesion != null) {
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTime(fechaConcesion);
                        cal.add(java.util.Calendar.YEAR, 1);
                        fechaVenc = new java.sql.Date(cal.getTimeInMillis());
                    } else if (anio > 1 && fechasPago[anio - 1] != null) {
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTime(fechasPago[anio - 1]);
                        cal.add(java.util.Calendar.YEAR, 1);
                        fechaVenc = new java.sql.Date(cal.getTimeInMillis());
                    }

                    EntityManager em = EntityManagerM.getEntityManager();
                    em.getTransaction().begin();
                    try {
                        List<Anualidad> existentes = em.createQuery(
                            "SELECT a FROM Anualidad a " +
                            "WHERE a.vegetableFormId = :vid AND a.anio = :anio",
                            Anualidad.class)
                            .setParameter("vid", vegFormId)
                            .setParameter("anio", anio)
                            .getResultList();

                        Anualidad anu;
                        boolean esNueva;
                        if (existentes.isEmpty()) {
                            anu = new Anualidad();
                            anu.setVegetableFormId(vegFormId);
                            anu.setAnio(anio);
                            anu.setEtiqueta("Año" + anio);
                            anu.setFechaCreacion(ahora);
                            anu.setUsuarioRegistro(usuario);
                            esNueva = true;
                        } else {
                            anu = existentes.get(0);
                            esNueva = false;
                        }

                        anu.setFechaVencimiento(fechaVenc);
                        anu.setFechaPago(fechaPago);
                        anu.setMonto(monto);
                        anu.setNumeroComprobante(comprobante);
                        anu.setValorPagadoRecargo(recargo);
                        anu.setNumeroComprobanteRecargo(compRec);
                        anu.setEstado(estado);
                        anu.setFechaModificacion(ahora);
                        // Calcular fechas límite si hay fecha de vencimiento
                        if (fechaVenc != null && anu.getPorcentajeRecargo() == null) {
                            anu.setPorcentajeRecargo(new BigDecimal("10.00"));
                        }
                        if (fechaVenc != null) {
                            anu.setFechaLimitePago(calcularFechaLimitePago(fechaVenc));
                            anu.setFechaLimiteGracia(calcularFechaLimiteGracia(fechaVenc));
                        }
                        if (monto != null && anu.getMontoOriginal() == null) {
                            anu.setMontoOriginal(monto);
                        }

                        if (esNueva) { em.persist(anu); insertados++; }
                        else         { actualizados++; }

                        em.getTransaction().commit();
                    } catch (Exception yearEx) {
                        if (em.getTransaction().isActive()) em.getTransaction().rollback();
                        Throwable causa = yearEx;
                        while (causa.getCause() != null) causa = causa.getCause();
                        mensajes.add("Fila " + (rowIdx + 1) + " [" + numeroTramite
                            + "] año " + anio + ": " + causa.getMessage());
                        errores++;
                    } finally {
                        em.close();
                    }
                }
            }
        }

        return new ImportResult(insertados, actualizados, errores, mensajes);
    }

    // -- Helpers de deteccion de columnas --

    private int resolveCol(Map<String, Integer> cols, String... names) {
        for (String name : names) {
            Integer idx = cols.get(name);
            if (idx != null) return idx;
        }
        return -1;
    }

    private String normalizarNombreCol(String raw) {
        if (raw == null) return "";
        return raw.trim().toLowerCase()
            .replace(" ", "_")
            .replace("á","a").replace("é","e").replace("í","i")
            .replace("ó","o").replace("ú","u").replace("ñ","n")
            .replace("°","").replace("n°","num_").replace("nº","num_");
    }

    // ── Helpers de lectura de celdas Excel ──

    private String truncate(String s, int maxLen) {
        if (s == null) return null;
        String t = s.trim();
        return t.length() > maxLen ? t.substring(0, maxLen) : t;
    }

    private boolean isFilaVacia(Row row) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String v = excelCellString(cell);
                if (v != null && !v.isEmpty()) return false;
            }
        }
        return true;
    }

    private String excelString(Row row, int colIdx) {
        if (row == null || colIdx < 0) return null;
        Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return excelCellString(cell);
    }

    private String excelCellString(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    java.util.Date d = cell.getDateCellValue();
                    return d != null ? new SimpleDateFormat("dd/MM/yyyy").format(d) : null;
                }
                double v = cell.getNumericCellValue();
                return (v == Math.floor(v) && !Double.isInfinite(v))
                    ? String.valueOf((long) v) : String.valueOf(v);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try { return cell.getStringCellValue().trim(); }
                catch (Exception e) {
                    try {
                        double fv = cell.getNumericCellValue();
                        return (fv == Math.floor(fv)) ? String.valueOf((long) fv) : String.valueOf(fv);
                    } catch (Exception e2) { return null; }
                }
            default:
                return null;
        }
    }

    private Integer excelInt(Row row, int colIdx) {
        String s = excelString(row, colIdx);
        if (s == null || s.isEmpty()) return null;
        try { return (int) Double.parseDouble(s.replace(",", ".")); }
        catch (NumberFormatException e) { return null; }
    }

    private java.sql.Date excelDate(Row row, int colIdx) {
        if (row == null || colIdx < 0) return null;
        Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        try {
            java.sql.Date result = null;
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                java.util.Date d = cell.getDateCellValue();
                if (d != null) result = new java.sql.Date(d.getTime());
            } else {
                String s = excelCellString(cell);
                if (s == null || s.isEmpty()) return null;
                for (String pat : new String[]{"dd/MM/yyyy","yyyy-MM-dd","dd-MM-yyyy","dd/MM/yy","M/d/yyyy"}) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat(pat);
                        sdf.setLenient(false);
                        java.util.Date d = sdf.parse(s);
                        result = new java.sql.Date(d.getTime());
                        break;
                    } catch (Exception ignored) {}
                }
            }
            // MySQL DATE soporta años 1000-9999; descartar fechas fuera de rango
            if (result != null) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(result);
                int year = cal.get(java.util.Calendar.YEAR);
                if (year < 1000 || year > 9999) return null;
            }
            return result;
        } catch (Exception e) { return null; }
    }

    private BigDecimal excelDecimal(Row row, int colIdx) {
        String s = excelString(row, colIdx);
        if (s == null || s.isEmpty()) return null;
        try {
            return new BigDecimal(s.replace(",", ".")).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) { return null; }
    }
}
