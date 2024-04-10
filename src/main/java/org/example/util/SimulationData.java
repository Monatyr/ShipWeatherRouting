package org.example.util;

import org.json.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.lang.Math.round;

public final class SimulationData {
    private static SimulationData instance;
    private final String configPath = "src/main/resources/config.json";

    public int mapHeight;
    public int mapWidth;
    public int numberOfIslands;
    public int populationSize;
    public Coordinates startPos;
    public Coordinates endPos;
    public double mutationRate;
    public double reproductionProbability;
    public double migrationProbability;


    private SimulationData() {
        try {
            String jsonString = new String(Files.readAllBytes(Paths.get(configPath)));
            JSONObject dataObject = new JSONObject(jsonString);

            JSONObject mapObject = dataObject.getJSONObject("map");
            mapHeight = mapObject.getInt("height");
            mapWidth = mapObject.getInt("width");

            JSONObject startPosObject = mapObject.getJSONObject("startPos");
            startPos = new Coordinates(startPosObject.getFloat("y"), startPosObject.getFloat("x"));
            JSONObject endPosObject = mapObject.getJSONObject("endPos");
            endPos = new Coordinates(endPosObject.getFloat("y"), endPosObject.getFloat("x"));

            JSONObject simulationObject = dataObject.getJSONObject("simulation");
            numberOfIslands = simulationObject.getInt("numberOfIslands");
            populationSize = simulationObject.getInt("populationSize");
            mutationRate = simulationObject.getDouble("mutationRate");
            reproductionProbability = simulationObject.getDouble("reproductionProbability");

            migrationProbability = 1.0 - reproductionProbability;
            migrationProbability = Math.round(migrationProbability * 100) / 100.0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static SimulationData getInstance() {
        if (instance == null) {
            instance = new SimulationData();
        }
        return instance;
    }
}
