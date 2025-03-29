package zero.mods.tpmanager.fabric.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import static zero.mods.tpmanager.TpManager.MOD_ID;

public record TeleportResponsePayload(String message, Boolean success) implements CustomPayload {
    public static final Id<TeleportResponsePayload> ID =
            new Id<>(Identifier.of(MOD_ID, "teleport_res"));

    public static final PacketCodec<PacketByteBuf, TeleportResponsePayload> CODEC =
            PacketCodec.of(
                    (payload, buf) -> {
                        buf.writeBoolean(payload.success());
                        buf.writeString(payload.message());
                    },
                    buf -> new TeleportResponsePayload(
                            buf.readString(),
                            buf.readBoolean()
                    )
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
