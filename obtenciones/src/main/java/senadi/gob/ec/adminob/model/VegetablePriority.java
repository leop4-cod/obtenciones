/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package senadi.gob.ec.adminob.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;

/**
 *
 * @author michael
 */
@Entity
@Table(name = "vegetable_priority")
public class VegetablePriority implements Serializable{
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;        
    
    @Column(name = "country_id")
    private Integer countryId;
    
    @Column(name = "application_date")
    
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date applicationDate;
    
    @Column(name = "applicant_name")
    private String applicantName;
    
    @Column(name = "generic_denomination")
    private String genericDenomination;
    
    @Column(name = "application_number")
    private String applicationNumber;
    
    @Transient
    private String country;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vegetable_form_id", unique = true)
    private VegetableForms vegetableForms;

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
     * @return the countryId
     */
    public Integer getCountryId() {
        return countryId;
    }

    /**
     * @param countryId the countryId to set
     */
    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    /**
     * @return the applicantName
     */
    public String getApplicantName() {
        return applicantName;
    }

    /**
     * @param applicantName the applicantName to set
     */
    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
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
     * @return the vegetableForms
     */
    public VegetableForms getVegetableForms() {
        return vegetableForms;
    }

    /**
     * @param vegetableForms the vegetableForms to set
     */
    public void setVegetableForms(VegetableForms vegetableForms) {
        this.vegetableForms = vegetableForms;
    }

    /**
     * @return the applicationDate
     */
    public Date getApplicationDate() {
        return applicationDate;
    }

    /**
     * @param applicationDate the applicationDate to set
     */
    public void setApplicationDate(Date applicationDate) {
        this.applicationDate = applicationDate;
    }

    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }
}
