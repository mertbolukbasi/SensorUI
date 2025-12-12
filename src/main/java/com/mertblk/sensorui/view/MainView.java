package com.mertblk.sensorui.view;

import com.mertblk.sensorui.db.DatabaseManager;
import com.mertblk.sensorui.model.SensorReading;
import com.mertblk.sensorui.viewmodel.SensorViewModel;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class MainView extends Application {

    private final SensorViewModel viewModel = new SensorViewModel();
    private File selectedDirectory;

    @Override
    public void start(Stage stage) {
        DatabaseManager.initializeDatabase();

        UserAgentBuilder.builder()
                .themes(JavaFXThemes.MODENA)
                .themes(MaterialFXStylesheets.forAssemble(true))
                .setDeploy(true)
                .setResolveAssets(true)
                .build()
                .setGlobal();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1e1e1e;");

        VBox topContainer = new VBox();
        MenuBar menuBar = createMenuBar(stage);
        HBox connectionBar = createConnectionBar();
        topContainer.getChildren().addAll(menuBar, connectionBar);
        root.setTop(topContainer);

        GridPane sensorGrid = createSensorGrid();
        root.setCenter(sensorGrid);
        BorderPane.setMargin(sensorGrid, new Insets(20));

        HBox statusBar = createStatusBar();
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 1000, 800);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/main-view.css")).toExternalForm());
        stage.setTitle("Sensor Dashboard");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(500);
        stage.show();

        stage.setOnCloseRequest(event -> viewModel.disconnect());
    }

    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();
        Menu dbMenu = new Menu("Database");
        MenuItem showDataMenuItem = new MenuItem("Show Records");
        showDataMenuItem.setOnAction(event -> {
            new DatabaseView(viewModel, viewModel.getCurrentSessionId()).show();
        });

        MenuItem exportExcelMenuItem = new MenuItem("Export to Excel");
        exportExcelMenuItem.setOnAction(event -> showExportDialog(stage));

        dbMenu.getItems().addAll(showDataMenuItem, exportExcelMenuItem);
        menuBar.getMenus().add(dbMenu);
        return menuBar;
    }

    private void showExportDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Export Options");
        dialog.setResizable(false);

        VBox dialogLayout = new VBox(15);
        dialogLayout.setPadding(new Insets(20));
        dialogLayout.setStyle("-fx-background-color: #2a2a2a;");
        dialogLayout.setAlignment(Pos.CENTER_LEFT);

        // Directory Chooser
        Label dirLabel = new Label("Save Location:");
        dirLabel.setTextFill(Color.WHITE);
        MFXTextField dirField = new MFXTextField();
        dirField.setFloatingText("Select a directory");
        dirField.setEditable(false);
        dirField.setPrefWidth(230);

        MFXButton browseButton = new MFXButton("Browse...");
        browseButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Directory");
            selectedDirectory = directoryChooser.showDialog(dialog);
            if (selectedDirectory != null) {
                dirField.setText(selectedDirectory.getAbsolutePath());
            }
        });
        HBox dirBox = new HBox(10, dirField, browseButton);
        dirBox.setAlignment(Pos.CENTER_LEFT);

        // File Name Input
        Label nameLabel = new Label("File Name (Optional):");
        nameLabel.setTextFill(Color.WHITE);
        MFXTextField nameField = new MFXTextField();
        nameField.setFloatingText("Enter file name");
        nameField.setPrefWidth(300);

        // Session Selection
        Label sessionLabel = new Label("Select Session:");
        sessionLabel.setTextFill(Color.WHITE);

        List<String> sessions = DatabaseManager.getDistinctSessionIds();
        MFXComboBox<String> sessionComboBox = new MFXComboBox<>(FXCollections.observableArrayList(sessions));
        sessionComboBox.setFloatingText("Choose a session");
        sessionComboBox.setPrefWidth(300);

        // Select All Checkbox
        CheckBox selectAllCheckBox = new CheckBox("Export All Data (All Sessions)");
        selectAllCheckBox.setTextFill(Color.WHITE);
        selectAllCheckBox.setSelected(false);

        // Logic: Disable combo box if "Select All" is checked
        sessionComboBox.disableProperty().bind(selectAllCheckBox.selectedProperty());

        // Button container
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        // Export Button
        MFXButton exportButton = new MFXButton("Export");
        exportButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        exportButton.setPrefWidth(100);

        exportButton.setOnAction(event -> {
            // Validation
            if (selectedDirectory == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText(null);
                alert.setContentText("Please select a save location.");
                alert.showAndWait();
                return;
            }
            if (!selectAllCheckBox.isSelected() && sessionComboBox.getValue() == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText(null);
                alert.setContentText("Please select a session or check 'Export All Data'.");
                alert.showAndWait();
                return;
            }

            // Determine Data to Export
            List<SensorReading> dataToExport;
            if (selectAllCheckBox.isSelected()) {
                dataToExport = DatabaseManager.getSensorReadings("ORDER BY timestamp DESC", List.of());
            } else {
                String selectedSession = sessionComboBox.getValue();
                dataToExport = DatabaseManager.getSensorReadings("WHERE session_id = ? ORDER BY timestamp DESC", List.of(selectedSession));
            }

            if (dataToExport.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Info");
                alert.setHeaderText(null);
                alert.setContentText("No data found to export.");
                alert.showAndWait();
                return;
            }

            // Determine File Name
            String inputName = nameField.getText().trim();
            String finalFileName = inputName.isEmpty() ? "SensorData_" + System.currentTimeMillis() : inputName;
            if (!finalFileName.endsWith(".xlsx")) {
                finalFileName += ".xlsx";
            }

            File file = new File(selectedDirectory, finalFileName);
            performExcelExport(file, dataToExport);
            dialog.close();
        });

        // Cancel Button
        MFXButton cancelButton = new MFXButton("Cancel");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        cancelButton.setPrefWidth(100);
        cancelButton.setOnAction(event -> dialog.close());

        buttonBox.getChildren().addAll(exportButton, cancelButton);

        dialogLayout.getChildren().addAll(dirLabel, dirBox, nameLabel, nameField, selectAllCheckBox, sessionLabel, sessionComboBox, buttonBox);

        Scene dialogScene = new Scene(dialogLayout, 400, 450);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void performExcelExport(File file, List<SensorReading> readings) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sensor Data");

            // Create header row
            String[] headers = {"ID", "Session ID", "Timestamp", "Elapsed (s)", "Temp (°C)", "Humidity (%)", "Light", "Fire", "S1", "S2", "SA"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Populate rows
            int rowNum = 1;
            for (SensorReading reading : readings) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(reading.getId());
                row.createCell(1).setCellValue(reading.getSessionId());
                row.createCell(2).setCellValue(reading.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                row.createCell(3).setCellValue(reading.getElapsedSeconds());
                row.createCell(4).setCellValue(reading.getTemperature());
                row.createCell(5).setCellValue(reading.getHumidity());
                row.createCell(6).setCellValue(reading.getLight());
                row.createCell(7).setCellValue(reading.getFire());
                row.createCell(8).setCellValue(reading.getS1());
                row.createCell(9).setCellValue(reading.getS2());
                row.createCell(10).setCellValue(reading.getSa());
            }

            // Write the workbook to the file
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }

            System.out.println("Excel file was created successfully: " + file.getAbsolutePath());

            // Optional: Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Export successful!\nSaved to: " + file.getAbsolutePath());
            alert.showAndWait();

        } catch (IOException e) {
            System.err.println("Error while exporting to Excel: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Export Failed");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private HBox createConnectionBar() {
        HBox connectionBar = new HBox(15);
        connectionBar.setPadding(new Insets(15));
        connectionBar.setAlignment(Pos.CENTER_LEFT);
        connectionBar.setStyle("-fx-background-color: #2a2a2a;");

        Label portLabel = new Label("Serial Port:");
        portLabel.setTextFill(Color.WHITE);

        MFXComboBox<String> comboPorts = new MFXComboBox<>(viewModel.getPortNames());
        comboPorts.setFloatingText("Select Port");
        comboPorts.setPrefWidth(150);

        MFXTextField sessionNameField = new MFXTextField();
        sessionNameField.setFloatingText("Optional: Session Name");
        sessionNameField.setFloatMode(FloatMode.INLINE);
        sessionNameField.setPrefWidth(250);

        MFXButton connectButton = new MFXButton("Connect");
        MFXButton disconnectButton = new MFXButton("Disconnect");
        MFXButton refreshButton = new MFXButton("Refresh Ports");

        refreshButton.setOnAction(event -> viewModel.scanPorts());

        connectButton.setOnAction(event -> {
            boolean success = viewModel.startRecording(sessionNameField.getText());
            if (success) {
                viewModel.connectToPort(comboPorts.getValue());
            }
        });
        disconnectButton.setOnAction(event -> viewModel.disconnect());

        comboPorts.disableProperty().bind(viewModel.connectedProperty());
        sessionNameField.disableProperty().bind(viewModel.connectedProperty());
        refreshButton.disableProperty().bind(viewModel.connectedProperty());
        connectButton.disableProperty().bind(viewModel.connectedProperty());
        disconnectButton.disableProperty().bind(viewModel.connectedProperty().not());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        connectionBar.getChildren().addAll(portLabel, comboPorts, sessionNameField, connectButton, disconnectButton, refreshButton, spacer);
        return connectionBar;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(10, 15, 10, 15));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setStyle("-fx-background-color: #2a2a2a;");

        Circle statusIndicator = new Circle(6, Color.GRAY);
        Label statusLabel = new Label();
        statusLabel.textProperty().bind(viewModel.statusInfoProperty());
        statusLabel.setTextFill(Color.WHITE);

        viewModel.connectedProperty().addListener((obs, wasConnected, isConnected) -> {
            if (isConnected) {
                statusIndicator.setFill(Color.LIMEGREEN);
            } else {
                statusIndicator.setFill(Color.GRAY);
            }
        });

        viewModel.statusInfoProperty().addListener((obs, oldStatus, newStatus) -> {
            if (newStatus != null && (newStatus.startsWith("Failed") || newStatus.startsWith("Connection lost") || newStatus.startsWith("Error"))) {
                statusIndicator.setFill(Color.ORANGERED);
            }
        });

        return statusBar;
    }

    private GridPane createSensorGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        grid.add(createSensorDisplay("Temperature", viewModel.getModel().temperatureProperty()), 0, 0);
        grid.add(createSensorDisplay("Humidity", viewModel.getModel().humidityProperty()), 1, 0);
        grid.add(createSensorDisplay("Light Level", viewModel.getModel().lightProperty()), 0, 1);
        grid.add(createSensorDisplay("Fire Alarm", viewModel.getModel().fireAlarmProperty()), 1, 1);
        grid.add(createSensorDisplay("Sound 1", viewModel.getModel().sound1Property()), 0, 2);
        grid.add(createSensorDisplay("Sound 2", viewModel.getModel().sound2Property()), 1, 2);

        Node soundAlarmDisplay = createSensorDisplay("Sound Alarm", viewModel.getModel().soundAlarmProperty());
        GridPane.setColumnSpan(soundAlarmDisplay, 2);
        grid.add(soundAlarmDisplay, 0, 3);

        return grid;
    }

    private Node createSensorDisplay(String title, StringProperty dataProperty) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #2a2a2a; -fx-padding: 20; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(new Font("System Bold", 18));
        titleLabel.setTextFill(Color.LIGHTGRAY);

        Label dataLabel = new Label();
        dataLabel.setFont(new Font("System Regular", 24));
        dataLabel.setTextFill(Color.WHITE);

        dataLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            String value = dataProperty.get();
            if (value != null && value.contains(":")) {
                return value.substring(value.indexOf(":") + 1).trim();
            }
            return "N/A";
        }, dataProperty));

        if (title.toLowerCase().contains("alarm")) {
            dataProperty.addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.contains("ON")) {
                    box.setStyle("-fx-background-color: #8B0000; -fx-padding: 20; -fx-border-radius: 8; -fx-background-radius: 8;");
                } else {
                    box.setStyle("-fx-background-color: #2a2a2a; -fx-padding: 20; -fx-border-radius: 8; -fx-background-radius: 8;");
                }
            });
        }

        box.getChildren().addAll(titleLabel, dataLabel);
        return box;
    }
}