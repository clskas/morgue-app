package com.gestionmorgue.dao;

import com.gestionmorgue.model.Attachment;
import com.gestionmorgue.util.DatabaseManager;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class AttachmentDao extends GenericDao<Attachment> {

    public AttachmentDao() {
        super(Attachment.class);
    }

    public List<Attachment> findByEntity(String entityType, Long entityId) {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Attachment> query = cb.createQuery(Attachment.class);
            Root<Attachment> root = query.from(Attachment.class);
            query.select(root)
                 .where(cb.equal(root.get("entityType"), entityType),
                        cb.equal(root.get("entityId"), entityId));
            return session.createQuery(query).list();
        }
    }

    public void deleteByEntity(String entityType, Long entityId) {
        Transaction tx = null;
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Attachment> query = cb.createQuery(Attachment.class);
            Root<Attachment> root = query.from(Attachment.class);
            query.select(root)
                 .where(cb.equal(root.get("entityType"), entityType),
                        cb.equal(root.get("entityId"), entityId));
            List<Attachment> attachments = session.createQuery(query).list();
            for (Attachment a : attachments) {
                session.remove(a);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Erreur suppression pièces jointes", e);
        }
    }
}
