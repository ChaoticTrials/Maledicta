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
     * Sets negative energy to exact value
     */
    void setEnergy(double energy);
}
