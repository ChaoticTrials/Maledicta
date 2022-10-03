package de.melanx.maledicta.capabilities;

import de.melanx.maledicta.util.Chance;

public interface EnergyCollector {

    /**
     * Range from 0 to 1
     */
    Chance negativeEnergy();

    /**
     * Adds negative energy
     */
    void addEnergy(double addition);

    /**
     * Removes negative energy
     */
    void removeEnergy(double subtraction);

    /**
     * Amount of purification processes
     */
    int purificationProcesses();

    /**
     * Increases the process count by 1
     */
    void increaseProcesses();

    /**
     * Resets process count to 0
     */
    void resetProcesses();
}
