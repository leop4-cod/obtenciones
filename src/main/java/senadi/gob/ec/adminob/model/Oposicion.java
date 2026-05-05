package senadi.gob.ec.adminob.model;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import senadi.gob.ec.adminob.enums.EstadoOposicion;

@Entity
@Table(name = "oposicion")
public class Oposicion implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "vegetable_form_id", nullable = false)
    private Integer vegetableFormId;

    @Column(name = "oponente")
    private String oponente;

    @Column(name = "fecha_presentacion")
    private Timestamp fechaPresentacion;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoOposicion estado;

    @Column(name = "fecha_resolucion")
    private Timestamp fechaResolucion;

    @Column(name = "resolucion_detalle", columnDefinition = "TEXT")
    private String resolucionDetalle;

    @Column(name = "resuelto_por")
    private String resueltoPor;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getVegetableFormId() { return vegetableFormId; }
    public void setVegetableFormId(Integer vegetableFormId) { this.vegetableFormId = vegetableFormId; }

    public String getOponente() { return oponente; }
    public void setOponente(String oponente) { this.oponente = oponente; }

    public Timestamp getFechaPresentacion() { return fechaPresentacion; }
    public void setFechaPresentacion(Timestamp fechaPresentacion) { this.fechaPresentacion = fechaPresentacion; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public EstadoOposicion getEstado() { return estado; }
    public void setEstado(EstadoOposicion estado) { this.estado = estado; }

    public Timestamp getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(Timestamp fechaResolucion) { this.fechaResolucion = fechaResolucion; }

    public String getResolucionDetalle() { return resolucionDetalle; }
    public void setResolucionDetalle(String resolucionDetalle) { this.resolucionDetalle = resolucionDetalle; }

    public String getResueltoPor() { return resueltoPor; }
    public void setResueltoPor(String resueltoPor) { this.resueltoPor = resueltoPor; }
}
