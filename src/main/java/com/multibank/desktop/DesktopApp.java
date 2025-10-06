package com.multibank.desktop;

import com.multibank.MultiBankApplication;
import com.multibank.dto.BankAccountResponse;
import com.multibank.domain.TransactionCategory;
import com.multibank.dto.SpendingChartPoint;
import com.multibank.dto.TransactionResponse;
import com.multibank.service.TransactionService;
import com.multibank.service.SavingsPlanService;
import com.multibank.dto.SavingsPlanRequest;
import com.multibank.dto.SavingsPlanResponse;
import com.multibank.service.AnalyticsService;
import com.multibank.service.BankAccountService;
import com.multibank.service.BankSynchronizationService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DesktopApp extends Application {

    private static ConfigurableApplicationContext springContext;

    private BankAccountService bankAccountService;
    private BankSynchronizationService bankSynchronizationService;
    private AnalyticsService analyticsService;
    private TransactionService transactionService;
    private SavingsPlanService savingsPlanService;

    private final TableView<BankAccountResponse> accountsTable = new TableView<>();
    private final TableView<TransactionResponse> transactionsTable = new TableView<>();
    private final TableView<SavingsPlanResponse> savingsTable = new TableView<>();
    private final Label statusBar = new Label("Gata.");
    private JMetro jMetro;
    private String currentUser;
    private final Map<String, String> users = new ConcurrentHashMap<>();

    @Override
    public void init() {
        springContext = new SpringApplicationBuilder(MultiBankApplication.class)
                .web(WebApplicationType.NONE)
                .run();

        bankAccountService = springContext.getBean(BankAccountService.class);
        bankSynchronizationService = springContext.getBean(BankSynchronizationService.class);
        analyticsService = springContext.getBean(AnalyticsService.class);
        transactionService = springContext.getBean(TransactionService.class);
        savingsPlanService = springContext.getBean(SavingsPlanService.class);

        // Hardcoded default user until DB integration
        users.putIfAbsent("dariusc", "parola123");
    }

    @Override
    public void start(Stage stage) {
        // Simple login gate (hardcoded user: "dariusc" / parola "parola123")
        boolean ok = showLoginDialog(stage);
        if (!ok) {
            Platform.exit();
            return;
        }
        stage.setTitle("MultiBank Desktop");

        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("app-tabs");
        tabs.getTabs().add(buildAccountsTab());
        tabs.getTabs().add(buildTransactionsTab());
        tabs.getTabs().add(buildAnalyticsTab());
        tabs.getTabs().add(buildSavingsTab());
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setCenter(tabs);
        // Sidebar navigation
        VBox sidebar = buildSidebar(tabs);
        root.setLeft(sidebar);
        statusBar.setPadding(new Insets(6, 10, 6, 10));
        root.setBottom(statusBar);

        // Header (title + user + theme toggle)
        HBox header = new HBox();
        header.getStyleClass().add("app-header");
        Label title = new Label("MultiBank Desktop");
        title.getStyleClass().add("app-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label userLabel = new Label(currentUser == null ? "" : ("Utilizator: " + currentUser));
        userLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.9);");
        ToggleButton themeToggle = new ToggleButton("Dark");
        themeToggle.getStyleClass().add("btn-ghost");
        header.getChildren().addAll(title, spacer, userLabel, themeToggle);
        root.setTop(header);

        Scene scene = new Scene(root, 1100, 700);
        // Apply JMetro modern theme and our accent stylesheet
        try {
            jMetro = new JMetro(Style.LIGHT);
            jMetro.setScene(scene);
        } catch (Throwable ignored) {}
        try {
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/desktop/styles.css")).toExternalForm()
            );
        } catch (Exception ignored) {}

        // Toggle light/dark theme
        themeToggle.setOnAction(e -> {
            if (jMetro != null) {
                if (themeToggle.isSelected()) {
                    jMetro.setStyle(Style.DARK);
                    themeToggle.setText("Light");
                    if (!root.getStyleClass().contains("theme-dark")) {
                        root.getStyleClass().add("theme-dark");
                    }
                } else {
                    jMetro.setStyle(Style.LIGHT);
                    themeToggle.setText("Dark");
                    root.getStyleClass().remove("theme-dark");
                }
            }
        });

        stage.setScene(scene);
        stage.show();

        // Load initial data
        reloadAccounts();
    }

    private boolean showLoginDialog(Stage owner) {
        final Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Autentificare");

        Label title = new Label("Autentificare");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label hint = new Label("Utilizator implicit: 'dariusc' / parola: 'parola123'");

        TextField userField = new TextField();
        userField.setPromptText("Utilizator");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Parola");
        passField.setPromptText("Parola (opțional)");

        Label error = new Label();
        error.setStyle("-fx-text-fill: #ff6b6b;");

        Button loginBtn = new Button("Login");
        Button signupBtn = new Button("Signup");
        Button cancelBtn = new Button("Renunță");
        loginBtn.setDefaultButton(true);
        cancelBtn.setCancelButton(true);

        Runnable attempt = () -> {
            String u = userField.getText() == null ? "" : userField.getText().trim();
            if ("dariusc".equalsIgnoreCase(u)) {
                currentUser = u;
                dialog.close();
            } else {
                error.setText("Utilizator invalid. Încearcă: dariusc");
            }
        };
        loginBtn.setOnAction(e -> attempt.run());
        // Override handler to enforce username+password via in-memory users
        loginBtn.setOnAction(e -> {
            String u = userField.getText() == null ? "" : userField.getText().trim();
            String p = passField.getText() == null ? "" : passField.getText();
            String stored = users.get(u.toLowerCase());
            if (stored != null && stored.equals(p)) {
                currentUser = u;
                dialog.close();
            } else {
                error.setText("Date de autentificare invalide.");
            }
        });
        // Signup opens registration dialog and auto-logs in
        signupBtn.setOnAction(e -> {
            String created = showSignupDialog(dialog);
            if (created != null) {
                currentUser = created;
                dialog.close();
            }
        });
        cancelBtn.setOnAction(e -> { currentUser = null; dialog.close(); });
        // Ensure final prompt text is set correctly
        passField.setPromptText("Parola");

        HBox actions = new HBox(10, loginBtn, signupBtn, cancelBtn);
        VBox box = new VBox(10, title, hint, userField, passField, error, actions);
        box.setPadding(new Insets(16));
        Scene scene = new Scene(box, 360, 220);
        try {
            // Apply same look if available
            if (jMetro == null) {
                jMetro = new JMetro(Style.LIGHT);
            }
            jMetro.setScene(scene);
            try {
                scene.getStylesheets().add(
                        Objects.requireNonNull(getClass().getResource("/desktop/styles.css")).toExternalForm()
                );
            } catch (Exception ignored) {}
        } catch (Throwable ignored) {}
        dialog.setScene(scene);
        dialog.showAndWait();
        return currentUser != null;
    }

    private String showSignupDialog(Stage owner) {
        final Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Înregistrare");

        Label title = new Label("Creează cont nou");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField userField = new TextField();
        userField.setPromptText("Utilizator");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Parola");
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirmă parola");

        Label error = new Label();
        error.setStyle("-fx-text-fill: #ff6b6b;");

        Button createBtn = new Button("Creează");
        Button cancelBtn = new Button("Anulează");
        createBtn.setDefaultButton(true);
        cancelBtn.setCancelButton(true);

        final String[] created = new String[1];
        Runnable attempt = () -> {
            String uRaw = userField.getText() == null ? "" : userField.getText().trim();
            String u = uRaw.toLowerCase();
            String p = passField.getText() == null ? "" : passField.getText();
            String c = confirmField.getText() == null ? "" : confirmField.getText();
            if (uRaw.isEmpty()) { error.setText("Introdu un nume de utilizator."); return; }
            if (p.length() < 4) { error.setText("Parola trebuie să aibă ≥ 4 caractere."); return; }
            if (!p.equals(c)) { error.setText("Parolele nu coincid."); return; }
            if (users.containsKey(u)) { error.setText("Utilizatorul există deja."); return; }
            users.put(u, p);
            created[0] = uRaw;
            dialog.close();
        };

        createBtn.setOnAction(e -> attempt.run());
        cancelBtn.setOnAction(e -> { created[0] = null; dialog.close(); });

        HBox actions = new HBox(10, createBtn, cancelBtn);
        VBox box = new VBox(10, title, userField, passField, confirmField, error, actions);
        box.setPadding(new Insets(16));
        Scene scene = new Scene(box, 360, 240);
        try {
            if (jMetro == null) jMetro = new JMetro(Style.LIGHT);
            jMetro.setScene(scene);
            try {
                scene.getStylesheets().add(
                        Objects.requireNonNull(getClass().getResource("/desktop/styles.css")).toExternalForm()
                );
            } catch (Exception ignored) {}
        } catch (Throwable ignored) {}
        dialog.setScene(scene);
        dialog.showAndWait();
        return created[0];
    }

    private VBox buildSidebar(TabPane tabs) {
        VBox box = new VBox(8);
        box.getStyleClass().add("app-sidebar");
        ToggleGroup group = new ToggleGroup();

        ToggleButton btnDashboard = makeNavButton("Dashboard", group);
        ToggleButton btnAccounts = makeNavButton("Conturi", group);
        ToggleButton btnTransactions = makeNavButton("Tranzacții", group);
        ToggleButton btnAnalytics = makeNavButton("Analiză", group);
        ToggleButton btnSavings = makeNavButton("Economii", group);

        btnDashboard.setSelected(true);
        // Map to tabs: 0=Conturi,1=Tranzacții,2=Analiză,3=Economii
        btnDashboard.setOnAction(e -> tabs.getSelectionModel().select(2));
        btnAccounts.setOnAction(e -> tabs.getSelectionModel().select(0));
        btnTransactions.setOnAction(e -> tabs.getSelectionModel().select(1));
        btnAnalytics.setOnAction(e -> tabs.getSelectionModel().select(2));
        btnSavings.setOnAction(e -> tabs.getSelectionModel().select(3));

        box.getChildren().addAll(btnDashboard, btnAccounts, btnTransactions, btnAnalytics, btnSavings);
        return box;
    }

    private ToggleButton makeNavButton(String text, ToggleGroup group) {
        ToggleButton b = new ToggleButton(text);
        b.getStyleClass().add("nav-btn");
        b.setMaxWidth(Double.MAX_VALUE);
        b.setToggleGroup(group);
        return b;
    }

    @SuppressWarnings("unchecked")
    private Tab buildAccountsTab() {
        Tab tab = new Tab("Conturi");

        // Toolbar
        Button syncBtn = new Button("Sincronizează");
        Button reloadBtn = new Button("Reîncarcă");
        HBox toolbar = new HBox(10, syncBtn, reloadBtn);
        toolbar.setPadding(new Insets(10));

        // Table
        accountsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<BankAccountResponse, String> bankCol = new TableColumn<>("Bancă");
        bankCol.setCellValueFactory(c -> new SimpleStringProperty(nullSafe(c.getValue().getBankName())));
        TableColumn<BankAccountResponse, String> numberCol = new TableColumn<>("Număr cont");
        numberCol.setCellValueFactory(c -> new SimpleStringProperty(nullSafe(c.getValue().getAccountNumber())));
        TableColumn<BankAccountResponse, String> ibanCol = new TableColumn<>("IBAN");
        ibanCol.setCellValueFactory(c -> new SimpleStringProperty(nullSafe(c.getValue().getIban())));
        TableColumn<BankAccountResponse, String> typeCol = new TableColumn<>("Tip");
        typeCol.setCellValueFactory(c -> new SimpleStringProperty(nullSafe(c.getValue().getType())));
        TableColumn<BankAccountResponse, String> currencyCol = new TableColumn<>("Monedă");
        currencyCol.setCellValueFactory(c -> new SimpleStringProperty(nullSafe(c.getValue().getCurrency())));
        TableColumn<BankAccountResponse, BigDecimal> balanceCol = new TableColumn<>("Sold");
        balanceCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getBalance()));
        accountsTable.getColumns().setAll(bankCol, numberCol, ibanCol, typeCol, currencyCol, balanceCol);

        VBox content = new VBox(5, toolbar, accountsTable);
        VBox.setVgrow(accountsTable, Priority.ALWAYS);
        content.setPadding(new Insets(10));

        syncBtn.setOnAction(e -> synchronizeBanks());
        reloadBtn.setOnAction(e -> reloadAccounts());

        tab.setContent(content);
        return tab;
    }

    	private Tab buildAnalyticsTab() {
		Tab tab = new Tab("Analiza");

		// KPI row
		HBox kpiRow = new HBox(12);
		kpiRow.getStyleClass().add("kpi-row");
		VBox k1 = createKpiCard("Luna aceasta", "1.935,50€", "5.600,00€", 0.34);
		VBox k2 = createKpiCard("Saptamana aceasta", "1.935,50€", "1.400,00€", 0.60);
		VBox k3 = createKpiCard("Astazi", "0,00€", "200,00€", 0.00);
		kpiRow.getChildren().addAll(k1, k2, k3);

		// Chart area
		CategoryAxis xAxis = new CategoryAxis();
		NumberAxis yAxis = new NumberAxis();
		LineChart<String, Number> monthlyChart = new LineChart<>(xAxis, yAxis);
		monthlyChart.setLegendVisible(false);
		monthlyChart.setAnimated(false);

		Button refreshBtn = new Button("Actualizeaza");
		HBox toolbar = new HBox(10, refreshBtn);
		toolbar.setPadding(new Insets(10));

		VBox content = new VBox(10, kpiRow, toolbar, monthlyChart);
		content.setPadding(new Insets(12));
		VBox.setVgrow(monthlyChart, Priority.ALWAYS);

		refreshBtn.setOnAction(e -> reloadAnalytics(monthlyChart, new PieChart()));
		tab.setContent(content);
		return tab;
	}

	private VBox createKpiCard(String title, String value, String sub, double progress) {
		ProgressIndicator ring = new ProgressIndicator(progress);
		ring.setPrefSize(44, 44);
		Label lTitle = new Label(title);
		lTitle.getStyleClass().add("kpi-title");
		Label lValue = new Label(value);
		lValue.getStyleClass().add("kpi-value");
		Label lSub = new Label(sub);
		lSub.getStyleClass().add("kpi-sub");
		VBox box = new VBox(6, ring, lTitle, lValue, lSub);
		box.getStyleClass().add("kpi-card");
		return box;
	}

    private Tab buildTransactionsTab() {
        Tab tab = new Tab("Tranzacții");

        // Filters
        ComboBox<BankAccountResponse> accountBox = new ComboBox<>();
        accountBox.setPrefWidth(260);
        accountBox.setPromptText("Toate conturile");
        ComboBox<TransactionCategory> categoryBox = new ComboBox<>();
        categoryBox.getItems().setAll(TransactionCategory.values());
        categoryBox.setPromptText("Toate categoriile");
        DatePicker startDate = new DatePicker();
        startDate.setPromptText("De la");
        DatePicker endDate = new DatePicker();
        endDate.setPromptText("Până la");
        Button searchBtn = new Button("Caută");
        HBox filters = new HBox(10, new Label("Cont:"), accountBox, new Label("Categorie:"), categoryBox,
                new Label("De la:"), startDate, new Label("Până la:"), endDate, searchBtn);
        filters.setPadding(new Insets(10));

        // Table
        transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<TransactionResponse, String> dateCol = new TableColumn<>("Data");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getBookingDate() == null ? "" : c.getValue().getBookingDate().format(dateFmt)));
        TableColumn<TransactionResponse, String> descCol = new TableColumn<>("Descriere");
        descCol.setCellValueFactory(c -> new SimpleStringProperty(nullSafe(c.getValue().getDescription())));
        TableColumn<TransactionResponse, String> merchCol = new TableColumn<>("Comerciant");
        merchCol.setCellValueFactory(c -> new SimpleStringProperty(nullSafe(c.getValue().getMerchant())));
        TableColumn<TransactionResponse, String> catCol = new TableColumn<>("Categorie");
        catCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCategory() == null ? "" : c.getValue().getCategory().name()));
        TableColumn<TransactionResponse, String> dirCol = new TableColumn<>("Direcție");
        dirCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDirection() == null ? "" : c.getValue().getDirection().name()));
        TableColumn<TransactionResponse, String> amtCol = new TableColumn<>("Sumă");
        amtCol.setCellValueFactory(c -> new SimpleStringProperty(
                (c.getValue().getAmount() == null ? "" : c.getValue().getAmount().toPlainString()) +
                        (c.getValue().getCurrency() == null ? "" : (" " + c.getValue().getCurrency()))));
        transactionsTable.getColumns().setAll(dateCol, descCol, merchCol, catCol, dirCol, amtCol);

        VBox content = new VBox(6, filters, transactionsTable);
        content.setPadding(new Insets(10));
        VBox.setVgrow(transactionsTable, Priority.ALWAYS);

        // Actions
        searchBtn.setOnAction(e -> reloadTransactions(
                accountBox.getValue() == null ? null : accountBox.getValue().getId(),
                categoryBox.getValue(),
                startDate.getValue(),
                endDate.getValue()
        ));

        // Populate accounts for filter
        runAsync("Încărcare conturi pentru filtre...", bankAccountService::getAllAccounts, list -> {
            accountBox.getItems().setAll(list);
            accountBox.setConverter(new javafx.util.StringConverter<>() {
                @Override public String toString(BankAccountResponse b) {
                    if (b == null) return "";
                    return nullSafe(b.getBankName()) + " - " + nullSafe(b.getAccountNumber());
                }
                @Override public BankAccountResponse fromString(String s) { return null; }
            });
        });

        tab.setContent(content);
        return tab;
    }

    private Tab buildSavingsTab() {
        Tab tab = new Tab("Economii");

        // Form create plan
        TextField nameField = new TextField();
        nameField.setPromptText("Denumire plan");
        TextField targetField = new TextField();
        targetField.setPromptText("Țintă (RON)");
        DatePicker targetDate = new DatePicker();
        targetDate.setPromptText("Termen");
        ComboBox<TransactionCategory> focusBox = new ComboBox<>();
        focusBox.getItems().setAll(TransactionCategory.values());
        focusBox.setPromptText("Categorie focus (opțional)");
        Button createBtn = new Button("Creează");
        HBox form = new HBox(10, new Label("Nume:"), nameField,
                new Label("Țintă:"), targetField, new Label("Termen:"), targetDate,
                new Label("Categorie:"), focusBox, createBtn);
        form.setPadding(new Insets(10));
        HBox.setHgrow(nameField, Priority.SOMETIMES);

        // Table
        savingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<SavingsPlanResponse, String> nameCol = new TableColumn<>("Plan");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(nullSafe(c.getValue().getName())));
        TableColumn<SavingsPlanResponse, String> tgtCol = new TableColumn<>("Țintă");
        tgtCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTargetAmount() == null ? "" : c.getValue().getTargetAmount().toPlainString() + " RON"));
        TableColumn<SavingsPlanResponse, String> curCol = new TableColumn<>("Economisit");
        curCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCurrentAmount() == null ? "" : c.getValue().getCurrentAmount().toPlainString() + " RON"));
        TableColumn<SavingsPlanResponse, String> dateCol = new TableColumn<>("Termen");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTargetDate() == null ? "" : c.getValue().getTargetDate().format(dateFmt)));
        TableColumn<SavingsPlanResponse, String> fcatCol = new TableColumn<>("Categorie");
        fcatCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFocusCategory() == null ? "" : c.getValue().getFocusCategory().name()));
        TableColumn<SavingsPlanResponse, String> progCol = new TableColumn<>("Progres");
        progCol.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f%%", c.getValue().getProgress())));
        savingsTable.getColumns().setAll(nameCol, tgtCol, curCol, dateCol, fcatCol, progCol);

        // Actions on selection
        Button contributeBtn = new Button("Contribuie");
        Button deleteBtn = new Button("Șterge");
        contributeBtn.setDisable(true);
        deleteBtn.setDisable(true);
        HBox actions = new HBox(10, contributeBtn, deleteBtn);
        actions.setPadding(new Insets(10));

        savingsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            boolean has = sel != null;
            contributeBtn.setDisable(!has);
            deleteBtn.setDisable(!has);
        });

        VBox content = new VBox(6, form, savingsTable, actions);
        content.setPadding(new Insets(10));
        VBox.setVgrow(savingsTable, Priority.ALWAYS);

        // Handlers
        createBtn.setOnAction(e -> {
            String name = nameField.getText() == null ? "" : nameField.getText().trim();
            String tgt = targetField.getText() == null ? "" : targetField.getText().trim();
            if (name.isEmpty() || tgt.isEmpty() || targetDate.getValue() == null) {
                showError(new IllegalArgumentException("Completează nume, țintă și termen."));
                return;
            }
            runAsync("Creare plan...", () -> {
                SavingsPlanRequest req = new SavingsPlanRequest();
                req.setName(name);
                try { req.setTargetAmount(new java.math.BigDecimal(tgt)); } catch (Exception ex) {
                    throw new IllegalArgumentException("Ținta trebuie să fie numerică");
                }
                req.setTargetDate(targetDate.getValue());
                req.setFocusCategory(focusBox.getValue());
                return savingsPlanService.createPlan(req);
            }, created -> {
                nameField.clear();
                targetField.clear();
                targetDate.setValue(null);
                focusBox.setValue(null);
                reloadSavings();
                setStatus("Plan creat");
            });
        });

        contributeBtn.setOnAction(e -> {
            SavingsPlanResponse sel = savingsTable.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            TextInputDialog dlg = new TextInputDialog();
            dlg.setHeaderText("Introdu suma contribuției (RON)");
            dlg.setContentText("Suma:");
            dlg.showAndWait().ifPresent(val -> {
                try {
                    java.math.BigDecimal amount = new java.math.BigDecimal(val);
                    if (amount.signum() <= 0) throw new IllegalArgumentException();
                    runAsync("Contribuire...", () -> savingsPlanService.contribute(sel.getId(), amount), ok -> {
                        reloadSavings();
                        setStatus("Contribuție adăugată");
                    });
                } catch (Exception ex) {
                    showError(new IllegalArgumentException("Introdu o sumă validă > 0"));
                }
            });
        });

        deleteBtn.setOnAction(e -> {
            SavingsPlanResponse sel = savingsTable.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Ștergi planul?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    runAsync("Ștergere...", () -> {
                        savingsPlanService.delete(sel.getId());
                        return null;
                    }, ok -> {
                        reloadSavings();
                        setStatus("Plan șters");
                    });
                }
            });
        });

        tab.setContent(content);
        // Initial load of savings
        reloadSavings();
        return tab;
    }

    private void reloadTransactions(Long accountId, TransactionCategory category,
                                    java.time.LocalDate startDate, java.time.LocalDate endDate) {
        runAsync("Căutare tranzacții...",
                () -> transactionService.searchTransactions(accountId, category, startDate, endDate),
                list -> {
                    ObservableList<TransactionResponse> data = FXCollections.observableArrayList(list);
                    transactionsTable.setItems(data);
                    setStatus("Tranzacții încărcate");
                });
    }

    private void reloadSavings() {
        runAsync("Încărcare planuri...", savingsPlanService::findAll, list -> {
            savingsTable.setItems(FXCollections.observableArrayList(list));
            setStatus("Planuri încărcate");
        });
    }

    private void synchronizeBanks() {
        runAsync("Sincronizare bănci...", () -> {
            bankSynchronizationService.synchronizeAll();
            return null;
        }, ok -> {
            setStatus("Sincronizare finalizată");
            reloadAccounts();
            reloadAnalytics();
        });
    }

    private void reloadAccounts() {
        runAsync("Încărcare conturi...", bankAccountService::getAllAccounts, list -> {
            ObservableList<BankAccountResponse> data = FXCollections.observableArrayList(list);
            accountsTable.setItems(data);
            setStatus("Conturi încărcate");
        });
    }

    private void reloadAnalytics() {
        // Overload to refresh internal charts within the Analytics tab after first render
        // This will search for charts in the current scene and update them if present
        Scene scene = statusBar.getScene();
        if (scene == null) return;
        LineChart<?, ?> monthly = (LineChart<?, ?>) scene.lookup(".chart");
        PieChart pie = (PieChart) scene.lookup(".chart-pie");
        // Safer: directly traverse content is omitted for brevity; use explicit method when button is pressed
    }

    private void reloadAnalytics(LineChart<String, Number> monthlyChart, PieChart categoryChart) {
        runAsync("Calcul analize...", () -> new Object[]{
                analyticsService.getMonthlySpending(null, null, null, null),
                analyticsService.getCategoryTotals(null, null, null, null)
        }, result -> {
            @SuppressWarnings("unchecked")
            List<SpendingChartPoint> monthly = (List<SpendingChartPoint>) result[0];
            @SuppressWarnings("unchecked")
            Map<TransactionCategory, BigDecimal> totals = (Map<TransactionCategory, BigDecimal>) result[1];

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Cheltuieli totale");
            monthlyChart.getData().clear();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yyyy");
            for (SpendingChartPoint p : monthly) {
                series.getData().add(new XYChart.Data<>(p.getPeriod().format(fmt), p.getTotalAmount()));
            }
            monthlyChart.getData().add(series);

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            totals.forEach((cat, sum) -> pieData.add(new PieChart.Data(cat == null ? "Fără" : cat.name(), sum.doubleValue())));
            categoryChart.setData(pieData);
            setStatus("Analize actualizate");
        });
    }

    private <T> void runAsync(String workingLabel, TaskSupplier<T> supplier, UiConsumer<T> onSuccess) {
        setStatus(workingLabel);
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return supplier.get();
            }
        };
        task.setOnSucceeded(evt -> {
            try {
                onSuccess.accept(task.getValue());
            } catch (Exception e) {
                showError(e);
            }
        });
        task.setOnFailed(evt -> showError(task.getException()));
        Thread t = new Thread(task, "mb-worker");
        t.setDaemon(true);
        t.start();
    }

    private void setStatus(String text) {
        Platform.runLater(() -> statusBar.setText(text));
    }

    private void showError(Throwable t) {
        final Throwable error = t == null ? new RuntimeException("Eroare necunoscută") : t;
        setStatus("Eroare: " + error.getMessage());
        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, Objects.toString(error.getMessage(), String.valueOf(error))).showAndWait());
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
    }

    @FunctionalInterface
    private interface TaskSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    private interface UiConsumer<T> {
        void accept(T data) throws Exception;
    }

    public static void main(String[] args) {
        Application.launch(DesktopApp.class, args);
    }
}
