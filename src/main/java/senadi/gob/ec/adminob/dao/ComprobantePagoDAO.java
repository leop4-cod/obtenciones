package senadi.gob.ec.adminob.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import senadi.gob.ec.adminob.model.ComprobantePago;
import senadi.gob.ec.adminob.model.Tramite;
import senadi.gob.ec.adminob.model.DocumentoTramite;
import senadi.gob.ec.adminob.model.VegetableAnnexesData;

public class ComprobantePagoDAO extends DAOAbstractM<ComprobantePago> {

    public ComprobantePagoDAO(ComprobantePago t) {
        super(t);
    }

    @Override
    public List<ComprobantePago> buscarTodos() {
        try {
            return getEntityManager()
                .createQuery("SELECT c FROM ComprobantePago c ORDER BY c.fechaCarga DESC",
                             ComprobantePago.class)
                .getResultList();
        } finally {
            getEntityManager().close();
        }
    }

    /** Devuelve todos los comprobantes de un trámite, ordenados del más reciente al más antiguo. */
    public List<ComprobantePago> getByVegetableFormId(Integer vegetableFormId) {
        javax.persistence.EntityManager em = EntityManagerM.getEntityManager();
        try {
            return em
                .createQuery(
                    "SELECT c FROM ComprobantePago c WHERE c.vegetableFormId = :vfid " +
                    "ORDER BY c.fechaCarga DESC",
                    ComprobantePago.class)
                .setParameter("vfid", vegetableFormId)
                .setHint("javax.persistence.cache.storeMode", "REFRESH")
                .getResultList();
        } finally {
            em.close();
        }
    }

