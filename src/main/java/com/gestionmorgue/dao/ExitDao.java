package com.gestionmorgue.dao;

import com.gestionmorgue.model.ExitAuthorization;
import com.gestionmorgue.util.DatabaseManager;
import com.gestionmorgue.util.PaginatedResult;
import org.hibernate.Session;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class ExitDao extends GenericDao<ExitAuthorization> {

    public ExitDao() {
        super(ExitAuthorization.class);
    }

    public PaginatedResult<ExitAuthorization> findPaginated(int page, int pageSize, String orderByField) {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> countQ = cb.createQuery(Long.class);
            countQ.select(cb.count(countQ.from(ExitAuthorization.class)));
            long total = session.createQuery(countQ).getSingleResult();

            List<ExitAuthorization> results = session.createQuery(
                    "select distinct e from ExitAuthorization e left join fetch e.deceased left join fetch e.authorizedBy order by e.authorizedAt desc",
                    ExitAuthorization.class)
                    .setFirstResult(page * pageSize)
                    .setMaxResults(pageSize)
                    .list();

            return new PaginatedResult<>(results, total, page, pageSize);
        }
    }
}