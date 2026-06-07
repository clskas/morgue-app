package com.gestionmorgue.service;

import com.gestionmorgue.dao.InterventionDao;
import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.model.Intervention;
import com.gestionmorgue.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class InterventionService {
    private final InterventionDao interventionDao;

    public InterventionService() {
        this.interventionDao = new InterventionDao();
    }

    public Intervention schedule(Deceased deceased, User performer, String type,
                                  LocalDateTime scheduledAt) {
        Intervention intervention = new Intervention();
        intervention.setDeceased(deceased);
        intervention.setPerformer(performer);
        intervention.setType(type);
        intervention.setScheduledAt(scheduledAt);
        intervention.setStatus("PLANIFIEE");
        return interventionDao.save(intervention);
    }

    public Intervention complete(Intervention intervention, String report, String productsUsed) {
        intervention.setStatus("TERMINEE");
        intervention.setCompletedAt(LocalDateTime.now());
        intervention.setReport(report);
        intervention.setProductsUsed(productsUsed);
        return interventionDao.update(intervention);
    }

    public List<Intervention> getPendingInterventions() {
        return interventionDao.findPending();
    }

    public List<Intervention> getByDateRange(LocalDateTime start, LocalDateTime end) {
        return interventionDao.findByDateRange(start, end);
    }

    public long countByStatus(String status) {
        return interventionDao.countByStatus(status);
    }
}
