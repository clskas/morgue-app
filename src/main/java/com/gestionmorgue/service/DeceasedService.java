package com.gestionmorgue.service;

import com.gestionmorgue.config.ConfigService;
import com.gestionmorgue.dao.DeceasedDao;
import com.gestionmorgue.model.Deceased;

import java.util.List;

public class DeceasedService {
    private final DeceasedDao deceasedDao;
    private int dossierCounter = 1;

    public DeceasedService() {
        this.deceasedDao = new DeceasedDao();
        dossierCounter = (int) deceasedDao.count() + 1;
    }

    public Deceased createDeceased(String lastName, String firstName,
                                    String birthDate, String deathDate,
                                    String placeOfDeath, String gender) {
        Deceased deceased = new Deceased();
        deceased.setDossierNumber(generateDossierNumber());
        deceased.setLastName(lastName.toUpperCase());
        deceased.setFirstName(firstName);
        if (birthDate != null && !birthDate.isEmpty())
            deceased.setBirthDate(java.time.LocalDate.parse(birthDate));
        if (deathDate != null && !deathDate.isEmpty())
            deceased.setDeathDate(java.time.LocalDate.parse(deathDate));
        deceased.setPlaceOfDeath(placeOfDeath);
        deceased.setGender(gender);
        return deceasedDao.save(deceased);
    }

    private String generateDossierNumber() {
        String prefix = ConfigService.getInstance().getDossierPrefix();
        String year = String.valueOf(java.time.Year.now().getValue());
        String number = String.format("%04d", dossierCounter++);
        return prefix + "-" + year + "-" + number;
    }

    public List<Deceased> search(String lastName, String firstName, String dossierNumber) {
        return deceasedDao.search(lastName, firstName, dossierNumber);
    }

    public List<Deceased> searchByQuery(String query, String dossierNumber, java.time.LocalDate dateFrom, java.time.LocalDate dateTo) {
        return deceasedDao.searchByQuery(query, dossierNumber, dateFrom, dateTo);
    }

    public List<Deceased> getRecentDeceased(int limit) {
        return deceasedDao.findRecentlyAdded(limit);
    }

    public Deceased update(Deceased deceased) {
        return deceasedDao.update(deceased);
    }

    public void delete(Deceased deceased) {
        deceasedDao.delete(deceased);
    }

    public Deceased findById(Long id) {
        return deceasedDao.findById(id).orElse(null);
    }

    public long countActiveAssignments(Long deceasedId) {
        try (var session = com.gestionmorgue.util.DatabaseManager.getSessionFactory().openSession()) {
            return session.createQuery(
                "select count(a) from StorageAssignment a where a.deceased.id = :did and a.releasedAt is null",
                Long.class).setParameter("did", deceasedId).getSingleResult();
        }
    }

    public long countPendingExits(Long deceasedId) {
        try (var session = com.gestionmorgue.util.DatabaseManager.getSessionFactory().openSession()) {
            return session.createQuery(
                "select count(e) from ExitAuthorization e where e.deceased.id = :did and e.status != 'SORTIE_EFFECTUEE'",
                Long.class).setParameter("did", deceasedId).getSingleResult();
        }
    }

    public long countPlannedInterventions(Long deceasedId) {
        try (var session = com.gestionmorgue.util.DatabaseManager.getSessionFactory().openSession()) {
            return session.createQuery(
                "select count(i) from Intervention i where i.deceased.id = :did and i.status = 'PLANIFIEE'",
                Long.class).setParameter("did", deceasedId).getSingleResult();
        }
    }

    public long getCount() {
        return deceasedDao.count();
    }
}
