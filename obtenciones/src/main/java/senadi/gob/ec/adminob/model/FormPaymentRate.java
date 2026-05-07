/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package senadi.gob.ec.adminob.model;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author Michael
 */

@Entity
@Table(name = "form_payment_rate")
public class FormPaymentRate implements Serializable{
    
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;    
    
    @Column(name = "form_payment_rate_id")
    private Integer formPaymentRateId;
    
    @Column(name = "payment_rate_id")
    private Integer paymentRateId;
    
    @Column(name = "create_date")    
    private Timestamp createDate;
    
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
     * @return the formPaymentRateId
     */
    public Integer getFormPaymentRateId() {
        return formPaymentRateId;
    }

    /**
     * @param formPaymentRateId the formPaymentRateId to set
     */
    public void setFormPaymentRateId(Integer formPaymentRateId) {
        this.formPaymentRateId = formPaymentRateId;
    }

    /**
     * @return the paymentRateId
     */
    public Integer getPaymentRateId() {
        return paymentRateId;
    }

    /**
     * @param paymentRateId the paymentRateId to set
     */
    public void setPaymentRateId(Integer paymentRateId) {
        this.paymentRateId = paymentRateId;
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
    
}
