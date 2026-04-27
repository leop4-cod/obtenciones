package senadi.gob.ec.adminob.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import senadi.gob.ec.adminob.embed.VegetableAnnexesDataId;
import senadi.gob.ec.adminob.model.VegetableAnnexes;
import senadi.gob.ec.adminob.model.VegetableForms;

@Generated(value="EclipseLink-2.7.12.v20230209-rNA", date="2026-04-27T16:48:33")
@StaticMetamodel(VegetableAnnexesData.class)
public class VegetableAnnexesData_ { 

    public static volatile SingularAttribute<VegetableAnnexesData, String> fileName;
    public static volatile SingularAttribute<VegetableAnnexesData, String> anotherAnnexe;
    public static volatile SingularAttribute<VegetableAnnexesData, VegetableAnnexesDataId> id;
    public static volatile SingularAttribute<VegetableAnnexesData, VegetableForms> vegetableForms;
    public static volatile SingularAttribute<VegetableAnnexesData, VegetableAnnexes> vegetableAnnexes;

}