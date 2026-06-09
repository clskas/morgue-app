package com.gestionmorgue.service;

import com.gestionmorgue.dao.DeceasedDao;
import com.gestionmorgue.dao.ExitDao;
import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.model.ExitAuthorization;
import com.gestionmorgue.model.User;
import com.gestionmorgue.util.DatabaseManager;
import com.gestionmorgue.util.PaginatedResult;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public class ExitService {
    private final DeceasedDao deceasedDao;
    private final ExitDao exitDao;

    public ExitService() {
        this.deceasedDao = new DeceasedDao();
        this.exitDao = new ExitDao();
    }

    public ExitAuthorization createAuthorization(Deceased deceased, User authorizedBy,
                                                  String transportCompany, String authorizedPerson, String notes) {
        Transaction tx = null;
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            ExitAuthorization auth = new ExitAuthorization();
            auth.setDeceased(deceased);
            auth.setAuthorizedBy(authorizedBy);
            auth.setTransportCompany(transportCompany);
            auth.setAuthorizedPerson(authorizedPerson);
            auth.setNotes(notes);
            auth.setStatus("PENDING");
            auth.setAuthorizedAt(LocalDateTime.now());
            session.persist(auth);
            tx.commit();
            return auth;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Erreur création autorisation", e);
        }
    }

    public void approve(ExitAuthorization auth) {
        Transaction tx = null;
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            auth.setStatus("APPROUVEE");
            session.merge(auth);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Erreur approbation", e);
        }
    }

    public void confirmExit(ExitAuthorization auth) {
        Transaction tx = null;
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            ExitAuthorization managed = session.get(ExitAuthorization.class, auth.getId());
            managed.setStatus("SORTIE_EFFECTUEE");
            managed.setEffectiveExitAt(LocalDateTime.now());
            session.merge(managed);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Erreur confirmation sortie", e);
        }
    }

    public List<ExitAuthorization> findAll() {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            return session.createQuery(
                "select e from ExitAuthorization e join fetch e.deceased",
                ExitAuthorization.class).list();
        }
    }

    public PaginatedResult<ExitAuthorization> findPaginated(int page, int pageSize) {
        return exitDao.findPaginated(page, pageSize, "authorizedAt");
    }

    public long countPending() {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            var cb = session.getCriteriaBuilder();
            var query = cb.createQuery(Long.class);
            var root = query.from(ExitAuthorization.class);
            query.select(cb.count(root)).where(cb.equal(root.get("status"), "PENDING"));
            return session.createQuery(query).getSingleResult();
        }
    }
}
