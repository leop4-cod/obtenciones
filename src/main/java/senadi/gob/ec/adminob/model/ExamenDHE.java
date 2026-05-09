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
import senadi.gob.ec.adminob.enums.ResultadoDHE;
import senadi.gob.ec.adminob.enums.TipoExamenDHE;

@Entity
@Table(name = "examen_dhe")
public class ExamenDHE implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "vegetable_form_id", nullable = false)
    private Integer vegetableFormId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_examen")
    private TipoExamenDHE tipoExamen;

    @Column(name = "fecha_solicitud")
    private Timestamp fechaSolicitud;

    @Column(name = "fecha_resultado")
    private Timestamp fechaResultado;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado")
    private ResultadoDHE resultado;

    @Column(name = "entidad_examinadora")
    private String entidadExaminadora;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "solicitado_por")
    private String solicitadoPor;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getVegetableFormId() { return vegetableFormId; }
    public void setVegetableFormId(Integer vegetableFormId) { this.vegetableFormId = vegetableFormId; }

    public TipoExamenDHE getTipoExamen() { return tipoExamen; }
    public void setTipoExamen(TipoExamenDHE tipoExamen) { this.tipoExamen = tipoExamen; }

    public Timestamp getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(Timestamp fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public Timestamp getFechaResultado() { return fechaResultado; }
    public void setFechaResultado(Timestamp fechaResultado) { this.fechaResultado = fechaResultado; }

    public ResultadoDHE getResultado() { return resultado; }
    public void setResultado(ResultadoDHE resultado) { this.resultado = resultado; }

    public String getEntidadExaminadora() { return entidadExaminadora; }
    public void setEntidadExaminadora(String entidadExaminadora) { this.entidadExaminadora = entidadExaminadora; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getSolicitadoPor() { return solicitadoPor; }
    public void setSolicitadoPor(String solicitadoPor) { this.solicitadoPor = solicitadoPor; }
}
