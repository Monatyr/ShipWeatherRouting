package org.example.util;

import com.google.gson.annotations.Expose;
import org.example.model.OptimizedFunction;
import org.example.model.WeatherConditions;
import org.json.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.*;

public final class SimulationData {
    private static SimulationData instance;
    private final String configPath = "src/main/resources/config.json";
    public final String weatherPath = "src/main/resources/weather-data-path-created-2.json";

    private JSONObject weatherData;
    @Expose
    public ZonedDateTime startingTime;
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
    public double migrationEnergyBound;
    public double migrationEnergyTaken;
    public double deathEnergyBound;
    public double initialMutationRate;
    public double mutationRate;
    public double eliteMutationRate;
    public double mutationProbability;
    public double initialMutationProbability;
    public double mutationEta;
    public double reproductionProbability;
    public double migrationProbability;
    public int neededPrestige;
    public double energyTaken;
    public double shipSpeed;
    public int routeSwitches;
    public double similarityEpsilon;
    public double paretoEpsilon;
    public Map<OptimizedFunction, Double> epsilonMap = new HashMap<>();
    public Map<OptimizedFunction, Double> crowdingFactorMap = new HashMap<>();
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
    public double minLoad;
    public double maxLoad;
    // ship speed
    public double minSpeed;
    public double maxSpeed;
    // conditions
    public double thresholdWindSpeed;
    public double thresholdWindSpeedMargin;
    // jmetal lib algorithms
    public double jmetalCrossoverProbability;
    public double jmetalMutationProbability;
    public double jmetalMutationEta;

    public int experimentNumber;

    private int id;


    private SimulationData() {
        try {
            readGridFromFile("scripts/heightmap.txt");

            String jsonString = new String(Files.readAllBytes(Paths.get(weatherPath)));
            weatherData = new JSONObject(jsonString);

            jsonString = new String(Files.readAllBytes(Paths.get(configPath)));
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
            initialMutationRate = simulationObject.getDouble("initialMutationRate");
            mutationRate = simulationObject.getDouble("mutationRate");
            eliteMutationRate = simulationObject.getDouble("eliteMutationRate");
            mutationProbability = simulationObject.getDouble("mutationProbability");
            initialMutationProbability = simulationObject.getDouble("initialMutationProbability");
            mutationEta = simulationObject.getDouble("mutationEta");
            reproductionProbability = simulationObject.getDouble("reproductionProbability");
            migrationProbability = Math.round((1.0 - reproductionProbability) * 1000) / 1000.0;
            System.out.println("MIGrATION: " + migrationProbability);
            neededPrestige = simulationObject.getInt("neededPrestige");
            initialEnergy = simulationObject.getDouble("initialEnergy");
            reproductionEnergyBound = simulationObject.getDouble("reproductionEnergyBound");
            reproductionEnergy = simulationObject.getDouble("reproductionEnergy");
            migrationEnergyBound = simulationObject.getDouble("migrationEnergyBound");
            migrationEnergyTaken = simulationObject.getDouble("migrationEnergyTaken");
            energyTaken = simulationObject.getDouble("energyTaken");
            deathEnergyBound = simulationObject.getDouble("deathEnergyBound");
            startingTime = ZonedDateTime.parse(simulationObject.getString("startingTime"));
            shipSpeed = simulationObject.getDouble("shipSpeed");
            routeSwitches = simulationObject.getInt("routeSwitches");
            similarityEpsilon = simulationObject.getDouble("similarityEpsilon");
            paretoEpsilon = simulationObject.getDouble("paretoEpsilon");
            epsilonMap.put(OptimizedFunction.TravelTime, simulationObject.getDouble("travelTimeEpsilon"));
            epsilonMap.put(OptimizedFunction.FuelUsed, simulationObject.getDouble("fuelEpsilon"));
            epsilonMap.put(OptimizedFunction.Danger, simulationObject.getDouble("safetyEpsilon"));
            experimentNumber = simulationObject.getInt("experimentNumber");

            crowdingFactorMap.put(OptimizedFunction.TravelTime, simulationObject.getDouble("timeCrowdingFactor"));
            crowdingFactorMap.put(OptimizedFunction.FuelUsed, simulationObject.getDouble("fuelCrowdingFactor"));
            crowdingFactorMap.put(OptimizedFunction.Danger, simulationObject.getDouble("safetyCrowdingFactor"));

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
            maxLoad = engineObject.getDouble("maxLoad");
            minSpeed = engineObject.getDouble("minSpeed");
            maxSpeed = engineObject.getDouble("maxSpeed");

            JSONObject conditionsObject = dataObject.getJSONObject("conditions");
            thresholdWindSpeed = conditionsObject.getDouble("thresholdWindSpeed");
            thresholdWindSpeedMargin = conditionsObject.getDouble("thresholdWindSpeedMargin");

            JSONObject jmetalObject = dataObject.getJSONObject("jmetal");
            jmetalCrossoverProbability = jmetalObject.getDouble("jmetalCrossoverProbability");
            jmetalMutationProbability = jmetalObject.getDouble("jmetalMutationProbability");
            jmetalMutationEta = jmetalObject.getDouble("jmetalMutationEta");


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
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            List<Integer> forces = Arrays.stream(line.split(" ")).map(Integer::valueOf).toList();
            gridForces.add(forces);
        }
        bufferedReader.close();
    }

    private ZonedDateTime getNearestFullHour(ZonedDateTime arrivalDateTime) {
        arrivalDateTime = arrivalDateTime.plusMinutes(30).withMinute(0).withSecond(0);
        return arrivalDateTime;
    }

    public WeatherConditions getWeatherConditions(Coordinates coordinates, ZonedDateTime arrivalDateTime) {
        JSONObject timestampData  = weatherData.getJSONObject(coordinates.toString());
        arrivalDateTime = getNearestFullHour(arrivalDateTime);
        String arrivalDateTimeStr = arrivalDateTime.toString().replace("Z", "");
        if (!timestampData.has(arrivalDateTimeStr)) {
//            System.out.println("missing");
            arrivalDateTimeStr = "2024-08-01T23:00";
        }
        JSONObject conditions = timestampData.getJSONObject(arrivalDateTimeStr);
        return new WeatherConditions(
                conditions.getDouble("wind_speed_10m") / 3.6, // from km/h to m/s // TODO: check is water. Use an external API (open-meteo cannot reliably tell)
                conditions.getDouble("wind_direction_10m"),
                conditions.getDouble("wave_height"),
                conditions.optDouble("ocean_current_velocity", 0) / 3.6, // from km/h to m/s
                conditions.optDouble("ocean_current_direction", 0)
        );
    }

    public boolean checkIfWater(Coordinates coordinates) {
        JSONObject pointData = weatherData.getJSONObject(coordinates.toString());
        return pointData.getBoolean("is_water");
    }
}
