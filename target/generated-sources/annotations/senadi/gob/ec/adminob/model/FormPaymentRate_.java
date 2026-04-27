package senadi.gob.ec.adminob.model;

import java.sql.Timestamp;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import senadi.gob.ec.adminob.model.VegetableForms;

@Generated(value="EclipseLink-2.7.12.v20230209-rNA", date="2026-04-27T16:48:33")
@StaticMetamodel(FormPaymentRate.class)
public class FormPaymentRate_ { 

    public static volatile SingularAttribute<FormPaymentRate, Integer> formPaymentRateId;
    public static volatile SingularAttribute<FormPaymentRate, Integer> id;
    public static volatile SingularAttribute<FormPaymentRate, VegetableForms> vegetableForms;
    public static volatile SingularAttribute<FormPaymentRate, Integer> paymentRateId;
    public static volatile SingularAttribute<FormPaymentRate, Timestamp> createDate;

}