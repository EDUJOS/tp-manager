package zero.mods.tpmanager.fabric.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import java.util.UUID;
import static zero.mods.tpmanager.TpManager.MOD_ID;

public record TeleportRequestPayload (UUID targetUuid) implements CustomPayload {
    public static final Id<TeleportRequestPayload> ID =
            new Id<>(Identifier.of(MOD_ID, "teleport_req"));

    public static final PacketCodec<PacketByteBuf, TeleportRequestPayload> CODEC =
            PacketCodec.of(
                    (payload, buf) -> buf.writeUuid(payload.targetUuid()),
                    buf -> new TeleportRequestPayload(buf.readUuid())
            );

    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
