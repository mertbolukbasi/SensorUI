package com.mertblk.sensorui.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SensorReading {
    private final IntegerProperty id;
    private final StringProperty sessionId;
    private final ObjectProperty<LocalDateTime> timestamp;
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
        this.timestamp = new SimpleObjectProperty<>(LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        this.elapsedSeconds = new SimpleDoubleProperty(elapsedSeconds);
        this.temperature = new SimpleFloatProperty(temperature);
        this.humidity = new SimpleIntegerProperty(humidity);
        this.light = new SimpleIntegerProperty(light);
        this.fire = new SimpleIntegerProperty(fire);
        this.s1 = new SimpleIntegerProperty(s1);
        this.s2 = new SimpleIntegerProperty(s2);
        this.sa = new SimpleIntegerProperty(sa);
    }

    // Standard Getters
    public int getId() { return id.get(); }
    public String getSessionId() { return sessionId.get(); }
    public LocalDateTime getTimestamp() { return timestamp.get(); }
    public double getElapsedSeconds() { return elapsedSeconds.get(); }
    public float getTemperature() { return temperature.get(); }
    public int getHumidity() { return humidity.get(); }
    public int getLight() { return light.get(); }
    public int getFire() { return fire.get(); }
    public int getS1() { return s1.get(); }
    public int getS2() { return s2.get(); }
    public int getSa() { return sa.get(); }


    // JavaFX Property Getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty sessionIdProperty() { return sessionId; }
    public ObjectProperty<LocalDateTime> timestampProperty() { return timestamp; }
    public DoubleProperty elapsedSecondsProperty() { return elapsedSeconds; }
    public FloatProperty temperatureProperty() { return temperature; }
    public IntegerProperty humidityProperty() { return humidity; }
    public IntegerProperty lightProperty() { return light; }
    public IntegerProperty fireProperty() { return fire; }
    public IntegerProperty s1Property() { return s1; }
    public IntegerProperty s2Property() { return s2; }
    public IntegerProperty saProperty() { return sa; }
}