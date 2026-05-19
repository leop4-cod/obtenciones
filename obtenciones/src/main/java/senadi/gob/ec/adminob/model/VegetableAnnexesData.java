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
import senadi.gob.ec.adminob.embed.VegetableAnnexesDataId;

/**
 *
 * @author michael
 */
@Entity
@Table(name = "vegetable_annexes_data")
public class VegetableAnnexesData implements Serializable{

    @EmbeddedId
    private VegetableAnnexesDataId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("vegetableFormId") // Mapea la parte del ID a la entidad
    @JoinColumn(name = "vegetable_form_id", nullable = false)
    private VegetableForms vegetableForms;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("vegetableAnnexesId") // Mapea la parte del ID a la entidad
    @JoinColumn(name = "vegetable_annexes_id", nullable = false)
    private VegetableAnnexes vegetableAnnexes;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "another_annexe")
    private String anotherAnnexe;

    // Getters y setters
    public VegetableAnnexesDataId getId() {
        return id;
    }

    public void setId(VegetableAnnexesDataId id) {
        this.id = id;
    }

    public VegetableAnnexes getVegetableAnnexes() {
        return vegetableAnnexes;
    }

    public void setVegetableAnnexes(VegetableAnnexes vegetableAnnexes) {
        this.vegetableAnnexes = vegetableAnnexes;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getAnotherAnnexe() {
        return anotherAnnexe;
    }

    public void setAnotherAnnexe(String anotherAnnexe) {
        this.anotherAnnexe = anotherAnnexe;
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
    public String toString(){
        return getVegetableForms().getId()+" - "+getVegetableAnnexes().getId();
    }
}
