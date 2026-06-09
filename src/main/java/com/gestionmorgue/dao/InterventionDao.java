package com.gestionmorgue.dao;

import com.gestionmorgue.model.Intervention;
import com.gestionmorgue.util.DatabaseManager;
import com.gestionmorgue.util.PaginatedResult;
import org.hibernate.Session;

import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.List;

public class InterventionDao extends GenericDao<Intervention> {

    public InterventionDao() {
        super(Intervention.class);
    }

    public List<Intervention> findPending() {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            return session.createQuery(
                    "select i from Intervention i left join fetch i.deceased left join fetch i.performer where i.status = 'PLANIFIEE' order by i.scheduledAt asc",
                    Intervention.class).list();
        }
    }

    public List<Intervention> findByDateRange(LocalDateTime start, LocalDateTime end) {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            return session.createQuery(
                    "select i from Intervention i left join fetch i.deceased left join fetch i.performer where i.scheduledAt between :start and :end order by i.scheduledAt asc",
                    Intervention.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .list();
        }
    }

    public long countByStatus(String status) {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> query = cb.createQuery(Long.class);
            Root<Intervention> root = query.from(Intervention.class);
            query.select(cb.count(root)).where(cb.equal(root.get("status"), status));
            return session.createQuery(query).getSingleResult();
        }
    }

    public PaginatedResult<Intervention> findPendingPaginated(int page, int pageSize) {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> countQ = cb.createQuery(Long.class);
            Root<Intervention> countRoot = countQ.from(Intervention.class);
            countQ.select(cb.count(countRoot)).where(cb.equal(countRoot.get("status"), "PLANIFIEE"));
            long total = session.createQuery(countQ).getSingleResult();

            List<Intervention> results = session.createQuery(
                    "select distinct i from Intervention i left join fetch i.deceased left join fetch i.performer where i.status = 'PLANIFIEE' order by i.scheduledAt asc",
                    Intervention.class)
                    .setFirstResult(page * pageSize)
                    .setMaxResults(pageSize)
                    .list();

            return new PaginatedResult<>(results, total, page, pageSize);
        }
    }
}
