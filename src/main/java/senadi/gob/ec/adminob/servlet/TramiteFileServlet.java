package senadi.gob.ec.adminob.servlet;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import senadi.gob.ec.adminob.bean.LoginBean;
import senadi.gob.ec.adminob.dao.DocumentoTramiteDAO;
import senadi.gob.ec.adminob.model.DocumentoTramite;

/** Sirve los archivos del módulo de Trámites. Requiere sesión activa. */
@WebServlet("/tramite-doc")
public class TramiteFileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        LoginBean loginBean = (LoginBean) req.getSession().getAttribute("loginBean");
        if (loginBean == null || !loginBean.estaLogeado()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sesión no iniciada");
            return;
        }

        String idParam = req.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parámetro id requerido");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idParam.trim());
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "id inválido");
            return;
        }

        DocumentoTramite doc;
        try {
            doc = new DocumentoTramiteDAO(null).buscarPorId(id);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        if (doc == null || doc.getRutaArchivo() == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Documento no encontrado en base de datos");
            return;
        }

        File file = new File(doc.getRutaArchivo());
        if (!file.exists() || !file.isFile()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                "Archivo no encontrado en disco: " + doc.getRutaArchivo());
            return;
        }

        String nombre = doc.getNombreArchivo() != null ? doc.getNombreArchivo() : file.getName();
        String nombreMin = nombre.toLowerCase();
        String contentType;
        if (nombreMin.endsWith(".pdf")) {
            contentType = "application/pdf";
        } else if (nombreMin.endsWith(".png")) {
            contentType = "image/png";
        } else if (nombreMin.endsWith(".jpg") || nombreMin.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else {
            contentType = "application/octet-stream";
        }

        resp.setContentType(contentType);
        resp.setHeader("Content-Disposition", "inline; filename=\"" + nombre + "\"");
        resp.setContentLengthLong(file.length());
        resp.setHeader("Cache-Control", "no-store");

        try (OutputStream out = resp.getOutputStream()) {
            Files.copy(file.toPath(), out);
        }
    }
}
