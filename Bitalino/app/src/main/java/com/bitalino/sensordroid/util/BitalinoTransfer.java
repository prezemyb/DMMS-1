package com.bitalino.sensordroid.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by sveinpg on 03.03.16.
 */
public class BitalinoTransfer {
    public final static int TYPE_OFF = 0;
    public final static int TYPE_RAW = 1;
    public final static int TYPE_LUX = 2;
    public final static int TYPE_ACC = 3;
    public final static int TYPE_PZT = 4;
    public final static int TYPE_ECG = 5;
    public final static int TYPE_EEG = 6;
    public final static int TYPE_EDA = 7;
    public final static int TYPE_EMG = 8;
    public final static int TYPE_TMP = 9;

    private final static double VCC = 3.3; // Operating voltage
    private final static int cMin = 208;
    private final static int cMax = 312;

    private final static String[] types = new String[]{"Raw data", "LUX",
            "ACC", "PZT", "ECG", "EEG", "EDA", "EMG", "TMP"};

    private final static String[] metric = new String[]{"Raw data", "Percent", "G-force",
        "Percent", "Millivolt", "Microvolt", "Micro-Siemens", "Millivolt", "Celsius"};

    public static String getMetric(int type){
        return metric[type-1];
    }

    public static String getType(int type){
        return types[type-1];
    }
    /*
        Scales luminous
        - metric: percent
        - range: [0, 100]
     */
    public static double scaleLUX(int value, int resolution){
        return ((double)value/Math.pow(2, resolution))*100;
    }

    /*
        Scales accelerometer
        - metric: g-force
        - range: [-3, 3]
     */
    public static double scaleACC(int value){
        return ((double)(value - cMin)/(cMax-cMin))*2 - 1;
    }


    /*
        Scales the electrocardiography
        - metric: millivolt
        - range: [-1.5, 1.5]
     */
    public static double scaleECG(int value, int resolution){
        double VCC = 3.3; // Operating voltage
        double G_ECG = 1100; // sensor gain

        return (((((float)value/Math.pow(2, resolution))-0.5)*VCC)/G_ECG)*1000;
    }


    /*
        Scales electrodermal activity
        - metric: micro-Siemens
        - range: [1, inf]
     */
    public static double scaleEDA(int value, int resolution){
        // Resistance in mega-Ohm
        double resistance = 1 - (double)value/Math.pow(2, resolution);

        return 1/resistance;
    }
    /**
     * ElectroMyoGraphy conversion.
     *
     * @param port
     *          the port where the <tt>raw</tt> value was read from.
     * @param raw
     *          the value read.
     * @return a value ranging between -1.65 and 1.65mV
     */
    public static double scaleEMG(final int port, final int raw) {
        final double result = (raw * VCC / getResolution(port) - VCC / 2);
        return new BigDecimal(result).setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static final double getResolution(final int port) {
        return (double) port < 4 ? 1023 : 63;
    }


    /**
     * Temperature conversion.
     *
     * @param port
     *          the port where the <tt>raw</tt> value was read from.
     * @param raw
     *          the value read.
     * @param celsius
     *          <tt>true</tt>:use celsius as metric,
     *          <tt>false</tt>: fahrenheit is used.
     * @return a value ranging between -40 and 125 Celsius (-40 and 257 Fahrenheit)
     */
    public static double scaleTMP(final int port, final int raw, boolean celsius){
        double result = (((raw/getResolution(port))*VCC) - 0.5)*100;

        if (!celsius)
            // Convert to fahrenheit
            result = result*((double)9/5) + 32;

        return new BigDecimal(result).setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Respiration conversion.
     *
     * @param port
     *          the port where the <tt>raw</tt> value was read from.
     * @param raw
     *          the value read.
     * @return a value ranging between -50% and 50%
     */
    public static double scalePZT(final int port, final int raw){
        double result =  ((raw/getResolution(port)) - 0.5)*100;
        return new BigDecimal(result).setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

    }

    /**
     * Electroencephalography conversion.
     *
     * @param port
     *          the port where the <tt>raw</tt> value was read from.
     * @param raw
     *          the value read.
     * @return a value ranging between -41.5 and 41.5 microvolt
     */
    public static double scaleEEG(final int port, final int raw){
        double G_ECG = 40000; // sensor gain

        // result rescaled to microvolt
        double result = (((raw/getResolution(port))-0.5)*VCC)/G_ECG;
        result = result*Math.pow(10, 6);

        return new BigDecimal(result).setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
