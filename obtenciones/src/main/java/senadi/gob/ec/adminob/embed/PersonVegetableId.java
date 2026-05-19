/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package senadi.gob.ec.adminob.embed;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import senadi.gob.ec.adminob.enums.PersonType;

/**
 *
 * @author michael
 */
@Embeddable
public class PersonVegetableId implements Serializable {

    @Column(name = "vegetable_form_id")
    private Integer vegetableFormId;

    @Column(name = "person_id")
    private Integer personId;
    
     @Column(name = "person_type", length = 30)
    private String personType;

    public PersonVegetableId() {}

    public PersonVegetableId(Integer vegetableFormId, Integer personId, PersonType personType) {
        this.vegetableFormId = vegetableFormId;
        this.personId = personId;
        this.personType = personType.name();
    }

    // Getters y setters
    public Integer getVegetableFormId() { return vegetableFormId; }
    public void setVegetableFormId(Integer vegetableFormId) { this.vegetableFormId = vegetableFormId; }

    public Integer getPersonId() { return personId; }
    public void setPersonId(Integer personId) { this.personId = personId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonVegetableId)) return false;
        PersonVegetableId that = (PersonVegetableId) o;
        return Objects.equals(vegetableFormId, that.vegetableFormId) &&
               Objects.equals(personId, that.personId) &&
               Objects.equals(personType, that.personType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vegetableFormId, personId, personType);
    }

    /**
     * @return the personType
     */
    public String getPersonType() {
        return personType;
    }
    /**
     * @param personType the personType to set
     */
    public void setPersonType(String personType) {
        this.personType = personType;
    }
    
    public void setPersonType(PersonType personType) {
        this.personType = personType.name();
    }

    public PersonType getPersonTypeEnum() {
        return PersonType.valueOf(this.personType);
    }
    
    @Override
    public String toString(){
        return "("+getPersonId()+","+getVegetableFormId()+","+getPersonType()+")";
    }
}
