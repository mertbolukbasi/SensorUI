package com.mertblk.sensorui.view;

import com.mertblk.sensorui.db.DatabaseManager;
import com.mertblk.sensorui.model.SensorReading;
import com.mertblk.sensorui.viewmodel.SensorViewModel;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DatabaseView {

    private final TableView<SensorReading> tableView = new TableView<>();
    private final ObservableList<SensorReading> tableData = FXCollections.observableArrayList();
    private final MFXDatePicker datePicker = new MFXDatePicker();
    private final MFXComboBox<String> sessionComboBox = new MFXComboBox<>();

    private final SensorViewModel viewModel;
    private final String initialSessionId;
    private final Runnable onNewDataListener;

    public DatabaseView(SensorViewModel viewModel, String initialSessionId) {
        this.viewModel = viewModel;
        this.initialSessionId = initialSessionId;
        this.onNewDataListener = () -> Platform.runLater(this::refreshData);
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Sensor Data Records");

        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #1e1e1e;");

        HBox filterBar = createFilterBar();
        setupTableView();
        root.getChildren().addAll(filterBar, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/database-view.css")).toExternalForm());
        stage.setScene(scene);
        stage.show();

        viewModel.addOnNewDataListener(onNewDataListener);

        stage.setOnCloseRequest(event -> viewModel.removeOnNewDataListener(onNewDataListener));

        loadInitialDataAndApplyFilter();
    }

    private HBox createFilterBar() {
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label("Filter by Date (from):");
        dateLabel.setStyle("-fx-text-fill: white;");

        Label sessionLabel = new Label("Filter by Session:");
        sessionLabel.setStyle("-fx-text-fill: white;");

        MFXButton filterButton = new MFXButton("Apply Filter");
        filterButton.setOnAction(event -> applyFilters());

        MFXButton resetButton = new MFXButton("Reset");
        resetButton.setOnAction(event -> {
            datePicker.setValue(null);
            datePicker.clear();
            sessionComboBox.setValue(null);
            sessionComboBox.clearSelection();
            applyFilters();
        });

        MFXButton currentSessionButton = new MFXButton("Show Current Session");
        currentSessionButton.setOnAction(event -> {
            datePicker.setValue(null);
            datePicker.clear();
            String currentSessionId = viewModel.getCurrentSessionId();
            if (currentSessionId != null) {
                // Ensure the current session ID is in the combo box list
                if (!sessionComboBox.getItems().contains(currentSessionId)) {
                    sessionComboBox.getItems().add(currentSessionId);
                }
                sessionComboBox.setValue(currentSessionId);
                applyFilters();
            }
        });

        currentSessionButton.disableProperty().bind(viewModel.connectedProperty().not());

        sessionComboBox.setPrefWidth(250);
        sessionComboBox.setFloatingText("Session");
        filterBar.getChildren().addAll(dateLabel, datePicker, sessionLabel, sessionComboBox, filterButton, resetButton, currentSessionButton);
        return filterBar;
    }

    private void setupTableView() {
        tableView.setItems(tableData);
        createColumn("ID", "id", 50);
        createColumn("Session ID", "sessionId", 250);
        createColumn("Timestamp", "timestamp", 150);
        createColumn("Elapsed (s)", "elapsedSeconds", 100);
        createColumn("Temp (°C)", "temperature", 80);
        createColumn("Humidity (%)", "humidity", 80);
        createColumn("Light", "light", 80);
        createColumn("Fire", "fire", 50);
        createColumn("S1", "s1", 50);
        createColumn("S2", "s2", 50);
        createColumn("SA", "sa", 50);
    }

    private <T> void createColumn(String title, String propertyName, double width) {
        TableColumn<SensorReading, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setPrefWidth(width);
        tableView.getColumns().add(column);
    }

    private void loadInitialDataAndApplyFilter() {
        List<String> sessionIds = DatabaseManager.getDistinctSessionIds();
        sessionComboBox.setItems(FXCollections.observableArrayList(sessionIds));

        if (initialSessionId != null && sessionIds.contains(initialSessionId)) {
            sessionComboBox.setValue(initialSessionId);
        }

        applyFilters();
    }

    private void refreshData() {
        List<String> sessionIds = DatabaseManager.getDistinctSessionIds();
        String selectedSession = sessionComboBox.getValue();
        sessionComboBox.setItems(FXCollections.observableArrayList(sessionIds));
        if (selectedSession != null && sessionIds.contains(selectedSession)) {
            sessionComboBox.setValue(selectedSession);
        }

        applyFilters();
    }

    private void applyFilters() {
        StringBuilder filterSql = new StringBuilder("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (datePicker.getValue() != null) {
            filterSql.append("AND timestamp >= ? ");
            params.add(datePicker.getValue().atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        if (sessionComboBox.getValue() != null && !sessionComboBox.getValue().isEmpty()) {
            filterSql.append("AND session_id = ? ");
            params.add(sessionComboBox.getValue());
        }

        filterSql.append("ORDER BY timestamp DESC");

        int selectedIndex = tableView.getSelectionModel().getSelectedIndex();

        tableData.setAll(DatabaseManager.getSensorReadings(filterSql.toString(), params));

        if (selectedIndex != -1 && selectedIndex < tableData.size()) {
            tableView.getSelectionModel().select(selectedIndex);
        }
    }
}