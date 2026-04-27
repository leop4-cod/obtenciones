package senadi.gob.ec.adminob.model;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import senadi.gob.ec.adminob.model.VegetableForms;

@Generated(value="EclipseLink-2.7.12.v20230209-rNA", date="2026-04-27T16:48:33")
@StaticMetamodel(VegetablePriority.class)
public class VegetablePriority_ { 

    public static volatile SingularAttribute<VegetablePriority, String> applicationNumber;
    public static volatile SingularAttribute<VegetablePriority, Integer> id;
    public static volatile SingularAttribute<VegetablePriority, String> applicantName;
    public static volatile SingularAttribute<VegetablePriority, VegetableForms> vegetableForms;
    public static volatile SingularAttribute<VegetablePriority, Integer> countryId;
    public static volatile SingularAttribute<VegetablePriority, Date> applicationDate;
    public static volatile SingularAttribute<VegetablePriority, String> genericDenomination;

}