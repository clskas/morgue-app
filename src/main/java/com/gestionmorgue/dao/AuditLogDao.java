package com.gestionmorgue.dao;

import com.gestionmorgue.model.AuditLog;
import com.gestionmorgue.util.DatabaseManager;
import com.gestionmorgue.util.PaginatedResult;
import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.List;

public class AuditLogDao extends GenericDao<AuditLog> {

    public AuditLogDao() {
        super(AuditLog.class);
    }

    public List<AuditLog> findRecent(int limit) {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            return session.createQuery(
                "select a from AuditLog a left join fetch a.user order by a.timestamp desc",
                AuditLog.class).setMaxResults(limit).list();
        }
    }

    public PaginatedResult<AuditLog> findPaginatedWithUser(int page, int pageSize) {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> countQ = cb.createQuery(Long.class);
            countQ.select(cb.count(countQ.from(AuditLog.class)));
            long total = session.createQuery(countQ).getSingleResult();

            List<AuditLog> results = session.createQuery(
                    "select a from AuditLog a left join fetch a.user order by a.timestamp desc",
                    AuditLog.class)
                    .setFirstResult(page * pageSize)
                    .setMaxResults(pageSize)
                    .list();

            return new PaginatedResult<>(results, total, page, pageSize);
        }
    }

    public PaginatedResult<AuditLog> findPaginatedByDateRange(LocalDateTime from, LocalDateTime to, int page, int pageSize) {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();

            CriteriaQuery<Long> countQ = cb.createQuery(Long.class);
            Root<AuditLog> countRoot = countQ.from(AuditLog.class);
            countQ.select(cb.count(countRoot));
            countQ.where(cb.between(countRoot.get("timestamp"), from, to));
            long total = session.createQuery(countQ).getSingleResult();

            CriteriaQuery<AuditLog> q = cb.createQuery(AuditLog.class);
            Root<AuditLog> root = q.from(AuditLog.class);
            root.fetch("user", JoinType.LEFT);
            q.select(root);
            q.where(cb.between(root.get("timestamp"), from, to));
            q.orderBy(cb.desc(root.get("timestamp")));

            List<AuditLog> results = session.createQuery(q)
                    .setFirstResult(page * pageSize)
                    .setMaxResults(pageSize)
                    .list();

            return new PaginatedResult<>(results, total, page, pageSize);
        }
    }

}
