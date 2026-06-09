package senadi.gob.ec.adminob.model;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entidad que representa un trámite en el módulo de Trámites SENADI.
 * Módulo independiente de VegetableForms/Obtenciones.
 */
@Entity
@Table(name = "tramites")
public class Tramite implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero_tramite", length = 100)
    private String numeroTramite;

    /** Número de trámite interno, ej: INT-2025-001. Debe ser único si se ingresa. */
    @Column(name = "numero_de_tramite", length = 50)
    private String numeroDeTramite;

    @Column(name = "titular", length = 200)
    private String titular;

    @Column(name = "denominacion", length = 300)
    private String denominacion;

    /** Estado del expediente SENADI. Solo ACCEPTED permite edición/eliminación. */
    @Column(name = "estado_actual", length = 100, nullable = false)
    private String estadoActual = "EN_PROCESO";

    /** true cuando estadoActual == 'ACCEPTED'. Se actualiza automáticamente en el bean. */
    @Column(name = "puede_editar", nullable = false)
    private boolean puedeEditar = false;

    @Column(name = "fecha_creacion", nullable = false)
    private Timestamp fechaCreacion;

    @Column(name = "fecha_modificacion")
    private Timestamp fechaModificacion;

    /** Fecha de presentación ante SENADI. No puede ser futura. */
    @Column(name = "fecha_presentacion")
    private Date fechaPresentacion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "usuario_creacion", length = 100)
    private String usuarioCreacion;

    @Column(name = "usuario_modificacion", length = 100)
    private String usuarioModificacion;

    /** ID de la solicitud del portal (vegetable_forms) vinculada. Solo se enlaza si está en proceso. */
    @Column(name = "vegetable_form_id")
    private Integer vegetableFormId;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNumeroTramite() { return numeroTramite; }
    public void setNumeroTramite(String numeroTramite) { this.numeroTramite = numeroTramite; }

    public String getNumeroDeTramite() { return numeroDeTramite; }
    public void setNumeroDeTramite(String numeroDeTramite) {
        this.numeroDeTramite = (numeroDeTramite != null && numeroDeTramite.trim().isEmpty())
                ? null : (numeroDeTramite != null ? numeroDeTramite.trim() : null);
    }

    public String getTitular() { return titular; }
    public void setTitular(String titular) { this.titular = titular; }

    public String getDenominacion() { return denominacion; }
    public void setDenominacion(String denominacion) { this.denominacion = denominacion; }

    public String getEstadoActual() { return estadoActual; }
    public void setEstadoActual(String estadoActual) {
        this.estadoActual = estadoActual;
        this.puedeEditar = "ACCEPTED".equals(estadoActual);
    }

    public boolean isPuedeEditar() { return puedeEditar; }
    public void setPuedeEditar(boolean puedeEditar) { this.puedeEditar = puedeEditar; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Timestamp getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(Timestamp fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    public Date getFechaPresentacion() { return fechaPresentacion; }
    public void setFechaPresentacion(Date fechaPresentacion) { this.fechaPresentacion = fechaPresentacion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getUsuarioCreacion() { return usuarioCreacion; }
    public void setUsuarioCreacion(String usuarioCreacion) { this.usuarioCreacion = usuarioCreacion; }

    public String getUsuarioModificacion() { return usuarioModificacion; }
    public void setUsuarioModificacion(String usuarioModificacion) { this.usuarioModificacion = usuarioModificacion; }

    public Integer getVegetableFormId() { return vegetableFormId; }
    public void setVegetableFormId(Integer vegetableFormId) { this.vegetableFormId = vegetableFormId; }
}