    /** Devuelve la lista unificada de todos los archivos del trámite (ComprobantePago, DocumentoTramite y comprobante de pago histórico de disco). */
    public List<ComprobantePago> getTodosLosArchivosPorVegetableFormId(Integer vegetableFormId) {
        List<ComprobantePago> archivos = new java.util.ArrayList<>();
        if (vegetableFormId == null) {
            return archivos;
        }
        
        // 1. Obtener comprobantes de pago registrados en la BD
        try {
            archivos.addAll(getByVegetableFormId(vegetableFormId));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Obtener documentos de trámites SENADI vinculados (DocumentoTramite) y anexos del inicio
        javax.persistence.EntityManager em = EntityManagerM.getEntityManager();
        try {
            List<Tramite> tramites = em.createQuery(
                "SELECT t FROM Tramite t WHERE t.vegetableFormId = :vfid", Tramite.class)
                .setParameter("vfid", vegetableFormId)
                .getResultList();
            
            for (Tramite t : tramites) {
                List<DocumentoTramite> docs = em.createQuery(
                    "SELECT d FROM DocumentoTramite d WHERE d.tramiteId = :tid ORDER BY d.fechaCarga DESC",
                    DocumentoTramite.class)
                    .setParameter("tid", t.getId())
                    .getResultList();
                
                for (DocumentoTramite doc : docs) {
                    ComprobantePago virtualCp = new ComprobantePago();
                    virtualCp.setId(-doc.getId()); // ID negativo para mapear a DocumentoTramite
                    virtualCp.setVegetableFormId(vegetableFormId);
                    virtualCp.setNombreArchivo(doc.getNombreArchivo());
                    virtualCp.setNombrePersonalizado(doc.getNombrePersonalizado() + " (Trámite)");
                    virtualCp.setRutaArchivo(doc.getRutaArchivo());
                    virtualCp.setFechaCarga(doc.getFechaCarga());
                    virtualCp.setCargadoPor(doc.getUsuarioCarga() != null ? doc.getUsuarioCarga() : "SISTEMA");
                    virtualCp.setTamanoBytes(doc.getTamanoBytes());
                    archivos.add(virtualCp);
                }
            }

            // Obtener archivos de VegetableAnnexesData (los anexos que ya venían de un inicio/portal)
            List<VegetableAnnexesData> annexesList = em.createQuery(
                "SELECT d FROM VegetableAnnexesData d WHERE d.vegetableForms.id = :vfid", VegetableAnnexesData.class)
                .setParameter("vfid", vegetableFormId)
                .getResultList();
            
            for (VegetableAnnexesData annex : annexesList) {
                ComprobantePago virtualCp = new ComprobantePago();
                int annexId = annex.getId().getVegetableAnnexesId();
                virtualCp.setId(-1000000000 - (vegetableFormId * 100) - annexId);
                virtualCp.setVegetableFormId(vegetableFormId);
                virtualCp.setNombreArchivo(annex.getFileName());
                String desc = annex.getVegetableAnnexes() != null && annex.getVegetableAnnexes().getName() != null 
                              ? annex.getVegetableAnnexes().getName() : "Anexo";
                virtualCp.setNombrePersonalizado(desc + " (Inicio)");
                
                String baseDir = senadi.gob.ec.adminob.util.AppConfig.get("upload.base.path", "UPLOAD_BASE_PATH",
                    "C:" + java.io.File.separator + "uploads");
                virtualCp.setRutaArchivo(baseDir + java.io.File.separator + vegetableFormId + java.io.File.separator + annex.getFileName());
                
                java.io.File diskFile = new java.io.File(virtualCp.getRutaArchivo());
                if (diskFile.exists() && diskFile.isFile()) {
                    virtualCp.setTamanoBytes(diskFile.length());
                    virtualCp.setFechaCarga(new java.sql.Timestamp(diskFile.lastModified()));
                } else {
                    virtualCp.setTamanoBytes(0L);
                    virtualCp.setFechaCarga(new java.sql.Timestamp(System.currentTimeMillis()));
                }
                virtualCp.setCargadoPor("PORTAL (INICIO)");
                
                // Evitar duplicados por ruta
                boolean alreadyRegistered = false;
                if (virtualCp.getRutaArchivo() != null) {
                    try {
                        String canonicalPath = new java.io.File(virtualCp.getRutaArchivo()).getCanonicalPath();
                        for (ComprobantePago cp : archivos) {
                            if (cp.getRutaArchivo() != null && new java.io.File(cp.getRutaArchivo()).getCanonicalPath().equals(canonicalPath)) {
                                alreadyRegistered = true;
                                break;
                            }
                        }
                    } catch (Exception ex) {}
                }
                if (!alreadyRegistered) {
                    archivos.add(virtualCp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }

        // --- CONSULTA A BASE DE DATOS EXTERNA EN TIEMPO REAL ---
        Connection extConn = null;
        try {
            extConn = senadi.gob.ec.adminob.util.Operations.doConnectionToFormularios();
            if (extConn != null) {
                // 1. Obtener anexos remotos de breeder_annexes_data
                String sqlAnnex = "SELECT d.breeder_annex_id, d.file, a.name " +
                                  "FROM breeder_annexes_data d " +
                                  "LEFT JOIN breeder_annexes a ON d.breeder_annex_id = a.id " +
                                  "WHERE d.breeder_form_id = ?";
                try (PreparedStatement ps = extConn.prepareStatement(sqlAnnex)) {
                    ps.setInt(1, vegetableFormId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int annexId = rs.getInt("breeder_annex_id");
                            String file = rs.getString("file");
                            String name = rs.getString("name");
                            if (file != null && !file.trim().isEmpty()) {
                                ComprobantePago virtualCp = new ComprobantePago();
                                virtualCp.setId(-1000000000 - (vegetableFormId * 100) - annexId);
                                virtualCp.setVegetableFormId(vegetableFormId);
                                virtualCp.setNombreArchivo(file);
                                virtualCp.setNombrePersonalizado((name != null ? name : "Anexo") + " (Portal - Inicio)");
                                virtualCp.setRutaArchivo(senadi.gob.ec.adminob.util.Parameter.RUTA_URL + vegetableFormId + "/" + file);
                                virtualCp.setFechaCarga(new java.sql.Timestamp(System.currentTimeMillis()));
                                virtualCp.setTamanoBytes(0L);
                                virtualCp.setCargadoPor("PORTAL (INICIO)");

                                // Evitar duplicados por nombre de archivo
                                boolean duplicated = false;
                                for (ComprobantePago cp : archivos) {
                                    if (cp.getNombreArchivo() != null && cp.getNombreArchivo().equalsIgnoreCase(file)) {
                                        duplicated = true;
                                        break;
                                    }
                                }
                                if (!duplicated) {
                                    archivos.add(virtualCp);
                                }
                            }
                        }
                    }
                }

                // 2. Obtener datos de breeder_forms
                String sqlForm = "SELECT application_number, payment_receipt_id, discount_file, evidence_file FROM breeder_forms WHERE id = ?";
                String appNumber = null;
                Integer paymentReceiptId = null;
                String discountFile = null;
                String evidenceFile = null;
                
                try (PreparedStatement ps = extConn.prepareStatement(sqlForm)) {
                    ps.setInt(1, vegetableFormId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            appNumber = rs.getString("application_number");
                            paymentReceiptId = (Integer) rs.getObject("payment_receipt_id");
                            discountFile = rs.getString("discount_file");
                            evidenceFile = rs.getString("evidence_file");
                        }
                    }
                }

                // Certificado de Descuento (Rango -2000000)
                if (discountFile != null && !discountFile.trim().isEmpty()) {
                    ComprobantePago virtualCp = new ComprobantePago();
                    virtualCp.setId(-2000000 - vegetableFormId);
                    virtualCp.setVegetableFormId(vegetableFormId);
                    virtualCp.setNombreArchivo(discountFile);
                    virtualCp.setNombrePersonalizado("Certificado de Descuento (Portal - Inicio)");
                    virtualCp.setRutaArchivo(senadi.gob.ec.adminob.util.Parameter.RUTA_CERT_DESC_URL + discountFile);
                    virtualCp.setFechaCarga(new java.sql.Timestamp(System.currentTimeMillis()));
                    virtualCp.setTamanoBytes(0L);
                    virtualCp.setCargadoPor("PORTAL (DESCUENTO)");
                    
                    boolean duplicated = false;
                    for (ComprobantePago cp : archivos) {
                        if (cp.getNombreArchivo() != null && cp.getNombreArchivo().equalsIgnoreCase(discountFile)) {
                            duplicated = true;
                            break;
                        }
                    }
                    if (!duplicated) {
                        archivos.add(virtualCp);
                    }
                }

                // Evidencia de Pago (Rango -3000000)
                if (evidenceFile != null && !evidenceFile.trim().isEmpty()) {
                    ComprobantePago virtualCp = new ComprobantePago();
                    virtualCp.setId(-3000000 - vegetableFormId);
                    virtualCp.setVegetableFormId(vegetableFormId);
                    virtualCp.setNombreArchivo(evidenceFile);
                    virtualCp.setNombrePersonalizado("Evidencia de Pago (Portal - Pago)");
                    virtualCp.setRutaArchivo(senadi.gob.ec.adminob.util.Parameter.RUTA_URL + vegetableFormId + "/" + evidenceFile);
                    virtualCp.setFechaCarga(new java.sql.Timestamp(System.currentTimeMillis()));
                    virtualCp.setTamanoBytes(0L);
                    virtualCp.setCargadoPor("PORTAL (EVIDENCIA)");
                    
                    boolean duplicated = false;
                    for (ComprobantePago cp : archivos) {
                        if (cp.getNombreArchivo() != null && cp.getNombreArchivo().equalsIgnoreCase(evidenceFile)) {
                            duplicated = true;
                            break;
                        }
                    }
                    if (!duplicated) {
                        archivos.add(virtualCp);
                    }
                }

                // 3. Obtener vouchers vinculados de la tabla voucher (Rango -10000000 - voucherId)
                java.util.Set<Integer> addedVoucherIds = new java.util.HashSet<>();
                if (paymentReceiptId != null || (appNumber != null && !appNumber.trim().isEmpty())) {
                    StringBuilder sqlVoucher = new StringBuilder("SELECT id, document_number, voucher_old_file, value, application_date FROM voucher WHERE ");
                    boolean hasReceipt = paymentReceiptId != null;
                    boolean hasApp = appNumber != null && !appNumber.trim().isEmpty();
                    if (hasReceipt && hasApp) {
                        sqlVoucher.append("payment_receipt_id = ? OR application_number = ?");
                    } else if (hasReceipt) {
                        sqlVoucher.append("payment_receipt_id = ?");
                    } else {
                        sqlVoucher.append("application_number = ?");
                    }
                    
                    try (PreparedStatement ps = extConn.prepareStatement(sqlVoucher.toString())) {
                        int paramIdx = 1;
                        if (hasReceipt) {
                            ps.setInt(paramIdx++, paymentReceiptId);
                        }
                        if (hasApp) {
                            ps.setString(paramIdx++, appNumber);
                        }
                        
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                int vId = rs.getInt("id");
                                String file = rs.getString("voucher_old_file");
                                String docNum = rs.getString("document_number");
                                Double value = rs.getDouble("value");
                                java.sql.Timestamp appDate = rs.getTimestamp("application_date");
                                
                                if (file != null && !file.trim().isEmpty() && !addedVoucherIds.contains(vId)) {
                                    addedVoucherIds.add(vId);
                                    ComprobantePago virtualCp = new ComprobantePago();
                                    virtualCp.setId(-10000000 - vId);
                                    virtualCp.setVegetableFormId(vegetableFormId);
                                    virtualCp.setNombreArchivo(file);
                                    
                                    String desc = "Comprobante de Pago (Portal - Pago";
                                    if (docNum != null && !docNum.trim().isEmpty()) {
                                        desc += " #" + docNum;
                                    }
                                    if (value != null && value > 0) {
                                        desc += " - $" + value;
                                    }
                                    desc += ")";
                                    
                                    virtualCp.setNombrePersonalizado(desc);
                                    virtualCp.setRutaArchivo(senadi.gob.ec.adminob.util.Parameter.RUTA_URL + vegetableFormId + "/" + file);
                                    virtualCp.setFechaCarga(appDate != null ? appDate : new java.sql.Timestamp(System.currentTimeMillis()));
                                    virtualCp.setTamanoBytes(0L);
                                    virtualCp.setCargadoPor("PORTAL (PAGO)");
                                    
                                    boolean duplicated = false;
                                    for (ComprobantePago cp : archivos) {
                                        if (cp.getNombreArchivo() != null && cp.getNombreArchivo().equalsIgnoreCase(file)) {
                                            duplicated = true;
                                            break;
                                        }
                                    }
                                    if (!duplicated) {
                                        archivos.add(virtualCp);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ComprobantePagoDAO] Error al consultar la base externa: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (extConn != null) {
                try {
                    extConn.close();
                } catch (SQLException ignore) {}
            }
        }

        // 3. Buscar todos los archivos físicos en el directorio de carga en disco
        try {
            String baseDir = senadi.gob.ec.adminob.util.AppConfig.get("upload.base.path", "UPLOAD_BASE_PATH",
                "C:" + java.io.File.separator + "uploads");
            
            java.io.File uploadDir = new java.io.File(baseDir + java.io.File.separator + vegetableFormId);
            if (uploadDir.exists() && uploadDir.isDirectory()) {
                java.io.File[] files = uploadDir.listFiles();
                if (files != null) {
                    java.util.List<java.io.File> fileList = new java.util.ArrayList<>();
                    for (java.io.File f : files) {
                        if (f.isFile() && f.length() > 0) {
                            fileList.add(f);
                        }
                    }
                    fileList.sort((f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));
                    
                    for (int i = 0; i < fileList.size(); i++) {
                        java.io.File f = fileList.get(i);
                        String canonicalPath = f.getCanonicalPath();
                        boolean alreadyRegistered = false;
                        for (ComprobantePago cp : archivos) {
                            if (cp.getRutaArchivo() != null) {
                                try {
                                    if (new java.io.File(cp.getRutaArchivo()).getCanonicalPath().equals(canonicalPath)) {
                                        alreadyRegistered = true;
                                        break;
                                    }
                                } catch (Exception ignored) {}
                            }
                            if (cp.getNombreArchivo() != null && cp.getNombreArchivo().equalsIgnoreCase(f.getName())) {
                                alreadyRegistered = true;
                                break;
                            }
                        }
                        if (!alreadyRegistered) {
                            ComprobantePago diskCp = new ComprobantePago();
                            // Generate a unique negative ID for files found on disk: -20000000 - (formId * 1000) - index
                            diskCp.setId(-20000000 - (vegetableFormId * 1000) - i);
                            diskCp.setVegetableFormId(vegetableFormId);
                            diskCp.setNombreArchivo(f.getName());
                            
                            if (f.getName().equals("pdf_voucher_breederfrm_" + vegetableFormId + ".pdf")) {
                                diskCp.setNombrePersonalizado("Comprobante de Pago Histórico (Portal)");
                            } else if (f.getName().equals("pdf_breederfrm_" + vegetableFormId + ".pdf")) {
                                diskCp.setNombrePersonalizado("Formulario de Obtentores Histórico (Portal)");
                            } else {
                                diskCp.setNombrePersonalizado(f.getName());
                            }
                            
                            diskCp.setRutaArchivo(f.getAbsolutePath());
                            diskCp.setFechaCarga(new java.sql.Timestamp(f.lastModified()));
                            diskCp.setCargadoPor("SISTEMA (DISCO)");
                            diskCp.setTamanoBytes(f.length());
                            archivos.add(diskCp);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return archivos;
    }

    /** Devuelve un comprobante por su ID primario. */
    public ComprobantePago getById(Integer id) {
        try {
            return getEntityManager()
                .createQuery("SELECT c FROM ComprobantePago c WHERE c.id = :id", ComprobantePago.class)
                .setParameter("id", id)
                .getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            return null;
        } finally {
            getEntityManager().close();
        }
    }

    /** Devuelve true si el trámite tiene al menos un comprobante registrado. */
    public boolean existsByVegetableFormId(Integer vegetableFormId) {
        try {
            Long count = getEntityManager()
                .createQuery(
                    "SELECT COUNT(c) FROM ComprobantePago c WHERE c.vegetableFormId = :vfid",
                    Long.class)
                .setParameter("vfid", vegetableFormId)
                .getSingleResult();
            return count > 0;
        } finally {
            getEntityManager().close();
        }
    }
    
    /** Una sola consulta que devuelve todos los vegetableFormId que tienen al menos un archivo. */
    public java.util.Set<Integer> getDistinctVegetableFormIds() {
        java.util.Set<Integer> ids = new java.util.HashSet<>();
        
        // 1. De la tabla local ComprobantePago
        try {
            java.util.List<Integer> list = getEntityManager()
                .createQuery("SELECT DISTINCT c.vegetableFormId FROM ComprobantePago c", Integer.class)
                .getResultList();
            ids.addAll(list);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. De la tabla local VegetableAnnexesData
        try {
            java.util.List<Integer> list = getEntityManager()
                .createQuery("SELECT DISTINCT d.vegetableForms.id FROM VegetableAnnexesData d", Integer.class)
                .getResultList();
            ids.addAll(list);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. De las tablas locales Tramite/DocumentoTramite
        try {
            java.util.List<Integer> list = getEntityManager()
                .createQuery("SELECT DISTINCT t.vegetableFormId FROM Tramite t WHERE t.id IN (SELECT DISTINCT d.tramiteId FROM DocumentoTramite d)", Integer.class)
                .getResultList();
            ids.addAll(list);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 4. De las carpetas fisicas en disco C:/uploads/id
        try {
            String baseDir = senadi.gob.ec.adminob.util.AppConfig.get("upload.base.path", "UPLOAD_BASE_PATH",
                "C:" + java.io.File.separator + "uploads");
            java.io.File dir = new java.io.File(baseDir);
            if (dir.exists() && dir.isDirectory()) {
                java.io.File[] subdirs = dir.listFiles();
                if (subdirs != null) {
                    for (java.io.File subdir : subdirs) {
                        if (subdir.isDirectory()) {
                            try {
                                int id = Integer.parseInt(subdir.getName());
                                java.io.File[] files = subdir.listFiles();
                                if (files != null) {
                                    for (java.io.File f : files) {
                                        if (f.isFile() && f.length() > 0) {
                                            ids.add(id);
                                            break;
                                        }
                                    }
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 5. De la base de datos externa remota
        Connection extConn = null;
        try {
            extConn = senadi.gob.ec.adminob.util.Operations.doConnectionToFormularios();
            if (extConn != null) {
                // Query breeder_annexes_data
                try (PreparedStatement ps = extConn.prepareStatement(
                        "SELECT DISTINCT breeder_form_id FROM breeder_annexes_data WHERE file IS NOT NULL AND file != ''")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            ids.add(rs.getInt(1));
                        }
                    }
                }
                // Query breeder_forms con discount_file o evidence_file
                try (PreparedStatement ps = extConn.prepareStatement(
                        "SELECT id FROM breeder_forms WHERE (discount_file IS NOT NULL AND discount_file != '') OR (evidence_file IS NOT NULL AND evidence_file != '')")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            ids.add(rs.getInt(1));
                        }
                    }
                }
                // Query vouchers
                try (PreparedStatement ps = extConn.prepareStatement(
                        "SELECT DISTINCT f.id FROM breeder_forms f JOIN voucher v ON (v.payment_receipt_id = f.payment_receipt_id OR v.application_number = f.application_number) WHERE v.voucher_old_file IS NOT NULL AND v.voucher_old_file != ''")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            ids.add(rs.getInt(1));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ComprobantePagoDAO] Error al consultar distinct IDs remotos: " + e.getMessage());
        } finally {
            if (extConn != null) {
                try {
                    extConn.close();
                } catch (SQLException ignore) {}
            }
        }

        return ids;
    }

    public void delete(Integer id) throws Exception {
        javax.persistence.EntityManager em = getEntityManager();
        em.getTransaction().begin();
        try {
            ComprobantePago cp = em.find(ComprobantePago.class, id);
            if (cp != null) {
                em.remove(cp);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
