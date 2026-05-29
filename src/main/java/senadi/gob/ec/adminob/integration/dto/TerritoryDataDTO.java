package senadi.gob.ec.adminob.integration.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Datos de prueba en territorio o fuera de territorio,
 * provistos por el aplicativo externo de variedades.
 */
public class TerritoryDataDTO implements Serializable {

    /** País o zona donde se realizaron las pruebas. */
    private String pais;

    /** Institución o entidad que realizó las pruebas. */
    private String entidad;

    /** Período de prueba (ej: "2023-2024"). */
    private String periodo;

    /** Descripción general de las condiciones de prueba. */
    private String descripcion;

    /** Lista de localidades o sitios de ensayo. */
    private List<String> localidades;

    /** Resultado resumido del ensayo. */
    private String resultado;

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public List<String> getLocalidades() { return localidades; }
    public void setLocalidades(List<String> localidades) { this.localidades = localidades; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
}
