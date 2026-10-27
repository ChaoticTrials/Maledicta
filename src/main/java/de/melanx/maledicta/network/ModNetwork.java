package de.melanx.maledicta.network;

import de.melanx.maledicta.storm.StormPhase;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.network.NetworkX;

public class ModNetwork extends NetworkX {

    public ModNetwork(ModX mod) {
        super(mod);

        this.register(new UpdateItemEnchantments());
        this.register(new HeartbeatEffect());
        this.register(new DarkSkyEffect());
        this.register(new CurseStormSync());
    }

    @Override
    protected String getVersion() {
        return "5";
    }

    public static void updateItemEnchantments(ItemEntity item) {
        PacketDistributor.sendToPlayersTrackingEntity(item, new UpdateItemEnchantments.Message(item.getId(), item.getItem()));
    }

    public static void heartbeatEffect(ServerPlayer player, boolean active) {
        PacketDistributor.sendToPlayer(player, new HeartbeatEffect.Message(active));
    }

    public static void darkSky(ServerPlayer player, int ticks) {
        PacketDistributor.sendToPlayer(player, new DarkSkyEffect.Message(ticks));
    }

    public static void curseStorm(ServerPlayer player, boolean tracked, StormPhase phase, int ticksLeft, int phaseLength) {
        PacketDistributor.sendToPlayer(player, new CurseStormSync.Message(tracked, phase, ticksLeft, phaseLength));
    }
}
