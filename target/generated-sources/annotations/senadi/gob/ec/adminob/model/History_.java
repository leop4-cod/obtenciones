package senadi.gob.ec.adminob.model;

import java.sql.Timestamp;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.12.v20230209-rNA", date="2026-04-27T16:48:33")
@StaticMetamodel(History.class)
public class History_ { 

    public static volatile SingularAttribute<History, Timestamp> fecha;
    public static volatile SingularAttribute<History, String> historyUser;
    public static volatile SingularAttribute<History, String> applicationNumber;
    public static volatile SingularAttribute<History, String> description;
    public static volatile SingularAttribute<History, Integer> id;

}