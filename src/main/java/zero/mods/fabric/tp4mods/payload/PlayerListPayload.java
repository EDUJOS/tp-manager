package zero.mods.fabric.tp4mods.payload;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.UUID;
import static zero.mods.fabric.tp4mods.Tp4Mods.MOD_ID;

public record PlayerListPayload(List<PlayerInfo> players) implements CustomPayload {
    public record PlayerInfo(UUID uuid, String name, Position position) {
        public static PlayerInfo fromPlayer(ServerPlayerEntity player) {
            return new PlayerInfo(player.getUuid(), player.getName().getString(), Position.fromPlayer(player));
        }

        public GameProfile toGameProfile() {
            return new GameProfile(uuid, name);
        }
    }

    public static final CustomPayload.Id<PlayerListPayload> ID =
            new CustomPayload.Id<>(Identifier.of(MOD_ID, "player_list"));

    public static final PacketCodec<PacketByteBuf, PlayerListPayload> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeCollection(payload.players(), (b, info) -> {
                b.writeUuid(info.uuid());
                b.writeString(info.name());
                info.position.write(b);
            }),
            buf -> new PlayerListPayload(buf.readList(b -> new PlayerInfo(b.readUuid(), b.readString(), Position.read(b))))
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}