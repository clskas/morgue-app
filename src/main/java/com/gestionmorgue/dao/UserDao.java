package com.gestionmorgue.dao;

import com.gestionmorgue.model.User;
import com.gestionmorgue.util.DatabaseManager;
import org.hibernate.Session;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Optional;

public class UserDao extends GenericDao<User> {

    public UserDao() {
        super(User.class);
    }

    public Optional<User> findByUsername(String username) {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> query = cb.createQuery(User.class);
            Root<User> root = query.from(User.class);
            query.select(root).where(cb.equal(root.get("username"), username));
            return Optional.ofNullable(session.createQuery(query).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
