package senadi.gob.ec.adminob.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import senadi.gob.ec.adminob.enums.EstadoAnualidad;

@Entity
@Table(name = "anualidades")
public class Anualidad implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "vegetable_form_id", nullable = false)
    private Integer vegetableFormId;

    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "etiqueta", nullable = false, length = 20)
    private String etiqueta;

    @Column(name = "fecha_vencimiento")
    private Date fechaVencimiento;

    @Column(name = "fecha_pago")
    private Date fechaPago;

    @Column(name = "monto", precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(name = "numero_comprobante", length = 150)
    private String numeroComprobante;

    @Column(name = "valor_pagado_recargo", precision = 15, scale = 2)
    private BigDecimal valorPagadoRecargo;

    @Column(name = "numero_comprobante_recargo", length = 150)
    private String numeroComprobanteRecargo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    private EstadoAnualidad estado = EstadoAnualidad.PENDIENTE;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "usuario_registro", length = 100)
    private String usuarioRegistro;

    @Column(name = "fecha_creacion", nullable = false)
    private Timestamp fechaCreacion;

    @Column(name = "fecha_modificacion")
    private Timestamp fechaModificacion;

    // ── Campos de control de plazos y recargos ──

    /** Monto base que debe pagarse (sin recargo). */
    @Column(name = "monto_original", precision = 15, scale = 2)
    private BigDecimal montoOriginal;

    /** Porcentaje de recargo durante el período de gracia (ej: 10.00 = 10%). */
    @Column(name = "porcentaje_recargo", precision = 5, scale = 2)
    private BigDecimal porcentajeRecargo;

    /** Fecha límite de pago sin recargo (fechaVencimiento + 4 meses). */
    @Column(name = "fecha_limite_pago")
    private Date fechaLimitePago;

    /** Fecha límite incluyendo período de gracia (fechaVencimiento + 10 meses). */
    @Column(name = "fecha_limite_gracia")
    private Date fechaLimiteGracia;

    /** Porcentaje de incremento quinquenal aplicado al calcular montoOriginal. */
    @Column(name = "incremento_aplicado", precision = 5, scale = 2)
    private BigDecimal incrementoAplicado;

    // ── getters / setters base ──

    public Integer getId()                              { return id; }
    public void    setId(Integer id)                    { this.id = id; }

    public Integer getVegetableFormId()                 { return vegetableFormId; }
    public void    setVegetableFormId(Integer v)        { this.vegetableFormId = v; }

    public Integer getAnio()                            { return anio; }
    public void    setAnio(Integer anio)                { this.anio = anio; }

    public String  getEtiqueta()                        { return etiqueta; }
    public void    setEtiqueta(String etiqueta)         { this.etiqueta = etiqueta; }

    public Date    getFechaVencimiento()                { return fechaVencimiento; }
    public void    setFechaVencimiento(Date v)          { this.fechaVencimiento = v; }

    public Date    getFechaPago()                       { return fechaPago; }
    public void    setFechaPago(Date v)                 { this.fechaPago = v; }

    public BigDecimal getMonto()                        { return monto; }
    public void       setMonto(BigDecimal v)            { this.monto = v; }

    public String  getNumeroComprobante()               { return numeroComprobante; }
    public void    setNumeroComprobante(String v)       { this.numeroComprobante = v; }

    public BigDecimal getValorPagadoRecargo()           { return valorPagadoRecargo; }
    public void       setValorPagadoRecargo(BigDecimal v){ this.valorPagadoRecargo = v; }

    public String  getNumeroComprobanteRecargo()        { return numeroComprobanteRecargo; }
    public void    setNumeroComprobanteRecargo(String v){ this.numeroComprobanteRecargo = v; }

    public EstadoAnualidad getEstado()                  { return estado; }
    public void            setEstado(EstadoAnualidad v) { this.estado = v; }

    public String  getObservaciones()                   { return observaciones; }
    public void    setObservaciones(String v)           { this.observaciones = v; }

    public String  getUsuarioRegistro()                 { return usuarioRegistro; }
    public void    setUsuarioRegistro(String v)         { this.usuarioRegistro = v; }

    public Timestamp getFechaCreacion()                 { return fechaCreacion; }
    public void      setFechaCreacion(Timestamp v)      { this.fechaCreacion = v; }

    public Timestamp getFechaModificacion()             { return fechaModificacion; }
    public void      setFechaModificacion(Timestamp v)  { this.fechaModificacion = v; }

    public BigDecimal getMontoOriginal()                { return montoOriginal; }
    public void       setMontoOriginal(BigDecimal v)    { this.montoOriginal = v; }

    public BigDecimal getPorcentajeRecargo()            { return porcentajeRecargo; }
    public void       setPorcentajeRecargo(BigDecimal v){ this.porcentajeRecargo = v; }

    public Date    getFechaLimitePago()                 { return fechaLimitePago; }
    public void    setFechaLimitePago(Date v)           { this.fechaLimitePago = v; }

    public Date    getFechaLimiteGracia()               { return fechaLimiteGracia; }
    public void    setFechaLimiteGracia(Date v)         { this.fechaLimiteGracia = v; }

    public BigDecimal getIncrementoAplicado()           { return incrementoAplicado; }
    public void       setIncrementoAplicado(BigDecimal v){ this.incrementoAplicado = v; }

    // ── helpers para p:datePicker (recibe java.util.Date) ──

    public java.util.Date getFechaPagoUtil() { return fechaPago; }
    public void setFechaPagoUtil(java.util.Date v) {
        this.fechaPago = v != null ? new java.sql.Date(v.getTime()) : null;
    }

    public java.util.Date getFechaVencimientoUtil() { return fechaVencimiento; }
    public void setFechaVencimientoUtil(java.util.Date v) {
        this.fechaVencimiento = v != null ? new java.sql.Date(v.getTime()) : null;
    }

    // ── cálculos de recargo y montos ──

    /** Valor del recargo = montoOriginal × porcentajeRecargo / 100. */
    public BigDecimal getMontoRecargo() {
        if (montoOriginal == null || porcentajeRecargo == null) return BigDecimal.ZERO;
        return montoOriginal.multiply(porcentajeRecargo)
               .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /** Monto pendiente = montoOriginal − lo ya pagado (monto). */
    public BigDecimal getMontoPendiente() {
        if (estado == EstadoAnualidad.PAGADO) return BigDecimal.ZERO;
        BigDecimal base  = montoOriginal != null ? montoOriginal : BigDecimal.ZERO;
        BigDecimal pagado = monto != null ? monto : BigDecimal.ZERO;
        BigDecimal diff  = base.subtract(pagado);
        return diff.compareTo(BigDecimal.ZERO) > 0 ? diff : BigDecimal.ZERO;
    }

    /** Total a pagar: solo añade recargo si está dentro del período de gracia (meses 4-10). */
    public BigDecimal getTotalAPagar() {
        BigDecimal pendiente = getMontoPendiente();
        if (isEnPeriodoGracia()) {
            return pendiente.add(getMontoRecargo());
        }
        return pendiente;
    }

    // ── detección de período ──

    /**
     * True ÚNICAMENTE si el pago ya superó los 4 meses de plazo normal
     * Y aún no venció el período de gracia de 6 meses (total 10 meses).
     * Usa tanto el estado guardado en BD como las fechas en tiempo real.
     */
    public boolean isEnPeriodoGracia() {
        if (estado == EstadoAnualidad.PAGADO)           return false;
        if (estado == EstadoAnualidad.PROCESO_EXPIRADO) return false;
        // Si el sistema ya marcó EN_GRACIA en BD, es suficiente
        if (estado == EstadoAnualidad.EN_GRACIA) return true;
        // Verificación en tiempo real por fechas
        if (fechaLimitePago == null) return false;
        long now       = System.currentTimeMillis();
        long limPago   = fechaLimitePago.getTime();
        long limGracia = fechaLimiteGracia != null ? fechaLimiteGracia.getTime() : Long.MAX_VALUE;
        return now > limPago && now <= limGracia;
    }

    /** True si expiró el período de gracia sin pago. */
    public boolean isProcesoExpirado() {
        if (estado == EstadoAnualidad.PAGADO) return false;
        if (fechaLimiteGracia == null) return false;
        return System.currentTimeMillis() > fechaLimiteGracia.getTime();
    }

    // ── métodos de estado (legado + nuevos) ──

    public boolean isProximaAVencer(int dias) {
        if (fechaLimitePago == null && fechaVencimiento == null) return false;
        if (estado == EstadoAnualidad.PAGADO) return false;
        Date referencia = fechaLimitePago != null ? fechaLimitePago : fechaVencimiento;
        long diff = referencia.getTime() - System.currentTimeMillis();
        return diff >= 0 && diff <= (long) dias * 86_400_000L;
    }

    public boolean isVencida() {
        if (estado == EstadoAnualidad.PAGADO) return false;
        if (estado == EstadoAnualidad.PROCESO_EXPIRADO) return false;
        if (fechaLimitePago != null) {
            return fechaLimitePago.getTime() < System.currentTimeMillis();
        }
        if (fechaVencimiento == null) return false;
        return fechaVencimiento.getTime() < System.currentTimeMillis();
    }

    public String getRowClass() {
        if (estado == EstadoAnualidad.PAGADO)           return "anu-row-pagada";
        if (estado == EstadoAnualidad.PROCESO_EXPIRADO) return "anu-row-expirada";
        if (estado == EstadoAnualidad.EN_GRACIA
                || isEnPeriodoGracia())                  return "anu-row-gracia";
        if (isVencida())                                return "anu-row-vencida";
        if (isProximaAVencer(15))                       return "anu-row-alerta";
        return "";
    }

    public String getEstadoLabel() {
        if (estado == null)                                      return "Pendiente";
        switch (estado) {
            case PAGADO:           return "Completado";
            case VENCIDO:          return "Vencido";
            case EN_GRACIA:        return "Período de Gracia";
            case PROCESO_EXPIRADO: return "Proceso Expirado";
            default:               return "Pendiente";
        }
    }

    public String getEstadoBadgeClass() {
        if (estado == EstadoAnualidad.PAGADO)           return "anu-badge anu-pagado";
        if (estado == EstadoAnualidad.PROCESO_EXPIRADO) return "anu-badge anu-expirado";
        if (estado == EstadoAnualidad.EN_GRACIA
                || isEnPeriodoGracia())                  return "anu-badge anu-gracia";
        if (isVencida())                                return "anu-badge anu-vencido";
        if (isProximaAVencer(15))                       return "anu-badge anu-alerta";
        return "anu-badge anu-pendiente";
    }

    public String getEstadoBadgeLabel() {
        if (estado == EstadoAnualidad.PAGADO)           return "✓ COMPLETADO";
        if (estado == EstadoAnualidad.PROCESO_EXPIRADO) return "✗ EXPIRADO";
        if (estado == EstadoAnualidad.EN_GRACIA
                || isEnPeriodoGracia())                  return "⚠ EN GRACIA";
        if (isVencida())                                return "VENCIDA";
        if (isProximaAVencer(15))                       return "⚠ 15 DÍAS";
        return "PENDIENTE";
    }

    /** Etiqueta del período actual para mostrar en UI. */
    public String getPeriodoActualLabel() {
        if (estado == EstadoAnualidad.PAGADO)           return "Pagado";
        if (estado == EstadoAnualidad.PROCESO_EXPIRADO) return "Proceso Expirado";
        long now = System.currentTimeMillis();
        if (fechaLimiteGracia != null && now > fechaLimiteGracia.getTime()) return "Expirado";
        if (fechaLimitePago   != null && now > fechaLimitePago.getTime())   return "Período de Gracia";
        if (fechaLimitePago   != null && now <= fechaLimitePago.getTime())  return "Plazo Normal";
        return "Sin fecha";
    }
}
