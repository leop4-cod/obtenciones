package senadi.gob.ec.adminob.integration.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Información técnica de la variedad provista por el aplicativo externo.
 * Incluye características morfológicas, metodología y similares.
 */
public class TechnicalInfoDTO implements Serializable {

    /** Especie botánica (taxón). */
    private String taxon;

    /** Nombre común de la variedad. */
    private String nombreComun;

    /** Método de reproducción (semilla, vegetativa, etc.). */
    private String mecanismoReproducccion;

    /** Origen geográfico del material. */
    private String origenGeografico;

    /** Genealogía / historial de cruce. */
    private String genealogia;

    /** Caracteres morfológicos diferenciales (lista de descriptores). */
    private List<String> caracteresMorfologicos;

    /** Metodología de evaluación usada (UPOV, ISTA, etc.). */
    private String metodologia;

    /** Observaciones técnicas adicionales. */
    private String observaciones;

    public String getTaxon() { return taxon; }
    public void setTaxon(String taxon) { this.taxon = taxon; }

    public String getNombreComun() { return nombreComun; }
    public void setNombreComun(String nombreComun) { this.nombreComun = nombreComun; }

    public String getMecanismoReproducccion() { return mecanismoReproducccion; }
    public void setMecanismoReproducccion(String m) { this.mecanismoReproducccion = m; }

    public String getOrigenGeografico() { return origenGeografico; }
    public void setOrigenGeografico(String origenGeografico) {
        this.origenGeografico = origenGeografico;
    }

    public String getGenealogy() { return genealogia; }
    public void setGenealogy(String genealogia) { this.genealogia = genealogia; }

    public List<String> getCaracteresMorfologicos() { return caracteresMorfologicos; }
    public void setCaracteresMorfologicos(List<String> c) { this.caracteresMorfologicos = c; }

    public String getMetodologia() { return metodologia; }
    public void setMetodologia(String metodologia) { this.metodologia = metodologia; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
