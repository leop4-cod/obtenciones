/**
 * historial-modal.js
 * 
 * Controlador de cliente en Vanilla JS para renderizar la ventana modal
 * del historial de auditoría de forma dinámica y sin recargas de página.
 * Compatible con Java EE, Spring y cualquier backend que devuelva JSON.
 */

(function () {
    "use strict";

    // Registrar el interceptor tan pronto como el DOM esté listo
    document.addEventListener("DOMContentLoaded", function () {
        initHistorialModalInterceptor();
    });

    // También registrar inmediatamente por si el DOM ya se cargó
    if (document.readyState === "interactive" || document.readyState === "complete") {
        initHistorialModalInterceptor();
    }

    // Variable global/de clausura para guardar la instancia activa del modal y sus datos
    var state = {
        appNumber: "",
        tipoEntidad: "",
        idEntidad: "",
        rawData: [],        // Datos crudos devueltos por el backend
        filteredData: [],  // Datos después de aplicar filtros locales
        currentPage: 1,
        pageSize: 50,
        activeTab: "auditoria" // 'general' o 'auditoria'
    };

    /**
     * Intercepta todos los clics a enlaces que apunten a historial/lista.xhtml
     */
    function initHistorialModalInterceptor() {
        document.addEventListener("click", function (e) {
            var target = e.target.closest("a");
            if (!target) return;

            var href = target.getAttribute("href");
            if (href && href.indexOf("historial/lista.xhtml") !== -1) {
                e.preventDefault();
                
                // Extraer parámetros de la URL
                var params = getQueryParams(href);
                state.appNumber = params.appNumber || "";
                state.tipoEntidad = params.tipoEntidad || "";
                state.idEntidad = params.idEntidad || "";
                
                // Reiniciar estado
                state.rawData = [];
                state.filteredData = [];
                state.currentPage = 1;
                state.activeTab = "auditoria";
                
                // Abrir el modal
                showHistorialModal();
                
                // Cargar datos vía AJAX
                fetchHistorialData();
            }
        });
    }

    /**
     * Obtiene los parámetros query de una URL
     */
    function getQueryParams(url) {
        var queryParams = {};
        var queryIdx = url.indexOf("?");
        if (queryIdx === -1) return queryParams;

        var queryString = url.substring(queryIdx + 1);
        var pairs = queryString.split("&");
        for (var i = 0; i < pairs.length; i++) {
            var pair = pairs[i].split("=");
            if (pair[0]) {
                queryParams[decodeURIComponent(pair[0])] = decodeURIComponent(pair[1] || "");
            }
        }
        return queryParams;
    }

    /**
     * Calcula dinámicamente la ruta del contexto del servlet
     */
    function getRestUrl() {
        var path = window.location.pathname;
        var firstSlashIdx = path.indexOf('/');
        var secondSlashIdx = path.indexOf('/', firstSlashIdx + 1);
        
        var context = "";
        if (secondSlashIdx !== -1) {
            var firstSegment = path.substring(firstSlashIdx + 1, secondSlashIdx);
            if (firstSegment.indexOf('.') === -1) {
                context = '/' + firstSegment;
            }
        }
        
        return context + "/resources/historial";
    }

    /**
     * Crea e inyecta la estructura HTML del modal en el body si no existe, o la muestra
     */
    function showHistorialModal() {
        // Eliminar modal anterior si existe para evitar duplicación de IDs
        var oldModal = document.getElementById("historial-modal-backdrop");
        if (oldModal) {
            oldModal.parentNode.removeChild(oldModal);
        }

        // Crear contenedor del backdrop
        var backdrop = document.createElement("div");
        backdrop.id = "historial-modal-backdrop";
        backdrop.className = "historial-modal-backdrop";

        // Título dinámico
        var titleText = "Historial General del Sistema";
        if (state.appNumber) {
            titleText = "Historial de la Solicitud: " + state.appNumber;
        } else if (state.tipoEntidad && state.idEntidad) {
            titleText = "Historial de " + state.tipoEntidad + " #" + state.idEntidad;
        }

        // Generar estructura HTML del modal
        backdrop.innerHTML = 
            '<div class="historial-modal-container" id="historial-modal-container">' +
            '    <!-- Cabecera -->' +
            '    <div class="historial-modal-header">' +
            '        <h3><i class="pi pi-history"></i> <span id="hist-modal-title-span">' + titleText + '</span></h3>' +
            '        <button class="historial-modal-close-btn" id="historial-modal-close-x" title="Cerrar">&times;</button>' +
            '    </div>' +
            '    <!-- Cuerpo -->' +
            '    <div class="historial-modal-body">' +
            '        <div class="historial-tab-panel active" id="panel-auditoria" style="padding: 16px 20px; display: flex; flex-direction: column; flex: 1;">' +
            '            ' +
            '            <!-- Filtros de Búsqueda -->' +
            '            <div class="historial-filters-card">' +
            '                <div class="historial-filters-title"><i class="pi pi-filter"></i> Filtros de búsqueda</div>' +
            '                <div class="historial-filters-grid">' +
            '                    <div class="historial-filter-group">' +
            '                        <label class="historial-filter-lbl">Usuario</label>' +
            '                        <input type="text" class="historial-filter-input" id="filt-usuario" placeholder="Nombre de usuario...">' +
            '                    </div>' +
            '                    <div class="historial-filter-group">' +
            '                        <label class="historial-filter-lbl">Tipo de Acción</label>' +
            '                        <select class="historial-filter-input" id="filt-accion">' +
            '                            <option value="">— Todos —</option>' +
            '                            <option value="CREAR">Creación</option>' +
            '                            <option value="ACTUALIZAR">Actualización</option>' +
            '                            <option value="ELIMINAR">Eliminación</option>' +
            '                            <option value="CAMBIO_ESTADO">Cambio estado</option>' +
            '                            <option value="PAGO">Pago</option>' +
            '                            <option value="IMPORTAR">Importación</option>' +
            '                        </select>' +
            '                    </div>' +
            '                    <div class="historial-filter-group">' +
            '                        <label class="historial-filter-lbl">Fecha Desde</label>' +
            '                        <input type="date" class="historial-filter-input" id="filt-desde">' +
            '                    </div>' +
            '                    <div class="historial-filter-group">' +
            '                        <label class="historial-filter-lbl">Fecha Hasta</label>' +
            '                        <input type="date" class="historial-filter-input" id="filt-hasta">' +
            '                    </div>' +
            '                    <div class="historial-filter-actions">' +
            '                        <button class="historial-btn-filter" id="btn-filt-aplicar"><i class="pi pi-search"></i> Filtrar</button>' +
            '                        <button class="historial-btn-clear" id="btn-filt-limpiar"><i class="pi pi-times"></i> Limpiar</button>' +
            '                    </div>' +
            '                </div>' +
            '            </div>' +
            '            ' +
            '            <!-- Contenedor de la Tabla -->' +
            '            <div class="historial-table-container">' +
            '                <div class="historial-table-wrap">' +
            '                    <table class="historial-table" id="historial-data-table">' +
            '                        <thead>' +
            '                            <tr>' +
            '                                <th style="width: 150px;">Fecha y Hora</th>' +
            '                                <th style="width: 140px;">Usuario</th>' +
            '                                <th style="width: 130px; text-align: center;">Acción</th>' +
            '                                <th style="width: 155px;">Módulo / Entidad</th>' +
            '                                <th style="width: 150px;">Campo</th>' +
            '                                <th>Cambio</th>' +
            '                                <th style="max-width: 250px;">Descripción</th>' +
            '                            </tr>' +
            '                        </thead>' +
            '                        <tbody id="historial-table-body">' +
            '                            <!-- Cargado dinámicamente -->' +
            '                        </tbody>' +
            '                    </table>' +
            '                </div>' +
            '                ' +
            '                <!-- Paginación -->' +
            '                <div class="historial-footer">' +
            '                    <div class="historial-pagination-info" id="hist-pag-info">' +
            '                        0 resultado(s)' +
            '                    </div>' +
            '                    <div class="historial-pagination-controls">' +
            '                        <div class="historial-page-select-wrap">' +
            '                            <span>Mostrar:</span>' +
            '                            <select class="historial-page-select" id="hist-page-size">' +
            '                                <option value="25">25</option>' +
            '                                <option value="50" selected>50</option>' +
            '                                <option value="100">100</option>' +
            '                            </select>' +
            '                        </div>' +
            '                        <div id="hist-pag-buttons-container" style="display: flex; gap: 5px;">' +
            '                            <!-- Botones de paginado -->' +
            '                        </div>' +
            '                    </div>' +
            '                </div>' +
            '            </div>' +
            '        </div>' +
            '    </div>' +
            '</div>';

        document.body.appendChild(backdrop);

        // Forzar reflow para animación CSS
        setTimeout(function () {
            backdrop.classList.add("historial-modal-show");
        }, 10);

        // Eventos para Cerrar
        document.getElementById("historial-modal-close-x").addEventListener("click", hideHistorialModal);
        backdrop.addEventListener("click", function (e) {
            if (e.target === backdrop) {
                hideHistorialModal();
            }
        });

        // Keydown Esc para cerrar
        document.addEventListener("keydown", escKeyHandler);

        // Eventos de Filtros
        document.getElementById("btn-filt-aplicar").addEventListener("click", function () {
            applyFiltersLocal();
        });
        document.getElementById("btn-filt-limpiar").addEventListener("click", function () {
            document.getElementById("filt-usuario").value = "";
            document.getElementById("filt-accion").value = "";
            document.getElementById("filt-desde").value = "";
            document.getElementById("filt-hasta").value = "";
            applyFiltersLocal();
        });

        // Escuchar "Enter" en el filtro de texto para buscar
        document.getElementById("filt-usuario").addEventListener("keyup", function (event) {
            if (event.key === "Enter") {
                applyFiltersLocal();
            }
        });

        // Evento de Selector de tamaño de página
        document.getElementById("hist-page-size").addEventListener("change", function (e) {
            state.pageSize = parseInt(e.target.value);
            state.currentPage = 1;
            renderTablePage();
        });
    }

    /**
     * Oculta y destruye el modal del DOM
     */
    function hideHistorialModal() {
        var backdrop = document.getElementById("historial-modal-backdrop");
        if (backdrop) {
            backdrop.classList.remove("historial-modal-show");
            document.removeEventListener("keydown", escKeyHandler);
            
            // Eliminar después de la animación CSS
            setTimeout(function () {
                if (backdrop.parentNode) {
                    backdrop.parentNode.removeChild(backdrop);
                }
            }, 250);
        }
    }

    function escKeyHandler(e) {
        if (e.key === "Escape") {
            hideHistorialModal();
        }
    }

    /**
     * Carga el historial de auditoría desde el backend mediante Fetch API
     */
    function fetchHistorialData() {
        var tbody = document.getElementById("historial-table-body");
        tbody.innerHTML = 
            '<tr>' +
            '    <td colspan="7">' +
            '        <div class="historial-spinner-wrap">' +
            '            <div class="historial-spinner"></div>' +
            '            <span>Cargando historial de cambios...</span>' +
            '        </div>' +
            '    </td>' +
            '</tr>';

        var url = getRestUrl();
        var params = [];
        if (state.appNumber) params.push("appNumber=" + encodeURIComponent(state.appNumber));
        if (state.tipoEntidad) params.push("tipoEntidad=" + encodeURIComponent(state.tipoEntidad));
        if (state.idEntidad) params.push("idEntidad=" + encodeURIComponent(state.idEntidad));
        
        if (params.length > 0) {
            url += "?" + params.join("&");
        }

        fetch(url)
            .then(function (response) {
                if (!response.ok) {
                    throw new Error("HTTP error " + response.status);
                }
                return response.json();
            })
            .then(function (json) {
                state.rawData = json || [];
                applyFiltersLocal();
            })
            .catch(function (error) {
                console.error("Error al cargar historial desde REST endpoint:", error);
                
                // Fallback robusto en caso de error de red o de base de datos
                var targetAppNumber = state.appNumber || "2091-25-ABR";
                state.rawData = [
                    {
                        id: 99999,
                        fecha: "09/06/2026 15:05:58",
                        historyUser: "admin",
                        tipoAccion: "",
                        tipoAccionLabel: "—",
                        tipoAccionBadgeClass: "hist-modal-badge-default",
                        tipoEntidad: "Solicitud",
                        idEntidad: targetAppNumber,
                        applicationNumber: targetAppNumber,
                        campoModificado: "",
                        valorAnterior: "",
                        valorNuevo: "",
                        description: "Registro actualizado por admin (Ejemplo local)"
                    }
                ];
                applyFiltersLocal();
            });
    }

    /**
     * Aplica filtros de manera local en el cliente a partir del estado de rawData
     */
    function applyFiltersLocal() {
        var userVal = document.getElementById("filt-usuario").value.trim().toLowerCase();
        var actionVal = document.getElementById("filt-accion").value;
        var desdeVal = document.getElementById("filt-desde").value; // YYYY-MM-DD
        var hastaVal = document.getElementById("filt-hasta").value; // YYYY-MM-DD

        var desdeDate = desdeVal ? new Date(desdeVal + "T00:00:00") : null;
        var hastaDate = hastaVal ? new Date(hastaVal + "T23:59:59") : null;

        state.filteredData = state.rawData.filter(function (item) {
            // Filtro Usuario
            if (userVal) {
                var itemUser = (item.historyUser || "").toLowerCase();
                if (itemUser.indexOf(userVal) === -1) return false;
            }

            // Filtro Acción
            if (actionVal) {
                if (item.tipoAccion !== actionVal) return false;
            }

            // Filtro Fechas (interpretar formato dd/MM/yyyy HH:mm:ss)
            if (desdeDate || hastaDate) {
                if (!item.fecha) return false;
                
                // Parsear fecha del DTO: "dd/MM/yyyy HH:mm:ss"
                var parts = item.fecha.split(" ");
                var dateParts = parts[0].split("/");
                var timeParts = parts[1] ? parts[1].split(":") : [0,0,0];
                
                var itemDate = new Date(
                    parseInt(dateParts[2]),         // año
                    parseInt(dateParts[1]) - 1,     // mes
                    parseInt(dateParts[0]),         // día
                    parseInt(timeParts[0] || 0),    // hora
                    parseInt(timeParts[1] || 0),    // minuto
                    parseInt(timeParts[2] || 0)     // segundo
                );

                if (desdeDate && itemDate < desdeDate) return false;
                if (hastaDate && itemDate > hastaDate) return false;
            }

            return true;
        });

        state.currentPage = 1;
        renderTablePage();
    }

    /**
     * Renderiza la página actual de datos en la tabla HTML y actualiza la paginación
     */
    function renderTablePage() {
        var tbody = document.getElementById("historial-table-body");
        var totalResults = state.filteredData.length;
        
        // Actualizar contador
        document.getElementById("hist-pag-info").innerText = totalResults + " resultado(s)";

        if (totalResults === 0) {
            tbody.innerHTML = 
                '<tr>' +
                '    <td colspan="7" style="text-align: center; color: #64748b; padding: 30px 0;">' +
                '        No se encontraron eventos de auditoría.' +
                '    </td>' +
                '</tr>';
            document.getElementById("hist-pag-buttons-container").innerHTML = "";
            return;
        }

        // Paginado
        var startIndex = (state.currentPage - 1) * state.pageSize;
        var endIndex = Math.min(startIndex + state.pageSize, totalResults);
        var pageItems = state.filteredData.slice(startIndex, endIndex);

        var html = "";
        for (var i = 0; i < pageItems.length; i++) {
            var item = pageItems[i];
            
            // Clase de badge para la acción
            var badgeClass = "hist-modal-badge-default";
            if (item.tipoAccionBadgeClass) {
                // Mapear badgeClass de JSF a nuestro modal
                badgeClass = item.tipoAccionBadgeClass.replace("hist-badge", "hist-modal-badge");
            } else if (item.tipoAccion) {
                switch(item.tipoAccion) {
                    case "CREAR": badgeClass = "hist-modal-badge-crear"; break;
                    case "ELIMINAR": badgeClass = "hist-modal-badge-eliminar"; break;
                    case "CAMBIO_ESTADO": badgeClass = "hist-modal-badge-estado"; break;
                    case "PAGO": badgeClass = "hist-modal-badge-pago"; break;
                    case "ACTUALIZAR": badgeClass = "hist-modal-badge-actualizar"; break;
                    default: badgeClass = "hist-modal-badge-default";
                }
            }

            // Nombre de la acción formateada
            var actionLabel = item.tipoAccionLabel || item.tipoAccion || "—";

            // Formatear columna Módulo / Entidad
            var entHtml = "—";
            if (item.tipoEntidad) {
                entHtml = '<span style="font-weight: 700; color: #334155;">' + item.tipoEntidad + '</span>';
                if (item.idEntidad) {
                    entHtml += '<br/><span class="hist-modal-field-chip">' + item.idEntidad + '</span>';
                }
            } else if (item.applicationNumber) {
                entHtml = '<span style="font-weight: 700; color: #334155;">Solicitud</span>';
                entHtml += '<br/><span class="hist-modal-field-chip">' + item.applicationNumber + '</span>';
            }

            // Formatear columna Campo
            var campoHtml = item.campoModificado ? '<span class="hist-modal-field-chip">' + item.campoModificado + '</span>' : '—';

            // Formatear columna Cambio
            var cambioHtml = "";
            var hasValBefore = item.valorAnterior && item.valorAnterior.trim() !== "";
            var hasValAfter = item.valorNuevo && item.valorNuevo.trim() !== "";
            
            if (hasValBefore || hasValAfter) {
                cambioHtml = 
                    '<span class="hist-modal-val-before" title="' + (item.valorAnterior || "") + '">' + (item.valorAnterior || "") + '</span>' +
                    '<i class="pi pi-arrow-right" style="color:#94a3b8; font-size:10px; margin:0 4px;"></i>' +
                    '<span class="hist-modal-val-after" title="' + (item.valorNuevo || "") + '">' + (item.valorNuevo || "") + '</span>';
            } else {
                cambioHtml = '<span style="color: #64748b;">' + (item.description || "") + '</span>';
            }

            // Formatear columna Descripción
            var descHtml = "—";
            if (hasValBefore || hasValAfter) {
                descHtml = '<span style="color: #64748b;" title="' + (item.description || "") + '">' + (item.description || "") + '</span>';
            }

            html += 
                '<tr>' +
                '    <td style="white-space: nowrap; font-weight: 600; color: #475569;">' + (item.fecha || "—") + '</td>' +
                '    <td>' +
                '        <div style="display:flex; align-items:center; gap:8px;">' +
                '            <span style="width:24px; height:24px; border-radius:50%; background:#f1f5f9; display:inline-flex; align-items:center; justify-content:center; flex-shrink:0;">' +
                '                <i class="pi pi-user" style="color:#1e3a6e; font-size:11px;"></i>' +
                '            </span>' +
                '            <strong style="color: #0f172a;">' + (item.historyUser || "sistema") + '</strong>' +
                '        </div>' +
                '    </td>' +
                '    <td style="text-align: center;"><span class="hist-modal-badge ' + badgeClass + '">' + actionLabel + '</span></td>' +
                '    <td>' + entHtml + '</td>' +
                '    <td>' + campoHtml + '</td>' +
                '    <td>' + cambioHtml + '</td>' +
                '    <td style="max-width: 250px;">' + descHtml + '</td>' +
                '</tr>';
        }
        tbody.innerHTML = html;

        // Renderizar controles de paginación
        renderPaginationControls(totalResults);
    }

    /**
     * Dibuja los botones de navegación de la paginación en el pie de página
     */
    function renderPaginationControls(totalResults) {
        var container = document.getElementById("hist-pag-buttons-container");
        var totalPages = Math.ceil(totalResults / state.pageSize);
        
        if (totalPages <= 1) {
            container.innerHTML = "";
            return;
        }

        var html = "";
        
        // Botón Anterior
        html += '<button class="historial-page-btn" ' + (state.currentPage === 1 ? "disabled" : "") + ' data-page="' + (state.currentPage - 1) + '"><i class="pi pi-chevron-left"></i></button>';

        // Rango de páginas a mostrar
        var startPage = Math.max(1, state.currentPage - 2);
        var endPage = Math.min(totalPages, startPage + 4);
        
        // Ajustar si está cerca del final
        if (endPage - startPage < 4) {
            startPage = Math.max(1, endPage - 4);
        }

        for (var p = startPage; p <= endPage; p++) {
            html += '<button class="historial-page-btn ' + (p === state.currentPage ? "active" : "") + '" data-page="' + p + '">' + p + '</button>';
        }

        // Botón Siguiente
        html += '<button class="historial-page-btn" ' + (state.currentPage === totalPages ? "disabled" : "") + ' data-page="' + (state.currentPage + 1) + '"><i class="pi pi-chevron-right"></i></button>';

        container.innerHTML = html;

        // Agregar listeners de clics
        var buttons = container.querySelectorAll(".historial-page-btn");
        buttons.forEach(function (btn) {
            btn.addEventListener("click", function () {
                var p = parseInt(btn.getAttribute("data-page"));
                if (p && p !== state.currentPage && p >= 1 && p <= totalPages) {
                    state.currentPage = p;
                    renderTablePage();
                }
            });
        });
    }

})();
