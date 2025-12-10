package com.mertblk.eegui.viewmodel;

import com.fazecast.jSerialComm.SerialPort;
import com.mertblk.eegui.db.DatabaseManager;
import com.mertblk.eegui.model.SensorData;
import com.mertblk.eegui.model.SensorDataModel;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SensorViewModel {

    private final SensorDataModel model = new SensorDataModel();
    private final StringProperty statusInfo = new SimpleStringProperty("Not Connected");
    private final ObservableList<String> portNames = FXCollections.observableArrayList();
    private final BooleanProperty connected = new SimpleBooleanProperty(false);
    private SerialPort activePort;

    private boolean isRecording = false;
    private String sessionId;
    private long startTime;

    private final List<Runnable> newDataAddedListeners = new ArrayList<>();

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
            stopRecording();
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
            connected.set(true);
            Thread dataReaderThread = new Thread(this::readData);
            dataReaderThread.setDaemon(true);
            dataReaderThread.start();
        } else {
            statusInfo.set("Failed to connect to " + portName);
            stopRecording();
        }
    }

    public void disconnect() {
        stopRecording();
        if (activePort != null && activePort.isOpen()) {
            activePort.closePort();
        }
        statusInfo.set("Disconnected");
        connected.set(false);
    }

    public boolean startRecording(String customSessionName) {
        String potentialSessionId;
        if (customSessionName == null || customSessionName.trim().isEmpty()) {
            potentialSessionId = "Session-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        } else {
            potentialSessionId = customSessionName.trim();
        }

        if (DatabaseManager.doesSessionIdExist(potentialSessionId)) {
            statusInfo.set("Error: Session name '" + potentialSessionId + "' already exists.");
            return false;
        }

        this.sessionId = potentialSessionId;
        this.startTime = System.nanoTime();
        this.isRecording = true;
        System.out.println("Started recording session: " + sessionId);
        return true;
    }

    public void stopRecording() {
        if (isRecording) {
            this.isRecording = false;
            System.out.println("Stopped recording session: " + sessionId);
        }
    }

    private void readData() {
        try (Scanner scanner = new Scanner(activePort.getInputStream())) {
            while (scanner.hasNextLine() && activePort.isOpen()) {
                String line = scanner.nextLine();
                Platform.runLater(() -> parseAndProcessData(line));
            }
        } catch (Exception e) {
            Platform.runLater(this::disconnect);
        }
    }

    private void parseAndProcessData(String dataLine) {
        SensorData currentData = new SensorData();
        String[] parts = dataLine.split(",");
        for (String part : parts) {
            String[] keyValue = part.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                try {
                    switch (key) {
                        case "TEMP":
                            model.setTemperature(value);
                            currentData.setTemperature(Float.parseFloat(value));
                            break;
                        case "HUM":
                            model.setHumidity(value);
                            currentData.setHumidity(Integer.parseInt(value));
                            break;
                        case "LIGHT":
                            model.setLight(value);
                            currentData.setLight(Integer.parseInt(value));
                            break;
                        case "FIRE":
                            model.setFireAlarm(value.equals("1") ? "ON" : "OFF");
                            currentData.setFire(Integer.parseInt(value));
                            break;
                        case "S1":
                            model.setSound1(value);
                            currentData.setS1(Integer.parseInt(value));
                            break;
                        case "S2":
                            model.setSound2(value);
                            currentData.setS2(Integer.parseInt(value));
                            break;
                        case "SA":
                            model.setSoundAlarm(value.equals("1") ? "ON" : "OFF");
                            currentData.setSa(Integer.parseInt(value));
                            break;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Could not parse value for " + key + ": " + value);
                }
            }
        }

        if (isRecording) {
            double elapsedSeconds = (System.nanoTime() - startTime) / 1_000_000_000.0;
            DatabaseManager.saveSensorData(sessionId, elapsedSeconds, currentData);
            notifyNewDataListeners();
        }
    }

    public void addOnNewDataListener(Runnable listener) {
        newDataAddedListeners.add(listener);
    }

    public void removeOnNewDataListener(Runnable listener) {
        newDataAddedListeners.remove(listener);
    }

    private void notifyNewDataListeners() {
        for (Runnable listener : newDataAddedListeners) {
            listener.run();
        }
    }

    public String getCurrentSessionId() {
        return isRecording ? sessionId : null;
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

    public BooleanProperty connectedProperty() {
        return connected;
    }
}
