package com.mertblk.sensorui.model;

import javafx.beans.property.*;

public class SensorReading {
    private final IntegerProperty id;
    private final StringProperty sessionId;
    private final StringProperty timestamp;
    private final DoubleProperty elapsedSeconds;
    private final FloatProperty temperature;
    private final IntegerProperty humidity;
    private final IntegerProperty light;
    private final IntegerProperty fire;
    private final IntegerProperty s1;
    private final IntegerProperty s2;
    private final IntegerProperty sa;

    public SensorReading(int id, String sessionId, String timestamp, double elapsedSeconds, float temperature, int humidity, int light, int fire, int s1, int s2, int sa) {
        this.id = new SimpleIntegerProperty(id);
        this.sessionId = new SimpleStringProperty(sessionId);
        this.timestamp = new SimpleStringProperty(timestamp);
        this.elapsedSeconds = new SimpleDoubleProperty(elapsedSeconds);
        this.temperature = new SimpleFloatProperty(temperature);
        this.humidity = new SimpleIntegerProperty(humidity);
        this.light = new SimpleIntegerProperty(light);
        this.fire = new SimpleIntegerProperty(fire);
        this.s1 = new SimpleIntegerProperty(s1);
        this.s2 = new SimpleIntegerProperty(s2);
        this.sa = new SimpleIntegerProperty(sa);
    }

    // JavaFX Property Getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty sessionIdProperty() { return sessionId; }
    public StringProperty timestampProperty() { return timestamp; }
    public DoubleProperty elapsedSecondsProperty() { return elapsedSeconds; }
    public FloatProperty temperatureProperty() { return temperature; }
    public IntegerProperty humidityProperty() { return humidity; }
    public IntegerProperty lightProperty() { return light; }
    public IntegerProperty fireProperty() { return fire; }
    public IntegerProperty s1Property() { return s1; }
    public IntegerProperty s2Property() { return s2; }
    public IntegerProperty saProperty() { return sa; }
}
