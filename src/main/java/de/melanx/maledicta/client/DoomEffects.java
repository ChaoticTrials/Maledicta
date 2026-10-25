package de.melanx.maledicta.client;

import de.melanx.maledicta.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;

@EventBusSubscriber(modid = "maledicta", value = Dist.CLIENT)
public class DoomEffects {

    private static final float[][] BEATS = {
            {0.200f, 0.493f, 0.85f}, {1.337f, 1.626f, 0.80f}, {2.433f, 2.717f, 0.85f}, {3.462f, 3.740f, 0.82f},
            {4.423f, 4.696f, 0.89f}, {5.336f, 5.599f, 1.04f}, {6.184f, 6.451f, 0.98f}, {6.997f, 7.254f, 0.97f},
            {7.777f, 8.021f, 1.09f}, {8.477f, 8.735f, 1.11f}, {9.129f, 9.376f, 1.07f}, {9.750f, 9.993f, 1.18f}
    };
    private static final float DUB_STRENGTH = 0.7f;
    private static final float COUNTDOWN = 10.0f;         // seconds, the effect is the strongest at the end
    private static final float FADE_OUT = 2.0f;           // seconds the picture needs to recover when the player is not doomed anymore
    private static final float SOUND_DELAY = 0.05f;       // seconds, the sound starts a little later than the packet arrives
    private static final float ATTACK = 0.02f;            // seconds, a beat hits fast...
    private static final float DECAY = 0.10f;             // ...and fades a bit slower
    private static final float ZOOM = 0.07f;              // how much the field of view narrows on a full beat
    private static final float ENVELOPE_PEAK = DoomEffects.envelopePeak();

    private static long startTick = -1;
    private static float fadeStart = -1;                  // in ticks, -1 if the picture is not recovering
    private static float fadeFrom;                        // the progress when the recovering started

    public static void setActive(boolean active) {
        Minecraft mc = Minecraft.getInstance();
        if (active) {
            startTick = mc.level != null ? mc.level.getGameTime() : -1;
            fadeStart = -1;
        } else if (fadeStart < 0) {
            // slowly fading back into color
            float progress = DoomEffects.progress();
            if (!Float.isNaN(progress)) {
                fadeFrom = progress;
                fadeStart = DoomEffects.now();
            }

            startTick = -1;
        }
    }

    @SubscribeEvent
    public static void onFov(ComputeFovModifierEvent event) {
        if (!ClientConfig.heartbeatZoom || event.getPlayer() != Minecraft.getInstance().player) {
            return;
        }

        float pulse = DoomEffects.pulse();
        if (pulse > 0) {
            event.setNewFovModifier(event.getNewFovModifier() * (1 - ZOOM * pulse));
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        DoomEffects.reset();
    }

    protected static float progress() {
        Minecraft mc = Minecraft.getInstance();
        if ((startTick < 0 && fadeStart < 0) || mc.level == null || mc.player == null || !mc.player.isAlive()) {
            DoomEffects.reset();
            return Float.NaN;
        }

        if (fadeStart >= 0) {
            float fade = (DoomEffects.now() - fadeStart) / (FADE_OUT * 20);
            if (fade >= 1) {
                DoomEffects.reset();
                return Float.NaN;
            }

            return fadeFrom * (1 - Math.max(fade, 0));
        }

        float time = DoomEffects.elapsed();
        return Float.isNaN(time) ? Float.NaN : Mth.clamp(time / COUNTDOWN, 0, 1);
    }

    protected static float pulse() {
        if (fadeStart >= 0) {
            return 0;
        }

        float time = DoomEffects.elapsed();
        return Float.isNaN(time) ? 0 : DoomEffects.intensity(time);
    }

    private static void reset() {
        startTick = -1;
        fadeStart = -1;
    }

    private static float now() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level == null ? 0 : mc.level.getGameTime() + mc.getTimer().getGameTimeDeltaPartialTick(false);
    }

    private static float elapsed() {
        Minecraft mc = Minecraft.getInstance();
        if (startTick < 0 || mc.level == null || mc.player == null || !mc.player.isAlive()) {
            return Float.NaN;
        }

        float time = (DoomEffects.now() - startTick) / 20f - SOUND_DELAY;
        if (time > COUNTDOWN + 2) {
            DoomEffects.reset();
            return Float.NaN;
        }

        return time;
    }

    private static float intensity(float time) {
        float pulse = 0;
        for (float[] beat : BEATS) {
            pulse += beat[2] * DoomEffects.envelope(time - beat[0]);
            pulse += beat[2] * DUB_STRENGTH * DoomEffects.envelope(time - beat[1]);
        }

        float level = 0.45f + 0.55f * Mth.clamp(time / COUNTDOWN, 0, 1);
        return Mth.clamp(pulse / ENVELOPE_PEAK * level, 0, 1);
    }

    private static float envelope(float time) {
        if (time < 0) {
            return 0;
        }

        return (1 - (float) Math.exp(-time / ATTACK)) * (float) Math.exp(-time / DECAY);
    }

    private static float envelopePeak() {
        float peakTime = ATTACK * (float) Math.log((DECAY + ATTACK) / ATTACK);
        return DoomEffects.envelope(peakTime);
    }
}
