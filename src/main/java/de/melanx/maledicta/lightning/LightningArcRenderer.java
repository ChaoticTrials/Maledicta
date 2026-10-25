package de.melanx.maledicta.lightning;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
        state.seed = entity.getSeed();
    }

    @Override
    protected boolean affectedByCulling(@Nonnull LightningArcEntity entity) {
        return false;
    }

    @Override
    public void submit(@Nonnull LightningArcRenderState state, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector submitNodeCollector, @Nonnull CameraRenderState camera) {
        double length = state.offset.length();
        if (length < 0.01) {
            return;
        }

        // two directions which are perpendicular to the arc and to each other
        Vec3 direction = state.offset.scale(1 / length);
        Vec3 reference = Math.abs(direction.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 sideA = direction.cross(reference).normalize();
        Vec3 sideB = sideA.cross(direction);
        Vec3[] points = LightningArcRenderer.points(state.offset, length, state.seed, sideA, sideB);

        int color = state.color < 0 ? 0x73737F : state.color;
        float red = (color >> 16 & 255) / 255F;
        float green = (color >> 8 & 255) / 255F;
        float blue = (color & 255) / 255F;
        float alpha = ALPHA * Mth.clamp(1 - state.ageInTicks / LightningArcEntity.LIFETIME, 0, 1);

        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lightning(), (pose, buffer) -> {
            Matrix4fc matrix = pose.pose();
            for (int layer = 0; layer < LAYERS; layer++) {
                double width = 0.03 + layer * 0.05;
                for (int i = 0; i < points.length - 1; i++) {
                    // two planes cross each other, so the arc has a body from every side
                    LightningArcRenderer.ribbon(matrix, buffer, points[i], points[i + 1], sideA.scale(width), red, green, blue, alpha);
                    LightningArcRenderer.ribbon(matrix, buffer, points[i], points[i + 1], sideB.scale(width), red, green, blue, alpha);
                }
            }
        });
    }

    // the points of the arc relative to its start
    // ends are fixed, in between it jitters sideways
    private static Vec3[] points(Vec3 offset, double length, long seed, Vec3 sideA, Vec3 sideB) {
        int segments = Mth.clamp((int) (length * 1.5), 6, 32);
        double spread = Math.min(1.2, length * 0.12);
        RandomSource random = RandomSource.createThreadLocalInstance(seed);
        Vec3[] points = new Vec3[segments + 1];
        for (int i = 0; i <= segments; i++) {
            double progress = (double) i / segments;
            Vec3 point = offset.scale(progress);
            if (i > 0 && i < segments) {
                double taper = Math.sin(Math.PI * progress) * spread;
                point = point.add(sideA.scale((random.nextDouble() - 0.5) * 2 * taper)).add(sideB.scale((random.nextDouble() - 0.5) * 2 * taper));
            }

            points[i] = point;
        }

        return points;
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
