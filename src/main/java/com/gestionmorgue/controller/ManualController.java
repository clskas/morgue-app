package com.gestionmorgue.controller;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class ManualController implements Initializable {

    @FXML
    private WebView webView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        WebEngine engine = webView.getEngine();
        URL manualUrl = getClass().getResource("/manual/index.html");
        if (manualUrl != null) {
            engine.load(manualUrl.toExternalForm());
        } else {
            engine.loadContent("<html><body><h2>Manuel non trouvé</h2></body></html>");
        }
    }

    @FXML
    private void handlePrint() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(webView.getScene().getWindow())) {
            webView.getEngine().print(job);
            job.endJob();
        }
    }
}
