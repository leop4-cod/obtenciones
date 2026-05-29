package senadi.gob.ec.adminob.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Parámetros de negocio configurables desde la interfaz.
 * Reemplaza los valores hardcodeados de meses de pago, período de gracia,
 * porcentaje de recargo e incremento quinquenal.
 */
@Entity
@Table(name = "parametros_sistema")
public class ParametroSistema implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Clave única del parámetro (p.ej. MESES_PLAZO_PAGO). */
    @Column(name = "clave", nullable = false, unique = true, length = 100)
    private String clave;

    /** Valor almacenado siempre como String; se convierte según tipo. */
    @Column(name = "valor", nullable = false, length = 500)
    private String valor;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    /** ENTERO | DECIMAL | TEXTO | BOOLEANO */
    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "usuario_mod", length = 100)
    private String usuarioMod;

    @Column(name = "fecha_mod")
    private Timestamp fechaMod;

    // ── getters / setters ──

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getUsuarioMod() { return usuarioMod; }
    public void setUsuarioMod(String usuarioMod) { this.usuarioMod = usuarioMod; }

    public Timestamp getFechaMod() { return fechaMod; }
    public void setFechaMod(Timestamp fechaMod) { this.fechaMod = fechaMod; }

    // ── helpers de conversión ──

    public int getValorEntero() {
        try { return Integer.parseInt(valor.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    public BigDecimal getValorDecimal() {
        try { return new BigDecimal(valor.trim()); }
        catch (NumberFormatException e) { return BigDecimal.ZERO; }
    }

    public boolean getValorBooleano() {
        return "true".equalsIgnoreCase(valor.trim()) || "1".equals(valor.trim());
    }
}
