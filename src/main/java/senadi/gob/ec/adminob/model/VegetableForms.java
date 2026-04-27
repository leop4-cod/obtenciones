/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package senadi.gob.ec.adminob.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import senadi.gob.ec.adminob.enums.DenominationType;
import senadi.gob.ec.adminob.enums.PersonType;
import senadi.gob.ec.adminob.enums.Status;
import senadi.gob.ec.adminob.enums.StatusFlow;
import senadi.gob.ec.adminob.enums.VarietyTransferType;
import senadi.gob.ec.adminob.solicitudes.Owners;
import senadi.gob.ec.adminob.util.Controller;

/**
 *
 * @author michael
 */
@Entity
@Table(name = "vegetable_forms")
public class VegetableForms implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "create_date")
    private Timestamp createDate;

    @Column(name = "priority_date")
    private Timestamp priorityDate;

    @Column(name = "application_number")
    private String applicationNumber;

    @Column(name = "botanical_taxon", columnDefinition = "TEXT")
    private String botanicalTaxon;

    @Column(name = "common_name", columnDefinition = "TEXT")
    private String commonName;

    @Column(name = "provitional_designation")
    private String provitionalDesignation;

    @Column(name = "generic_denomination")
    private String genericDenomination;

    @Enumerated(EnumType.STRING)
    @Column(name = "denomination_type")
    private DenominationType denominationType;

    @Column(name = "variety_transfer")
    private Boolean varietyTransfer;

    /*Sección cómo se ha transferido la variedad...*/
    @Enumerated(EnumType.STRING)
    @Column(name = "variety_transfer_type")
    private VarietyTransferType varietyTransferType;

    @Column(name = "variety_transfer_description")
    private String varietyTransferDescription;

    @Column(name = "geographic_origin")
    private Integer geographicOrigin;
    /*fin Sección cómo se ha transferido la variedad...*/

    @Column(name = "has_other_applications")
    private Boolean hasOtherApplications;

    @Column(name = "priority_claim")
    private Boolean priorityClaim;

    @Column(name = "in_territory")
    private Boolean inTerritory;

    @Column(name = "out_territory")
    private Boolean outTerritory;

    @Column(name = "exam_performed")
    private Boolean examPerformed;

    @Column(name = "exam_in_process")
    private Boolean examInProcess;

    @Column(name = "no_exam_yet")
    private Boolean noExamYet;

    @Column(name = "country_exam")
    private Integer countryExam;

    @Column(name = "living_sample")
    private Boolean livingSample;

    @Column(name = "sample_place")
    private String samplePlace;

    @Column(name = "country_living_sample")
    private Integer countryLivingSample;

    @Column(name = "genealogy")
    private String genealogy;

    @Column(name = "process_history")
    private String processHistory;

    @Column(name = "features_description")
    private String featuresDescription;

    @Column(name = "geographical_material_origin")
    private String geographicalMaterialOrigin;

    @Column(name = "geographical_variety_origin")
    private String geographicalVarietyOrigin;

    @Column(name = "reproduction_mechanism")
    private String reproductionMechanism;

    @Column(name = "additional_information")
    private String additionalInformation;

    @Column(name = "material_variety_identification")
    private Boolean materialVarietyIdentification;

    @Column(name = "product_variety_identification")
    private Boolean productVarietyIdentification;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "application_date")
    private Timestamp applicationDate;

    @Column(name = "owner_id")
    private Integer ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "person_noti_direction")
    private PersonType personNotiDirection;

    @Column(name = "electronic_communication_consent")
    private Boolean electronicCommunicationConsent;
    
    @Column(name = "varietal_group")
    private String varietalGroup;
    
    @Column(name = "payment_receipt_id")
    private Integer paymentReceiptId;
    
    @Column(name = "discount_file")
    private String discountFile;
    
    @Column(name = "assigned_user")
    private String assignedUser;
    
    @Column(name = "assigned_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date assignedDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status_flow")
    private StatusFlow statusFlow;
    
    @Transient
    private String locker;

    @OneToOne(mappedBy = "vegetableForms", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private VegetablePriority vegetablePriority;

    @OneToMany(mappedBy = "vegetableForms", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VegetableProtection> vegetableProtections = new ArrayList<>();

    @OneToMany(mappedBy = "vegetableForms", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExploitedSelled> exploitedSelleds = new ArrayList<>();

    @OneToMany(mappedBy = "vegetableForms", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VarietyCharacters> varietiesCharacters = new ArrayList<>();    

    @OneToMany(mappedBy = "vegetableForms", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SimilaryVariety> similaritiesVariety = new ArrayList<>();    
    
    @OneToMany(mappedBy = "vegetableForms", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VegetableAnnexesData> annexesData = new ArrayList<>();

    @OneToMany(mappedBy = "vegetableForms", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VegetableMethodology> vegetableMethodologies = new ArrayList<>();

    @OneToMany(mappedBy = "vegetableForms", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonVegetable> personVegetables = new ArrayList<>();
    
    @OneToOne(mappedBy = "vegetableForms", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Declaration declaration;
    
    @OneToOne(mappedBy = "vegetableForms", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private FormPaymentRate formPaymentRate;        

    public VegetableForms() {
        botanicalTaxon = "";
        commonName = "";
        provitionalDesignation = "";
        varietyTransferDescription = "";
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the createDate
     */
    public Timestamp getCreateDate() {
        return createDate;
    }

    /**
     * @param createDate the createDate to set
     */
    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    /**
     * @return the priorityDate
     */
    public Timestamp getPriorityDate() {
        return priorityDate;
    }

    /**
     * @param priorityDate the priorityDate to set
     */
    public void setPriorityDate(Timestamp priorityDate) {
        this.priorityDate = priorityDate;
    }

    /**
     * @return the applicationNumber
     */
    public String getApplicationNumber() {
        return applicationNumber;
    }

    /**
     * @param applicationNumber the applicationNumber to set
     */
    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    /**
     * @return the botanicalTaxon
     */
    public String getBotanicalTaxon() {
        return botanicalTaxon;
    }

    /**
     * @param botanicalTaxon the botanicalTaxon to set
     */
    public void setBotanicalTaxon(String botanicalTaxon) {
        this.botanicalTaxon = botanicalTaxon;
    }

    /**
     * @return the commonName
     */
    public String getCommonName() {
        return commonName;
    }

    /**
     * @param commonName the commonName to set
     */
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    /**
     * @return the provitionalDesignation
     */
    public String getProvitionalDesignation() {
        return provitionalDesignation;
    }

    /**
     * @param provitionalDesignation the provitionalDesignation to set
     */
    public void setProvitionalDesignation(String provitionalDesignation) {
        this.provitionalDesignation = provitionalDesignation;
    }

    /**
     * @return the genericDenomination
     */
    public String getGenericDenomination() {
        return genericDenomination;
    }

    /**
     * @param genericDenomination the genericDenomination to set
     */
    public void setGenericDenomination(String genericDenomination) {
        this.genericDenomination = genericDenomination;
    }

    /**
     * @return the denominationType
     */
    public DenominationType getDenominationType() {
        return denominationType;
    }

    /**
     * @param denominationType the denominationType to set
     */
    public void setDenominationType(DenominationType denominationType) {
        this.denominationType = denominationType;
    }

    /**
     * @return the varietyTransferType
     */
    public VarietyTransferType getVarietyTransferType() {
        return varietyTransferType;
    }

    /**
     * @param varietyTransferType the varietyTransferType to set
     */
    public void setVarietyTransferType(VarietyTransferType varietyTransferType) {
        this.varietyTransferType = varietyTransferType;
    }

    /**
     * @return the varietyTransferDescription
     */
    public String getVarietyTransferDescription() {
        return varietyTransferDescription;
    }

    /**
     * @param varietyTransferDescription the varietyTransferDescription to set
     */
    public void setVarietyTransferDescription(String varietyTransferDescription) {
        this.varietyTransferDescription = varietyTransferDescription;
    }

    /**
     * @return the geographicOrigin
     */
    public Integer getGeographicOrigin() {
        return geographicOrigin;
    }

    /**
     * @param geographicOrigin the geographicOrigin to set
     */
    public void setGeographicOrigin(Integer geographicOrigin) {
        this.geographicOrigin = geographicOrigin;
    }

    /**
     * @return the examPerformed
     */
    public Boolean getExamPerformed() {
        return examPerformed;
    }

    /**
     * @param examPerformed the examPerformed to set
     */
    public void setExamPerformed(Boolean examPerformed) {
        this.examPerformed = examPerformed;
    }

    /**
     * @return the examInProcess
     */
    public Boolean getExamInProcess() {
        return examInProcess;
    }

    /**
     * @param examInProcess the examInProcess to set
     */
    public void setExamInProcess(Boolean examInProcess) {
        this.examInProcess = examInProcess;
    }

    /**
     * @return the noExamYet
     */
    public Boolean getNoExamYet() {
        return noExamYet;
    }

    /**
     * @param noExamYet the noExamYet to set
     */
    public void setNoExamYet(Boolean noExamYet) {
        this.noExamYet = noExamYet;
    }

    /**
     * @return the livingSample
     */
    public Boolean getLivingSample() {
        return livingSample;
    }

    /**
     * @param livingSample the livingSample to set
     */
    public void setLivingSample(Boolean livingSample) {
        this.livingSample = livingSample;
    }

    /**
     * @return the samplePlace
     */
    public String getSamplePlace() {
        return samplePlace;
    }

    /**
     * @param samplePlace the samplePlace to set
     */
    public void setSamplePlace(String samplePlace) {
        this.samplePlace = samplePlace;
    }

    /**
     * @return the genealogy
     */
    public String getGenealogy() {
        return genealogy;
    }

    /**
     * @param genealogy the genealogy to set
     */
    public void setGenealogy(String genealogy) {
        this.genealogy = genealogy;
    }

    /**
     * @return the processHistory
     */
    public String getProcessHistory() {
        return processHistory;
    }

    /**
     * @param processHistory the processHistory to set
     */
    public void setProcessHistory(String processHistory) {
        this.processHistory = processHistory;
    }

    /**
     * @return the featuresDescription
     */
    public String getFeaturesDescription() {
        return featuresDescription;
    }

    /**
     * @param featuresDescription the featuresDescription to set
     */
    public void setFeaturesDescription(String featuresDescription) {
        this.featuresDescription = featuresDescription;
    }

    /**
     * @return the geographicalMaterialOrigin
     */
    public String getGeographicalMaterialOrigin() {
        return geographicalMaterialOrigin;
    }

    /**
     * @param geographicalMaterialOrigin the geographicalMaterialOrigin to set
     */
    public void setGeographicalMaterialOrigin(String geographicalMaterialOrigin) {
        this.geographicalMaterialOrigin = geographicalMaterialOrigin;
    }

    /**
     * @return the geographicalVarietyOrigin
     */
    public String getGeographicalVarietyOrigin() {
        return geographicalVarietyOrigin;
    }

    /**
     * @param geographicalVarietyOrigin the geographicalVarietyOrigin to set
     */
    public void setGeographicalVarietyOrigin(String geographicalVarietyOrigin) {
        this.geographicalVarietyOrigin = geographicalVarietyOrigin;
    }

    /**
     * @return the reproductionMechanism
     */
    public String getReproductionMechanism() {
        return reproductionMechanism;
    }

    /**
     * @param reproductionMechanism the reproductionMechanism to set
     */
    public void setReproductionMechanism(String reproductionMechanism) {
        this.reproductionMechanism = reproductionMechanism;
    }

    /**
     * @return the additionalInformation
     */
    public String getAdditionalInformation() {
        return additionalInformation;
    }

    /**
     * @param additionalInformation the additionalInformation to set
     */
    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    /**
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @return the applicationDate
     */
    public Timestamp getApplicationDate() {
        return applicationDate;
    }

    /**
     * @param applicationDate the applicationDate to set
     */
    public void setApplicationDate(Timestamp applicationDate) {
        this.applicationDate = applicationDate;
    }

    /**
     * @return the materialVarietyIdentification
     */
    public Boolean getMaterialVarietyIdentification() {
        return materialVarietyIdentification;
    }

    /**
     * @param materialVarietyIdentification the materialVarietyIdentification to
     * set
     */
    public void setMaterialVarietyIdentification(Boolean materialVarietyIdentification) {
        this.materialVarietyIdentification = materialVarietyIdentification;
    }

    /**
     * @return the productVarietyIdentification
     */
    public Boolean getProductVarietyIdentification() {
        return productVarietyIdentification;
    }

    /**
     * @param productVarietyIdentification the productVarietyIdentification to
     * set
     */
    public void setProductVarietyIdentification(Boolean productVarietyIdentification) {
        this.productVarietyIdentification = productVarietyIdentification;
    }

    /**
     * @return the annexesData
     */
    public List<VegetableAnnexesData> getAnnexesData() {
        return annexesData;
    }

    /**
     * @param annexesData the annexesData to set
     */
    public void setAnnexesData(List<VegetableAnnexesData> annexesData) {
        this.annexesData = annexesData;
    }

    /**
     * @return the vegetableMethodologies
     */
    public List<VegetableMethodology> getVegetableMethodologies() {
        return vegetableMethodologies;
    }

    /**
     * @param vegetableMethodologies the vegetableMethodologies to set
     */
    public void setVegetableMethodologies(List<VegetableMethodology> vegetableMethodologies) {
        this.vegetableMethodologies = vegetableMethodologies;
    }

    /**
     * @return the personVegetables
     */
    public List<PersonVegetable> getPersonVegetables() {
        return personVegetables;
    }

    /**
     * @param personVegetables the personVegetables to set
     */
    public void setPersonVegetables(List<PersonVegetable> personVegetables) {
        this.personVegetables = personVegetables;
    }

    /**
     * @return the vegetableProtections
     */
    public List<VegetableProtection> getVegetableProtections() {
        return vegetableProtections;
    }

    /**
     * @param vegetableProtections the vegetableProtections to set
     */
    public void setVegetableProtections(List<VegetableProtection> vegetableProtections) {
        this.vegetableProtections = vegetableProtections;
    }

    /**
     * @return the exploitedSelleds
     */
    public List<ExploitedSelled> getExploitedSelleds() {
        return exploitedSelleds;
    }

    /**
     * @param exploitedSelleds the exploitedSelleds to set
     */
    public void setExploitedSelleds(List<ExploitedSelled> exploitedSelleds) {
        this.exploitedSelleds = exploitedSelleds;
    }

    /**
     * @return the ownerId
     */
    public Integer getOwnerId() {
        return ownerId;
    }

    /**
     * @param ownerId the ownerId to set
     */
    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * @return the vegetablePriority
     */
    public VegetablePriority getVegetablePriority() {
        return vegetablePriority;
    }

    /**
     * @param vegetablePriority the vegetablePriority to set
     */
    public void setVegetablePriority(VegetablePriority vegetablePriority) {
        this.vegetablePriority = vegetablePriority;
    }

    /**
     * @return the personNotiDirection
     */
    public PersonType getPersonNotiDirection() {
        return personNotiDirection;
    }

    /**
     * @param personNotiDirection the personNotiDirection to set
     */
    public void setPersonNotiDirection(PersonType personNotiDirection) {
        this.personNotiDirection = personNotiDirection;
    }

    /**
     * @return the hasOtherApplications
     */
    public Boolean getHasOtherApplications() {
        return hasOtherApplications;
    }

    /**
     * @param hasOtherApplications the hasOtherApplications to set
     */
    public void setHasOtherApplications(Boolean hasOtherApplications) {
        this.hasOtherApplications = hasOtherApplications;
    }

    /**
     * @return the priorityClaim
     */
    public Boolean getPriorityClaim() {
        return priorityClaim;
    }

    /**
     * @param priorityClaim the priorityClaim to set
     */
    public void setPriorityClaim(Boolean priorityClaim) {
        this.priorityClaim = priorityClaim;
    }

    /**
     * @return the inTerritory
     */
    public Boolean getInTerritory() {
        return inTerritory;
    }

    /**
     * @param inTerritory the inTerritory to set
     */
    public void setInTerritory(Boolean inTerritory) {
        this.inTerritory = inTerritory;
    }

    /**
     * @return the outTerritory
     */
    public Boolean getOutTerritory() {
        return outTerritory;
    }

    /**
     * @param outTerritory the outTerritory to set
     */
    public void setOutTerritory(Boolean outTerritory) {
        this.outTerritory = outTerritory;
    }

    /**
     * @return the countryLivingSample
     */
    public Integer getCountryLivingSample() {
        return countryLivingSample;
    }

    /**
     * @param countryLivingSample the countryLivingSample to set
     */
    public void setCountryLivingSample(Integer countryLivingSample) {
        this.countryLivingSample = countryLivingSample;
    }

    /**
     * @return the varietyTransfer
     */
    public Boolean getVarietyTransfer() {
        return varietyTransfer;
    }

    /**
     * @param varietyTransfer the varietyTransfer to set
     */
    public void setVarietyTransfer(Boolean varietyTransfer) {
        this.varietyTransfer = varietyTransfer;
    }

    /**
     * @return the varietiesCharacters
     */
    public List<VarietyCharacters> getVarietiesCharacters() {
        return varietiesCharacters;
    }

    /**
     * @param varietiesCharacters the varietiesCharacters to set
     */
    public void setVarietiesCharacters(List<VarietyCharacters> varietiesCharacters) {
        this.varietiesCharacters = varietiesCharacters;
    }

    /**
     * @return the similaritiesVariety
     */
    public List<SimilaryVariety> getSimilaritiesVariety() {
        return similaritiesVariety;
    }

    /**
     * @param similaritiesVariety the similaritiesVariety to set
     */
    public void setSimilaritiesVariety(List<SimilaryVariety> similaritiesVariety) {
        this.similaritiesVariety = similaritiesVariety;
    }

    /**
     * @return the electronicCommunicationConsent
     */
    public Boolean getElectronicCommunicationConsent() {
        return electronicCommunicationConsent;
    }

    /**
     * @param electronicCommunicationConsent the electronicCommunicationConsent
     * to set
     */
    public void setElectronicCommunicationConsent(Boolean electronicCommunicationConsent) {
        this.electronicCommunicationConsent = electronicCommunicationConsent;
    }

    /**
     * @return the countryExam
     */
    public Integer getCountryExam() {
        return countryExam;
    }

    /**
     * @param countryExam the countryExam to set
     */
    public void setCountryExam(Integer countryExam) {
        this.countryExam = countryExam;
    }

    @Override
    public String toString() {
        return getId() + " - " + getBotanicalTaxon() + " - " + getCommonName();
    }

    /**
     * @return the declaration
     */
    public Declaration getDeclaration() {
        return declaration;
    }

    /**
     * @param declaration the declaration to set
     */
    public void setDeclaration(Declaration declaration) {
        this.declaration = declaration;
    }

    /**
     * @return the formPaymentRate
     */
    public FormPaymentRate getFormPaymentRate() {
        return formPaymentRate;
    }

    /**
     * @param formPaymentRate the formPaymentRate to set
     */
    public void setFormPaymentRate(FormPaymentRate formPaymentRate) {
        this.formPaymentRate = formPaymentRate;
    }

    /**
     * @return the varietalGroup
     */
    public String getVarietalGroup() {
        return varietalGroup;
    }

    /**
     * @param varietalGroup the varietalGroup to set
     */
    public void setVarietalGroup(String varietalGroup) {
        this.varietalGroup = varietalGroup;
    }
    
    public List<Person> getPersonsType(String type){
        List<PersonVegetable> persons = getPersonVegetables();
        List<Person> people = new ArrayList<>();
        for (int i = 0; i < persons.size(); i++) {
            if(persons.get(i).getPersonType().equals(PersonType.APPLICANT)){
                people.add(persons.get(i).getPerson());
            }
        }
        return people;
    }

    /**
     * @return the paymentReceiptId
     */
    public Integer getPaymentReceiptId() {
        return paymentReceiptId;
    }

    /**
     * @param paymentReceiptId the paymentReceiptId to set
     */
    public void setPaymentReceiptId(Integer paymentReceiptId) {
        this.paymentReceiptId = paymentReceiptId;
    }

    /**
     * @return the discountFile
     */
    public String getDiscountFile() {
        return discountFile;
    }

    /**
     * @param discountFile the discountFile to set
     */
    public void setDiscountFile(String discountFile) {
        this.discountFile = discountFile;
    }

    /**
     * @return the locker
     */
    public String getLocker() {
        if (locker != null && !locker.trim().isEmpty()) {
            return locker;
        }
        try {
            Controller c = new Controller();
            Owners ow = c.getOwnerById(getOwnerId());
            if (ow == null) {
                return "SIN CASILLERO";
            }
            String casillero = ow.getCasillero() == null ? "SIN CASILLERO" : ow.getCasillero();
            String nombres = ((ow.getFirsName() == null ? "" : ow.getFirsName()) + " "
                    + (ow.getLastName() == null ? "" : ow.getLastName())).trim();
            if (nombres.isEmpty()) {
                return casillero;
            }
            return casillero + " - " + nombres;
        } catch (Exception ex) {
            return "SIN CASILLERO";
        }
    }

    /**
     * @param locker the locker to set
     */
    public void setLocker(String locker) {
        this.locker = locker;
    }       

    /**
     * @return the assignedUser
     */
    public String getAssignedUser() {
        return assignedUser;
    }

    /**
     * @param assignedUser the assignedUser to set
     */
    public void setAssignedUser(String assignedUser) {
        this.assignedUser = assignedUser;
    }

    /**
     * @return the assignedDate
     */
    public Date getAssignedDate() {
        return assignedDate;
    }

    /**
     * @param assignedDate the assignedDate to set
     */
    public void setAssignedDate(Date assignedDate) {
        this.assignedDate = assignedDate;
    }

    /**
     * @return the statusFlow
     */
    public StatusFlow getStatusFlow() {
        return statusFlow;
    }

    /**
     * @param statusFlow the statusFlow to set
     */
    public void setStatusFlow(StatusFlow statusFlow) {
        this.statusFlow = statusFlow;
    }

}
