/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package senadi.gob.ec.adminob.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 *
 * @author michael
 */
public class Operations {

    public static String INFORMACION = "INFORMACIÓN";
    public static String ERROR = "ERROR";
    public static String AVISO = "AVISO";

    public static void message(String tipo, String mensaje) {
        FacesMessage msg = null;
        switch (tipo) {
            case "ERROR":
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", mensaje);
                break;
            case "INFORMACIÓN":
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "INFORMACIÓN", mensaje);
                break;
            case "AVISO":
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "AVISO", mensaje);
                break;
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public static Connection doConnectionToCasilleros() throws SQLException {
        Connection con = null;
        con = DriverManager.getConnection(Parameter.iepi_casilleros, Parameter.USER, Parameter.PASSWORD);
        return con;
    }

    public static Connection doConnectionToFormularios() throws SQLException {
        Connection con = null;
        con = DriverManager.getConnection(Parameter.iepi_formularios, Parameter.USER, Parameter.PASSWORD);
        return con;
    }
    
    
    /* Retorna un hash MD5 a partir de un texto */
    public static String md5(String txt) {
        return getHash(txt, "MD5");
    }
    
    /* Retorna un hash a partir de un tipo y un texto */
    public static String getHash(String txt, String hashType) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest
                    .getInstance(hashType);
            byte[] array = md.digest(txt.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
                        .substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
    
    /**
     * Da formato a la fecha recibida en el siguiente orden 'yyyy-mm-dd'
     *
     * @param date
     * @return
     */
    public static String formatDate(Date date) {
//        System.out.println("--> "+date.toString());
        int dia = date.getDate();
        int mes = date.getMonth() + 1;
        int año = date.getYear() + 1900;
        String d = dia + "";
        String m = mes + "";
        if (dia < 10) {
            d = "0" + dia;
        }
        if (mes < 10) {
            m = "0" + mes;
        }

        String fecha = año + "-" + m + "-" + d;
//        System.out.println("<-- "+fecha+"\n");
        return fecha;
    }
    
    public static boolean validateDate(Date fecha) {
        try {
            fecha.toString();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
