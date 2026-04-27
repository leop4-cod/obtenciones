package senadi.gob.ec.adminob.model;

import java.sql.Timestamp;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import senadi.gob.ec.adminob.model.VegetableForms;

@Generated(value="EclipseLink-2.7.12.v20230209-rNA", date="2026-04-27T16:48:33")
@StaticMetamodel(Declaration.class)
public class Declaration_ { 

    public static volatile SingularAttribute<Declaration, Timestamp> declarationDate;
    public static volatile SingularAttribute<Declaration, String> name;
    public static volatile SingularAttribute<Declaration, Integer> id;
    public static volatile SingularAttribute<Declaration, String> place;
    public static volatile SingularAttribute<Declaration, VegetableForms> vegetableForms;

}