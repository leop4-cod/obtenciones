/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package senadi.gob.ec.adminob.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.primefaces.model.file.UploadedFile;

/**
 *
 * @author michael
 */
@Entity
@Table(name = "vegetable_annexes")
public class VegetableAnnexes implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;
    
    @Lob
    @Column(name = "full_name")
    private String fullName;
    
    @Transient
    private boolean required;
    
    @Transient
    private boolean error;
    
    @Transient
    private UploadedFile currentFile;
    
    @Transient
    private byte[] fileContent;
    
    @Transient
    private boolean withFile;
    
    @Transient
    private Integer idAnnexes;
    
    @Transient
    private Integer idVegatableForms;

    @OneToMany(mappedBy = "vegetableAnnexes", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VegetableAnnexesData> annexesData = new ArrayList<>();

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
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param fullName the fullName to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return the currentFile
     */
    public UploadedFile getCurrentFile() {
        return currentFile;
    }

    /**
     * @param currentFile the currentFile to set
     */
    public void setCurrentFile(UploadedFile currentFile) {
        this.currentFile = currentFile;
    }    

    /**
     * @return the required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * @param required the required to set
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * @return the error
     */
    public boolean isError() {
        return error;
    }

    /**
     * @param error the error to set
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * @return the fileContent
     */
    public byte[] getFileContent() {
        return fileContent;
    }

    /**
     * @param fileContent the fileContent to set
     */
    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    /**
     * @return the withFile
     */
    public boolean isWithFile() {
        return withFile;
    }

    /**
     * @param withFile the withFile to set
     */
    public void setWithFile(boolean withFile) {
        this.withFile = withFile;
    }

    /**
     * @return the idAnnexes
     */
    public Integer getIdAnnexes() {
        return idAnnexes;
    }

    /**
     * @param idAnnexes the idAnnexes to set
     */
    public void setIdAnnexes(Integer idAnnexes) {
        this.idAnnexes = idAnnexes;
    }

    /**
     * @return the idVegatableForms
     */
    public Integer getIdVegatableForms() {
        return idVegatableForms;
    }

    /**
     * @param idVegatableForms the idVegatableForms to set
     */
    public void setIdVegatableForms(Integer idVegatableForms) {
        this.idVegatableForms = idVegatableForms;
    }

}
