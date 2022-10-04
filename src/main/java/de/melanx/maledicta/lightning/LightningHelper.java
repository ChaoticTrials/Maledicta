package de.melanx.maledicta.lightning;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LightningBolt;

public class LightningHelper {

    public static final EntityDataAccessor<String> COLOR_ACCESSOR = SynchedEntityData.defineId(LightningBolt.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Boolean> CURSED_ACCESSOR = SynchedEntityData.defineId(LightningBolt.class, EntityDataSerializers.BOOLEAN);

    public static void setCursed(LightningBolt lightning) {
        lightning.getEntityData().set(CURSED_ACCESSOR, true);
    }

    public static boolean isCursed(LightningBolt lightning) {
        return lightning.getEntityData().get(CURSED_ACCESSOR);
    }

    public static void setColor(LightningBolt lightning, String hex) {
        if (!hex.startsWith("#")) {
            hex = "#" + hex;
        }

        lightning.getEntityData().set(COLOR_ACCESSOR, hex);
    }

    public static String getColor(LightningBolt lightning) {
        return lightning.getEntityData().get(COLOR_ACCESSOR);
    }
}
