package senadi.gob.ec.adminob.integration.dto;

import java.io.Serializable;

/**
 * DTO raíz que agrupa los datos que el aplicativo externo de variedades
 * entregará para una solicitud dada.
 *
 * Estructura esperada del JSON del servicio externo:
 * {
 *   "applicationNumber": "...",
 *   "enTerritorio":   { ... },
 *   "fueraTerritorio": { ... },
 *   "informacionTecnica": { ... }
 * }
 *
 * NOTA: El aplicativo externo aún está en desarrollo. Este DTO y su cliente
 * están diseñados para integrarse cuando el servicio esté disponible.
 * Mientras tanto, el sistema mostrará la sección como "No disponible".
 */
public class ExternalVarietyDataDTO implements Serializable {

    private String applicationNumber;
    private TerritoryDataDTO enTerritorio;
    private TerritoryDataDTO fueraTerritorio;
    private TechnicalInfoDTO informacionTecnica;

    public String getApplicationNumber() { return applicationNumber; }
    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public TerritoryDataDTO getEnTerritorio() { return enTerritorio; }
    public void setEnTerritorio(TerritoryDataDTO enTerritorio) {
        this.enTerritorio = enTerritorio;
    }

    public TerritoryDataDTO getFueraTerritorio() { return fueraTerritorio; }
    public void setFueraTerritorio(TerritoryDataDTO fueraTerritorio) {
        this.fueraTerritorio = fueraTerritorio;
    }

    public TechnicalInfoDTO getInformacionTecnica() { return informacionTecnica; }
    public void setInformacionTecnica(TechnicalInfoDTO informacionTecnica) {
        this.informacionTecnica = informacionTecnica;
    }

    /** True cuando el DTO viene con al menos un bloque de datos completo. */
    public boolean tieneDatos() {
        return enTerritorio != null || fueraTerritorio != null || informacionTecnica != null;
    }
}
