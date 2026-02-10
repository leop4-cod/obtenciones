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
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 *
 * @author michael
 */
@Entity
@Table(name = "person")
public class Person implements Serializable {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;
    
    @Column(name = "create_date")
    private Timestamp createDate;
    
    @Column(name = "identification_number")
    private String identificationNumber;
    
    @Transient
    private String identificationType;        
    
    @Transient
    private String address;
    
    @Transient
    private String email;
    
    @Transient
    private String phone;
    
    @Transient
    private String nationality;
    
    @Transient
    private Integer countryId;
    
    @Transient
    private Integer cityAddress;
    
    @Transient
    private Date dateBirth;
    
    @Transient
    private String gender;
    
    @Transient
    private String province;
    
    @Transient
    private String city;
    
    @Transient
    private String personNumber;
    
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonVegetable> personVegetables = new ArrayList<>();

    
    public Person(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }
    
    public Person() {
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
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
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
     * @return the identificationType
     */
    public String getIdentificationType() {
        return identificationType;
    }

    /**
     * @param identificationType the identificationType to set
     */
    public void setIdentificationType(String identificationType) {
        this.identificationType = identificationType;
    }

    /**
     * @return the identificationNumber
     */
    public String getIdentificationNumber() {
        return identificationNumber;
    }

    /**
     * @param identificationNumber the identificationNumber to set
     */
    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
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
     * @return the nationality
     */
    public String getNationality() {
        return nationality;
    }

    /**
     * @param nationality the nationality to set
     */
    public void setNationality(String nationality) {
        this.nationality = nationality;
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
     * @return the cityAddress
     */
    public Integer getCityAddress() {
        return cityAddress;
    }

    /**
     * @param cityAddress the cityAddress to set
     */
    public void setCityAddress(Integer cityAddress) {
        this.cityAddress = cityAddress;
    }

    /**
     * @return the dateBirth
     */
    public Date getDateBirth() {
        return dateBirth;
    }

    /**
     * @param dateBirth the dateBirth to set
     */
    public void setDateBirth(Date dateBirth) {
        this.dateBirth = dateBirth;
    }

    /**
     * @return the gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * @param gender the gender to set
     */
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    @Override
    public String toString(){
        return getName()+" -- "+getIdentificationNumber();
    }

    /**
     * @return the province
     */
    public String getProvince() {
        return province;
    }

    /**
     * @param province the province to set
     */
    public void setProvince(String province) {
        this.province = province;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return the personNumber
     */
    public String getPersonNumber() {
        return personNumber;
    }

    /**
     * @param personNumber the personNumber to set
     */
    public void setPersonNumber(String personNumber) {
        this.personNumber = personNumber;
    }
}
