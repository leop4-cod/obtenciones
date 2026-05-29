/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package senadi.gob.ec.adminob.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author michael
 */

@Entity
@Table(name = "variety_characters")
public class VarietyCharacters implements Serializable{

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "characters")
    private String characters;

    @Column(name = "expresion_level")
    private String expressionLevel;

    @Column(name = "sample_title_variety")
    private String sampleTitleVariety;

    @Column(name = "note")
    private String note;

    @ManyToOne
    @JoinColumn(name = "vegetable_forms_id", nullable = false)
    private VegetableForms vegetableForms;
    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the characters
     */
    public String getCharacters() {
        return characters;
    }

    /**
     * @param characters the characters to set
     */
    public void setCharacters(String characters) {
        this.characters = characters;
    }

    /**
     * @return the expressionLevel
     */
    public String getExpressionLevel() {
        return expressionLevel;
    }

    /**
     * @param expressionLevel the expressionLevel to set
     */
    public void setExpressionLevel(String expressionLevel) {
        this.expressionLevel = expressionLevel;
    }

    /**
     * @return the sampleTitleVariety
     */
    public String getSampleTitleVariety() {
        return sampleTitleVariety;
    }

    /**
     * @param sampleTitleVariety the sampleTitleVariety to set
     */
    public void setSampleTitleVariety(String sampleTitleVariety) {
        this.sampleTitleVariety = sampleTitleVariety;
    }

    /**
     * @return the note
     */
    public String getNote() {
        return note;
    }

    /**
     * @param note the note to set
     */
    public void setNote(String note) {
        this.note = note;
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
}
