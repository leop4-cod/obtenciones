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
@Table(name = "deposito_muestra")
public class DepositoMuestra implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "vegetable_form_id", nullable = false)
    private Integer vegetableFormId;

    @Column(name = "fecha_deposito")
    private Timestamp fechaDeposito;

    @Column(name = "lugar_deposito")
    private String lugarDeposito;

    @Column(name = "responsable")
    private String responsable;

    @Column(name = "num_acta")
    private String numActa;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_acta")
    private Timestamp fechaActa;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getVegetableFormId() { return vegetableFormId; }
    public void setVegetableFormId(Integer vegetableFormId) { this.vegetableFormId = vegetableFormId; }

    public Timestamp getFechaDeposito() { return fechaDeposito; }
    public void setFechaDeposito(Timestamp fechaDeposito) { this.fechaDeposito = fechaDeposito; }

    public String getLugarDeposito() { return lugarDeposito; }
    public void setLugarDeposito(String lugarDeposito) { this.lugarDeposito = lugarDeposito; }

    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }

    public String getNumActa() { return numActa; }
    public void setNumActa(String numActa) { this.numActa = numActa; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Timestamp getFechaActa() { return fechaActa; }
    public void setFechaActa(Timestamp fechaActa) { this.fechaActa = fechaActa; }
}
