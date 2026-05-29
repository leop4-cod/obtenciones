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
@Table(name = "dictamen_tecnico")
public class DictamenTecnico implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "vegetable_form_id", nullable = false)
    private Integer vegetableFormId;

    @Column(name = "examen_dhe_id")
    private Integer examenDheId;

    @Column(name = "fecha_dictamen")
    private Timestamp fechaDictamen;

    @Column(name = "tecnico")
    private String tecnico;

    @Column(name = "dictamen", columnDefinition = "TEXT")
    private String dictamen;

    @Column(name = "recomendacion")
    private String recomendacion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getVegetableFormId() { return vegetableFormId; }
    public void setVegetableFormId(Integer vegetableFormId) { this.vegetableFormId = vegetableFormId; }

    public Integer getExamenDheId() { return examenDheId; }
    public void setExamenDheId(Integer examenDheId) { this.examenDheId = examenDheId; }

    public Timestamp getFechaDictamen() { return fechaDictamen; }
    public void setFechaDictamen(Timestamp fechaDictamen) { this.fechaDictamen = fechaDictamen; }

    public String getTecnico() { return tecnico; }
    public void setTecnico(String tecnico) { this.tecnico = tecnico; }

    public String getDictamen() { return dictamen; }
    public void setDictamen(String dictamen) { this.dictamen = dictamen; }

    public String getRecomendacion() { return recomendacion; }
    public void setRecomendacion(String recomendacion) { this.recomendacion = recomendacion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
