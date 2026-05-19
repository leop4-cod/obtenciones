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
@Table(name = "publicacion_gaceta")
public class PublicacionGaceta implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "vegetable_form_id", nullable = false)
    private Integer vegetableFormId;

    @Column(name = "denominacion_generica")
    private String denominacionGenerica;

    @Column(name = "denominacion_valida")
    private Boolean denominacionValida;

    @Column(name = "num_gaceta")
    private String numGaceta;

    @Column(name = "fecha_publicacion")
    private Timestamp fechaPublicacion;

    @Column(name = "fecha_fin_oposicion")
    private Timestamp fechaFinOposicion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "tiene_oposicion")
    private Boolean tieneOposicion;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getVegetableFormId() { return vegetableFormId; }
    public void setVegetableFormId(Integer vegetableFormId) { this.vegetableFormId = vegetableFormId; }

    public String getDenominacionGenerica() { return denominacionGenerica; }
    public void setDenominacionGenerica(String denominacionGenerica) { this.denominacionGenerica = denominacionGenerica; }

    public Boolean getDenominacionValida() { return denominacionValida; }
    public void setDenominacionValida(Boolean denominacionValida) { this.denominacionValida = denominacionValida; }

    public String getNumGaceta() { return numGaceta; }
    public void setNumGaceta(String numGaceta) { this.numGaceta = numGaceta; }

    public Timestamp getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(Timestamp fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }

    public Timestamp getFechaFinOposicion() { return fechaFinOposicion; }
    public void setFechaFinOposicion(Timestamp fechaFinOposicion) { this.fechaFinOposicion = fechaFinOposicion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Boolean getTieneOposicion() { return tieneOposicion; }
    public void setTieneOposicion(Boolean tieneOposicion) { this.tieneOposicion = tieneOposicion; }
}
