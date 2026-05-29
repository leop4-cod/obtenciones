package senadi.gob.ec.adminob.integration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import senadi.gob.ec.adminob.dao.ParametroSistemaDAO;
import senadi.gob.ec.adminob.integration.dto.ExternalVarietyDataDTO;
import senadi.gob.ec.adminob.integration.dto.TechnicalInfoDTO;
import senadi.gob.ec.adminob.integration.dto.TerritoryDataDTO;

/**
 * Cliente HTTP para consultar el aplicativo externo de variedades vegetales.
 *
 * El aplicativo externo está aún en desarrollo; este cliente está diseñado
 * para fallar de forma silenciosa (retorna null) cuando el servicio no está
 * disponible, sin bloquear la operación del sistema actual.
 *
 * Configuración (tabla parametros_sistema):
 *   URL_APP_EXTERNA  → URL base del API externo (ej: http://192.168.1.50:8081/api)
 *   TOKEN_APP_EXTERNA → Bearer token de autenticación (puede estar vacío)
 *
 * Endpoint esperado:
 *   GET {URL_BASE}/variedades/{applicationNumber}
 *   → Retorna JSON con estructura de ExternalVarietyDataDTO
 *
 * NOTA: Se usa HttpURLConnection (Java SE) para evitar dependencias extra.
 * Cuando el API externo finalice su especificación, puede reemplazarse por
 * JAX-RS Client (javax.ws.rs.client.ClientBuilder) disponible en WildFly 19.
 */
public class ExternalAppRestClient {

    private static final Logger LOG = Logger.getLogger(ExternalAppRestClient.class.getName());
    private static final int TIMEOUT_MS = 5000;

    /**
     * Consulta los datos de la variedad para el número de solicitud dado.
     *
     * @param applicationNumber Número de solicitud del portal (applicationNumber de VegetableForms)
     * @return DTO con los datos, o {@code null} si el servicio no está disponible o falla.
     */
    public ExternalVarietyDataDTO obtenerDatos(String applicationNumber) {
        if (applicationNumber == null || applicationNumber.trim().isEmpty()) return null;
        String baseUrl = cargarParametro("URL_APP_EXTERNA", "");
        if (baseUrl.isEmpty()) {
            LOG.info("[ExternalAppRestClient] URL_APP_EXTERNA no configurada, omitiendo consulta.");
            return null;
        }
        String endpoint = baseUrl.replaceAll("/+$", "")
            + "/variedades/" + applicationNumber.trim();
        String token = cargarParametro("TOKEN_APP_EXTERNA", "");
        try {
            String json = get(endpoint, token);
            if (json == null || json.trim().isEmpty()) return null;
            return parsearJson(json, applicationNumber);
        } catch (Exception e) {
            LOG.log(Level.WARNING,
                "[ExternalAppRestClient] No se pudo consultar el aplicativo externo: {0}",
                e.getMessage());
            return null;
        }
    }

    // ── HTTP GET ─────────────────────────────────────────────────────────────

    private String get(String endpoint, String token) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        if (token != null && !token.trim().isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token.trim());
        }
        int status = conn.getResponseCode();
        if (status == 404) return null;
        if (status != 200) {
            throw new Exception("HTTP " + status + " al consultar: " + endpoint);
        }
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }

    // ── Parseo JSON manual (sin librerías externas) ───────────────────────────
    //
    // NOTA: Este parseo manual es intencionalmente simple para no requerir
    // Jackson/Gson como dependencia nueva. Cuando el API externo finalice,
    // se recomienda migrar a Jackson (ya incluido en WildFly 19) usando:
    //   new com.fasterxml.jackson.databind.ObjectMapper()
    //       .readValue(json, ExternalVarietyDataDTO.class);
    //
    // El contrato JSON esperado es:
    // {
    //   "applicationNumber": "SOL-2025-001",
    //   "enTerritorio": {
    //     "pais": "Ecuador", "entidad": "INIAP", "periodo": "2023-2024",
    //     "descripcion": "...", "resultado": "FAVORABLE"
    //   },
    //   "fueraTerritorio": { ... },
    //   "informacionTecnica": {
    //     "taxon": "Solanum tuberosum L.",
    //     "nombreComun": "Papa",
    //     "mecanismoReproducccion": "Vegetativa",
    //     "origenGeografico": "Andes del Ecuador",
    //     "genealogia": "...",
    //     "metodologia": "UPOV TG/23/6",
    //     "observaciones": "..."
    //   }
    // }

    private ExternalVarietyDataDTO parsearJson(String json, String appNumber) {
        ExternalVarietyDataDTO dto = new ExternalVarietyDataDTO();
        dto.setApplicationNumber(appNumber);

        String enTer  = extraerBloque(json, "enTerritorio");
        String fueraTer = extraerBloque(json, "fueraTerritorio");
        String tecnica  = extraerBloque(json, "informacionTecnica");

        if (enTer  != null) dto.setEnTerritorio(parsearTerritory(enTer));
        if (fueraTer != null) dto.setFueraTerritorio(parsearTerritory(fueraTer));
        if (tecnica  != null) dto.setInformacionTecnica(parsearTechnical(tecnica));

        return dto.tieneDatos() ? dto : null;
    }

    private TerritoryDataDTO parsearTerritory(String bloque) {
        TerritoryDataDTO t = new TerritoryDataDTO();
        t.setPais(extraerCadena(bloque, "pais"));
        t.setEntidad(extraerCadena(bloque, "entidad"));
        t.setPeriodo(extraerCadena(bloque, "periodo"));
        t.setDescripcion(extraerCadena(bloque, "descripcion"));
        t.setResultado(extraerCadena(bloque, "resultado"));
        return t;
    }

    private TechnicalInfoDTO parsearTechnical(String bloque) {
        TechnicalInfoDTO t = new TechnicalInfoDTO();
        t.setTaxon(extraerCadena(bloque, "taxon"));
        t.setNombreComun(extraerCadena(bloque, "nombreComun"));
        t.setMecanismoReproducccion(extraerCadena(bloque, "mecanismoReproducccion"));
        t.setOrigenGeografico(extraerCadena(bloque, "origenGeografico"));
        t.setGenealogy(extraerCadena(bloque, "genealogia"));
        t.setMetodologia(extraerCadena(bloque, "metodologia"));
        t.setObservaciones(extraerCadena(bloque, "observaciones"));
        return t;
    }

    /** Extrae el primer valor String de una clave JSON (parseo muy simple). */
    private String extraerCadena(String json, String clave) {
        String patron = "\"" + clave + "\"";
        int idx = json.indexOf(patron);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx + patron.length());
        if (colon < 0) return null;
        int inicio = json.indexOf('"', colon + 1);
        if (inicio < 0) return null;
        int fin = json.indexOf('"', inicio + 1);
        if (fin < 0) return null;
        String val = json.substring(inicio + 1, fin).trim();
        return val.isEmpty() ? null : val;
    }

    /** Extrae el contenido de un bloque objeto JSON para una clave dada. */
    private String extraerBloque(String json, String clave) {
        String patron = "\"" + clave + "\"";
        int idx = json.indexOf(patron);
        if (idx < 0) return null;
        int braceOpen = json.indexOf('{', idx + patron.length());
        if (braceOpen < 0) return null;
        int depth = 0;
        for (int i = braceOpen; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return json.substring(braceOpen, i + 1);
            }
        }
        return null;
    }

    private String cargarParametro(String clave, String defecto) {
        try { return new ParametroSistemaDAO(null).getValor(clave, defecto); }
        catch (Exception e) { return defecto; }
    }
}
