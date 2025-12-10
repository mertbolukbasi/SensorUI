package com.mertblk.eegui;

import com.fazecast.jSerialComm.SerialPort;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.mfxcore.controls.Label;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Scanner;

public class HelloApplication extends Application {

    private SerialPort activePort;
    private MFXComboBox<String> comboPorts;
    private Label statusInfo = new Label("");

    @Override
    public void start(Stage stage) throws IOException {
        VBox root = new VBox();
        HBox hBox = new HBox();
        MFXButton connectButton = new MFXButton("Connect");

        connectButton.setOnAction(event -> initilaze());


        Label temp = new Label("Temp");
        Label status = new Label("Status");
        hBox.getChildren().addAll(temp, status);
        hBox.setPadding(new Insets(10, 10, 10, 10));
        Label fireAlarm = new Label("Fire Alarm");
        Label humidity = new Label("Humidity");
        Label light = new Label("Light");
        Label sound1 = new Label("Sound 1");
        Label sound2 = new Label("Sound 2");
        Label soundAlarm = new Label("Sound Alarm");
        root.getChildren().addAll(
                connectButton, status, temp, fireAlarm, humidity, light, sound1, sound2, soundAlarm
        );
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    private void initilaze() {

        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            comboPorts.getItems().add(port.getSystemPortName());
        }
        if (comboPorts.getItems().isEmpty()) {
            comboPorts.setText("Port Bulunamadı");
        } else {
            comboPorts.getSelectionModel().selectFirst();
        }

        connectToSelectedPort();
    }

    private void connectToSelectedPort() {
        String selectedPortName = comboPorts.getValue();
        if (selectedPortName == null) return;

        activePort = SerialPort.getCommPort(selectedPortName);
        activePort.setBaudRate(9600); // Arduino/HC-05 hızıyla aynı olmalı
        // Bluetooth bağlantısı bazen zaman alır, timeout ayarı iyi olur
        activePort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        if (activePort.openPort()) {
            statusInfo.setText(selectedPortName + " portuna bağlanıldı!\n");
            //btnConnect.setDisable(true);

            // Veri okumayı başlat
            Thread thread = new Thread(this::readData);
            thread.setDaemon(true);
            thread.start();
        } else {
            statusInfo.setText("HATA: Bağlantı kurulamadı. Doğru portu seçtiğinden emin ol.\n");
        }
    }

    private void readData() {
        try (Scanner scanner = new Scanner(activePort.getInputStream())) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Platform.runLater(() -> {
                    //txtOutput.appendText(line + "\n");
                    // Burada gelen veriye göre MaterialFX bileşenlerini güncelleyebilirsin
                });
            }
        } catch (Exception e) {
            Platform.runLater(() -> statusInfo.setText("Bağlantı koptu!\n"));
        }
    }
}
