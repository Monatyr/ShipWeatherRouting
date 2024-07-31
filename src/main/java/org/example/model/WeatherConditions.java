package org.example.model;

public record WeatherConditions(
        Double windSpeed,
        Double windAngle,
        Double waveHeight,
        Double oceanCurrentSpeed,
        Double oceanCurrentDirection
) {
}
