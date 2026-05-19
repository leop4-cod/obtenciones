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
@Table(name = "similary_varitety")
public class SimilaryVariety implements Serializable {
    
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "denomination")
    private String denomination;
    
    @Column(name = "different_character")
    private String differentCharacter;
    
    @Column(name = "similary_expression_level")
    private String similaryExpressionLevel;
    
    @Column(name = "register_expression_level")
    private String registerExpressionLevel;
    
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
     * @return the denomination
     */
    public String getDenomination() {
        return denomination;
    }

    /**
     * @param denomination the denomination to set
     */
    public void setDenomination(String denomination) {
        this.denomination = denomination;
    }

    /**
     * @return the similaryExpressionLevel
     */
    public String getSimilaryExpressionLevel() {
        return similaryExpressionLevel;
    }

    /**
     * @param similaryExpressionLevel the similaryExpressionLevel to set
     */
    public void setSimilaryExpressionLevel(String similaryExpressionLevel) {
        this.similaryExpressionLevel = similaryExpressionLevel;
    }

    /**
     * @return the registerExpressionLevel
     */
    public String getRegisterExpressionLevel() {
        return registerExpressionLevel;
    }

    /**
     * @param registerExpressionLevel the registerExpressionLevel to set
     */
    public void setRegisterExpressionLevel(String registerExpressionLevel) {
        this.registerExpressionLevel = registerExpressionLevel;
    }

    /**
     * @return the differentCharacter
     */
    public String getDifferentCharacter() {
        return differentCharacter;
    }

    /**
     * @param differentCharacter the differentCharacter to set
     */
    public void setDifferentCharacter(String differentCharacter) {
        this.differentCharacter = differentCharacter;
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
