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
@Table(name = "expediente")
public class Expediente implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "vegetable_form_id", nullable = false)
    private Integer vegetableFormId;

    @Column(name = "expediente_number", unique = true)
    private String expedienteNumber;

    @Column(name = "providencia_date")
    private Timestamp providenciaDate;

    @Column(name = "admission_date")
    private Timestamp admissionDate;

    @Column(name = "archived_date")
    private Timestamp archivedDate;

    @Column(name = "archived_reason", columnDefinition = "TEXT")
    private String archivedReason;

    @Column(name = "tecnico")
    private String tecnico;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @Column(name = "substance_review_date")
    private Timestamp substanceReviewDate;

    @Column(name = "substance_observations", columnDefinition = "TEXT")
    private String substanceObservations;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getVegetableFormId() { return vegetableFormId; }
    public void setVegetableFormId(Integer vegetableFormId) { this.vegetableFormId = vegetableFormId; }

    public String getExpedienteNumber() { return expedienteNumber; }
    public void setExpedienteNumber(String expedienteNumber) { this.expedienteNumber = expedienteNumber; }

    public Timestamp getProvidenciaDate() { return providenciaDate; }
    public void setProvidenciaDate(Timestamp providenciaDate) { this.providenciaDate = providenciaDate; }

    public Timestamp getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(Timestamp admissionDate) { this.admissionDate = admissionDate; }

    public Timestamp getArchivedDate() { return archivedDate; }
    public void setArchivedDate(Timestamp archivedDate) { this.archivedDate = archivedDate; }

    public String getArchivedReason() { return archivedReason; }
    public void setArchivedReason(String archivedReason) { this.archivedReason = archivedReason; }

    public String getTecnico() { return tecnico; }
    public void setTecnico(String tecnico) { this.tecnico = tecnico; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public Timestamp getSubstanceReviewDate() { return substanceReviewDate; }
    public void setSubstanceReviewDate(Timestamp substanceReviewDate) { this.substanceReviewDate = substanceReviewDate; }

    public String getSubstanceObservations() { return substanceObservations; }
    public void setSubstanceObservations(String substanceObservations) { this.substanceObservations = substanceObservations; }
}
