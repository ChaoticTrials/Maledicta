package de.melanx.maledicta.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.function.Supplier;

public record UpdateItemEnchantments(int id, CompoundTag tag) {

    public static class Handler implements PacketHandler<UpdateItemEnchantments> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(UpdateItemEnchantments msg, Supplier<NetworkEvent.Context> ctx) {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return true;
            Entity item = level.getEntity(msg.id);
            if (item instanceof ItemEntity) {
                ((ItemEntity) item).getItem().setTag(msg.tag);
            }

            return true;
        }
    }

    public static class Serializer implements PacketSerializer<UpdateItemEnchantments> {

        @Override
        public Class<UpdateItemEnchantments> messageClass() {
            return UpdateItemEnchantments.class;
        }

        @Override
        public void encode(UpdateItemEnchantments msg, FriendlyByteBuf buffer) {
            buffer.writeInt(msg.id);
            buffer.writeNbt(msg.tag);
        }

        @Override
        public UpdateItemEnchantments decode(FriendlyByteBuf buffer) {
            return new UpdateItemEnchantments(buffer.readInt(), buffer.readNbt());
        }
    }
}
