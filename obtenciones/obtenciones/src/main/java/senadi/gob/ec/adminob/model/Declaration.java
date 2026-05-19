/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package senadi.gob.ec.adminob.model;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author michael
 */
@Entity
@Table(name = "declaration")
public class Declaration implements Serializable{

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "place")
    private String place;
    
    @Column(name = "declaration_date")
    private Timestamp declarationDate;
    
    @Column(name = "name")
    private String name;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vegetable_form_id", unique = true)
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
     * @return the place
     */
    public String getPlace() {
        return place;
    }

    /**
     * @param place the place to set
     */
    public void setPlace(String place) {
        this.place = place;
    }

    /**
     * @return the declarationDate
     */
    public Timestamp getDeclarationDate() {
        return declarationDate;
    }

    /**
     * @param declarationDate the declarationDate to set
     */
    public void setDeclarationDate(Timestamp declarationDate) {
        this.declarationDate = declarationDate;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
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
