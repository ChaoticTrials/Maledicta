package de.melanx.maledicta.client;

import de.melanx.maledicta.lightning.LightningArcEntity;
import de.melanx.maledicta.registration.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ClientArcs {

    private static final double RANGE = 128;
    private static int NEXT_ID = -1;

    public static boolean isNear(Vec3 pos) {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null && minecraft.player.distanceToSqr(pos) < RANGE * RANGE;
    }

    public static void spawn(Level level, Vec3 from, Vec3 to, int color) {
        if (!(level instanceof ClientLevel clientLevel)) {
            return;
        }

        LightningArcEntity arc = ModEntities.lightningArc.create(clientLevel, EntitySpawnReason.EVENT);
        if (arc == null) {
            return;
        }

        arc.setId(NEXT_ID--);
        arc.setup(from, to, color);
        clientLevel.addEntity(arc);
    }
}
