package zero.mods.fabric.tp4mods.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.UUID;
import static zero.mods.fabric.tp4mods.Tp4Mods.MOD_ID;

//public record PlayerListPayload(List<UUID> playerUuids) implements CustomPayload {
//    public static PlayerListPayload fromPlayerInfos(List<PlayerInfoPayload> players) {
//        return new PlayerListPayload(players.stream().map(PlayerInfoPayload::uuid).toList());
//    }
//
//    public static final CustomPayload.Id<PlayerListPayload> ID =
//            new CustomPayload.Id<>(Identifier.of(MOD_ID, "player_list"));
//
//    // Registro del códec (necesario para Fabric 1.21.4+)
//    public static final PacketCodec<PacketByteBuf, PlayerListPayload> CODEC =
//            PacketCodec.of(
//                    (payload, buf) -> buf.writeCollection(payload.playerUuids(), (b, uuid) -> b.writeUuid(uuid)),
//                    buf -> new PlayerListPayload(buf.readList(b -> b.readUuid()))
//            );
//
//    @Override
//    public Id<? extends CustomPayload> getId() {
//        return ID;
//    }
//}

public record PlayerListPayload(List<PlayerInfo> players) implements CustomPayload {
    public record PlayerInfo(UUID uuid, String name) {
        public static PlayerInfo fromPlayer(ServerPlayerEntity player) {
            return new PlayerInfo(player.getUuid(), player.getName().getString());
        }
    }

    public static final CustomPayload.Id<PlayerListPayload> ID =
            new CustomPayload.Id<>(Identifier.of(MOD_ID, "player_list"));

    public static final PacketCodec<PacketByteBuf, PlayerListPayload> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeCollection(payload.players(), (b, info) -> {
                b.writeUuid(info.uuid());
                b.writeString(info.name());
            }),
            buf -> new PlayerListPayload(buf.readList(b -> new PlayerInfo(b.readUuid(), b.readString())))
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}