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
import senadi.gob.ec.adminob.enums.TipoResolucion;

@Entity
@Table(name = "resolucion")
public class Resolucion implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "vegetable_form_id", nullable = false)
    private Integer vegetableFormId;

    @Column(name = "num_resolucion", unique = true)
    private String numResolucion;

    @Column(name = "fecha_resolucion")
    private Timestamp fechaResolucion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    private TipoResolucion tipo;

    @Column(name = "fundamento", columnDefinition = "TEXT")
    private String fundamento;

    @Column(name = "emitido_por")
    private String emitidoPor;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getVegetableFormId() { return vegetableFormId; }
    public void setVegetableFormId(Integer vegetableFormId) { this.vegetableFormId = vegetableFormId; }

    public String getNumResolucion() { return numResolucion; }
    public void setNumResolucion(String numResolucion) { this.numResolucion = numResolucion; }

    public Timestamp getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(Timestamp fechaResolucion) { this.fechaResolucion = fechaResolucion; }

    public TipoResolucion getTipo() { return tipo; }
    public void setTipo(TipoResolucion tipo) { this.tipo = tipo; }

    public String getFundamento() { return fundamento; }
    public void setFundamento(String fundamento) { this.fundamento = fundamento; }

    public String getEmitidoPor() { return emitidoPor; }
    public void setEmitidoPor(String emitidoPor) { this.emitidoPor = emitidoPor; }
}
