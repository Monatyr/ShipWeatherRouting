package org.example.physicalModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PhysicalModel {
    /** RESISTANCE IN CALM WATER VARIABLES */

    /** Taken from the paper */
    public static final double L = 205.0;
    public static final double L_pp = 200.0;
    public static final double B = 32.0;
    public static final double T_F = 10.0;
    public static final double T_A = 10.0;
    public static final double T_avg = (T_F + T_A) / 2;
    public static final double displacement = 37500;
    public static final double A_BT = 20.0;
    public static final double h_B = 4.0;
    public static final double C_M = 0.980;
    public static final double C_WP = 0.750;
    public static final double A_T = 16.0;
    public static final double S_APP = 50.0;
    public static final double C_stern = 10.0;
    public static final double D = 8.0;
    public static final double Z = 4;
    public static final double clearance = 0.20;

    public static final double A_M = C_M * B * T_avg;
    public static final double C_P = displacement / L / A_M;

    /** Assumed from the paper */
    public static final double lcb = -0.75;

    /** Assumed exemplary values */
    public static double viscosity = 1.19 * Math.pow(10, -6);
    public static double density = 1025;
    public static final double g = 9.81;
    public static double v = 25.0 * 0.514444;

    public static double k_1;
    public static double C_f;
    public static double S;
    public static double C_B;
    public static double c_2;

    /** ADDED RESISTANCE VARIABLES */

    public static double C_beta;
    public static double C_U;
    public static double C_Form;

    /** GENERAL USE FUNCTIONS */

    public static double getReynolds(double v, double lwl, double viscosity) {
        return v * lwl / viscosity;
    }

    public static double getFroude(double u, double lwl) {
        return u / Math.sqrt(g * lwl);
    }


    /**
     *  CALM WATER RESISTANCE FUNCTIONS
     */

    public static double getC_f(double v, double viscosity) {
        return 0.075 / Math.pow(Math.log10(getReynolds(v, L_pp, viscosity)) - 2, 2);
    }

    public static double getFrictionalResistance(double v, double viscosity) {
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
        System.out.println(S);
        k_1 = c_13 * (0.93 + c_12 * Math.pow(B / L_R, 0.92497) * Math.pow(0.95 - C_P, -0.521448)
                * Math.pow(1 - C_P + 0.0225 * lcb, 0.6906)) - 1;
        System.out.println(k_1);
        C_f = getC_f(v, viscosity);
        System.out.println(C_f);
        return 0.5 * density * Math.pow(v, 2) * S * C_f;
    }

    public static double getAppendageResistance(double v, double viscosity) {
        double k_2 = 1.50 - 1.0;
        return 0.5 * density * Math.pow(v, 2) * S_APP * (1 + k_2) * C_f;
    }

    public static double getWaveResistance() {
        double c_7 = (B / L < 0.11) ? 0.229577 * Math.pow(B / L, 0.33333)
                : ((B / L < 0.25) ? B / L : 0.5 - 0.0625 * L / B);
        double i_E = 1 + 89 * Math.exp(-Math.pow(L / B, 0.80856) * Math.pow(1 - C_WP, 0.30484)
                * Math.pow(1 - C_P - 0.0225 * lcb, 0.6367) * Math.pow(L / B, 0.34574)
                * Math.pow(100 * displacement / Math.pow(L, 3), 0.16302));
        double c_1 = 2223105 * Math.pow(c_7, 3.78613) * Math.pow(T_A / B, 1.07961) * Math.pow(90 - i_E, -1.37565);
        double c_3 = 0.56 * Math.pow(A_BT, 1.5) / (B * T_avg * (0.31 * Math.sqrt(A_BT) + T_F - h_B));
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

    public static double getBulbousBowResistance(double v) {
        double P_B = 0.56 * Math.sqrt(A_BT) / (T_F - 1.5 * h_B);
        double F_ni = v / Math.sqrt(g * (T_F - h_B - 0.25 * Math.sqrt(A_BT)) + 0.15 * Math.pow(v, 2));
        return 0.11 * Math.exp(-3 * Math.pow(P_B, -2)) * Math.pow(F_ni, 3) * Math.pow(A_BT, 1.5) * density * g / (1 + Math.pow(F_ni, 2));
    }

    public static double getImmersedTransomAdditionalPressureResistance(double v) {
        double F_nT = v / Math.sqrt(2 * g * A_T / (B + B * C_WP));
        double c_6 = F_nT < 5 ? 0.2 * (1 - 0.2 * F_nT) : 0;
        return 0.5 * density * Math.pow(v, 2) * A_T * c_6;
    }

    public static double getModelShipCorrelationResistance(double v) {
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

    public static double getDirectionReductionCoefficient(double beta, int BN) {
        if (beta < 30) {
            return 1;
        } else if (beta < 60) {
            return (1.7 - 0.03 * Math.pow(BN - 4, 2)) / 2;
        } else if (beta < 150) {
            return (0.9 - 0.06 * Math.pow(BN - 6, 2)) / 2;
        } else if (beta < 180) {
            return (0.4 - 0.03 * Math.pow(BN - 8, 2)) / 2;
        }
        return 0.0;
    }

    public static double findClosestInArray(double value, double[] array) {
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

    public static double getSpeedReductionCoefficient(double C_B, double v, String loadingConditions) {
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

    public static double getShipFormCoefficient(int BN) {
        System.out.println();
        return 0.7 * BN + Math.pow(BN, 6.5) / (22 * Math.pow(displacement, 2.0/3));
    }

    public static double getEndSpeed(double calmWaterSpeed, double beta, int BN) {
        C_beta = getDirectionReductionCoefficient(beta, BN);
        C_U = getSpeedReductionCoefficient(C_B, calmWaterSpeed, "normal");
        C_Form = getShipFormCoefficient(BN);
        System.out.println("C_beta: " + C_beta + "\nC_U: " + C_U + "\nC_Form: " + C_Form);
        return calmWaterSpeed - C_B * C_U * C_Form * calmWaterSpeed / 100;
    }

    public static void main(String[] args) {
        double res = PhysicalModel.getTotalCalmWaterResistance(v, viscosity);
        System.out.println(res);

        double endSpeed = getEndSpeed(v, 20, 10);
        System.out.println(v);
        System.out.println(endSpeed);
    }
}
