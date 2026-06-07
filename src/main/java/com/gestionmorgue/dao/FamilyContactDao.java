package com.gestionmorgue.dao;

import com.gestionmorgue.model.FamilyContact;
import com.gestionmorgue.util.DatabaseManager;
import org.hibernate.Session;

import java.util.List;

public class FamilyContactDao extends GenericDao<FamilyContact> {

    public FamilyContactDao() {
        super(FamilyContact.class);
    }

    public List<FamilyContact> findByDeceasedId(Long deceasedId) {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            return session.createQuery(
                "select f from FamilyContact f where f.deceased.id = :did order by f.fullName",
                FamilyContact.class).setParameter("did", deceasedId).list();
        }
    }
}
