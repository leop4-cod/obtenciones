package senadi.gob.ec.adminob.model;

import java.sql.Timestamp;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import senadi.gob.ec.adminob.enums.DenominationType;
import senadi.gob.ec.adminob.enums.PersonType;
import senadi.gob.ec.adminob.enums.Status;
import senadi.gob.ec.adminob.enums.StatusFlow;
import senadi.gob.ec.adminob.enums.VarietyTransferType;
import senadi.gob.ec.adminob.model.Declaration;
import senadi.gob.ec.adminob.model.ExploitedSelled;
import senadi.gob.ec.adminob.model.FormPaymentRate;
import senadi.gob.ec.adminob.model.PersonVegetable;
import senadi.gob.ec.adminob.model.SimilaryVariety;
import senadi.gob.ec.adminob.model.VarietyCharacters;
import senadi.gob.ec.adminob.model.VegetableAnnexesData;
import senadi.gob.ec.adminob.model.VegetableMethodology;
import senadi.gob.ec.adminob.model.VegetablePriority;
import senadi.gob.ec.adminob.model.VegetableProtection;

@Generated(value="EclipseLink-2.7.12.v20230209-rNA", date="2026-04-27T16:48:33")
@StaticMetamodel(VegetableForms.class)
public class VegetableForms_ { 

    public static volatile SingularAttribute<VegetableForms, Date> assignedDate;
    public static volatile SingularAttribute<VegetableForms, String> commonName;
    public static volatile SingularAttribute<VegetableForms, StatusFlow> statusFlow;
    public static volatile SingularAttribute<VegetableForms, String> geographicalVarietyOrigin;
    public static volatile SingularAttribute<VegetableForms, Boolean> productVarietyIdentification;
    public static volatile ListAttribute<VegetableForms, VarietyCharacters> varietiesCharacters;
    public static volatile SingularAttribute<VegetableForms, Boolean> livingSample;
    public static volatile SingularAttribute<VegetableForms, FormPaymentRate> formPaymentRate;
    public static volatile SingularAttribute<VegetableForms, Integer> ownerId;
    public static volatile SingularAttribute<VegetableForms, String> genealogy;
    public static volatile SingularAttribute<VegetableForms, DenominationType> denominationType;
    public static volatile SingularAttribute<VegetableForms, String> processHistory;
    public static volatile ListAttribute<VegetableForms, VegetableMethodology> vegetableMethodologies;
    public static volatile ListAttribute<VegetableForms, VegetableProtection> vegetableProtections;
    public static volatile SingularAttribute<VegetableForms, Integer> id;
    public static volatile SingularAttribute<VegetableForms, String> assignedUser;
    public static volatile SingularAttribute<VegetableForms, String> reproductionMechanism;
    public static volatile SingularAttribute<VegetableForms, String> additionalInformation;
    public static volatile SingularAttribute<VegetableForms, Integer> countryExam;
    public static volatile SingularAttribute<VegetableForms, Boolean> hasOtherApplications;
    public static volatile SingularAttribute<VegetableForms, String> provitionalDesignation;
    public static volatile SingularAttribute<VegetableForms, PersonType> personNotiDirection;
    public static volatile SingularAttribute<VegetableForms, Boolean> noExamYet;
    public static volatile ListAttribute<VegetableForms, ExploitedSelled> exploitedSelleds;
    public static volatile SingularAttribute<VegetableForms, String> botanicalTaxon;
    public static volatile SingularAttribute<VegetableForms, String> samplePlace;
    public static volatile SingularAttribute<VegetableForms, String> geographicalMaterialOrigin;
    public static volatile SingularAttribute<VegetableForms, Timestamp> priorityDate;
    public static volatile SingularAttribute<VegetableForms, Boolean> varietyTransfer;
    public static volatile SingularAttribute<VegetableForms, Status> status;
    public static volatile SingularAttribute<VegetableForms, Timestamp> applicationDate;
    public static volatile SingularAttribute<VegetableForms, String> applicationNumber;
    public static volatile SingularAttribute<VegetableForms, Boolean> examInProcess;
    public static volatile SingularAttribute<VegetableForms, Boolean> inTerritory;
    public static volatile SingularAttribute<VegetableForms, Boolean> outTerritory;
    public static volatile SingularAttribute<VegetableForms, Boolean> electronicCommunicationConsent;
    public static volatile ListAttribute<VegetableForms, SimilaryVariety> similaritiesVariety;
    public static volatile SingularAttribute<VegetableForms, Integer> paymentReceiptId;
    public static volatile SingularAttribute<VegetableForms, Timestamp> createDate;
    public static volatile SingularAttribute<VegetableForms, VegetablePriority> vegetablePriority;
    public static volatile SingularAttribute<VegetableForms, Boolean> priorityClaim;
    public static volatile SingularAttribute<VegetableForms, VarietyTransferType> varietyTransferType;
    public static volatile SingularAttribute<VegetableForms, Boolean> examPerformed;
    public static volatile SingularAttribute<VegetableForms, Boolean> materialVarietyIdentification;
    public static volatile ListAttribute<VegetableForms, VegetableAnnexesData> annexesData;
    public static volatile SingularAttribute<VegetableForms, String> discountFile;
    public static volatile SingularAttribute<VegetableForms, Declaration> declaration;
    public static volatile SingularAttribute<VegetableForms, String> genericDenomination;
    public static volatile SingularAttribute<VegetableForms, String> featuresDescription;
    public static volatile SingularAttribute<VegetableForms, Integer> countryLivingSample;
    public static volatile ListAttribute<VegetableForms, PersonVegetable> personVegetables;
    public static volatile SingularAttribute<VegetableForms, String> varietalGroup;
    public static volatile SingularAttribute<VegetableForms, Integer> geographicOrigin;
    public static volatile SingularAttribute<VegetableForms, String> varietyTransferDescription;

}