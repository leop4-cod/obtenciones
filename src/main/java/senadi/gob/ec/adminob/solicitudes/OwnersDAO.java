/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package senadi.gob.ec.adminob.solicitudes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import senadi.gob.ec.adminob.util.Operations;

/**
 *
 * @author Michael Y.
 */
public class OwnersDAO {

    public List<Owners> buscarTodos() {
        String query = "Select * from owners";
        try {
            Connection con = Operations.doConnectionToCasilleros();
            PreparedStatement pst = con.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            List<Owners> owners = new ArrayList<>();
            while (rs.next()) {
                Owners owner = new Owners();
                owner.setId(rs.getInt("id"));
                owner.setFirsName(rs.getString("firstname"));
                owner.setLastName(rs.getString("lastname"));
                owner.setDocument(rs.getString("document"));
                owner.setEmail(rs.getString("email"));
                owner.setPassword(rs.getString("password"));
                owners.add(owner);
            }
            con.close();
            return owners;
        } catch (Exception ex) {
            System.out.println("error en obtener datos owners todos: " + ex);
            return new ArrayList<>();
        }
    }

    public boolean existsUser(String document, String pass) {
        String query = "Select * from owners where document = '" + document + "' and password = '" + Operations.md5(pass) + "'";
        try {
            Connection con = Operations.doConnectionToCasilleros();
            PreparedStatement pst = con.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            Owners owner = new Owners();
            while (rs.next()) {
                owner.setId(rs.getInt("id"));
                owner.setFirsName(rs.getString("firstname"));
                owner.setLastName(rs.getString("lastname"));
                owner.setDocument(rs.getString("document"));
                owner.setEmail(rs.getString("email"));
                owner.setPassword(rs.getString("password"));
            }
            con.close();
            if (owner.getId() != null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            System.out.println("error en obtener datos owners exists user: " + ex);
            return false;
        }
    }

    public Owners getOwnersByLogin(String document, String pass) {

        String query = "Select o.id as owner_id, o.firstname, o.lastname,"
                + "o.document,o.email,o.phone,o.mobile,o.type,o.law_firm,o.status,o.legal_firstname,o.legal_lastname,"
                + "lo.id as casillero, o.password "
                + "from owners o "
                + "inner join lockers as lo on o.id = lo.owner_id "
                + "where o.document = '" + document + "' and o.password = '" + Operations.md5(pass) + "'";
        try {
            Connection con = Operations.doConnectionToCasilleros();
            PreparedStatement pst = con.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            Owners owner = new Owners();
            while (rs.next()) {
                owner.setId(rs.getInt("owner_id"));
                owner.setFirsName(rs.getString("firstname"));
                owner.setLastName(rs.getString("lastname"));
                owner.setDocument(rs.getString("document"));
                owner.setEmail(rs.getString("email"));
                owner.setCasillero(rs.getString("casillero"));
                owner.setPassword(rs.getString("password"));
            }
            con.close();
            return owner;
        } catch (Exception ex) {
            System.out.println("error en obtener datos owners exists user: " + ex);
            return new Owners();
        }
    }

    public Owners getOwnerByIdentificacion(String identificacion) {

        String query = "Select o.id, o.firstname, o.lastname,"
                + "o.document,o.email,o.phone,o.mobile,o.type,o.law_firm,o.status,o.legal_firstname,o.legal_lastname,"
                + "lo.id as casillero, o.password "
                + "from owners o "
                + "inner join lockers as lo on o.id = lo.owner_id "
                + "where o.document = '" + identificacion + "'";
        try {
            Connection con = Operations.doConnectionToCasilleros();
            PreparedStatement pst = con.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            Owners owner = new Owners();
            while (rs.next()) {
                owner.setId(rs.getInt("id"));
                owner.setFirsName(rs.getString("firstname"));
                owner.setLastName(rs.getString("lastname"));
                owner.setDocument(rs.getString("document"));
                owner.setEmail(rs.getString("email"));
                owner.setCasillero(rs.getString("casillero"));
                owner.setPassword(rs.getString("password"));
            }
            con.close();

            return owner;

        } catch (Exception ex) {
            System.out.println("error en obtener datos owners exists user: " + ex);
            return null;
        }
    }
    
    public java.util.Map<Integer, String> getLockersByOwnerIds(java.util.List<Integer> ownerIds) {
        java.util.Map<Integer, String> result = new java.util.HashMap<>();
        if (ownerIds == null || ownerIds.isEmpty()) return result;

        java.util.Set<Integer> unique = new java.util.LinkedHashSet<>(ownerIds);
        StringBuilder sb = new StringBuilder();
        for (Integer id : unique) {
            if (sb.length() > 0) sb.append(",");
            sb.append(id);
        }
        String query = "SELECT o.id, o.firstname, o.lastname, lo.id AS casillero "
                + "FROM owners o INNER JOIN lockers lo ON o.id = lo.owner_id "
                + "WHERE o.id IN (" + sb + ")";
        try {
            Connection con = Operations.doConnectionToCasilleros();
            PreparedStatement pst = con.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String cas = rs.getString("casillero");
                if (cas == null) cas = "SIN CASILLERO";
                String fn = rs.getString("firstname");
                String ln = rs.getString("lastname");
                String nombres = ((fn == null ? "" : fn) + " " + (ln == null ? "" : ln)).trim();
                result.put(id, nombres.isEmpty() ? cas : cas + " - " + nombres);
            }
            con.close();
        } catch (Exception ex) {
            System.err.println("Error en getLockersByOwnerIds: " + ex);
        }
        return result;
    }

    public Owners getOwnerById(Integer ownerId) {

        String query = "Select o.id, o.firstname, o.lastname,"
                + "o.document,o.email,o.phone,o.mobile,o.type,o.law_firm,o.status,o.legal_firstname,o.legal_lastname,"
                + "lo.id as casillero, o.password "
                + "from owners o "
                + "inner join lockers as lo on o.id = lo.owner_id "
                + "where o.id = " + ownerId;
        try {
            Connection con = Operations.doConnectionToCasilleros();
            PreparedStatement pst = con.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            Owners owner = new Owners();
            while (rs.next()) {
                owner.setId(rs.getInt("id"));
                owner.setFirsName(rs.getString("firstname"));
                owner.setLastName(rs.getString("lastname"));
                owner.setDocument(rs.getString("document"));
                owner.setEmail(rs.getString("email"));
                owner.setCasillero(rs.getString("casillero"));
                owner.setPassword(rs.getString("password"));
            }
            con.close();

            return owner;

        } catch (Exception ex) {
            System.out.println("error en obtener datos owners exists user: " + ex);
            return null;
        }
    }

}
