package senadi.gob.ec.adminob.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import senadi.gob.ec.adminob.embed.VegetableMethodologyId;
import senadi.gob.ec.adminob.model.Methodology;
import senadi.gob.ec.adminob.model.VegetableForms;

@Generated(value="EclipseLink-2.7.12.v20230209-rNA", date="2026-04-27T16:48:33")
@StaticMetamodel(VegetableMethodology.class)
public class VegetableMethodology_ { 

    public static volatile SingularAttribute<VegetableMethodology, String> description;
    public static volatile SingularAttribute<VegetableMethodology, VegetableMethodologyId> id;
    public static volatile SingularAttribute<VegetableMethodology, VegetableForms> vegetableForms;
    public static volatile SingularAttribute<VegetableMethodology, Methodology> methodology;

}