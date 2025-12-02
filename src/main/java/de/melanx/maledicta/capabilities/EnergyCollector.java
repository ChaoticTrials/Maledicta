package de.melanx.maledicta.capabilities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.maledicta.util.Chance;
import net.minecraft.util.Mth;

public class EnergyCollector {

    public static final Codec<EnergyCollector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Chance.CODEC.fieldOf("chance").forGetter(EnergyCollector::negativeEnergy),
            Codec.INT.fieldOf("purification_processes").forGetter(EnergyCollector::purificationProcesses)
    ).apply(instance, EnergyCollector::new));

    public static final EnergyCollector EMPTY = new EnergyCollector(new Chance(0), 0);

    private Chance negativeEnergy;
    private int purificationProcesses;

    public EnergyCollector(Chance negativeEnergy, int purificationProcesses) {
        this.negativeEnergy = negativeEnergy;
        this.purificationProcesses = purificationProcesses;
    }

    public Chance negativeEnergy() {
        return this.negativeEnergy;
    }

    private int purificationProcesses() {
        return this.purificationProcesses;
    }

    public void addEnergy(double addition) {
        if (addition >= 0) {
            this.negativeEnergy.set(Mth.clamp(this.negativeEnergy.get() + addition, 0, 100));
        }
    }

    public void removeEnergy(double subtraction) {
        if (subtraction >= 0) {
            this.negativeEnergy.set(Mth.clamp(this.negativeEnergy.get() - subtraction, 0, 100));
        }
    }

    public void setEnergy(double energy) {
        this.negativeEnergy = new Chance(energy);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
