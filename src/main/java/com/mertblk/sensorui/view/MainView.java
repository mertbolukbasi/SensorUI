package com.mertblk.sensorui.view;

import com.mertblk.sensorui.db.DatabaseManager;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Objects;

public class MainView extends Application {

    private final SensorViewModel viewModel = new SensorViewModel();

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
        MenuBar menuBar = createMenuBar();
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

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu dbMenu = new Menu("Database");
        MenuItem showDataMenuItem = new MenuItem("Show Records");
        showDataMenuItem.setOnAction(event -> {
            new DatabaseView(viewModel, viewModel.getCurrentSessionId()).show();
        });

        dbMenu.getItems().add(showDataMenuItem);
        menuBar.getMenus().add(dbMenu);
        return menuBar;
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
