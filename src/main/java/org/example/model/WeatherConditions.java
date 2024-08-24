package org.example.model;

import com.google.gson.annotations.Expose;

public record WeatherConditions(
        @Expose Double windSpeed,
        @Expose Double windAngle,
        @Expose Double waveHeight,
        @Expose Double oceanCurrentSpeed,
        @Expose Double oceanCurrentDirection
) {
}
