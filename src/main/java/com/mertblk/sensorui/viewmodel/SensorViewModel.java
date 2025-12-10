package com.mertblk.sensorui.viewmodel;

import com.fazecast.jSerialComm.SerialPort;
import com.mertblk.sensorui.db.DatabaseManager;
import com.mertblk.sensorui.model.SensorData;
import com.mertblk.sensorui.model.SensorDataModel;
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

        // Temperature
        model.setTemperature(parts[1]);
        currentData.setTemperature(Float.parseFloat(parts[1]));

        //Fire Alarm
        int fireAlarmData = Integer.parseInt(parts[2]);
        String fireAlarm;
        if(fireAlarmData == 0) fireAlarm = "Convenient";
        else fireAlarm = "Hot";
        model.setFireAlarm(fireAlarm);
        currentData.setFire(fireAlarmData);

        //Humidity
        float humidityData = Float.parseFloat(parts[3]);
        String humidity = "% " + humidityData;
        model.setHumidity(humidity);
        currentData.setHumidity((int) humidityData);

        //Light
        int lightData = Integer.parseInt(parts[4]);
        String light;
        if(lightData == 0) light = "Off";
        else light = "On";
        model.setLight(light);
        currentData.setLight(lightData);

        //Sound 1
        int sound1Data = Integer.parseInt(parts[5]);
        String sound1;
        if(sound1Data == 0) sound1 = "Quite";
        else sound1 = "Noisy";
        model.setSound1(sound1);
        currentData.setS1(sound1Data);

        //Sound 2
        int sound2Data = Integer.parseInt(parts[6]);
        String sound2;
        if(sound2Data == 0) sound2 = "Quite";
        else sound2 = "Noisy";
        model.setSound2(sound2);
        currentData.setS2(sound2Data);

        //Sound Alarm
        int soundAlarmData = Integer.parseInt(parts[7]);
        String soundAlarm;
        if(soundAlarmData == 0) soundAlarm = "Chill";
        else soundAlarm = "Loud";
        model.setSoundAlarm(soundAlarm);
        currentData.setSa(soundAlarmData);


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
