package de.melanx.maledicta.client;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.RenderSystem;
import de.melanx.maledicta.ClientConfig;
import de.melanx.maledicta.Maledicta;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.io.IOException;

@EventBusSubscriber(modid = "maledicta", value = Dist.CLIENT)
public class DoomShader {

    private static final ResourceLocation EFFECT = ResourceLocation.fromNamespaceAndPath("maledicta", "shaders/post/doom_pulse.json");

    private static PostChain CHAIN;
    private static int WIDTH;
    private static int HEIGHT;
    private static boolean FAILED;

    @SubscribeEvent
    public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> DoomShader.reset());
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL || !ClientConfig.heartbeatShader || FAILED) {
            return;
        }

        float progress = DoomEffects.progress();
        if (Float.isNaN(progress)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (CHAIN == null && !DoomShader.load(mc)) {
            return;
        }

        if (WIDTH != mc.getWindow().getWidth() || HEIGHT != mc.getWindow().getHeight()) {
            WIDTH = mc.getWindow().getWidth();
            HEIGHT = mc.getWindow().getHeight();
            CHAIN.resize(WIDTH, HEIGHT);
        }

        CHAIN.setUniform("Pulse", DoomEffects.pulse());
        CHAIN.setUniform("Progress", progress);

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.resetTextureMatrix();
        CHAIN.process(event.getPartialTick().getGameTimeDeltaPartialTick(false));
        mc.getMainRenderTarget().bindWrite(false);
        RenderSystem.enableDepthTest();
    }

    private static boolean load(Minecraft mc) {
        try {
            CHAIN = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), EFFECT);
            WIDTH = mc.getWindow().getWidth();
            HEIGHT = mc.getWindow().getHeight();
            CHAIN.resize(WIDTH, HEIGHT);
            return true;
        } catch (IOException | JsonSyntaxException e) {
            Maledicta.getInstance().logger.error("Failed to load the heartbeat shader, it will be disabled until the resources are reloaded", e);
            FAILED = true;
            CHAIN = null;
            return false;
        }
    }

    private static void reset() {
        if (CHAIN != null) {
            CHAIN.close();
            CHAIN = null;
        }

        FAILED = false;
    }
}
