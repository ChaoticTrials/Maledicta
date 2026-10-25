package de.melanx.maledicta.network;

import de.melanx.maledicta.Maledicta;
import de.melanx.maledicta.client.DarkSky;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;

public class DarkSkyEffect extends PacketHandler<DarkSkyEffect.Message> {

    public static final CustomPacketPayload.Type<Message> TYPE = new CustomPacketPayload.Type<>(Maledicta.getInstance().id("dark_sky_effect"));

    public DarkSkyEffect() {
        super(TYPE, PacketFlow.CLIENTBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        DarkSky.start(msg.ticks());
    }

    public record Message(int ticks) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, Message> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Message::ticks,
                Message::new
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return DarkSkyEffect.TYPE;
        }
    }
}
