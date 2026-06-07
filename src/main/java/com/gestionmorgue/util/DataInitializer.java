package com.gestionmorgue.util;

import com.gestionmorgue.config.DatabaseConfig;
import com.gestionmorgue.model.StorageLocation;
import com.gestionmorgue.model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.criteria.CriteriaQuery;

public class DataInitializer {

    public static void initialize() {
        String testDbUrl = System.getProperty("test.db.url");
        if (testDbUrl != null) {
            DatabaseConfig.setDbUrl(testDbUrl);
        }
        DatabaseManager.shutdown();
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaQuery<Long> countQuery = session.getCriteriaBuilder().createQuery(Long.class);
            countQuery.select(session.getCriteriaBuilder().count(countQuery.from(User.class)));
            long userCount = session.createQuery(countQuery).getSingleResult();

            if (userCount == 0) {
                seedDefaultAdmin(session);
                seedStorageLocations(session);
                System.out.println("[DataInitializer] Données initiales créées avec succès.");
            }
        } catch (Exception e) {
            System.err.println("[DataInitializer] Erreur: " + e.getMessage());
        }
    }

    private static void seedDefaultAdmin(Session session) {
        Transaction tx = session.beginTransaction();
        try {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(BCrypt.hashpw("admin123", BCrypt.gensalt()));
            admin.setFullName("Administrateur");
            admin.setRole("ADMIN");
            admin.setEmail("admin@gestionmorgue.com");
            admin.setActive(true);
            session.persist(admin);

            User medecin = new User();
            medecin.setUsername("medecin");
            medecin.setPasswordHash(BCrypt.hashpw("medecin123", BCrypt.gensalt()));
            medecin.setFullName("Dr. Martin");
            medecin.setRole("MEDECIN");
            medecin.setEmail("medecin@gestionmorgue.com");
            medecin.setActive(true);
            session.persist(medecin);

            User thanato = new User();
            thanato.setUsername("thanato");
            thanato.setPasswordHash(BCrypt.hashpw("thanato123", BCrypt.gensalt()));
            thanato.setFullName("Pierre Dubois");
            thanato.setRole("THANATOPRACTEUR");
            thanato.setEmail("thanato@gestionmorgue.com");
            thanato.setActive(true);
            session.persist(thanato);

            tx.commit();
            System.out.println("[DataInitializer] Utilisateurs créés : admin/admin123, medecin/medecin123, thanato/thanato123");
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }

    private static void seedStorageLocations(Session session) {
        Transaction tx = session.beginTransaction();
        try {
            String[][] locations = {
                {"A-01", "Tiroir A1", "A", "4"},
                {"A-02", "Tiroir A2", "A", "4"},
                {"A-03", "Tiroir A3", "A", "4"},
                {"A-04", "Tiroir A4", "A", "4"},
                {"B-01", "Tiroir B1", "B", "4"},
                {"B-02", "Tiroir B2", "B", "4"},
                {"B-03", "Tiroir B3", "B", "4"},
                {"B-04", "Tiroir B4", "B", "4"},
                {"C-01", "Chambre froide C1", "C", "2"},
                {"C-02", "Chambre froide C2", "C", "2"},
            };

            for (String[] loc : locations) {
                StorageLocation sl = new StorageLocation();
                sl.setCode(loc[0]);
                sl.setLabel(loc[1]);
                sl.setZone(loc[2]);
                sl.setTemperature(Integer.parseInt(loc[3]));
                sl.setOccupied(false);
                session.persist(sl);
            }
            tx.commit();
            System.out.println("[DataInitializer] 10 emplacements de stockage créés.");
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }
}
