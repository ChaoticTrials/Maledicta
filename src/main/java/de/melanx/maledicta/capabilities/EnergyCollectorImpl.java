package de.melanx.maledicta.capabilities;

import de.melanx.maledicta.util.Chance;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnergyCollectorImpl implements EnergyCollector, ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<EnergyCollector> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });

    public final LazyOptional<EnergyCollector> holder = LazyOptional.of(() -> this);
    private Chance negativeEnergy = new Chance(0);
    private int purificationProcesses;

    @Override
    public Chance negativeEnergy() {
        return this.negativeEnergy;
    }

    @Override
    public void addEnergy(double addition) {
        if (addition >= 0) {
            this.negativeEnergy.set(Mth.clamp(this.negativeEnergy.get() + addition, 0, 100));
        }
    }

    @Override
    public void removeEnergy(double subtraction) {
        if (subtraction >= 0) {
            this.negativeEnergy.set(Mth.clamp(this.negativeEnergy.get() - subtraction, 0, 100));
        }
    }

    @Override
    public void setEnergy(double energy) {
        this.negativeEnergy = new Chance(energy / 100d);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == INSTANCE) {
            return INSTANCE.orEmpty(cap, this.holder);
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("energy", this.negativeEnergy.get());
        tag.putInt("processes", this.purificationProcesses);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        double energy = tag.getDouble("energy");
        if (energy > 1 || energy < 0) {
            energy = 0;
        }
        this.negativeEnergy = new Chance(energy);
        this.purificationProcesses = tag.getInt("processes");
    }
}
