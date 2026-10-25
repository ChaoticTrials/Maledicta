package de.melanx.maledicta.network;

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
    }

    @Override
    protected String getVersion() {
        return "3";
    }

    public static void updateItemEnchantments(ItemEntity item) {
        PacketDistributor.sendToPlayersTrackingEntity(item, new UpdateItemEnchantments.Message(item.getId(), item.getItem()));
    }

    public static void heartbeatEffect(ServerPlayer player, boolean active) {
        PacketDistributor.sendToPlayer(player, new HeartbeatEffect.Message(active));
    }
}
