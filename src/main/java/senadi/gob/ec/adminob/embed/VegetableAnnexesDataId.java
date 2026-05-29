/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package senadi.gob.ec.adminob.embed;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author michael
 */
@Embeddable
public class VegetableAnnexesDataId implements Serializable {

    @Column(name = "vegetable_form_id")
    private Integer vegetableFormId;

    @Column(name = "vegetable_annexes_id")
    private Integer vegetableAnnexesId;

    public VegetableAnnexesDataId() {}

    public VegetableAnnexesDataId(Integer vegetableFormId, Integer vegetableAnnexesId) {
        this.vegetableFormId = vegetableFormId;
        this.vegetableAnnexesId = vegetableAnnexesId;
    }

    public Integer getVegetableFormId() {
        return vegetableFormId;
    }

    public void setVegetableFormId(Integer vegetableFormId) {
        this.vegetableFormId = vegetableFormId;
    }

    public Integer getVegetableAnnexesId() {
        return vegetableAnnexesId;
    }

    public void setVegetableAnnexesId(Integer vegetableAnnexesId) {
        this.vegetableAnnexesId = vegetableAnnexesId;
    }

    // Es muy importante implementar equals() y hashCode() en claves compuestas:
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VegetableAnnexesDataId)) return false;
        VegetableAnnexesDataId that = (VegetableAnnexesDataId) o;
        return vegetableFormId.equals(that.vegetableFormId) &&
               vegetableAnnexesId.equals(that.vegetableAnnexesId);
    }

    @Override
    public int hashCode() {
        return 31 * vegetableFormId.hashCode() + vegetableAnnexesId.hashCode();
    }
}