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
 * Documento adjunto a un trámite. Relación: Tramite 1 → N DocumentoTramite.
 */
@Entity
@Table(name = "documentos_tramite")
public class DocumentoTramite implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** FK a tramites.id */
    @Column(name = "tramite_id", nullable = false)
    private Integer tramiteId;

    /** Nombre descriptivo asignado por el usuario (mínimo 5 caracteres). */
    @Column(name = "nombre_personalizado", nullable = false, length = 255)
    private String nombrePersonalizado;

    /** Nombre original del archivo al momento de la carga. */
    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    /** Ruta absoluta en el servidor donde está almacenado el archivo. */
    @Column(name = "ruta_archivo", nullable = false, length = 500)
    private String rutaArchivo;

    @Column(name = "tipo_archivo", length = 50)
    private String tipoArchivo;

    @Column(name = "tamano_bytes")
    private Long tamanoBytes;

    @Column(name = "fecha_carga", nullable = false)
    private Timestamp fechaCarga;

    @Column(name = "usuario_carga", length = 50)
    private String usuarioCarga;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getTramiteId() { return tramiteId; }
    public void setTramiteId(Integer tramiteId) { this.tramiteId = tramiteId; }

    public String getNombrePersonalizado() { return nombrePersonalizado; }
    public void setNombrePersonalizado(String nombrePersonalizado) { this.nombrePersonalizado = nombrePersonalizado; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }

    public String getTipoArchivo() { return tipoArchivo; }
    public void setTipoArchivo(String tipoArchivo) { this.tipoArchivo = tipoArchivo; }

    public Long getTamanoBytes() { return tamanoBytes; }
    public void setTamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; }

    public Timestamp getFechaCarga() { return fechaCarga; }
    public void setFechaCarga(Timestamp fechaCarga) { this.fechaCarga = fechaCarga; }

    public String getUsuarioCarga() { return usuarioCarga; }
    public void setUsuarioCarga(String usuarioCarga) { this.usuarioCarga = usuarioCarga; }

    /** Tamaño formateado para mostrar en pantalla. */
    public String getTamanoFormateado() {
        if (tamanoBytes == null) return "—";
        if (tamanoBytes < 1024) return tamanoBytes + " B";
        if (tamanoBytes < 1024 * 1024) return (tamanoBytes / 1024) + " KB";
        return String.format("%.1f MB", tamanoBytes / (1024.0 * 1024.0));
    }
}
