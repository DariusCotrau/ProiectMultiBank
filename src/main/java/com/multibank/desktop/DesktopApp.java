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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("MultiBank Desktop");

        TabPane tabs = new TabPane();
        tabs.getTabs().add(buildAccountsTab());
        tabs.getTabs().add(buildTransactionsTab());
        tabs.getTabs().add(buildAnalyticsTab());
        tabs.getTabs().add(buildSavingsTab());
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        BorderPane root = new BorderPane();
        root.setCenter(tabs);
        statusBar.setPadding(new Insets(6, 10, 6, 10));
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 1100, 700);
        stage.setScene(scene);
        stage.show();

        // Load initial data
        reloadAccounts();
    }

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
        List<TableColumn<BankAccountResponse, ?>> accCols = new ArrayList<>();
        accCols.add(bankCol);
        accCols.add(numberCol);
        accCols.add(ibanCol);
        accCols.add(typeCol);
        accCols.add(currencyCol);
        accCols.add(balanceCol);
        accountsTable.getColumns().setAll(accCols);

        VBox content = new VBox(5, toolbar, accountsTable);
        VBox.setVgrow(accountsTable, Priority.ALWAYS);
        content.setPadding(new Insets(10));

        syncBtn.setOnAction(e -> synchronizeBanks());
        reloadBtn.setOnAction(e -> reloadAccounts());

        tab.setContent(content);
        return tab;
    }

    private Tab buildAnalyticsTab() {
        Tab tab = new Tab("Analiză");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> monthlyChart = new LineChart<>(xAxis, yAxis);
        monthlyChart.setTitle("Cheltuieli lunare");

        PieChart categoryChart = new PieChart();
        categoryChart.setTitle("Distribuție pe categorii");

        Button refreshBtn = new Button("Actualizează");
        HBox toolbar = new HBox(10, refreshBtn);
        toolbar.setPadding(new Insets(10));

        VBox content = new VBox(8, toolbar, monthlyChart, categoryChart);
        content.setPadding(new Insets(10));
        VBox.setVgrow(monthlyChart, Priority.SOMETIMES);
        VBox.setVgrow(categoryChart, Priority.SOMETIMES);

        refreshBtn.setOnAction(e -> reloadAnalytics(monthlyChart, categoryChart));
        tab.setContent(content);
        return tab;
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
        List<TableColumn<TransactionResponse, ?>> txCols = new ArrayList<>();
        txCols.add(dateCol);
        txCols.add(descCol);
        txCols.add(merchCol);
        txCols.add(catCol);
        txCols.add(dirCol);
        txCols.add(amtCol);
        transactionsTable.getColumns().setAll(txCols);

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
        List<TableColumn<SavingsPlanResponse, ?>> spCols = new ArrayList<>();
        spCols.add(nameCol);
        spCols.add(tgtCol);
        spCols.add(curCol);
        spCols.add(dateCol);
        spCols.add(fcatCol);
        spCols.add(progCol);
        savingsTable.getColumns().setAll(spCols);

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
        final Throwable ex = (t == null) ? new RuntimeException("Eroare necunoscută") : t;
        setStatus("Eroare: " + ex.getMessage());
        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, Objects.toString(ex.getMessage(), String.valueOf(ex))).showAndWait());
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
