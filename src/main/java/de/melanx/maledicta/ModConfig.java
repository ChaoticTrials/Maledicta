package de.melanx.maledicta;

import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;
import org.moddingx.libx.config.Group;
import org.moddingx.libx.config.validate.DoubleRange;
import org.moddingx.libx.config.validate.IntRange;

@RegisterConfig
public class ModConfig {

    @Config("The chance that the karma enchantment will take effect")
    @DoubleRange(min = 0, max = 1)
    public static double karmaChance = 0.2;

    @Config("The chance that the kindness enchantment will take effect")
    @DoubleRange(min = 0, max = 1)
    public static double kindnessChance = 0.4;

    @Config("The minimum time to wait after an item will be checked for getting cursed")
    @IntRange(min = 0)
    public static int minCurseDelay = 6000;

    @Config("The maximum time to wait after an item will be checked for getting cursed")
    @IntRange(min = 0)
    public static int maxCurseDelay = 12000;

    @Config("Settings can only be transferred, not 'getting lost'")
    public static boolean onlyTransferCurses = false;

    @Group
    public static class NegativeEnergy {

        @Config("Should negative energy even be enabled")
        public static boolean enabled = true;

        @Config("The negative energy each item in hand will receive each second while in nether - 0.01 = 1%")
        @DoubleRange(min = 0, max = 1)
        public static double netherAddition = 0.00001;

        @Config("The negative energy the item you hurt an undead mob with - 0.01 = 1%")
        @DoubleRange(min = 0, max = 1)
        public static double hurtAddition = 0.0001;
    }
}
