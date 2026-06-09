/**
 * dialog-click-outside.js
 * 
 * Permite cerrar diálogos modales de PrimeFaces haciendo clic fuera del diálogo
 * (en la máscara/overlay de fondo). Solo aplica a diálogos modales y cerrables.
 * Implementado en Vanilla JS puro en fase de captura para anticiparse a stopPropagation.
 */
document.addEventListener('click', function(e) {
    var target = e.target;
    if (!target) return;

    // Verificar si el clic fue en la máscara o el overlay
    var isMask = target.classList.contains('ui-dialog-mask') || 
                 target.classList.contains('ui-widget-overlay') ||
                 (target.tagName === 'DIV' && target.style.position === 'fixed' && target.classList.contains('ui-dialog-visible'));

    if (isMask) {
        if (window.PrimeFaces) {
            // 1. Intentar buscar el diálogo dentro de la máscara (caso común de estructura anidada)
            var dialogEl = target.querySelector('.ui-dialog');
            
            // 2. Si no es hijo, buscar cualquier diálogo visible en la página
            if (!dialogEl) {
                var visibleDialogs = Array.from(document.querySelectorAll('.ui-dialog')).filter(function(el) {
                    return window.getComputedStyle(el).display !== 'none';
                });
                if (visibleDialogs.length > 0) {
                    // Ordenar por z-index descendente para cerrar el que esté más al frente
                    visibleDialogs.sort(function(a, b) {
                        var zA = parseInt(window.getComputedStyle(a).zIndex) || 0;
                        var zB = parseInt(window.getComputedStyle(b).zIndex) || 0;
                        return zB - zA;
                    });
                    dialogEl = visibleDialogs[0];
                }
            }
            
            if (dialogEl) {
                var widget = null;
                var id = dialogEl.id;
                
                // Buscar el widget de forma súper robusta
                if (id) {
                    widget = findPrimeFacesWidget(id);
                }
                
                // Fallback con jQuery data
                if (!widget && window.jQuery) {
                    widget = window.jQuery(dialogEl).data('widget');
                }
                
                if (widget && widget.cfg.modal && widget.cfg.closable !== false) {
                    widget.hide();
                }
            }
        }
    }
}, true); // Usar fase de captura para adelantarse a cualquier stopPropagation() de PrimeFaces/jQuery

function findPrimeFacesWidget(id) {
    if (!window.PrimeFaces || !PrimeFaces.widgets) return null;
    
    // Intento 1: Buscar por ID directo en PrimeFaces.widgets
    if (PrimeFaces.widgets[id]) {
        return PrimeFaces.widgets[id];
    }
    
    // Intento 2: Buscar usando la función nativa si existe
    if (typeof PrimeFaces.getWidgetById === 'function') {
        var w = PrimeFaces.getWidgetById(id);
        if (w) return w;
    }
    
    // Intento 3: Iterar todos los widgets registrados
    for (var key in PrimeFaces.widgets) {
        var w = PrimeFaces.widgets[key];
        if (w && (w.id === id || w.uuid === id)) {
            return w;
        }
    }
    
    return null;
}
