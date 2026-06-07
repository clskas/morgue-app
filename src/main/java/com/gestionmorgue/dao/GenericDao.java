package com.gestionmorgue.dao;

import com.gestionmorgue.util.DatabaseManager;
import com.gestionmorgue.util.PaginatedResult;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenericDao<T> {
    protected final Class<T> entityClass;

    public GenericDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public T save(T entity) {
        Transaction tx = null;
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(entity);
            tx.commit();
            return entity;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Erreur sauvegarde " + entityClass.getSimpleName(), e);
        }
    }

    public T update(T entity) {
        Transaction tx = null;
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(entity);
            tx.commit();
            return entity;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Erreur mise à jour " + entityClass.getSimpleName(), e);
        }
    }

    public void delete(T entity) {
        Transaction tx = null;
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.remove(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Erreur suppression " + entityClass.getSimpleName(), e);
        }
    }

    public Optional<T> findById(Long id) {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(entityClass, id));
        }
    }

    public List<T> findAll() {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaQuery<T> query = session.getCriteriaBuilder().createQuery(entityClass);
            query.select(query.from(entityClass));
            return session.createQuery(query).list();
        }
    }

    public long count() {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaQuery<Long> query = session.getCriteriaBuilder().createQuery(Long.class);
            query.select(session.getCriteriaBuilder().count(query.from(entityClass)));
            return session.createQuery(query).getSingleResult();
        }
    }

    public PaginatedResult<T> findPaginated(int page, int pageSize) {
        return findPaginated(page, pageSize, null);
    }

    public PaginatedResult<T> findPaginated(int page, int pageSize, String orderByField) {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();

            CriteriaQuery<Long> countQ = cb.createQuery(Long.class);
            countQ.select(cb.count(countQ.from(entityClass)));
            long total = session.createQuery(countQ).getSingleResult();

            CriteriaQuery<T> query = cb.createQuery(entityClass);
            Root<T> root = query.from(entityClass);
            query.select(root);
            if (orderByField != null) {
                query.orderBy(cb.desc(root.get(orderByField)));
            }
            List<T> results = session.createQuery(query)
                    .setFirstResult(page * pageSize)
                    .setMaxResults(pageSize)
                    .list();

            return new PaginatedResult<>(results, total, page, pageSize);
        }
    }

    public PaginatedResult<T> searchPaginated(String searchField, String searchValue, int page, int pageSize) {
        if (searchValue == null || searchValue.trim().isEmpty()) {
            return findPaginated(page, pageSize);
        }
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();

            CriteriaQuery<Long> countQ = cb.createQuery(Long.class);
            Root<T> countRoot = countQ.from(entityClass);
            countQ.select(cb.count(countRoot));
            countQ.where(cb.like(cb.lower(countRoot.get(searchField)), "%" + searchValue.toLowerCase() + "%"));
            long total = session.createQuery(countQ).getSingleResult();

            CriteriaQuery<T> query = cb.createQuery(entityClass);
            Root<T> root = query.from(entityClass);
            query.select(root);
            query.where(cb.like(cb.lower(root.get(searchField)), "%" + searchValue.toLowerCase() + "%"));
            List<T> results = session.createQuery(query)
                    .setFirstResult(page * pageSize)
                    .setMaxResults(pageSize)
                    .list();

            return new PaginatedResult<>(results, total, page, pageSize);
        }
    }
}
