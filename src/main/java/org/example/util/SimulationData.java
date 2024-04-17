package org.example.util;

import org.json.*;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class SimulationData {
    private static SimulationData instance;
    private final String configPath = "src/main/resources/config.json";

    public int mapHeight;
    public int mapWidth;
    public int numberOfIslands;
    public int populationSize;
    public Point2D startPos;
    public Point2D endPos;
    public int maxIterations;
    public int maxPopulation;
    public int maxVerticalDistance;
    public double initialEnergy;
    public double reproductionEnergyBound;
    public double reproductionEnergy;
    public double migrationEnergy;
    public double deathEnergyBound;
    public double mutationRate;
    public double reproductionProbability;
    public double migrationProbability;
    public double energyTaken;
    private int id;


    private SimulationData() {
        try {
            String jsonString = new String(Files.readAllBytes(Paths.get(configPath)));
            JSONObject dataObject = new JSONObject(jsonString);

            JSONObject mapObject = dataObject.getJSONObject("map");
            mapHeight = mapObject.getInt("height");
            mapWidth = mapObject.getInt("width");

            JSONObject startPosObject = mapObject.getJSONObject("startPos");
            startPos = new Point2D.Double(startPosObject.getFloat("x"), startPosObject.getFloat("y"));
            JSONObject endPosObject = mapObject.getJSONObject("endPos");
            endPos = new Point2D.Double(endPosObject.getFloat("x"), endPosObject.getFloat("y"));

            JSONObject simulationObject = dataObject.getJSONObject("simulation");
            maxIterations = simulationObject.getInt("maxIterations");
            maxPopulation = simulationObject.getInt("maxPopulation");
            maxVerticalDistance = simulationObject.getInt("maxVerticalDistance");
            numberOfIslands = simulationObject.getInt("numberOfIslands");
            populationSize = simulationObject.getInt("populationSize");
            mutationRate = simulationObject.getDouble("mutationRate");
            reproductionProbability = simulationObject.getDouble("reproductionProbability");
            migrationProbability = 1.0 - reproductionProbability;
            migrationProbability = Math.round(migrationProbability * 100) / 100.0;
            initialEnergy = simulationObject.getDouble("initialEnergy");
            reproductionEnergyBound = simulationObject.getDouble("reproductionEnergyBound");
            reproductionEnergy = simulationObject.getDouble("reproductionEnergy");
            migrationEnergy = simulationObject.getDouble("migrationEnergy");
            energyTaken = simulationObject.getDouble("energyTaken");
            deathEnergyBound = simulationObject.getDouble("deathEnergyBound");
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

    public int generateId() {
        return id++;
    }
}
