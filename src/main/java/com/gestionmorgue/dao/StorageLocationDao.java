package com.gestionmorgue.dao;

import com.gestionmorgue.model.StorageLocation;
import com.gestionmorgue.util.DatabaseManager;
import com.gestionmorgue.util.PaginatedResult;
import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class StorageLocationDao extends GenericDao<StorageLocation> {

    public StorageLocationDao() {
        super(StorageLocation.class);
    }

    public List<StorageLocation> findAvailable() {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<StorageLocation> query = cb.createQuery(StorageLocation.class);
            Root<StorageLocation> root = query.from(StorageLocation.class);
            query.select(root).where(cb.isFalse(root.get("occupied")));
            return session.createQuery(query).list();
        }
    }

    public List<StorageLocation> findByZone(String zone) {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<StorageLocation> query = cb.createQuery(StorageLocation.class);
            Root<StorageLocation> root = query.from(StorageLocation.class);
            query.select(root).where(cb.equal(root.get("zone"), zone));
            return session.createQuery(query).list();
        }
    }

    public long countOccupied() {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> query = cb.createQuery(Long.class);
            Root<StorageLocation> root = query.from(StorageLocation.class);
            query.select(cb.count(root)).where(cb.isTrue(root.get("occupied")));
            return session.createQuery(query).getSingleResult();
        }
    }

    public PaginatedResult<StorageLocation> findPaginatedWithAssignments(int page, int pageSize) {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> countQ = cb.createQuery(Long.class);
            countQ.select(cb.count(countQ.from(StorageLocation.class)));
            long total = session.createQuery(countQ).getSingleResult();

            CriteriaQuery<Long> idQ = cb.createQuery(Long.class);
            Root<StorageLocation> idRoot = idQ.from(StorageLocation.class);
            idQ.select(idRoot.get("id"));
            List<Long> ids = session.createQuery(idQ)
                    .setFirstResult(page * pageSize)
                    .setMaxResults(pageSize)
                    .list();

            if (ids.isEmpty()) {
                return new PaginatedResult<>(List.of(), total, page, pageSize);
            }

            List<StorageLocation> results = session.createQuery(
                    "select distinct l from StorageLocation l left join fetch l.assignments a left join fetch a.deceased where a.releasedAt is null and l.id in :ids",
                    StorageLocation.class)
                    .setParameter("ids", ids)
                    .list();

            return new PaginatedResult<>(results, total, page, pageSize);
        }
    }
}
