package de.melanx.maledicta.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.attribute.WeatherAttributes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/**
 * A dark sky for a while, without rain. The darkness is the look of a thunderstorm, which the game applies to the sky,
 * the fog, the clouds and the light, see {@link de.melanx.maledicta.mixin.ClientLevelMixin}.
 */
@EventBusSubscriber(modid = "maledicta", value = Dist.CLIENT)
public class DarkSky {

    /**
     * Tells the weather layers how stormy it is. Both values are the same, so the game only applies the thunder look and no rain look
     */
    public static final WeatherAttributes.WeatherAccess WEATHER = new WeatherAttributes.WeatherAccess() {
        @Override
        public float rainLevel() {
            return DarkSky.factor();
        }

        @Override
        public float thunderLevel() {
            return DarkSky.factor();
        }
    };

    private static final int FADE_IN = 30;
    private static final int FADE_OUT = 60;

    private static long START_TICK = -1;
    private static int DURATION;

    public static void start(int ticks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            START_TICK = mc.level.getGameTime();
            DURATION = ticks;
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        START_TICK = -1;
    }

    // darkness from 0 to 1
    public static float factor() {
        Minecraft mc = Minecraft.getInstance();
        if (START_TICK < 0 || mc.level == null) {
            return 0;
        }

        long elapsed = mc.level.getGameTime() - START_TICK;
        if (elapsed < 0 || elapsed >= DURATION) {
            START_TICK = -1;
            return 0;
        }

        float in = Math.min(1, (float) elapsed / FADE_IN);
        float out = Math.min(1, (float) (DURATION - elapsed) / FADE_OUT);
        float linear = Math.min(in, out);
        return linear * linear * (3 - 2 * linear);
    }
}
