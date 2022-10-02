package de.melanx.maledicta;

import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;
import org.moddingx.libx.config.validate.DoubleRange;

@RegisterConfig
public class ModConfig {

    @Config
    @DoubleRange(min = 0, max = 1)
    public static double karmaChance = 0.2;

    @Config
    @DoubleRange(min = 0, max = 1)
    public static double kindnessChance = 0.5;
}
