package com.gestionmorgue.service;

import com.gestionmorgue.dao.StorageLocationDao;
import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.model.StorageAssignment;
import com.gestionmorgue.model.StorageLocation;
import com.gestionmorgue.util.DatabaseManager;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public class StorageService {
    private final StorageLocationDao locationDao;

    public StorageService() {
        this.locationDao = new StorageLocationDao();
    }

    public StorageLocation createLocation(String code, String label, String zone, int temperature) {
        StorageLocation loc = new StorageLocation();
        loc.setCode(code);
        loc.setLabel(label);
        loc.setZone(zone);
        loc.setTemperature(temperature);
        loc.setOccupied(false);
        return locationDao.save(loc);
    }

    public StorageAssignment assignLocation(Deceased deceased, StorageLocation location) {
        if (location.isOccupied()) {
            throw new RuntimeException("Emplacement déjà occupé");
        }
        Transaction tx = null;
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            StorageAssignment assignment = new StorageAssignment();
            assignment.setDeceased(deceased);
            assignment.setLocation(location);
            assignment.setAssignedAt(LocalDateTime.now());
            location.setOccupied(true);
            session.merge(location);
            session.persist(assignment);
            tx.commit();
            return assignment;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Erreur assignation emplacement", e);
        }
    }

    public void releaseLocation(StorageAssignment assignment, String releasedBy) {
        Transaction tx = null;
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            assignment.setReleasedAt(LocalDateTime.now());
            assignment.setReleasedBy(releasedBy);
            StorageLocation loc = assignment.getLocation();
            loc.setOccupied(false);
            session.merge(loc);
            session.merge(assignment);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Erreur libération emplacement", e);
        }
    }

    public List<StorageLocation> getAvailableLocations() {
        return locationDao.findAvailable();
    }

    public List<StorageLocation> getAllLocations() {
        return locationDao.findAll();
    }

    public long getOccupiedCount() {
        return locationDao.countOccupied();
    }

    public long getTotalCount() {
        return locationDao.count();
    }

    public List<StorageAssignment> getAllAssignments() {
        try (var session = DatabaseManager.getSessionFactory().openSession()) {
            return session.createQuery(
                "from StorageAssignment a left join fetch a.deceased left join fetch a.location order by a.assignedAt desc",
                StorageAssignment.class).list();
        }
    }
}
