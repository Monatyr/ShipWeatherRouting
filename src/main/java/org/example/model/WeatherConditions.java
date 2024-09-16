package org.example.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class WeatherConditions implements Serializable {
    @Expose
    public Double windSpeed;
    @Expose
    public Double windAngle;
    @Expose
    public Double waveHeight;
    @Expose
    public Double oceanCurrentSpeed;
    @Expose
    public Double oceanCurrentDirection;

    public WeatherConditions(Double windSpeed, Double windAngle, Double waveHeight, Double oceanCurrentSpeed, Double oceanCurrentDirection) {
        this.windSpeed = windSpeed;
        this.windAngle = windAngle;
        this.waveHeight = waveHeight;
        this.oceanCurrentSpeed = oceanCurrentSpeed;
        this.oceanCurrentDirection = oceanCurrentDirection;
    }
}
