package de.melanx.maledicta;

import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;
import org.moddingx.libx.config.Group;
import org.moddingx.libx.config.validate.DoubleRange;
import org.moddingx.libx.config.validate.IntRange;

@RegisterConfig
public class ModConfig {

    @Config({"If set to true, the lightnings for the Maledictus Aufero will only be visible",
            "This will protect your item from getting burnt"})
    public static boolean safeLightnings = true;

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
    public static class Strife {

        @Config({"The chance per tick that the Curse of Strife turns nearby monsters against its wearer"})
        @DoubleRange(min = 0, max = 1)
        public static double chance = 1.0 / 1337;

        @Config("The distance in blocks in which monsters are turned against the wearer")
        @IntRange(min = 1, max = 64)
        public static int radius = 16;

        @Config({"If set to true, only monsters which can see the wearer are turned against them",
                "Otherwise they will come for the wearer even through walls, as long as they find a way"})
        public static boolean requireLineOfSight = false;
    }

    @Group("The Maledictus Aufero stores the curses which a lightning strike takes from items")
    public static class MaledictusAufero {

        @Config("The number of curses a Maledictus Aufero can store")
        @IntRange(min = 1, max = 256)
        public static int curseCapacity = 16;

        @Config({"If set to true, a full Maledictus Aufero pushes its oldest enchantment onto a nearby player to make room for a new one",
                "Without a player who can take it, the rod refuses the new enchantment and the item keeps it"})
        public static boolean overflow = true;

        @Group("Curses jumping to players who are near the rod")
        public static class Jump {

            @Config({"The time in ticks between two jump checks, which give a player near the rod the chance to get cursed",
                    "A jump check affects one random player in range and one random enchantment at most"})
            @IntRange(min = 1)
            public static int interval = 100;

            @Config("The base chance that a enchantment jumps to the checked player during a jump check, before the distance is applied")
            @DoubleRange(min = 0, max = 1)
            public static double chance = 0.25;

            @Config({"How much bad energy speeds the jump checks up, the interval is divided by 1 + bad energy * this value",
                    "0 turns the effect off"})
            @DoubleRange(min = 0, max = 10)
            public static double boost = 1.0;

            @Config("The distance in blocks in which a enchantment can jump to a player. The closer the player, the likelier")
            @IntRange(min = 1, max = 64)
            public static int range = 16;
        }

        @Group("The slow decay of the stored curses")
        public static class Decay {

            @Config({"The time in ticks between two decay steps of the stored curses",
                    "A decay step lets a random stored enchantment try to jump to the players in range, one after another, or, if it does not jump, lose a level"})
            @IntRange(min = 20)
            public static int interval = 1200;

            @Config({"How much bad energy speeds the decay up, the interval is divided by 1 + bad energy * this value",
                    "0 turns the effect off"})
            @DoubleRange(min = 0, max = 10)
            public static double boost = 1.0;

            @Config("The base chance that a enchantment jumps to a player in range during a decay step, before the distance is applied")
            @DoubleRange(min = 0, max = 1)
            public static double jumpChance = 0.45;
        }

        @Group({"Bad energy is the sum of the values below for the conditions which currently apply to the rod, it is 0 on a calm day",
                "The more bad energy, the more often curses jump and decay, see 'boost' in the groups 'Jump' and 'Decay'",
                "A value of 0 turns the condition off"})
        public static class BadEnergy {

            @Config("Bad energy from a thunderstorm, rain gives half of it")
            @DoubleRange(min = 0, max = 10)
            public static double storm = 1.0;

            @Config("Bad energy while it is dark outside")
            @DoubleRange(min = 0, max = 10)
            public static double night = 0.5;

            @Config("Bad energy while the rod is not in the overworld")
            @DoubleRange(min = 0, max = 10)
            public static double dimension = 1.0;

            @Config("Bad energy from a full rod, scaled by how full it is")
            @DoubleRange(min = 0, max = 10)
            public static double fill = 1.0;

            @Config("Bad energy while a Curse Storm rages in the rod's dimension")
            @DoubleRange(min = 0, max = 10)
            public static double curseStorm = 1.0;
        }

        @Group("What happens when a rod with stored curses is broken or destroyed")
        public static class Break {

            @Config("The distance in blocks in which players are cursed when the rod is broken. The sky gets dark in twice this distance")
            @IntRange(min = 1, max = 64)
            public static int radius = 16;

            @Config("The maximum number of stored curses which jump to a single player when the rod is broken")
            @IntRange(min = 1, max = 16)
            public static int maxCursesPerPlayer = 3;

            @Config("The power of the explosion per stored enchantment when the rod is broken. It is capped at 4, which is as strong as TNT")
            @DoubleRange(min = 0, max = 4)
            public static double explosionPowerPerCurse = 2;

            @Config({"If set to true, the explosion when the rod is broken destroys blocks around it",
                    "Otherwise it only hurts entities"})
            public static boolean explosionDamagesBlocks = true;

            @Config({"If set to true, the lightnings when the rod is broken are real: they hurt entities, set things on fire and destroy items",
                    "Otherwise they are only visible"})
            public static boolean lightningsStrike = true;

            @Config("The time in ticks the sky stays dark per stored enchantment when the rod is broken")
            @IntRange(min = 0, max = 6000)
            public static int darkSkyTicksPerCurse = 100;
        }
    }

    @Group({"A Curse Storm is a storm in the nether. While it rages, every loaded Maledictus Aufero in the nether gets struck now and then",
            "Storm phases phases: calm -> brewing -> active -> clearing -> calm",
            "All times are in ticks"})
    public static class CurseStorm {

        @Config("If set to false, there are no Curse Storms at all")
        public static boolean enabled = true;

        @Config("The time the storm needs to clear up after the strikes have stopped")
        @IntRange(min = 1)
        public static int clearing = 400;

        @Config("The average time between two strikes on the same rod while a storm is active")
        @IntRange(min = 1)
        public static int strikeInterval = 600;

        @Group("The time of clam between two storms")
        public static class Interval {

            @Config
            @IntRange(min = 1)
            public static int min = 36000;

            @Config
            @IntRange(min = 1)
            public static int max = 72000;
        }

        @Group("The time during the beginning of storm")
        public static class Brewing {

            @Config
            @IntRange(min = 1)
            public static int min = 600;

            @Config
            @IntRange(min = 1)
            public static int max = 1200;
        }

        @Group("The time a storm strikes the rods")
        public static class Duration {

            @Config
            @IntRange(min = 1)
            public static int min = 6000;

            @Config
            @IntRange(min = 1)
            public static int max = 12000;
        }
    }

    @Group("A room in nether fortresses, which contains a Maledictus Aufero filled with curses")
    public static class FortressRoom {

        @Config("The chance that a nether fortress has such a room. A fortress never has more than one")
        @DoubleRange(min = 0, max = 1)
        public static double chance = 0.8;

        @Config("The minimum number of curses the rod in the room is filled with")
        @IntRange(min = 0, max = 256)
        public static int minCurses = 4;

        @Config("The maximum number of curses the rod in the room is filled with. Never more than the capacity of the rod")
        @IntRange(min = 0, max = 256)
        public static int maxCurses = 8;
    }

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
