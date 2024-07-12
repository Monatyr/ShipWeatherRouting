package org.example.model;

public record WeatherConditions(
        double windSpeed,
        double windAngle,
        double waveHeight,
        double waveSpeed
) {
}
