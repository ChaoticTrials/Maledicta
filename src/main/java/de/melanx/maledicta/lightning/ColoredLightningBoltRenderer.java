package de.melanx.maledicta.lightning;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import net.minecraft.client.renderer.entity.state.LightningBoltRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LightningBolt;
import org.joml.Matrix4fc;

import javax.annotation.Nonnull;
import java.awt.Color;

public class ColoredLightningBoltRenderer extends LightningBoltRenderer {

    private static final Color DEFAULT_COLOR = new Color(0.45F, 0.45F, 0.5F);

    public ColoredLightningBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Nonnull
    @Override
    public LightningBoltRenderState createRenderState() {
        return new ColoredLightningBoltRenderState();
    }

    @Override
    public void extractRenderState(@Nonnull LightningBolt entity, @Nonnull LightningBoltRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        if (entity instanceof ColoredLightningBoltEntity colored && state instanceof ColoredLightningBoltRenderState coloredState) {
            coloredState.color = colored.getColor();
        }
    }

    // [Vanilla copy] + color
    @Override
    public void submit(@Nonnull LightningBoltRenderState state, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector submitNodeCollector, @Nonnull CameraRenderState camera) {
        if (!(state instanceof ColoredLightningBoltRenderState coloredLightning)) {
            return;
        }

        float[] xOffs = new float[8];
        float[] zOffs = new float[8];
        float xOff = 0.0F;
        float zOff = 0.0F;
        RandomSource random = RandomSource.createThreadLocalInstance(state.seed);

        for (int h = 7; h >= 0; h--) {
            xOffs[h] = xOff;
            zOffs[h] = zOff;
            xOff += random.nextInt(11) - 5;
            zOff += random.nextInt(11) - 5;
        }

        float finalXOff = xOff;
        float finalZOff = zOff;

        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lightning(), (pose, buffer) -> {
            Matrix4fc poseMatrix = pose.pose();

            Color color = this.getColor(coloredLightning);
            for (int r = 0; r < 4; r++) {
                RandomSource randomx = RandomSource.createThreadLocalInstance(state.seed);

                for (int p = 0; p < 3; p++) {
                    int hs = 7;
                    int ht = 0;
                    if (p > 0) {
                        hs = 7 - p;
                    }

                    if (p > 0) {
                        ht = hs - 2;
                    }

                    float xo0 = xOffs[hs] - finalXOff;
                    float zo0 = zOffs[hs] - finalZOff;

                    for (int h = hs; h >= ht; h--) {
                        float xo1 = xo0;
                        float zo1 = zo0;
                        if (p == 0) {
                            xo0 += randomx.nextInt(11) - 5;
                            zo0 += randomx.nextInt(11) - 5;
                        } else {
                            xo0 += randomx.nextInt(31) - 15;
                            zo0 += randomx.nextInt(31) - 15;
                        }

                        float rr1 = 0.1F + r * 0.2F;
                        if (p == 0) {
                            rr1 *= h * 0.1F + 1.0F;
                        }

                        float rr2 = 0.1F + r * 0.2F;
                        if (p == 0) {
                            rr2 *= (h - 1.0F) * 0.1F + 1.0F;
                        }

                        LightningBoltRenderer.quad(poseMatrix, buffer, xo0, zo0, h, xo1, zo1, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, rr1, rr2, false, false, true, false);
                        LightningBoltRenderer.quad(poseMatrix, buffer, xo0, zo0, h, xo1, zo1, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, rr1, rr2, true, false, true, true);
                        LightningBoltRenderer.quad(poseMatrix, buffer, xo0, zo0, h, xo1, zo1, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, rr1, rr2, true, true, false, true);
                        LightningBoltRenderer.quad(poseMatrix, buffer, xo0, zo0, h, xo1, zo1, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, rr1, rr2, false, true, false, false);
                    }
                }
            }
        });
    }

    private Color getColor(ColoredLightningBoltRenderState lightning) {
        int hex = lightning.color;

        if (hex < 0) {
            return DEFAULT_COLOR;
        }

        return new Color(hex);
    }
}
