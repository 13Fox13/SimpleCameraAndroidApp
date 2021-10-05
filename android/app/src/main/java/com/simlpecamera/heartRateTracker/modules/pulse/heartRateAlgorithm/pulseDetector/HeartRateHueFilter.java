package com.simlpecamera.heartRateTracker.modules.pulse.heartRateAlgorithm.pulseDetector;

interface Rough {
        int numberOfZeros = 10;
        int numberOfPoles = 10;
        double gain = 1.894427025e+01;
}

public final class HeartRateHueFilter {

        public static double[] getBooleanArray(int numbers) {
                double[] list = new double[numbers];
                for (int i = 0; i < numbers; i++) {
                        list[i] = 0.0;
                }
                return list;
        };

private double[] xvRough = getBooleanArray(Rough.numberOfZeros + 1);
private double[] yvRough = getBooleanArray(Rough.numberOfPoles + 1);

        public double rgb2hsv(int red, int green, int blue) {
                double min = red < green ? (Math.min(red, blue)) : (Math.min(green, blue));
                double max = red > green ? (Math.max(red, blue)) : (Math.max(green, blue));

                double delta = max - min;

                double h = hue(max, delta, red, green, blue) * 60;
                return h;
        }

        private double hue(double max, double delta, int red, int green, int blue) {
                if (red == max) {
                        return (green - blue) / delta;
                } else if (green == max) {
                        return 2 + (blue - red) / delta;
                } else {
                        return 4 + (red - green) / delta;
                }
        }

        public Double butterworthBandpassRoughFilter(Double value) {
                xvRough[0] = xvRough[1];
                xvRough[1] = xvRough[2];
                xvRough[2] = xvRough[3];
                xvRough[3] = xvRough[4];
                xvRough[4] = xvRough[5];
                xvRough[5] = xvRough[6];
                xvRough[6] = xvRough[7];
                xvRough[7] = xvRough[8];
                xvRough[8] = xvRough[9];
                xvRough[9] = xvRough[10];
                xvRough[10] = value / Rough.gain;

                yvRough[0] = yvRough[1];
                yvRough[1] = yvRough[2];
                yvRough[2] = yvRough[3];
                yvRough[3] = yvRough[4];
                yvRough[4] = yvRough[5];
                yvRough[5] = yvRough[6];
                yvRough[6] = yvRough[7];
                yvRough[7] = yvRough[8];
                yvRough[8] = yvRough[9];
                yvRough[9] = yvRough[10];

                yvRough[10] = (xvRough[10] - xvRough[0]) + 5 * (xvRough[2] - xvRough[8]) + 10 * (xvRough[6] - xvRough[4])
                        + (-0.0000000000 * yvRough[0]) + (0.0357796363 * yvRough[1])
                        + (-0.1476158522 * yvRough[2]) + (0.3992561394 * yvRough[3])
                        + (-1.1743136181 * yvRough[4]) + (2.4692165842 * yvRough[5])
                        + (-3.3820859632 * yvRough[6]) + (3.9628972812 * yvRough[7])
                        + (-4.3832594900 * yvRough[8]) + (3.2101976096 * yvRough[9]);

                return yvRough[10];
        }
}

