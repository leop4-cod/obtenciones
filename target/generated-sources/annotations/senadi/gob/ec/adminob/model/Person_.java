package senadi.gob.ec.adminob.model;

import java.sql.Timestamp;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import senadi.gob.ec.adminob.model.PersonVegetable;

@Generated(value="EclipseLink-2.7.12.v20230209-rNA", date="2026-04-27T16:48:33")
@StaticMetamodel(Person.class)
public class Person_ { 

    public static volatile SingularAttribute<Person, String> name;
    public static volatile SingularAttribute<Person, String> identificationNumber;
    public static volatile ListAttribute<Person, PersonVegetable> personVegetables;
    public static volatile SingularAttribute<Person, Integer> id;
    public static volatile SingularAttribute<Person, Timestamp> createDate;

}