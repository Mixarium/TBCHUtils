package org.tbch.tbchutils.tasks;

import java.util.LinkedList;

/* admittedly, code was taken from Project-Poseidon's /tps command
* https://github.com/retromcorg/Project-Poseidon/commit/9435b4df9d3949a316d6dbd509e0a6324f3e772d */

public class TPSMeasurer implements Runnable {
    private static final LinkedList<Double> tpsRecords = new LinkedList<>();
    private long lastTick = System.currentTimeMillis();
    private int tickCount = 0;

    public static LinkedList<Double> getTpsRecords() {
        return tpsRecords;
    }

    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();
        tickCount++;

        //Check if a second has passed
        if (currentTime - lastTick >= 1000) {
            double tps = tickCount / ((currentTime - lastTick) / 1000.0);
            tpsRecords.addFirst(tps);
            if(tpsRecords.size() > 900) { //Don't keep more than 15 minutes of data
                tpsRecords.removeLast();
            }

            tickCount = 0;
            lastTick = currentTime;
        }
    }
}
