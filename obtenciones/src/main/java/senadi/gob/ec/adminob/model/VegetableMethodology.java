/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package senadi.gob.ec.adminob.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import senadi.gob.ec.adminob.embed.VegetableMethodologyId;

/**
 *
 * @author michael
 */
@Entity
@Table(name = "vegetable_methodology")
public class VegetableMethodology implements Serializable {

    @EmbeddedId
    private VegetableMethodologyId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("vegetableFormId") // Mapea la parte del ID a la entidad
    @JoinColumn(name = "vegetable_form_id", nullable = false)
    private VegetableForms vegetableForms;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("methodologyId") // Mapea la parte del ID a la entidad
    @JoinColumn(name = "methodology_id", nullable = false)
    private Methodology methodology;

    @Column(name = "description")
    private String description;

    /**
     * @return the id
     */
    public VegetableMethodologyId getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(VegetableMethodologyId id) {
        this.id = id;
    }

    /**
     * @return the methodology
     */
    public Methodology getMethodology() {
        return methodology;
    }

    /**
     * @param methodology the methodology to set
     */
    public void setMethodology(Methodology methodology) {
        this.methodology = methodology;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the vegetableForms
     */
    public VegetableForms getVegetableForms() {
        return vegetableForms;
    }

    /**
     * @param vegetableForms the vegetableForms to set
     */
    public void setVegetableForms(VegetableForms vegetableForms) {
        this.vegetableForms = vegetableForms;
    }

    @Override
    public String toString() {
        return getMethodology().getId().toString() + " - " + getVegetableForms().getId();
    }
}
