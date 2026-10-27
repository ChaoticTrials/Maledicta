package de.melanx.maledicta.network;

import de.melanx.maledicta.Maledicta;
import de.melanx.maledicta.client.CurseStormClient;
import de.melanx.maledicta.storm.StormPhase;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;

public class CurseStormSync extends PacketHandler<CurseStormSync.Message> {

    public static final CustomPacketPayload.Type<Message> TYPE = new CustomPacketPayload.Type<>(Maledicta.getInstance().id("curse_storm_sync"));

    public CurseStormSync() {
        super(TYPE, PacketFlow.CLIENTBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        CurseStormClient.update(msg.tracked(), msg.phase(), msg.ticksLeft(), msg.phaseLength());
    }

    public record Message(boolean tracked, StormPhase phase, int ticksLeft, int phaseLength) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, Message> CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, Message::tracked,
                StormPhase.STREAM_CODEC, Message::phase,
                ByteBufCodecs.VAR_INT, Message::ticksLeft,
                ByteBufCodecs.VAR_INT, Message::phaseLength,
                Message::new
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return CurseStormSync.TYPE;
        }
    }
}
