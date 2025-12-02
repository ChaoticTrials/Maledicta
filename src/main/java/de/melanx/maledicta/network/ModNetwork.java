package de.melanx.maledicta.network;

import net.minecraft.world.entity.item.ItemEntity;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.network.NetworkX;

public class ModNetwork extends NetworkX {

    public ModNetwork(ModX mod) {
        super(mod);

        this.register(new UpdateItemEnchantments());
    }

    @Override
    protected String getVersion() {
        return "2";
    }

    public static void updateItemEnchantments(ItemEntity item) {
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity(item, new UpdateItemEnchantments.Message(item.getId(), item.getItem()));
    }
}
