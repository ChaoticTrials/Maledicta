package de.melanx.maledicta.lightning;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LightningBolt;

public class LightningHelper {

    public static final EntityDataAccessor<String> COLOR_ACCESSOR = SynchedEntityData.defineId(LightningBolt.class, EntityDataSerializers.STRING);

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
