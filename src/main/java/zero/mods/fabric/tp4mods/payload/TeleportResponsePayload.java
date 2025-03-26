package zero.mods.fabric.tp4mods.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import zero.mods.fabric.tp4mods.Tp4Mods;

public record TeleportResponsePayload(String message, Boolean success) implements CustomPayload {
    // Identificador único del paquete
    public static final CustomPayload.Id<TeleportResponsePayload> ID =
            new CustomPayload.Id<>(Identifier.of(Tp4Mods.MOD_ID, "teleport_res"));

    // Codec para serializar/deserializar
//    public static final PacketCodec<PacketByteBuf, TeleportResponsePayload> CODEC =
//            PacketCodec.of(
//                    (payload, buf) -> buf.writeString(payload.message()), // Serialización
//                    buf -> new TeleportResponsePayload(buf.readString())    // Deserialización
//            );

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
