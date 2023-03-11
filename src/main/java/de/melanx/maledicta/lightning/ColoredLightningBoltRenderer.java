package de.melanx.maledicta.lightning;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LightningBolt;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import java.awt.Color;

public class ColoredLightningBoltRenderer extends LightningBoltRenderer {

    private static final Color DEFAULT_COLOR = new Color(0.45F, 0.45F, 0.5F);

    public ColoredLightningBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    // [Vanilla copy] + color
    @Override
    public void render(@Nonnull LightningBolt lightning, float entityYaw, float partialTick, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int packedLight) {
        float[] afloat = new float[8];
        float[] afloat1 = new float[8];
        float f = 0.0F;
        float f1 = 0.0F;
        RandomSource random = RandomSource.create(lightning.seed);

        for (int i = 7; i >= 0; --i) {
            afloat[i] = f;
            afloat1[i] = f1;
            f += (float) (random.nextInt(11) - 5);
            f1 += (float) (random.nextInt(11) - 5);
        }

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f matrix4f = poseStack.last().pose();

        Color color = this.getColor(lightning);
        for (int j = 0; j < 4; ++j) {
            RandomSource random1 = RandomSource.create(lightning.seed);

            for (int k = 0; k < 3; ++k) {
                int l = 7;
                int i1 = 0;
                if (k > 0) {
                    l = 7 - k;
                }

                if (k > 0) {
                    i1 = l - 2;
                }

                float f2 = afloat[l] - f;
                float f3 = afloat1[l] - f1;

                for (int j1 = l; j1 >= i1; --j1) {
                    float f4 = f2;
                    float f5 = f3;
                    if (k == 0) {
                        f2 += (float) (random1.nextInt(11) - 5);
                        f3 += (float) (random1.nextInt(11) - 5);
                    } else {
                        f2 += (float) (random1.nextInt(31) - 15);
                        f3 += (float) (random1.nextInt(31) - 15);
                    }

                    float f10 = 0.1F + (float) j * 0.2F;
                    if (k == 0) {
                        f10 *= (float) j1 * 0.1F + 1.0F;
                    }

                    float f11 = 0.1F + (float) j * 0.2F;
                    if (k == 0) {
                        f11 *= ((float) j1 - 1.0F) * 0.1F + 1.0F;
                    }

                    LightningBoltRenderer.quad(matrix4f, vertexConsumer, f2, f3, j1, f4, f5, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, f10, f11, false, false, true, false);
                    LightningBoltRenderer.quad(matrix4f, vertexConsumer, f2, f3, j1, f4, f5, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, f10, f11, true, false, true, true);
                    LightningBoltRenderer.quad(matrix4f, vertexConsumer, f2, f3, j1, f4, f5, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, f10, f11, true, true, false, true);
                    LightningBoltRenderer.quad(matrix4f, vertexConsumer, f2, f3, j1, f4, f5, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, f10, f11, false, true, false, false);
                }
            }
        }
    }

    private Color getColor(LightningBolt lightning) {
        int hex = LightningHelper.getColor(lightning);

        if (hex < 0) {
            return DEFAULT_COLOR;
        }

        return new Color(hex);
    }
}
