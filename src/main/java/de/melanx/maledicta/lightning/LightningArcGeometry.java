package de.melanx.maledicta.lightning;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public record LightningArcGeometry(Vec3 sideA, Vec3 sideB, Vec3[] points) {

    public static LightningArcGeometry create(Vec3 offset, long seed) {
        double length = offset.length();
        Vec3 direction = offset.scale(1 / length);
        Vec3 reference = Math.abs(direction.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 sideA = direction.cross(reference).normalize();
        Vec3 sideB = sideA.cross(direction);

        // ends are fixed, in between it jitters sideways
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

        return new LightningArcGeometry(sideA, sideB, points);
    }
}
