package senadi.gob.ec.adminob.model;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Comprobante de pago asociado a un trámite de obtención vegetal.
 * Relación: VegetableForms 1 → N ComprobantePago.
 *
 * Cada archivo subido genera un registro independiente en esta tabla,
 * eliminando la restricción anterior de un único paymentReceiptId por trámite.
 */
@Entity
@Table(name = "comprobante_pago")
public class ComprobantePago implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** FK hacia vegetable_forms.id */
    @Column(name = "vegetable_form_id", nullable = false)
    private Integer vegetableFormId;

    /** Nombre original del archivo tal como lo subió el usuario. */
    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    /**
     * Ruta absoluta en el servidor donde está almacenado el archivo.
     * Ejemplo: /opt/uploads/obtenciones/42/comprobante_42_1714823400000.pdf
     */
    @Column(name = "ruta_archivo", nullable = false, length = 500)
    private String rutaArchivo;

    @Column(name = "fecha_carga", nullable = false)
    private Timestamp fechaCarga;

    /** Usuario del sistema que realizó la carga. */
    @Column(name = "cargado_por", length = 100)
    private String cargadoPor;

    @Column(name = "tamano_bytes")
    private Long tamanoBytes;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getVegetableFormId() { return vegetableFormId; }
    public void setVegetableFormId(Integer vegetableFormId) { this.vegetableFormId = vegetableFormId; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }

    public Timestamp getFechaCarga() { return fechaCarga; }
    public void setFechaCarga(Timestamp fechaCarga) { this.fechaCarga = fechaCarga; }

    public String getCargadoPor() { return cargadoPor; }
    public void setCargadoPor(String cargadoPor) { this.cargadoPor = cargadoPor; }

    public Long getTamanoBytes() { return tamanoBytes; }
    public void setTamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; }
}
