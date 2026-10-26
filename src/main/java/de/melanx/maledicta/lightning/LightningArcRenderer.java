package de.melanx.maledicta.lightning;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;

import javax.annotation.Nonnull;

// Draws a jagged lightning between two points in the same style as the vanilla lightning bolt: a few layers of
// thin, transparent quads of different widths around a line.
public class LightningArcRenderer extends EntityRenderer<LightningArcEntity, LightningArcRenderState> {

    private static final int LAYERS = 4;
    private static final float ALPHA = 0.3F;

    public LightningArcRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Nonnull
    @Override
    public LightningArcRenderState createRenderState() {
        return new LightningArcRenderState();
    }

    @Override
    public void extractRenderState(@Nonnull LightningArcEntity entity, @Nonnull LightningArcRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.offset = entity.getOffset();
        state.color = entity.getColor();
        state.geometry = state.offset.lengthSqr() < 0.01 * 0.01 ? null : entity.getGeometry();
    }

    @Override
    protected boolean affectedByCulling(@Nonnull LightningArcEntity entity) {
        return false;
    }

    @Override
    public void submit(@Nonnull LightningArcRenderState state, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector submitNodeCollector, @Nonnull CameraRenderState camera) {
        LightningArcGeometry geometry = state.geometry;
        if (geometry == null) {
            return;
        }

        Vec3[] points = geometry.points();
        int color = state.color < 0 ? 0x73737F : state.color;
        float red = (color >> 16 & 255) / 255F;
        float green = (color >> 8 & 255) / 255F;
        float blue = (color & 255) / 255F;
        float alpha = ALPHA * Mth.clamp(1 - state.ageInTicks / LightningArcEntity.LIFETIME, 0, 1);

        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lightning(), (pose, buffer) -> {
            Matrix4fc matrix = pose.pose();
            for (int layer = 0; layer < LAYERS; layer++) {
                double width = 0.03 + layer * 0.05;
                Vec3 halfA = geometry.sideA().scale(width);
                Vec3 halfB = geometry.sideB().scale(width);
                for (int i = 0; i < points.length - 1; i++) {
                    // two planes cross each other, so the arc has a body from every side
                    LightningArcRenderer.ribbon(matrix, buffer, points[i], points[i + 1], halfA, red, green, blue, alpha);
                    LightningArcRenderer.ribbon(matrix, buffer, points[i], points[i + 1], halfB, red, green, blue, alpha);
                }
            }
        });
    }

    // A flat strip from a to b, drawn from both sides so it does not matter which one faces the camera
    private static void ribbon(Matrix4fc matrix, VertexConsumer buffer, Vec3 a, Vec3 b, Vec3 half, float red, float green, float blue, float alpha) {
        Vec3 a1 = a.subtract(half);
        Vec3 a2 = a.add(half);
        Vec3 b1 = b.subtract(half);
        Vec3 b2 = b.add(half);
        LightningArcRenderer.vertices(matrix, buffer, red, green, blue, alpha, a1, a2, b2, b1);
        LightningArcRenderer.vertices(matrix, buffer, red, green, blue, alpha, b1, b2, a2, a1);
    }

    private static void vertices(Matrix4fc matrix, VertexConsumer buffer, float red, float green, float blue, float alpha, Vec3... corners) {
        for (Vec3 corner : corners) {
            buffer.addVertex(matrix, (float) corner.x, (float) corner.y, (float) corner.z).setColor(red, green, blue, alpha);
        }
    }
}
