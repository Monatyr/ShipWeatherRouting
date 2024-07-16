package org.example.util;

import org.json.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SimulationData {
    private static SimulationData instance;
    private final String configPath = "src/main/resources/config.json";

    // temporary static grid with forces in each of its cells
    public List<List<Integer>> gridForces;
    // grid
    public double maxLatitude;
    public double minLatitude;
    public int mapHeight;
    public int mapWidth;
    public int numberOfIslands;
    public int populationSize;
    public Coordinates startCoordinates;
    public Coordinates endCoordinates;
    // simulation
    public int maxIterations;
    public int maxPopulation;
    public int maxVerticalDistance;
    public double initialEnergy;
    public double reproductionEnergyBound;
    public double reproductionEnergy;
    public double migrationEnergy;
    public double deathEnergyBound;
    public double mutationRate;
    public double eliteMutationRate;
    public double reproductionProbability;
    public double migrationProbability;
    public double eliteMigrationProbability;
    public int neededPrestige;
    public double energyTaken;
    public int startingTime;
    public double shipSpeed;
    public double similarityEpsilon;
    public double paretoEpsilon;
    // ship
    public double L;
    public double L_pp;
    public double B;
    public double T_F;
    public double T_A;
    public double displacement;
    public double A_BT;
    public double h_B;
    public double C_M;
    public double C_WP;
    public double A_T;
    public double S_APP;
    public double C_stern;
    public double D;
    public int Z;
    public double clearance;
    public double totalEfficiency;
    // engine
    public double minOutput;
    public double maxOutput;
    public double minLoad = 0.3;
    public double maxLoad = 1.0;


    private int id;


    private SimulationData() {
        try {
            readGridFromFile("scripts/heightmap.txt");

            String jsonString = new String(Files.readAllBytes(Paths.get(configPath)));
            JSONObject dataObject = new JSONObject(jsonString);

            JSONObject mapObject = dataObject.getJSONObject("map");
            maxLatitude = mapObject.getDouble("maxLatitude");
            minLatitude = mapObject.getDouble("minLatitude");
            mapHeight = mapObject.getInt("rows");
            mapWidth = mapObject.getInt("columns");

            JSONObject startPosObject = mapObject.getJSONObject("startPos");
            startCoordinates = new Coordinates(startPosObject.getDouble("latitude"), startPosObject.getDouble("longitude"));
            JSONObject endPosObject = mapObject.getJSONObject("endPos");
            endCoordinates = new Coordinates(endPosObject.getDouble("latitude"), endPosObject.getDouble("longitude"));

            JSONObject simulationObject = dataObject.getJSONObject("simulation");
            maxIterations = simulationObject.getInt("maxIterations");
            maxPopulation = simulationObject.getInt("maxPopulation");
            maxVerticalDistance = simulationObject.getInt("maxVerticalDistance");
            numberOfIslands = simulationObject.getInt("numberOfIslands");
            populationSize = simulationObject.getInt("populationSize");
            mutationRate = simulationObject.getDouble("mutationRate");
            eliteMutationRate = simulationObject.getDouble("eliteMutationRate");
            reproductionProbability = simulationObject.getDouble("reproductionProbability");
            migrationProbability = Math.round((1.0 - reproductionProbability) * 100) / 100.0;
            eliteMigrationProbability = simulationObject.getDouble("eliteMigrationProbability");
            neededPrestige = simulationObject.getInt("neededPrestige");
            initialEnergy = simulationObject.getDouble("initialEnergy");
            reproductionEnergyBound = simulationObject.getDouble("reproductionEnergyBound");
            reproductionEnergy = simulationObject.getDouble("reproductionEnergy");
            migrationEnergy = simulationObject.getDouble("migrationEnergy");
            energyTaken = simulationObject.getDouble("energyTaken");
            deathEnergyBound = simulationObject.getDouble("deathEnergyBound");
            startingTime = simulationObject.getInt("startingTime");
            shipSpeed = simulationObject.getDouble("shipSpeed");
            similarityEpsilon = simulationObject.getDouble("similarityEpsilon");
            paretoEpsilon = simulationObject.getDouble("paretoEpsilon");

            JSONObject shipObject = dataObject.getJSONObject("ship");
            L = shipObject.getDouble("L");
            L_pp = shipObject.getDouble("L_pp");
            B = shipObject.getDouble("B");
            T_F = shipObject.getDouble("T_F");
            T_A = shipObject.getDouble("T_A");
            displacement = shipObject.getDouble("displacement");
            A_BT = shipObject.getDouble("A_BT");
            h_B = shipObject.getDouble("h_B");
            C_M = shipObject.getDouble("C_M");
            C_WP = shipObject.getDouble("C_WP");
            A_T = shipObject.getDouble("A_T");
            S_APP = shipObject.getDouble("S_APP");
            C_stern = shipObject.getDouble("C_stern");
            D = shipObject.getDouble("D");
            Z = shipObject.getInt("Z");
            clearance = shipObject.getDouble("clearance");
            totalEfficiency = shipObject.getDouble("totalEfficiency");

            JSONObject engineObject = dataObject.getJSONObject("engine");
            minOutput = engineObject.getDouble("minOutput");
            maxOutput = engineObject.getDouble("maxOutput");
            minLoad = engineObject.getDouble("minLoad");
            maxLatitude = engineObject.getDouble("maxLoad");

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

    public void readGridFromFile(String filename) throws IOException {
        gridForces = new ArrayList<>();

        FileReader fileReader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> lines = new ArrayList<String>();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            List<Integer> forces = Arrays.stream(line.split(" ")).map(s -> Integer.valueOf(s)).toList();
            gridForces.add(forces);
        }
        bufferedReader.close();
    }
}
