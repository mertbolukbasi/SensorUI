package com.mertblk.sensorui.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SensorDataModel {

    private final StringProperty temperature = new SimpleStringProperty("Temp: N/A");
    private final StringProperty humidity = new SimpleStringProperty("Humidity: N/A");
    private final StringProperty light = new SimpleStringProperty("Light: N/A");
    private final StringProperty fireAlarm = new SimpleStringProperty("Fire Alarm: OFF");
    private final StringProperty sound1 = new SimpleStringProperty("Sound 1: N/A");
    private final StringProperty sound2 = new SimpleStringProperty("Sound 2: N/A");
    private final StringProperty soundAlarm = new SimpleStringProperty("Sound Alarm: OFF");

    public StringProperty temperatureProperty() {
        return temperature;
    }

    public StringProperty humidityProperty() {
        return humidity;
    }

    public StringProperty lightProperty() {
        return light;
    }

    public StringProperty fireAlarmProperty() {
        return fireAlarm;
    }

    public StringProperty sound1Property() {
        return sound1;
    }

    public StringProperty sound2Property() {
        return sound2;
    }

    public StringProperty soundAlarmProperty() {
        return soundAlarm;
    }

    public void setTemperature(String temperature) {
        this.temperature.set("Temp: " + temperature);
    }

    public void setHumidity(String humidity) {
        this.humidity.set("Humidity: " + humidity);
    }

    public void setLight(String light) {
        this.light.set("Light: " + light);
    }

    public void setFireAlarm(String fireAlarm) {
        this.fireAlarm.set("Fire Alarm: " + fireAlarm);
    }

    public void setSound1(String sound1) {
        this.sound1.set("Sound 1: " + sound1);
    }

    public void setSound2(String sound2) {
        this.sound2.set("Sound 2: " + sound2);
    }

    public void setSoundAlarm(String soundAlarm) {
        this.soundAlarm.set("Sound Alarm: " + soundAlarm);
    }
}
