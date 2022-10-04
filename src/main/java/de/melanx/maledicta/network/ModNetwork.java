package de.melanx.maledicta.network;

import de.melanx.maledicta.Maledicta;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.network.NetworkX;

public class ModNetwork extends NetworkX {

    public ModNetwork(ModX mod) {
        super(mod);
    }

    @Override
    protected Protocol getProtocol() {
        return Protocol.of("1");
    }

    @Override
    protected void registerPackets() {
        this.registerGame(NetworkDirection.PLAY_TO_CLIENT, new UpdateItemEnchantments.Serializer(), () -> UpdateItemEnchantments.Handler::new);
    }

    public static void updateItemEnchantments(ItemEntity item) {
        Maledicta.getNetwork().channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> item), new UpdateItemEnchantments(item.getId(), item.getItem().getOrCreateTag()));
    }
}
