package de.melanx.maledicta.lightning;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.Vec3;

public class LightningArcRenderState extends EntityRenderState {

    public Vec3 offset = Vec3.ZERO;
    // negative for the vanilla color
    public int color = -1;
    public long seed;
}
