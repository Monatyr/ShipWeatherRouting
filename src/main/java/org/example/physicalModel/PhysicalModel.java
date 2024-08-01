package org.example.physicalModel;

import org.example.util.Coordinates;
import org.example.util.SimulationData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PhysicalModel {
    private static SimulationData simulationData = SimulationData.getInstance();

    // RESISTANCE IN CALM WATER VARIABLES

    // Taken from the paper
    private static final double L = simulationData.L;
    private static final double L_pp = simulationData.L_pp;
    private static final double B = simulationData.B;
    private static final double T_F = simulationData.T_F;
    private static final double T_A = simulationData.T_A;
    private static final double T_avg = (T_F + T_A) / 2;
    private static final double displacement = simulationData.displacement;
    private static final double A_BT = simulationData.A_BT;
    private static final double h_B = simulationData.h_B;
    private static final double C_M = simulationData.C_M;
    private static final double C_WP = simulationData.C_WP;
    private static final double A_T = simulationData.A_T;
    private static final double S_APP = simulationData.S_APP;
    private static final double C_stern = simulationData.C_stern;
    private static final double D = simulationData.D;
    private static final double Z = simulationData.Z;
    private static final double clearance = simulationData.clearance;
    private static final double totalEfficiency = simulationData.totalEfficiency;

    private static final double A_M = C_M * B * T_avg;
    private static final double C_P = displacement / L / A_M;

    // Assumed from the paper
    private static final double lcb = -0.75;

    // Assumed exemplary values
    private static double viscosity = 1.19 * Math.pow(10, -6);
    private static double density = 1025;
    private static final double g = 9.81;
    private static double v = 25.0 * 0.514444;

    private static double k_1;
    private static double C_f;
    private static double S;
    private static double C_B;
    private static double c_3;
    private static double c_2;
    private static double C_A;

    // ADED RESISTANCE VARIABLES
    private static double C_beta;
    private static double C_U;
    private static double C_Form;


    /** GENERAL USE FUNCTIONS */

    private static double getReynolds(double v, double lwl, double viscosity) {
        return v * lwl / viscosity;
    }

    private static double getFroude(double u, double lwl) {
        return u / Math.sqrt(g * lwl);
    }

    /**
     *  CALM WATER RESISTANCE FUNCTIONS
     */

    private static double getC_f(double v, double viscosity) {
        return 0.075 / Math.pow(Math.log10(getReynolds(v, L_pp, viscosity)) - 2, 2);
    }

    private static double getFrictionalResistance(double v, double viscosity) {
        double A_M = C_M * B * T_avg;
        double C_P = displacement / (L * A_M);
        double L_R = (1 - C_P + 0.06 * C_P * lcb / (4 * C_P - 1)) * L;
        double c_12;
        if (T_avg / L > 0.05) {
            c_12 = Math.pow(T_avg / L, 0.2228446);
        } else if (T_avg / L > 0.02) {
            c_12 = 48.20 * Math.pow(T_avg / L - 0.02, 2.078) + 0.479948;
        } else {
            c_12 = 0.479948;
        }
        double c_13 = 1 + 0.003 * C_stern;
        C_B = C_M * C_P;
        S = L * (2 * T_avg + B) * Math.sqrt(C_M)
                * (0.453 + 0.4425 * C_B - 0.2862 * C_M - 0.003467 * B / T_avg + 0.3696 * C_WP)
                + 2.38 * A_BT / C_B;
        k_1 = c_13 * (0.93 + c_12 * Math.pow(B / L_R, 0.92497) * Math.pow(0.95 - C_P, -0.521448)
                * Math.pow(1 - C_P + 0.0225 * lcb, 0.6906)) - 1;
        C_f = getC_f(v, viscosity);
        return 0.5 * density * Math.pow(v, 2) * S * C_f;
    }

    private static double getAppendageResistance(double v, double viscosity) {
        double k_2 = 1.50 - 1.0;
        return 0.5 * density * Math.pow(v, 2) * S_APP * (1 + k_2) * C_f;
    }

    private static double getWaveResistance() {
        double c_7 = (B / L < 0.11) ? 0.229577 * Math.pow(B / L, 0.33333)
                : ((B / L < 0.25) ? B / L : 0.5 - 0.0625 * L / B);
        double i_E = 1 + 89 * Math.exp(-Math.pow(L / B, 0.80856) * Math.pow(1 - C_WP, 0.30484)
                * Math.pow(1 - C_P - 0.0225 * lcb, 0.6367) * Math.pow(L / B, 0.34574)
                * Math.pow(100 * displacement / Math.pow(L, 3), 0.16302));
        double c_1 = 2223105 * Math.pow(c_7, 3.78613) * Math.pow(T_A / B, 1.07961) * Math.pow(90 - i_E, -1.37565);
        c_3 = 0.56 * Math.pow(A_BT, 1.5) / (B * T_avg * (0.31 * Math.sqrt(A_BT) + T_F - h_B));
        c_2 = Math.exp(-1.89 * Math.sqrt(c_3));
        double c_5 = 1 - 0.8 * A_T / (B * T_avg * C_M);
        double c_16 = (C_P < 0.8) ? 8.07981 * C_P - 13.8673 * Math.pow(C_P, 2) + 6.984388 * Math.pow(C_P, 3)
                : 1.73014 - 0.7067 * C_P;
        double m_1 = 0.0140407 * L / T_avg - 1.75254 * Math.pow(displacement, 1.0 / 3) / L - 4.79323 * B / L - c_16;
        double c_15;
        if (Math.pow(L, 3) / displacement < 512) {
            c_15 = -1.69385;
        } else if (Math.pow(L, 3) / displacement < 1727) {
            c_15 = -1.69385 + (L / Math.pow(displacement, 1.0 / 3) - 8.0) / 2.36;
        } else {
            c_15 = 0.0;
        }
        double F_n = getFroude(v, L);
        double d = -0.9;
        double m_2 = c_15 * Math.pow(C_P, 2) * Math.exp(-0.1 * Math.pow(F_n, -2));
        double lambda_var = (L / B <= 12) ? 1.446 * C_P - 0.03 * L / B : 1.446 * C_P - 0.36;
        return c_1 * c_2 * c_5 * displacement * density * g
                * Math.exp(m_1 * Math.pow(F_n, d) + m_2 * Math.cos(lambda_var * Math.pow(F_n, -2)));
    }

    private static double getBulbousBowResistance(double v) {
        double P_B = 0.56 * Math.sqrt(A_BT) / (T_F - 1.5 * h_B);
        double F_ni = v / Math.sqrt(g * (T_F - h_B - 0.25 * Math.sqrt(A_BT)) + 0.15 * Math.pow(v, 2));
        return 0.11 * Math.exp(-3 * Math.pow(P_B, -2)) * Math.pow(F_ni, 3) * Math.pow(A_BT, 1.5) * density * g / (1 + Math.pow(F_ni, 2));
    }

    private static double getImmersedTransomAdditionalPressureResistance(double v) {
        double F_nT = v / Math.sqrt(2 * g * A_T / (B + B * C_WP));
        double c_6 = F_nT < 5 ? 0.2 * (1 - 0.2 * F_nT) : 0;
        return 0.5 * density * Math.pow(v, 2) * A_T * c_6;
    }

    private static double getModelShipCorrelationResistance(double v) {
        double c_4 = Math.min(T_F / L, 0.04);
        double C_A = 0.006 * Math.pow(L + 100, -0.16) - 0.00205 + 0.003 * Math.sqrt(L / 7.5) * Math.pow(C_B, 4) * c_2 * (0.04 - c_4);
        return density * Math.pow(v, 2) * S * C_A / 2;
    }

    public static double getTotalCalmWaterResistance(double v, double viscosity) {
        double R_F = getFrictionalResistance(v, viscosity);
        double R_APP = getAppendageResistance(v, viscosity);
        double R_W = getWaveResistance();
        double R_B = getBulbousBowResistance(v);
        double R_TR = getImmersedTransomAdditionalPressureResistance(v);
        double R_A = getModelShipCorrelationResistance(v);
        return R_F * (1 + k_1) + R_APP + R_W + R_B + R_TR + R_A;
    }

    /**
     *  ADDED RESISTANCE DUE TO WEATHER CONDITIONS FUNCTIONS
     */

    private static double getDirectionReductionCoefficient(double shipHeadingAngle, double windAngle, int BN) {
        double beta = getRelativeWindAngle(shipHeadingAngle, windAngle);
        beta = Math.abs(beta); // beta is <-180, 180>; Only the abs value of the angle is important in the calculations
        if (beta < 30) {
            return 1;
        } else if (beta < 60) {
            return (1.7 - 0.03 * Math.pow(BN - 4, 2)) / 2;
        } else if (beta < 150) {
            return (0.9 - 0.06 * Math.pow(BN - 6, 2)) / 2;
        } else if (beta <= 180) {
            return (0.4 - 0.03 * Math.pow(BN - 8, 2)) / 2;
        }
        return 0.0;
    }

    private static double findClosestInArray(double value, double[] array) {
        double actualValue = value;
        double diff = Double.POSITIVE_INFINITY;
        for (double item : array) {
            double absDiff = Math.abs(actualValue - item);
            if (absDiff > diff) {
                break;
            }
            value = item;
            diff = absDiff;
        }
        return value;
    }

    private static double getSpeedReductionCoefficient(double C_B, double v, String loadingConditions) {
        double F_n = getFroude(v, L);
        double[] normalConditions = {0.55, 0.6, 0.65, 0.7, 0.75, 0.8, 0.85};
        double[] loadedConditions = {0.75, 0.8, 0.85};
        double[] ballastConditions = {0.75, 0.8, 0.85};

        if (loadingConditions == "normal") {
            C_B = findClosestInArray(C_B, normalConditions);
            if (C_B == 0.55) return 1.7 - 1.4 * F_n - 7.4 * Math.pow(F_n, 2);
            if (C_B == 0.60) return 2.2 - 2.5 * F_n - 9.7 * Math.pow(F_n, 2);
            if (C_B == 0.65) return 2.6 - 3.7 * F_n - 11.6 * Math.pow(F_n, 2);
            if (C_B == 0.70) return 3.1 - 5.3 * F_n - 12.4 * Math.pow(F_n, 2);
            if (C_B == 0.75) return 2.4 - 10.6 * F_n - 9.5 * Math.pow(F_n, 2);
            if (C_B == 0.80) return 2.6 - 13.1 * F_n - 15.1 * Math.pow(F_n, 2);
            if (C_B == 0.85) return 3.1 - 18.7 * F_n + 28.0 * Math.pow(F_n, 2);
        } else if (loadingConditions == "loaded") {
            C_B = findClosestInArray(C_B, loadedConditions);
            if (C_B == 0.75) return 2.4 - 10.6 * F_n - 9.5 * Math.pow(F_n, 2);
            if (C_B == 0.80) return 2.6 - 13.1 * F_n - 15.1 * Math.pow(F_n, 2);
            if (C_B == 0.85) return 3.1 - 18.7 * F_n + 28.0 * Math.pow(F_n, 2);
        } else {
            C_B = findClosestInArray(C_B, ballastConditions);
            if (C_B == 0.75) return 2.6 - 12.5 * F_n - 13.5 * Math.pow(F_n , 2);
            if (C_B == 0.80) return 3.0 - 16.3 * F_n - 21.6 * Math.pow(F_n, 2);
            if (C_B == 0.85) return 3.4 - 20.9 * F_n + 31.8 * Math.pow(F_n, 2);
        }
        return 0.0;
    }

    private static double getShipFormCoefficient(int BN) {
        return 0.7 * BN + Math.pow(BN, 6.5) / (22 * Math.pow(displacement, 2.0/3));
    }

    private static double getSpeedAfterVoluntarySlowDownDueToWaveHeight(double v, double waveHeight) {
        if (waveHeight <= 6) {
            return v;
        } else if (waveHeight <= 9) {
            return v * 0.75;
        } else if (waveHeight <= 12) {
            return v * 0.5;
        } else {
            return v * 0.25;
        }
    }

    // in head wind BN >= 10 give speeds close to 0 or even negative; 12 BN can give e.g. -25m/s
    public static double getEndSpeed(double calmWaterSpeed, double shipHeadingAngle, double windAngle, int BN) {
        C_beta = getDirectionReductionCoefficient(shipHeadingAngle, windAngle, BN);
        C_U = getSpeedReductionCoefficient(C_B, calmWaterSpeed, "normal");
        C_Form = getShipFormCoefficient(BN);
        return calmWaterSpeed - C_beta * C_U * C_Form * calmWaterSpeed / 100;
    }

    /** CALM WATER SPEED AND RESISTANCE BASED ON TARGET END SPEED */

    // get calm water speed based on the target end speed using Newton-Raphson method. Does not work for BN >= 11
    public static double getCalmWaterSpeed(double endSpeed, double tolerance, int maxIterations, int BN, double shipHeadingAngle, double windAngle) {
        C_Form = getShipFormCoefficient(BN);
        C_beta = getDirectionReductionCoefficient(shipHeadingAngle, windAngle, BN);
        double calmWaterSpeed = endSpeed;
        for (int i = 0; i < maxIterations; i++) {
            double Fn = calmWaterSpeed / Math.sqrt(g * L);
            double CU = 1.7 - 1.4 * Fn - 7.4 * Fn * Fn;
            double fCalmWaterSpeed = calmWaterSpeed * (1 - (C_beta * C_Form * CU / 100.0)) - endSpeed;
            double fPrimeCalmWaterSpeed = 1 - (C_beta * C_Form / 100.0) * (1.7 - 2.8 * Fn - 22.2 * Fn * Fn / Math.sqrt(g * L));
            double calmWaterSpeedNext = calmWaterSpeed - fCalmWaterSpeed / fPrimeCalmWaterSpeed;

            if (Math.abs(calmWaterSpeedNext - calmWaterSpeed) < tolerance) {
                return calmWaterSpeedNext;
            }
            calmWaterSpeed = calmWaterSpeedNext;
        }
        return calmWaterSpeed;
    }

    public static double adjustSpeedForWaveHeight(double calmWaterSpeed, double waveHeight) {
        if (waveHeight < 6) {
            return calmWaterSpeed;
        } else if (waveHeight < 9) {
            return 0.75 * calmWaterSpeed;
        } else if (waveHeight < 12) {
            return 0.5 * calmWaterSpeed;
        }
        return 0.25 * calmWaterSpeed;
    }

    /** Utils functions */

    // 0 deg -> North   90 deg -> East   180 -> South   270 -> West
    public static double getShipHeadingAngle(Coordinates startCoords, Coordinates endCoords) {
        double startLat = Math.toRadians(startCoords.latitude());
        double startLong = Math.toRadians(startCoords.longitude());
        double endLat = Math.toRadians(endCoords.latitude());
        double endLong = Math.toRadians(endCoords.longitude());
        double longitudeDiff = endLong - startLong;
        double X = Math.cos(endLat) * Math.sin(longitudeDiff);
        double Y = Math.cos(startLat) * Math.sin(endLat) - Math.sin(startLat) * Math.cos(endLat) * Math.cos(longitudeDiff);
        double bearing = Math.atan2(X, Y);
        return Math.toDegrees(bearing);
    }

    /**
     * @param shipHeadingAngle Angle IN which the ship is heading
     * @param windAngle Angle FROM which the wind is blowing
     * @return Angle relative to the ship FROM which the wind is blowing
     */
    public static double getRelativeWindAngle(double shipHeadingAngle, double windAngle) {
        double relativeAngle = (windAngle - shipHeadingAngle + 360) % 360;
        return relativeAngle <= 180 ? relativeAngle : relativeAngle - 360;
    }


    /** PROPULSION AND POWER REQUIRED */
    // For now I will just use a static, assumed total efficiency of ~63.5%, which is the result from the Holtrop and Mennen paper
    //https://www.man-es.com/docs/default-source/document-sync/basic-principles-of-ship-propulsion-eng.pdf

    private static double getWakeFraction() {
        double C_V = (1 + (0.156 + 0.5) / 2) * C_f + C_A;
        System.out.println("C_V: " + C_V);
        System.out.println(0.3 * C_B + 10 * C_V * C_B - 0.1);
        return 0.5 * C_B - 0.05;
    }

    private static double getThrustDeductionCoefficient() {
        return 0.27 * C_B;
    }



    public static double getBrakePower(double totalResistance, double shipSpeed) { // TODO: affect ship speed if brake power is limited by the engine's capabilities
        double effectivePower = totalResistance * shipSpeed;
        return effectivePower / totalEfficiency;
    }

    /**
     * FUEL USAGE
     */

    // TODO: change fuel used to full cost or something like that.
    public static double getFuelUsed(double brakePower, double journeyTimeInHours) {
        double engineLoad = brakePower / (simulationData.maxOutput);
        if (engineLoad > maxRecordedEngineLoad) {
            System.out.println("New max engine load (should be <=0 1) " + engineLoad);
            maxRecordedEngineLoad = engineLoad;
        }
        double fuelUsageRate = 17.28 * Math.pow(engineLoad, 3) + 11.23 * Math.pow(engineLoad, 2) - 47.36 * engineLoad + 180.54; // taken from the paper (described in the fuel usage Story)
        double fuelUsed = fuelUsageRate * brakePower * journeyTimeInHours / 1000; // in grams
        fuelUsed = fuelUsed / 1000 / 1000; // in tons
        return fuelUsed;
    }


    /**
     * SAFETY (based on https://www.researchgate.net/publication/261041886_Multicriteria_Optimisation_in_Weather_Routing)
     */
    public static double getShapeCoefficient(double windAngle) {
        if (windAngle < 0) {
            windAngle += 360;
        }
        if (windAngle > 180) {
            windAngle = 360 - windAngle;
        }
        if (windAngle >= 135) {
            return 0.25;
        }
        // function interpolated by Wolfram from the plot values in the paper
        return 0.0000235217 * Math.pow(windAngle, 2) + 0.000631451 * windAngle - 0.253409;
    }

    public static double getFractionalSafetyCoefficient(
            double windSpeed,
            double thresholdWindSpeed,
            double thresholdWindSpeedMargin,
            double windAngle
    ) {
        windSpeed = 1.94384 * windSpeed; // to knots
        double shapeCoefficient = getShapeCoefficient(windAngle);
        double maxWindSpeed = thresholdWindSpeed - shapeCoefficient * thresholdWindSpeedMargin;
        if ((maxWindSpeed - windSpeed) / maxWindSpeed <= 0) {
            System.out.println(maxWindSpeed + " " + windSpeed);
            System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        } else if ((maxWindSpeed - windSpeed) / maxWindSpeed > 1) {
            System.out.println("?????" + (maxWindSpeed - windSpeed) / maxWindSpeed);
        }
        return (maxWindSpeed - windSpeed) / maxWindSpeed;
    }


    /**
     * UTILS
     */

    public static int convertWindSpeedToBeaufort(double windSpeedMs) {
        if (windSpeedMs < 0.3) {
            return 0; // Calm
        } else if (windSpeedMs < 1.6) {
            return 1; // Light Air
        } else if (windSpeedMs < 3.4) {
            return 2; // Light Breeze
        } else if (windSpeedMs < 5.5) {
            return 3; // Gentle Breeze
        } else if (windSpeedMs < 8.0) {
            return 4; // Moderate Breeze
        } else if (windSpeedMs < 10.8) {
            return 5; // Fresh Breeze
        } else if (windSpeedMs < 13.9) {
            return 6; // Strong Breeze
        } else if (windSpeedMs < 17.2) {
            return 7; // Near Gale
        } else if (windSpeedMs < 20.8) {
            return 8; // Gale
        } else if (windSpeedMs < 24.5) {
            return 9; // Strong Gale
        } else if (windSpeedMs < 28.5) {
            return 10; // Storm
        } else if (windSpeedMs < 32.7) {
            return 11; // Violent Storm
        } else {
            return 12; // Hurricane Force
        }
    }

    private static double maxRecordedEngineLoad = 0.0;

    public static void main(String[] args) {
        double res = PhysicalModel.getTotalCalmWaterResistance(v, viscosity);
        System.out.println(res);

        double calmWaterSpeed = 12;
        double shipHeadingAngle = 30;
        double windAngle = -30;
        int BN = 7;

        double bearing = getShipHeadingAngle(
                new Coordinates(39.099912, -94.581213),
                new Coordinates(38.627089, -90.200203)
        );
        System.out.println(bearing);
        System.out.println(getRelativeWindAngle(30, -30));

        double endSpeed = getEndSpeed(calmWaterSpeed, shipHeadingAngle, windAngle, BN);
        calmWaterSpeed = getCalmWaterSpeed(endSpeed, 1e-2, 10, BN, shipHeadingAngle, windAngle);

        System.out.println("End speed: " + endSpeed);;
        System.out.println("Calm water speed: " + calmWaterSpeed);

        System.out.println(getShapeCoefficient(0));
    }
}
