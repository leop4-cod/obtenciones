/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package senadi.gob.ec.adminob.embed;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author michael
 */
@Embeddable
public class VegetableMethodologyId implements Serializable {

    @Column(name = "vegetable_form_id")
    private Integer vegetableFormId;

    @Column(name = "methodology_id")
    private Integer methodologyId;

    /**
     * @return the vegetableFormId
     */
    public Integer getVegetableFormId() {
        return vegetableFormId;
    }

    /**
     * @param vegetableFormId the vegetableFormId to set
     */
    public void setVegetableFormId(Integer vegetableFormId) {
        this.vegetableFormId = vegetableFormId;
    }

    /**
     * @return the methodologyId
     */
    public Integer getMethodologyId() {
        return methodologyId;
    }

    /**
     * @param methodologyId the methodologyId to set
     */
    public void setMethodologyId(Integer methodologyId) {
        this.methodologyId = methodologyId;
    }

    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VegetableMethodologyId)) return false;
        VegetableMethodologyId that = (VegetableMethodologyId) o;
        return vegetableFormId.equals(that.vegetableFormId) &&
               methodologyId.equals(that.methodologyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vegetableFormId, methodologyId);
    }
}
