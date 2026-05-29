package senadi.gob.ec.adminob.model;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Registro de auditoría/histórico de cambios en el sistema.
 * Captura quién hizo qué cambio, sobre qué entidad y cuándo,
 * con soporte para registrar el valor anterior y nuevo de cada campo.
 */
@Entity
@Table(name = "`history`")
public class History implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Número de solicitud del portal (applicationNumber de VegetableForms). Puede ser null. */
    @Column(name = "application_number", length = 200)
    private String applicationNumber;

    /**
     * Nombre de la entidad/módulo modificado.
     * Ej: "VegetableForms", "Tramite", "Anualidad"
     */
    @Column(name = "tipo_entidad", length = 100)
    private String tipoEntidad;

    /**
     * ID o número identificador de la entidad modificada.
     * Ej: "42" (ID de VegetableForms), "SOL-2025-001" (applicationNumber)
     */
    @Column(name = "id_entidad", length = 100)
    private String idEntidad;

    /**
     * Tipo de acción realizada.
     * Valores posibles: CREAR | ACTUALIZAR | ELIMINAR | CAMBIO_ESTADO | PAGO | IMPORTAR
     */
    @Column(name = "tipo_accion", length = 50)
    private String tipoAccion;

    /**
     * Nombre del campo o sección específica que fue modificada.
     * Puede ser null cuando el evento afecta al registro completo.
     */
    @Column(name = "campo_modificado", length = 200)
    private String campoModificado;

    /** Valor del campo ANTES del cambio. Null cuando es creación. */
    @Column(name = "valor_anterior", columnDefinition = "TEXT")
    private String valorAnterior;

    /** Valor del campo DESPUÉS del cambio. Null cuando es eliminación. */
    @Column(name = "valor_nuevo", columnDefinition = "TEXT")
    private String valorNuevo;

    /** Descripción libre / resumen del evento. */
    @Column(name = "razon", columnDefinition = "TEXT")
    private String description;

    /** Timestamp exacto del cambio. */
    @Column(name = "fecha")
    private Timestamp fecha;

    /** Usuario autenticado que realizó el cambio (login LDAP). */
    @Column(name = "history_user", length = 200)
    private String historyUser;

    // ── getters / setters ──

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getApplicationNumber() { return applicationNumber; }
    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public String getTipoEntidad() { return tipoEntidad; }
    public void setTipoEntidad(String tipoEntidad) { this.tipoEntidad = tipoEntidad; }

    public String getIdEntidad() { return idEntidad; }
    public void setIdEntidad(String idEntidad) { this.idEntidad = idEntidad; }

    public String getTipoAccion() { return tipoAccion; }
    public void setTipoAccion(String tipoAccion) { this.tipoAccion = tipoAccion; }

    public String getCampoModificado() { return campoModificado; }
    public void setCampoModificado(String campoModificado) {
        this.campoModificado = campoModificado;
    }

    public String getValorAnterior() { return valorAnterior; }
    public void setValorAnterior(String valorAnterior) { this.valorAnterior = valorAnterior; }

    public String getValorNuevo() { return valorNuevo; }
    public void setValorNuevo(String valorNuevo) { this.valorNuevo = valorNuevo; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    public String getHistoryUser() { return historyUser; }
    public void setHistoryUser(String historyUser) { this.historyUser = historyUser; }

    // ── helpers de UI ──

    public String getTipoAccionLabel() {
        if (tipoAccion == null) return "—";
        switch (tipoAccion) {
            case "CREAR":        return "Creación";
            case "ACTUALIZAR":   return "Actualización";
            case "ELIMINAR":     return "Eliminación";
            case "CAMBIO_ESTADO":return "Cambio de estado";
            case "PAGO":         return "Registro de pago";
            case "IMPORTAR":     return "Importación";
            default:             return tipoAccion;
        }
    }

    public String getTipoAccionBadgeClass() {
        if (tipoAccion == null) return "hist-badge-default";
        switch (tipoAccion) {
            case "CREAR":        return "hist-badge-crear";
            case "ELIMINAR":     return "hist-badge-eliminar";
            case "CAMBIO_ESTADO":return "hist-badge-estado";
            case "PAGO":         return "hist-badge-pago";
            default:             return "hist-badge-actualizar";
        }
    }

    @Override
    public String toString() {
        return getFecha() + " [" + getHistoryUser() + "] " + getDescription();
    }
}
