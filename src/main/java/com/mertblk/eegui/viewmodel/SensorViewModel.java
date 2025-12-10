package com.mertblk.eegui.viewmodel;

import com.fazecast.jSerialComm.SerialPort;
import com.mertblk.eegui.model.SensorDataModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Arrays;
import java.util.Scanner;

public class SensorViewModel {

    private final SensorDataModel model = new SensorDataModel();
    private final StringProperty statusInfo = new SimpleStringProperty("Not Connected");
    private final ObservableList<String> portNames = FXCollections.observableArrayList();
    private SerialPort activePort;

    public SensorViewModel() {
        scanPorts();
    }

    public void scanPorts() {
        portNames.clear();
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            portNames.add(port.getSystemPortName());
        }
        if (portNames.isEmpty()) {
            statusInfo.set("No serial ports found.");
        }
    }

    public void connectToPort(String portName) {
        if (portName == null || portName.isEmpty()) {
            statusInfo.set("No port selected.");
            return;
        }

        if (activePort != null && activePort.isOpen()) {
            disconnect();
        }

        activePort = SerialPort.getCommPort(portName);
        activePort.setBaudRate(9600);
        activePort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        if (activePort.openPort()) {
            statusInfo.set("Connected to " + portName);
            Thread dataReaderThread = new Thread(this::readData);
            dataReaderThread.setDaemon(true);
            dataReaderThread.start();
        } else {
            statusInfo.set("Failed to connect to " + portName);
        }
    }

    public void disconnect() {
        if (activePort != null && activePort.isOpen()) {
            activePort.closePort();
            statusInfo.set("Disconnected");
        }
    }

    private void readData() {
        try (Scanner scanner = new Scanner(activePort.getInputStream())) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Platform.runLater(() -> parseAndUpdatemodel(line));
            }
        } catch (Exception e) {
            Platform.runLater(() -> statusInfo.set("Connection lost."));
        }
    }

    private void parseAndUpdatemodel(String data) {
        // "TEMP:25.1,HUM:55,LIGHT:300,FIRE:0,S1:10,S2:20,SA:1"
        String[] parts = data.split(",");
        for (String part : parts) {
            String[] keyValue = part.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                switch (key) {
                    case "TEMP":
                        model.setTemperature(value);
                        break;
                    case "HUM":
                        model.setHumidity(value);
                        break;
                    case "LIGHT":
                        model.setLight(value);
                        break;
                    case "FIRE":
                        model.setFireAlarm(value.equals("1") ? "ON" : "OFF");
                        break;
                    case "S1":
                        model.setSound1(value);
                        break;
                    case "S2":
                        model.setSound2(value);
                        break;
                    case "SA":
                        model.setSoundAlarm(value.equals("1") ? "ON" : "OFF");
                        break;
                }
            }
        }
    }

    public SensorDataModel getModel() {
        return model;
    }

    public StringProperty statusInfoProperty() {
        return statusInfo;
    }

    public ObservableList<String> getPortNames() {
        return portNames;
    }
}
