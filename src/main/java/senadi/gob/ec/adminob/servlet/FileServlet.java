package senadi.gob.ec.adminob.servlet;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import senadi.gob.ec.adminob.bean.LoginBean;
import senadi.gob.ec.adminob.dao.ComprobantePagoDAO;
import senadi.gob.ec.adminob.model.ComprobantePago;

@WebServlet("/file-view")
public class FileServlet extends HttpServlet {

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

        File file = null;
        String nombre = null;

        if (id <= -20000000 && id >= -29999999) { // Archivo físico arbitrario en disco (Rango -20000000)
            int offset = -id - 20000000;
            int formId = offset / 1000;
            int fileIndex = offset % 1000;
            
            String baseDir = senadi.gob.ec.adminob.util.AppConfig.get("upload.base.path", "UPLOAD_BASE_PATH",
                "C:" + File.separator + "uploads");
            File uploadDir = new File(baseDir + File.separator + formId);
            if (uploadDir.exists() && uploadDir.isDirectory()) {
                File[] files = uploadDir.listFiles();
                if (files != null) {
                    java.util.List<File> fileList = new java.util.ArrayList<>();
                    for (File f : files) {
                        if (f.isFile() && f.length() > 0) {
                            fileList.add(f);
                        }
                    }
                    fileList.sort((f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));
                    if (fileIndex >= 0 && fileIndex < fileList.size()) {
                        file = fileList.get(fileIndex);
                        nombre = file.getName();
                    }
                }
            }
        } else if (id <= -1000000 && id >= -1999999) { // Comprobante de pago histórico estándar en disco (Rango -1000000)
            int formId = -1000000 - id;
            String baseDir = senadi.gob.ec.adminob.util.AppConfig.get("upload.base.path", "UPLOAD_BASE_PATH",
                "C:" + File.separator + "uploads");
            file = new File(baseDir + File.separator + formId + File.separator + "pdf_voucher_breederfrm_" + formId + ".pdf");
            if (!file.exists()) {
                file = new File(baseDir + File.separator + "obtenciones" + File.separator + formId + File.separator + "pdf_voucher_breederfrm_" + formId + ".pdf");
            }
            
            // Si el comprobante histórico físico no existe en disco local, hacer redirección al portal remoto
            if (!file.exists()) {
                resp.sendRedirect(senadi.gob.ec.adminob.util.Parameter.RUTA_URL + formId + "/pdf_voucher_breederfrm_" + formId + ".pdf");
                return;
            }
            nombre = "pdf_voucher_breederfrm_" + formId + ".pdf";
        } else if (id <= -2000000 && id >= -2999999) { // Certificado de descuento (Rango -2000000)
            int formId = -2000000 - id;
            String remoteFileName = null;
            Connection extConn = null;
            try {
                extConn = senadi.gob.ec.adminob.util.Operations.doConnectionToFormularios();
                if (extConn != null) {
                    String sql = "SELECT discount_file FROM breeder_forms WHERE id = ?";
                    try (PreparedStatement ps = extConn.prepareStatement(sql)) {
                        ps.setInt(1, formId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                remoteFileName = rs.getString("discount_file");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (extConn != null) {
                    try { extConn.close(); } catch (Exception ignore) {}
                }
            }
            if (remoteFileName != null && !remoteFileName.trim().isEmpty()) {
                resp.sendRedirect(senadi.gob.ec.adminob.util.Parameter.RUTA_CERT_DESC_URL + remoteFileName);
                return;
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Certificado de descuento remoto no encontrado");
                return;
            }
        } else if (id <= -3000000 && id >= -3999999) { // Evidencia de pago (Rango -3000000)
            int formId = -3000000 - id;
            String remoteFileName = null;
            Connection extConn = null;
            try {
                extConn = senadi.gob.ec.adminob.util.Operations.doConnectionToFormularios();
                if (extConn != null) {
                    String sql = "SELECT evidence_file FROM breeder_forms WHERE id = ?";
                    try (PreparedStatement ps = extConn.prepareStatement(sql)) {
                        ps.setInt(1, formId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                remoteFileName = rs.getString("evidence_file");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (extConn != null) {
                    try { extConn.close(); } catch (Exception ignore) {}
                }
            }
            if (remoteFileName != null && !remoteFileName.trim().isEmpty()) {
                resp.sendRedirect(senadi.gob.ec.adminob.util.Parameter.RUTA_URL + formId + "/" + remoteFileName);
                return;
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Evidencia de pago remota no encontrada");
                return;
            }
        } else if (id <= -10000000 && id >= -19999999) { // Vouchers externos del portal (Rango -10000000)
            int voucherId = -10000000 - id;
            String remoteFileName = null;
            int formId = -1;
            Connection extConn = null;
            try {
                extConn = senadi.gob.ec.adminob.util.Operations.doConnectionToFormularios();
                if (extConn != null) {
                    String sql = "SELECT voucher_old_file, payment_receipt_id, application_number FROM voucher WHERE id = ?";
                    Integer paymentReceiptId = null;
                    String appNumber = null;
                    try (PreparedStatement ps = extConn.prepareStatement(sql)) {
                        ps.setInt(1, voucherId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                remoteFileName = rs.getString("voucher_old_file");
                                paymentReceiptId = (Integer) rs.getObject("payment_receipt_id");
                                appNumber = rs.getString("application_number");
                            }
                        }
                    }
                    
                    if (paymentReceiptId != null) {
                        String sqlForm = "SELECT id FROM breeder_forms WHERE payment_receipt_id = ?";
                        try (PreparedStatement ps = extConn.prepareStatement(sqlForm)) {
                            ps.setInt(1, paymentReceiptId);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    formId = rs.getInt("id");
                                }
                            }
                        }
                    }
                    
                    if (formId == -1 && appNumber != null && !appNumber.trim().isEmpty()) {
                        String sqlForm = "SELECT id FROM breeder_forms WHERE application_number = ?";
                        try (PreparedStatement ps = extConn.prepareStatement(sqlForm)) {
                            ps.setString(1, appNumber);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    formId = rs.getInt("id");
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (extConn != null) {
                    try { extConn.close(); } catch (Exception ignore) {}
                }
            }
            if (remoteFileName != null && !remoteFileName.trim().isEmpty() && formId != -1) {
                resp.sendRedirect(senadi.gob.ec.adminob.util.Parameter.RUTA_URL + formId + "/" + remoteFileName);
                return;
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Voucher remoto no encontrado");
                return;
            }
        } else if (id < -999999999) { // Anexos
            int temp = -1000000000 - id;
            int annexId = temp % 100;
            int formId = temp / 100;
            senadi.gob.ec.adminob.model.VegetableAnnexesData annex = null;
            javax.persistence.EntityManager em = null;
            try {
                em = new senadi.gob.ec.adminob.dao.ComprobantePagoDAO(null).getEntityManager();
                annex = em.createQuery(
                    "SELECT d FROM VegetableAnnexesData d WHERE d.id.vegetableFormId = :fid AND d.id.vegetableAnnexesId = :aid",
                    senadi.gob.ec.adminob.model.VegetableAnnexesData.class)
                    .setParameter("fid", formId)
                    .setParameter("aid", annexId)
                    .getSingleResult();
            } catch (Exception e) {
                // Si falla localmente, no abortamos; permitimos intentar la base remota.
            } finally {
                if (em != null) em.close();
            }

            String baseDir = senadi.gob.ec.adminob.util.AppConfig.get("upload.base.path", "UPLOAD_BASE_PATH",
                "C:" + File.separator + "uploads");
            
            if (annex != null && annex.getFileName() != null) {
                file = new File(baseDir + File.separator + formId + File.separator + annex.getFileName());
                nombre = annex.getFileName();
            }
            
            // Si el anexo no está en el disco local o no se encontró en la BD local, consultar en tiempo real al portal y redirigir
            if (file == null || !file.exists()) {
                String remoteFileName = null;
                Connection extConn = null;
                try {
                    extConn = senadi.gob.ec.adminob.util.Operations.doConnectionToFormularios();
                    if (extConn != null) {
                        String sql = "SELECT file FROM breeder_annexes_data WHERE breeder_form_id = ? AND breeder_annex_id = ?";
                        try (PreparedStatement ps = extConn.prepareStatement(sql)) {
                            ps.setInt(1, formId);
                            ps.setInt(2, annexId);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    remoteFileName = rs.getString("file");
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (extConn != null) {
                        try { extConn.close(); } catch (Exception ignore) {}
                    }
                }
                if (remoteFileName != null && !remoteFileName.trim().isEmpty()) {
                    resp.sendRedirect(senadi.gob.ec.adminob.util.Parameter.RUTA_URL + formId + "/" + remoteFileName);
                    return;
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Anexo no encontrado");
                    return;
                }
            }
        } else if (id < 0) {
            int realDocId = -id;
            senadi.gob.ec.adminob.model.DocumentoTramite doc;
            try {
                doc = new senadi.gob.ec.adminob.dao.DocumentoTramiteDAO(null).buscarPorId(realDocId);
            } catch (Exception e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            if (doc == null || doc.getRutaArchivo() == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Documento de trámite no encontrado");
                return;
            }
            file = new File(doc.getRutaArchivo());
            nombre = doc.getNombreArchivo();
        } else {
            ComprobantePago cp;
            try {
                cp = new ComprobantePagoDAO(null).getById(id);
            } catch (Exception e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            if (cp == null || cp.getRutaArchivo() == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Archivo no encontrado en base de datos");
                return;
            }
            file = new File(cp.getRutaArchivo());
            nombre = cp.getNombreArchivo() != null ? cp.getNombreArchivo() : file.getName();
        }

        if (file == null || !file.exists() || !file.isFile()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Archivo no encontrado en disco");
            return;
        }
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
