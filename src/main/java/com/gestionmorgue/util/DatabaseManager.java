package com.gestionmorgue.util;

import com.gestionmorgue.config.DatabaseConfig;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class DatabaseManager {
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                        .applySettings(DatabaseConfig.getHibernateProperties())
                        .build();
                MetadataSources sources = new MetadataSources(registry);
                sources.addAnnotatedClass(com.gestionmorgue.model.Deceased.class);
                sources.addAnnotatedClass(com.gestionmorgue.model.StorageLocation.class);
                sources.addAnnotatedClass(com.gestionmorgue.model.StorageAssignment.class);
                sources.addAnnotatedClass(com.gestionmorgue.model.Intervention.class);
                sources.addAnnotatedClass(com.gestionmorgue.model.ExitAuthorization.class);
                sources.addAnnotatedClass(com.gestionmorgue.model.User.class);
                sources.addAnnotatedClass(com.gestionmorgue.model.AuditLog.class);
                sources.addAnnotatedClass(com.gestionmorgue.model.FamilyContact.class);
                sources.addAnnotatedClass(com.gestionmorgue.model.Attachment.class);
                Metadata metadata = sources.getMetadataBuilder().build();
                sessionFactory = metadata.getSessionFactoryBuilder().build();
            } catch (Exception e) {
                throw new RuntimeException("Erreur initialisation Hibernate", e);
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }
}
