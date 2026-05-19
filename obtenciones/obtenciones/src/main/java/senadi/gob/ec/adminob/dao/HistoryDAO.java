/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package senadi.gob.ec.adminob.dao;

import java.util.List;
import javax.persistence.Query;
import senadi.gob.ec.adminob.model.History;

/**
 *
 * @author michael
 */
public class HistoryDAO extends DAOAbstractM<History> {

    public HistoryDAO(History t) {
        super(t);
    }

    @Override
    public List<History> buscarTodos() {
        try {
            Query query = this.getEntityManager().createQuery("SELECT h from History h order by h.id");
            return query.getResultList();
        } finally {
            this.getEntityManager().close();
        }
    }

    public List<History> getHistoriesByAppNumber(String applicationNumber) {
        try {
            Query query = this.getEntityManager().createQuery("Select h from History h where h.applicationNumber = :appnumber order by h.fecha desc");
            query.setParameter("appnumber", applicationNumber);
            return query.getResultList();
        } finally {
            this.getEntityManager().close();
        }

    }
}
