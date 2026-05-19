package senadi.gob.ec.adminob.enums;

/**
 * Fases del proceso de Protección de Obtenciones Vegetales.
 * El orden del enum refleja el orden cronológico del flujo.
 * TramiteFlowService valida que cada transición sea legal.
 */
public enum FlowPhase {

    /** Solicitud ingresada por el ciudadano. Pendiente de asignación. */
    INITIAL,

    /** Técnico asignado. En espera de revisión de requisitos de forma. */
    ASSIGNED,

    /** En proceso de revisión de requisitos de forma (fase intermedia explícita). */
    FORM_REVIEW,

    /** Admitida: requisitos de forma cumplidos. Expediente generado. */
    ADMITTED,

    /** En revisión de fondo (sustancia). */
    SUBSTANCE_REVIEW,

    /** Depósito de muestra viva registrado. */
    LIVE_SAMPLE_DEPOSIT,

    /** Certificado de depósito de muestra viva emitido. */
    DEPOSIT_CERTIFICATE,

    /**
     * Publicado en gaceta. Período de oposición de 30 días activo.
     * El trámite permanece en esta fase hasta que se registre una oposición
     * o hasta que se llame a continuarSinOposicion() con el plazo vencido.
     */
    OPPOSITION_PERIOD,

    /** Oposición recibida y en proceso de resolución. */
    OPPOSITION_RESOLUTION,

    /** Examen DHE (Distinción, Homogeneidad, Estabilidad) en curso. */
    DHE_EXAM,

    /** Dictamen técnico emitido. */
    TECHNICAL_OPINION,

    /** Resolución emitida (concesión o negación). */
    RESOLUTION,

    /** Certificado de obtentor emitido. Proceso concluido. */
    CERTIFICATE_ISSUED,

    /** Expediente archivado por incumplimiento de requisitos o negación. */
    ARCHIVED
}
