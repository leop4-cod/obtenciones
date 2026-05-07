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
@Table(name = "certificado_deposito")
public class CertificadoDeposito implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "vegetable_form_id", nullable = false)
    private Integer vegetableFormId;

    @Column(name = "deposito_muestra_id")
    private Integer depositoMuestraId;

    @Column(name = "num_certificado", unique = true)
    private String numCertificado;

    @Column(name = "fecha_emision")
    private Timestamp fechaEmision;

    @Column(name = "emitido_por")
    private String emitidoPor;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getVegetableFormId() { return vegetableFormId; }
    public void setVegetableFormId(Integer vegetableFormId) { this.vegetableFormId = vegetableFormId; }

    public Integer getDepositoMuestraId() { return depositoMuestraId; }
    public void setDepositoMuestraId(Integer depositoMuestraId) { this.depositoMuestraId = depositoMuestraId; }

    public String getNumCertificado() { return numCertificado; }
    public void setNumCertificado(String numCertificado) { this.numCertificado = numCertificado; }

    public Timestamp getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(Timestamp fechaEmision) { this.fechaEmision = fechaEmision; }

    public String getEmitidoPor() { return emitidoPor; }
    public void setEmitidoPor(String emitidoPor) { this.emitidoPor = emitidoPor; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
