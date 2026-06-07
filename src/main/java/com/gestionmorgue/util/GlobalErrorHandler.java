package com.gestionmorgue.util;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalErrorHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Exception non capturée dans le thread " + t.getName(), e);
        Platform.runLater(() -> {
            NotificationUtil.showError("Erreur critique",
                    "Une erreur inattendue est survenue.\n\n" + e.getClass().getSimpleName() + ": " + e.getMessage() +
                    "\n\nConsultez les logs pour plus de détails.");
        });
    }

    public static void install() {
        Thread.setDefaultUncaughtExceptionHandler(new GlobalErrorHandler());
    }
}
