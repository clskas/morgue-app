package com.gestionmorgue.util;

import com.gestionmorgue.config.DatabaseConfig;
import org.flywaydb.core.Flyway;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseManager {
    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                runFlywayMigrations();
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

    private static void runFlywayMigrations() {
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(
                            DatabaseConfig.getJdbcUrl(),
                            DatabaseConfig.getJdbcUser(),
                            DatabaseConfig.getJdbcPassword()
                    )
                    .locations("classpath:db/migration")
                    .load();
            flyway.migrate();
            log.info("Flyway migrations executed successfully");
        } catch (Exception e) {
            log.error("Flyway migration failed", e);
            throw e;
        }
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }
}
