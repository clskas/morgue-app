package com.gestionmorgue.dao;

import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.util.DatabaseManager;
import org.hibernate.Session;

import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DeceasedDao extends GenericDao<Deceased> {

    public DeceasedDao() {
        super(Deceased.class);
    }

    public List<Deceased> search(String lastName, String firstName, String dossierNumber) {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Deceased> query = cb.createQuery(Deceased.class);
            Root<Deceased> root = query.from(Deceased.class);

            List<Predicate> predicates = new ArrayList<>();
            if (lastName != null && !lastName.isEmpty())
                predicates.add(cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
            if (firstName != null && !firstName.isEmpty())
                predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
            if (dossierNumber != null && !dossierNumber.isEmpty())
                predicates.add(cb.like(root.get("dossierNumber"), "%" + dossierNumber + "%"));

            query.select(root).where(cb.and(predicates.toArray(new Predicate[0])));
            query.orderBy(cb.desc(root.get("createdAt")));
            return session.createQuery(query).list();
        }
    }

    public List<Deceased> searchByQuery(String query, String dossierNumber, LocalDate dateFrom, LocalDate dateTo) {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Deceased> cq = cb.createQuery(Deceased.class);
            Root<Deceased> root = cq.from(Deceased.class);
            List<Predicate> predicates = new ArrayList<>();
            if (query != null && !query.isEmpty()) {
                String pattern = "%" + query.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("lastName")), pattern),
                    cb.like(cb.lower(root.get("firstName")), pattern)
                ));
            }
            if (dossierNumber != null && !dossierNumber.isEmpty())
                predicates.add(cb.like(root.get("dossierNumber"), "%" + dossierNumber + "%"));
            if (dateFrom != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("deathDate"), dateFrom));
            if (dateTo != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("deathDate"), dateTo));
            cq.select(root).where(cb.and(predicates.toArray(new Predicate[0])));
            cq.orderBy(cb.desc(root.get("createdAt")));
            return session.createQuery(cq).list();
        }
    }

    public List<Deceased> findRecentlyAdded(int limit) {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Deceased> query = cb.createQuery(Deceased.class);
            Root<Deceased> root = query.from(Deceased.class);
            query.select(root).orderBy(cb.desc(root.get("createdAt")));
            return session.createQuery(query).setMaxResults(limit).list();
        }
    }
}
