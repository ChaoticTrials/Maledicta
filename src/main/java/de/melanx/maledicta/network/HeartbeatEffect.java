package de.melanx.maledicta.network;

import de.melanx.maledicta.Maledicta;
import de.melanx.maledicta.client.DoomEffects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;

public class HeartbeatEffect extends PacketHandler<HeartbeatEffect.Message> {

    public static final CustomPacketPayload.Type<Message> TYPE = new CustomPacketPayload.Type<>(Maledicta.getInstance().id("heartbeat_effect"));

    public HeartbeatEffect() {
        super(TYPE, PacketFlow.CLIENTBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        DoomEffects.setActive(msg.active());
    }

    public record Message(boolean active) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, Message> CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, Message::active,
                Message::new
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return HeartbeatEffect.TYPE;
        }
    }
}
