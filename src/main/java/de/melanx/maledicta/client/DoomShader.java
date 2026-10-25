package de.melanx.maledicta.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import de.melanx.maledicta.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

/**
 * A post-processing shader which distorts the picture to the beat of heartbeat.ogg, see {@link DoomEffects} for the timing.
 * <p>
 * The effect is defined in {@code post_effect/doom_pulse.json} and runs in the post effect slot of the game renderer, after
 * the world and before the gui. The values of a post pass are baked into a gpu buffer when the chain is loaded, so the
 * buffer of the {@code DoomConfig} block is replaced by one we can write every frame (see the access transformer).
 */
@EventBusSubscriber(modid = "maledicta", value = Dist.CLIENT)
public class DoomShader {

    private static final Identifier EFFECT = Identifier.fromNamespaceAndPath("maledicta", "doom_pulse");
    private static final String UNIFORM_BLOCK = "DoomConfig";
    private static final int UNIFORM_SIZE = new Std140SizeCalculator().putFloat().putFloat().get();

    private static PostChain patchedChain;
    private static GpuBuffer uniforms;

    @SubscribeEvent
    public static void onFrame(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        GameRenderer renderer = mc.gameRenderer;
        boolean ours = EFFECT.equals(renderer.currentPostEffect());

        float progress = ClientConfig.heartbeatShader ? DoomEffects.progress() : Float.NaN;
        if (Float.isNaN(progress)) {
            if (ours) {
                renderer.clearPostEffect();
            }

            return;
        }

        // a vanilla effect wins, for example when spectating a creeper. The game clears our effect when the camera changes, so set it every frame
        if (renderer.currentPostEffect() != null && !ours) {
            return;
        }

        renderer.setPostEffect(EFFECT);
        PostChain chain = mc.getShaderManager().getPostChain(EFFECT, LevelTargetBundle.MAIN_TARGETS);
        if (chain == null) {
            return; // the game has logged why
        }

        if (chain != patchedChain) {
            DoomShader.patch(chain);
        }

        DoomShader.write(DoomEffects.pulse(), progress);
    }

    private static void patch(PostChain chain) {
        // the old buffer belongs to a chain which was closed by a resource reload, and closed it with it
        if (uniforms != null && !uniforms.isClosed()) {
            uniforms.close();
        }

        uniforms = RenderSystem.getDevice().createBuffer(() -> "Doom pulse uniforms", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, UNIFORM_SIZE);
        for (PostPass pass : chain.passes) {
            GpuBuffer baked = pass.customUniforms.get(UNIFORM_BLOCK);
            if (baked != null) {
                pass.customUniforms.put(UNIFORM_BLOCK, uniforms); // closed together with the pass
                baked.close();
            }
        }

        patchedChain = chain;
    }

    private static void write(float pulse, float progress) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer data = Std140Builder.onStack(stack, UNIFORM_SIZE).putFloat(pulse).putFloat(progress).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(uniforms.slice(), data);
        }
    }
}
