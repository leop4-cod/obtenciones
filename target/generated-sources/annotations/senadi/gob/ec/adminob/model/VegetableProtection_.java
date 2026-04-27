package senadi.gob.ec.adminob.model;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import senadi.gob.ec.adminob.enums.ProtectionType;
import senadi.gob.ec.adminob.model.VegetableForms;

@Generated(value="EclipseLink-2.7.12.v20230209-rNA", date="2026-04-27T16:48:33")
@StaticMetamodel(VegetableProtection.class)
public class VegetableProtection_ { 

    public static volatile SingularAttribute<VegetableProtection, ProtectionType> protectionType;
    public static volatile SingularAttribute<VegetableProtection, String> applicationNumber;
    public static volatile SingularAttribute<VegetableProtection, Date> submissionDate;
    public static volatile SingularAttribute<VegetableProtection, Integer> id;
    public static volatile SingularAttribute<VegetableProtection, String> registerNumber;
    public static volatile SingularAttribute<VegetableProtection, VegetableForms> vegetableForms;
    public static volatile SingularAttribute<VegetableProtection, Integer> submissionCountryId;
    public static volatile SingularAttribute<VegetableProtection, String> status;
    public static volatile SingularAttribute<VegetableProtection, String> denomination;

}