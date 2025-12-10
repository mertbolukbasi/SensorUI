package com.mertblk.sensorui.model;

public class SensorData {
    private float temperature;
    private int humidity;
    private int light;
    private int fire;
    private int s1;
    private int s2;
    private int sa;

    // Getters
    public float getTemperature() { return temperature; }
    public int getHumidity() { return humidity; }
    public int getLight() { return light; }
    public int getFire() { return fire; }
    public int getS1() { return s1; }
    public int getS2() { return s2; }
    public int getSa() { return sa; }

    // Setters
    public void setTemperature(float temperature) { this.temperature = temperature; }
    public void setHumidity(int humidity) { this.humidity = humidity; }
    public void setLight(int light) { this.light = light; }
    public void setFire(int fire) { this.fire = fire; }
    public void setS1(int s1) { this.s1 = s1; }
    public void setS2(int s2) { this.s2 = s2; }
    public void setSa(int sa) { this.sa = sa; }
}
