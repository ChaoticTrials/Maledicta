package de.melanx.maledicta.client;

import de.melanx.maledicta.storm.StormPhase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = "maledicta", value = Dist.CLIENT)
public class CurseStormClient {

    private static final int FOG_COLOR = 0x5A0A0A;
    private static final int FLASH_COLOR = 0xB4321E;
    private static final float FOG_CLOSER = 0.4f;
    private static final int ASH_PER_TICK = 240;
    private static final float MAX_FADE_PER_TICK = 1 / 100f;

    private static boolean TRACKED;
    private static StormPhase PHASE = StormPhase.CALM;
    private static int TICKS_LEFT;
    private static int PHASE_LENGTH = 1;

    private static float INTENSITY;
    private static float LAST_INTENSITY;
    private static float FLASH;
    private static float LAST_FLASH;

    public static void update(boolean tracked, StormPhase phase, int ticksLeft, int phaseLength) {
        boolean changed = CurseStormClient.TRACKED && tracked && CurseStormClient.PHASE != phase;
        CurseStormClient.TRACKED = tracked;
        CurseStormClient.PHASE = phase;
        CurseStormClient.TICKS_LEFT = ticksLeft;
        CurseStormClient.PHASE_LENGTH = Math.max(phaseLength, 1);

        if (changed) {
            CurseStormClient.playCue(phase);
        }

        if (!tracked) {
            INTENSITY = LAST_INTENSITY = 0;
            FLASH = LAST_FLASH = 0;
        }
    }

    public static float intensity(float partialTick) {
        return Mth.lerp(partialTick, LAST_INTENSITY, INTENSITY);
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        float partialTick = (float) event.getPartialTick();
        float strength = CurseStormClient.intensity(partialTick) * 0.85f;
        if (strength <= 0) {
            return;
        }

        // distant flashes brighten the fog for a moment
        int storm = CurseStormClient.lerpColor(Mth.lerp(partialTick, LAST_FLASH, FLASH), FOG_COLOR, FLASH_COLOR);

        event.setRed(Mth.lerp(strength, event.getRed(), ARGB.redFloat(storm)));
        event.setGreen(Mth.lerp(strength, event.getGreen(), ARGB.greenFloat(storm)));
        event.setBlue(Mth.lerp(strength, event.getBlue(), ARGB.blueFloat(storm)));
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        float strength = CurseStormClient.intensity((float) event.getPartialTick());
        if (strength > 0 && event.getType() == FogType.NONE) {
            event.scaleFarPlaneDistance(1 - FOG_CLOSER * strength);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) {
            return;
        }

        if (TICKS_LEFT > 0) {
            TICKS_LEFT--;
        }

        LAST_INTENSITY = INTENSITY;
        LAST_FLASH = FLASH;
        // never jumps, for example when the player joins during a storm
        float target = TRACKED ? CurseStormClient.targetIntensity() : 0;
        INTENSITY += Mth.clamp(target - INTENSITY, -MAX_FADE_PER_TICK, MAX_FADE_PER_TICK);
        FLASH = Math.max(0, FLASH - 0.08f);

        if (INTENSITY > 0 && mc.player != null) {
            CurseStormClient.ambience(mc.level, mc.player, INTENSITY);
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        CurseStormClient.update(false, StormPhase.CALM, 0, 1);
    }

    // fades in while the storm is brewing and out while it clears
    private static float targetIntensity() {
        float progress = 1 - Mth.clamp((float) TICKS_LEFT / PHASE_LENGTH, 0, 1);
        float linear = switch(PHASE) {
            case CALM -> 0;
            case BREWING -> progress;
            case ACTIVE -> 1;
            case CLEARING -> 1 - progress;
        };

        return linear * linear * (3 - 2 * linear);
    }

    private static void ambience(ClientLevel level, LocalPlayer player, float strength) {
        RandomSource random = level.getRandom();

        // ash falling around the player
        int ash = Mth.ceil(ASH_PER_TICK * strength);
        for (int i = 0; i < ash; i++) {
            double x = player.getX() + (random.nextDouble() - 0.5) * 32;
            double y = player.getY() + random.nextDouble() * 16 - 4;
            double z = player.getZ() + (random.nextDouble() - 0.5) * 32;

            level.addParticle(random.nextInt(3) == 0 ? ParticleTypes.WHITE_ASH : ParticleTypes.ASH, x, y, z, 0, -0.05, 0);
        }

        // a deep rumble, only while the storm is strong
        if (strength > 0.5f && random.nextInt(240) == 0) {
            level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.AMBIENT_BASALT_DELTAS_MOOD.value(), SoundSource.WEATHER, 0.6f * strength, 0.5f, false);
        }

        // distant thunder with a flash in the fog, the sound comes later the farther away it is
        if (PHASE == StormPhase.ACTIVE && random.nextInt(160) == 0) {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = 48 + random.nextDouble() * 96;
            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;

            level.playLocalSound(x, player.getY() + 16, z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 3.0f, 0.5f + random.nextFloat() * 0.2f, true);
            FLASH = 1;
        }
    }

    private static void playCue(StormPhase phase) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        switch(phase) {
            case BREWING ->
                    mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.WEATHER, 0.6f, 0.5f, false);
            case CLEARING ->
                    mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.WEATHER, 1.0f, 0.5f, false);
        }
    }

    private static int lerpColor(float delta, int from, int to) {
        int r = (int) Mth.lerp(delta, (from >> 16) & 0xFF, (to >> 16) & 0xFF);
        int g = (int) Mth.lerp(delta, (from >> 8) & 0xFF, (to >> 8) & 0xFF);
        int b = (int) Mth.lerp(delta, from & 0xFF, to & 0xFF);

        return (r << 16) | (g << 8) | b;
    }
}
