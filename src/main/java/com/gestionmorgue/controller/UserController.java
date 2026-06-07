package com.gestionmorgue.controller;

import com.gestionmorgue.dao.UserDao;
import com.gestionmorgue.model.User;
import com.gestionmorgue.service.AuthService;
import com.gestionmorgue.util.I18nUtil;
import com.gestionmorgue.util.NotificationUtil;
import com.gestionmorgue.util.PaginatedResult;
import com.gestionmorgue.util.SecurityUtil;
import com.gestionmorgue.util.SessionManager;
import com.gestionmorgue.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colActive;

    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private CheckBox activeCheck;
    @FXML private Button saveButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button resetPasswordButton;

    private AuthService authService;
    private UserDao userDao;
    private ObservableList<User> users;
    private User selectedUser;
    private int currentPage = 0;
    private boolean hasMore = true;
    private static final int PAGE_SIZE = 50;

    @FXML
    public void initialize() {
        if (!SecurityUtil.isAdmin()) {
            saveButton.setDisable(true);
            updateButton.setDisable(true);
            deleteButton.setDisable(true);
            resetPasswordButton.setDisable(true);
        }

        authService = new AuthService();
        userDao = new UserDao();
        users = FXCollections.observableArrayList();
        roleCombo.setItems(FXCollections.observableArrayList("ADMIN", "MEDECIN", "THANATOPRACTEUR", "GREFFIER"));

        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colActive.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().isActive() ? I18nUtil.t("confirm.yes") : I18nUtil.t("confirm.no")));

        userTable.setItems(users);
        userTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> selectUser(n));
        setupScrollListener();
        refresh();
    }

    private void setupScrollListener() {
        userTable.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                ScrollBar scrollBar = (ScrollBar) userTable.lookup(".scroll-bar:vertical");
                if (scrollBar != null) {
                    scrollBar.valueProperty().addListener((obs2, oldVal, newVal) -> {
                        if ((double) newVal >= 0.95 && hasMore) {
                            loadNextPage();
                        }
                    });
                }
            }
        });
    }

    @FXML
    private void handleSave() {
        SecurityUtil.requireAdmin();
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String role = roleCombo.getValue();

        if (!ValidationUtil.isNotEmpty(username) || !ValidationUtil.isNotEmpty(fullName) || role == null) {
            NotificationUtil.showWarning(I18nUtil.t("warning.title"), I18nUtil.t("user.validation.required"));
            return;
        }
        try {
            if (!ValidationUtil.isNotEmpty(passwordField.getText())) {
                NotificationUtil.showWarning(I18nUtil.t("warning.title"), I18nUtil.t("user.validation.password.required"));
                return;
            }
            authService.createUser(username, passwordField.getText(), fullName, role, emailField.getText().trim());
            NotificationUtil.showInfo(I18nUtil.t("info.title"), I18nUtil.t("user.save.success"));
            refresh();
            clearForm();
        } catch (Exception e) {
            NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        SecurityUtil.requireAdmin();
        if (selectedUser == null) return;
        String fullName = fullNameField.getText().trim();
        String role = roleCombo.getValue();
        if (!ValidationUtil.isNotEmpty(fullName) || role == null) {
            NotificationUtil.showWarning(I18nUtil.t("warning.title"), I18nUtil.t("user.validation.update.required"));
            return;
        }
        try {
            selectedUser.setFullName(fullName);
            selectedUser.setRole(role);
            selectedUser.setEmail(emailField.getText().trim());
            selectedUser.setActive(activeCheck.isSelected());
            var dao = new com.gestionmorgue.dao.UserDao();
            dao.update(selectedUser);
            NotificationUtil.showInfo(I18nUtil.t("info.title"), I18nUtil.t("user.update.success"));
            refresh();
            clearForm();
        } catch (Exception e) {
            NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        SecurityUtil.requireAdmin();
        if (selectedUser == null) return;
        if (selectedUser.getId().equals(SessionManager.getInstance().getCurrentUser().getId())) {
            NotificationUtil.showWarning(I18nUtil.t("warning.title"), I18nUtil.t("user.delete.self"));
            return;
        }
        boolean confirm = NotificationUtil.showConfirm(I18nUtil.t("confirm.title"),
                I18nUtil.t("user.delete.confirm", selectedUser.getUsername()));
        if (confirm) {
            try {
                var dao = new com.gestionmorgue.dao.UserDao();
                dao.delete(selectedUser);
                NotificationUtil.showInfo(I18nUtil.t("info.title"), I18nUtil.t("user.delete.success"));
                refresh();
                clearForm();
            } catch (Exception e) {
                NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
            }
        }
    }

    @FXML
    private void handleResetPassword() {
        SecurityUtil.requireAdmin();
        if (selectedUser == null) return;
        if (!ValidationUtil.isNotEmpty(passwordField.getText())) {
            NotificationUtil.showWarning(I18nUtil.t("warning.title"), I18nUtil.t("user.validation.newpassword"));
            return;
        }
        boolean confirm = NotificationUtil.showConfirm(I18nUtil.t("confirm.title"),
                I18nUtil.t("user.resetpassword.confirm", selectedUser.getUsername()));
        if (confirm) {
            try {
                selectedUser.setPasswordHash(
                        org.mindrot.jbcrypt.BCrypt.hashpw(passwordField.getText(), org.mindrot.jbcrypt.BCrypt.gensalt()));
                var dao = new com.gestionmorgue.dao.UserDao();
                dao.update(selectedUser);
                NotificationUtil.showInfo(I18nUtil.t("info.title"), I18nUtil.t("user.resetpassword.success"));
                clearForm();
            } catch (Exception e) {
                NotificationUtil.showError(I18nUtil.t("error.title"), e.getMessage());
            }
        }
    }

    private void selectUser(User u) {
        selectedUser = u;
        if (u != null) {
            usernameField.setText(u.getUsername());
            fullNameField.setText(u.getFullName());
            emailField.setText(u.getEmail());
            roleCombo.setValue(u.getRole());
            activeCheck.setSelected(u.isActive());
            passwordField.clear();
        }
    }

    private void clearForm() {
        selectedUser = null;
        usernameField.clear();
        fullNameField.clear();
        emailField.clear();
        passwordField.clear();
        roleCombo.setValue(null);
        activeCheck.setSelected(true);
        userTable.getSelectionModel().clearSelection();
    }

    private void loadNextPage() {
        currentPage++;
        PaginatedResult<User> result = userDao.findPaginated(currentPage, PAGE_SIZE, "username");
        hasMore = result.hasNext();
        users.addAll(result.getResults());
    }

    private void loadPage(int page) {
        currentPage = page;
        PaginatedResult<User> result = userDao.findPaginated(page, PAGE_SIZE, "username");
        hasMore = result.hasNext();
        users.setAll(result.getResults());
    }

    private void refresh() {
        loadPage(0);
    }
}
