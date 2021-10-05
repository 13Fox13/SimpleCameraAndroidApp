package com.simlpecamera.heartRateTracker.modules.pulse.heartRateAlgorithm.pulseDetector;

interface HeartRatePulse {
    static double[] getBooleanArray(int averageMax) {
        double[] list = new double[averageMax];
        for (int i = 0; i < averageMax; i++) {
            list[i] = 0.0;
        }
        return list;
    };

    int maxPeriodsToStore = 20;
    int averageSize = 20;
    double maxPeriod = 1.5;
    double minPeriod = 0.1;
    double invalidEntry = Double.valueOf(-100);
    double[] upVals = getBooleanArray(averageSize);
    double[] downVals = getBooleanArray(averageSize);
    double[] periods = getBooleanArray(averageSize);
    double[] periodTimes = getBooleanArray(averageSize);
}


public final class HeartRatePulseDetector implements HeartRatePulse {
    private int upValIndex = 0;
    private int downValIndex = 0;
    private int periodIndex = 0;
    private boolean wasDown = false;
    private double periodStart = 0.0;

    public void reset() {
        for (int i = 0; i < maxPeriodsToStore; i++) {
            periods[i] = invalidEntry;
        }
        for (int i = 0; i < averageSize; i++) {
            upVals[i] = invalidEntry;
            downVals[i] = invalidEntry;
        }
        periodIndex = 0;
        downValIndex = 0;
        upValIndex = 0;
    }

        public int addNewValue (double newVal, double time) {
            if (newVal > 0) {
                upVals[upValIndex] = newVal;
                upValIndex += 1;
                if (upValIndex >= averageSize) {
                    upValIndex = 0;
                }
            }

            if (newVal< 0) {
                downVals[downValIndex] = -newVal;
                downValIndex += 1;
                if (downValIndex >= averageSize) {
                    downValIndex = 0;
                }
            }

            double count = 0;
            double total = 0;
            for (int i = 0; i < averageSize; i++) {
                if (upVals[i] != invalidEntry) {
                    count += 1;
                    total += upVals[i];
                }
            }
            double averageUp = total / count;
            count = 0;
            total = 0;
            for (int i = 0; i < averageSize; i++) {
                if (downVals[i] != invalidEntry) {
                    count += 1;
                    total += downVals[i];
                }
            }
            double averageDown = total / count;

            if (newVal < -0.5 * averageDown) {
                wasDown = true;
            }

            if (newVal >= 0.5 * averageUp && wasDown) {
                wasDown = false;

                if (time - periodStart < maxPeriod && time - periodStart > minPeriod) {
                    periods[periodIndex] = time - periodStart;
                    periodTimes[periodIndex] = time;
                    periodIndex += 1;
                    if (periodIndex >= maxPeriodsToStore) {
                        periodIndex = 0;
                    }
                }

                periodStart = time;
            }

            if (newVal < -0.5 * averageDown) {
                return -1;
            } else if (newVal > 0.5 * averageUp) {
                return 1;
            }
            return 0;
        }

    public void main(String[] args) {
        reset();
    }

}


