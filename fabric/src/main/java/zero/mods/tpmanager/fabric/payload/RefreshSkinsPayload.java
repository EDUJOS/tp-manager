package zero.mods.tpmanager.fabric.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import static zero.mods.tpmanager.TpManager.MOD_ID;

public record RefreshSkinsPayload(Boolean refreshSkins) implements CustomPayload {
    public static final Id<RefreshSkinsPayload> ID = new Id<>(Identifier.of(MOD_ID, "refresh_skins"));

    public static final PacketCodec<PacketByteBuf, RefreshSkinsPayload> CODEC =
            PacketCodec.of(
                    (payload, buf) -> buf.writeBoolean(payload.refreshSkins()),
                    buf -> new RefreshSkinsPayload(
                            buf.readBoolean()
                    )
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
