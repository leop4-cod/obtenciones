package senadi.gob.ec.adminob.model;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "certificado_obtentor")
public class CertificadoObtentor implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "vegetable_form_id", nullable = false)
    private Integer vegetableFormId;

    @Column(name = "resolucion_id")
    private Integer resolucionId;

    @Column(name = "num_certificado", unique = true)
    private String numCertificado;

    @Column(name = "fecha_emision")
    private Timestamp fechaEmision;

    @Column(name = "vigencia_years")
    private Integer vigenciaYears;

    @Column(name = "fecha_vencimiento")
    private Timestamp fechaVencimiento;

    @Column(name = "emitido_por")
    private String emitidoPor;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getVegetableFormId() { return vegetableFormId; }
    public void setVegetableFormId(Integer vegetableFormId) { this.vegetableFormId = vegetableFormId; }

    public Integer getResolucionId() { return resolucionId; }
    public void setResolucionId(Integer resolucionId) { this.resolucionId = resolucionId; }

    public String getNumCertificado() { return numCertificado; }
    public void setNumCertificado(String numCertificado) { this.numCertificado = numCertificado; }

    public Timestamp getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(Timestamp fechaEmision) { this.fechaEmision = fechaEmision; }

    public Integer getVigenciaYears() { return vigenciaYears; }
    public void setVigenciaYears(Integer vigenciaYears) { this.vigenciaYears = vigenciaYears; }

    public Timestamp getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(Timestamp fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public String getEmitidoPor() { return emitidoPor; }
    public void setEmitidoPor(String emitidoPor) { this.emitidoPor = emitidoPor; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
