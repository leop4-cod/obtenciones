package senadi.gob.ec.adminob.model;

import java.io.Serializable;
import java.math.BigDecimal;
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

    /** Fecha en que se realizó el pago de la anualidad */
    @Column(name = "fecha_pago")
    private Date fechaPago;

    /** Valor pagado de la anualidad */
    @Column(name = "monto", precision = 10, scale = 2)
    private BigDecimal monto;

    /** N° comprobante del pago de la anualidad */
    @Column(name = "numero_comprobante", length = 150)
    private String numeroComprobante;

    /** Valor pagado por recargo (pago tardío) */
    @Column(name = "valor_pagado_recargo", precision = 10, scale = 2)
    private BigDecimal valorPagadoRecargo;

    /** N° comprobante del recargo */
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

    // ── getters / setters ──

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

    // ── helpers para p:datePicker (recibe java.util.Date) ──

    public java.util.Date getFechaPagoUtil() { return fechaPago; }
    public void setFechaPagoUtil(java.util.Date v) {
        this.fechaPago = v != null ? new java.sql.Date(v.getTime()) : null;
    }

    public java.util.Date getFechaVencimientoUtil() { return fechaVencimiento; }
    public void setFechaVencimientoUtil(java.util.Date v) {
        this.fechaVencimiento = v != null ? new java.sql.Date(v.getTime()) : null;
    }

    // ── métodos de estado ──

    public boolean isProximaAVencer(int dias) {
        if (fechaVencimiento == null) return false;
        if (estado == EstadoAnualidad.PAGADO) return false;
        long diff = fechaVencimiento.getTime() - System.currentTimeMillis();
        return diff >= 0 && diff <= (long) dias * 86_400_000L;
    }

    public boolean isVencida() {
        if (fechaVencimiento == null) return false;
        if (estado == EstadoAnualidad.PAGADO) return false;
        return fechaVencimiento.getTime() < System.currentTimeMillis();
    }

    /** Clase CSS para la fila — PAGADO se verifica primero para garantizar fila verde */
    public String getRowClass() {
        if (estado == EstadoAnualidad.PAGADO) return "anu-row-pagada";
        if (isVencida())          return "anu-row-vencida";
        if (isProximaAVencer(15)) return "anu-row-alerta";
        return "";
    }

    public String getEstadoLabel() {
        if (estado == null)               return "Pendiente";
        if (estado == EstadoAnualidad.PAGADO)  return "Completado";
        if (estado == EstadoAnualidad.VENCIDO) return "Vencido";
        return "Pendiente";
    }

    public String getEstadoBadgeClass() {
        if (estado == EstadoAnualidad.PAGADO) return "anu-badge anu-pagado";
        if (isVencida())          return "anu-badge anu-vencido";
        if (isProximaAVencer(15)) return "anu-badge anu-alerta";
        return "anu-badge anu-pendiente";
    }

    public String getEstadoBadgeLabel() {
        if (estado == EstadoAnualidad.PAGADO) return "✓ COMPLETADO";
        if (isVencida())          return "VENCIDA";
        if (isProximaAVencer(15)) return "⚠ 15 DÍAS";
        return "PENDIENTE";
    }
}
