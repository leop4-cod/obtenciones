package senadi.gob.ec.adminob.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import senadi.gob.ec.adminob.embed.PersonVegetableId;
import senadi.gob.ec.adminob.enums.PersonType;
import senadi.gob.ec.adminob.model.Person;
import senadi.gob.ec.adminob.model.VegetableForms;

@Generated(value="EclipseLink-2.7.12.v20230209-rNA", date="2026-04-27T16:48:33")
@StaticMetamodel(PersonVegetable.class)
public class PersonVegetable_ { 

    public static volatile SingularAttribute<PersonVegetable, Person> person;
    public static volatile SingularAttribute<PersonVegetable, String> judicialLocker;
    public static volatile SingularAttribute<PersonVegetable, String> emailLawyerAttorney;
    public static volatile SingularAttribute<PersonVegetable, String> powerCode;
    public static volatile SingularAttribute<PersonVegetable, PersonVegetableId> id;
    public static volatile SingularAttribute<PersonVegetable, VegetableForms> vegetableForms;
    public static volatile SingularAttribute<PersonVegetable, PersonType> personType;

}