package de.melanx.maledicta;

import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;
import org.moddingx.libx.config.validate.DoubleRange;
import org.moddingx.libx.config.validate.IntRange;

@RegisterConfig
public class ModConfig {

    @Config
    @DoubleRange(min = 0, max = 1)
    public static double karmaChance = 0.2;

    @Config
    @DoubleRange(min = 0, max = 1)
    public static double kindnessChance = 0.5;

    @Config
    @IntRange(min = 0)
    public static int minCurseDelay = 6000;

    @Config
    @IntRange(min = 0)
    public static int maxCurseDelay = 12000;
}
