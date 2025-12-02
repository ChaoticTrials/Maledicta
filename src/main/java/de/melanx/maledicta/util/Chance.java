package de.melanx.maledicta.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;

import java.text.DecimalFormat;

public class Chance {

    public static final Codec<Chance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("value", 0d).forGetter(Chance::get)
    ).apply(instance, Chance::new));

    private static final RandomSource random = RandomSource.create("SpookyJam2022".hashCode());
    private double value;

    public Chance(double value) {
        if (value < 0 || value > 1) {
            throw new IllegalStateException("Chance cannot be greater than 1 or lower than 0. Yours is " + value);
        }
        this.value = value;
    }

    public boolean test() {
        return random.nextDouble() <= this.value;
    }

    public void set(double value) {
        if (value < 0 || value > 1) {
            throw new IllegalStateException("Chance cannot be greater than 1 or lower than 0. Yours is " + value);
        }
        this.value = value;
    }

    public double get() {
        return this.value;
    }

    @Override
    public String toString() {
        DecimalFormat format = new DecimalFormat("#.###");
        return format.format(this.value * 100);
    }
}
