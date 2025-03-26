package zero.mods.fabric.tp4mods.payload;

import java.util.UUID;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.PacketByteBuf;

public record PlayerInfoPayload(
    UUID uuid,
    String name,
    Position position,
    String worldName
) {
    public static final Identifier ID = Identifier.of("tp4mods", "player_info");
    
    public static PlayerInfoPayload fromPlayer(ServerPlayerEntity player) {
        return new PlayerInfoPayload(
            player.getUuid(),
            player.getName().getString(),
            Position.fromPlayer(player),
            player.getWorld().getRegistryKey().getValue().toString()
        );
    }

    public static PlayerInfoPayload read(PacketByteBuf buf) {
        return new PlayerInfoPayload(
            buf.readUuid(),
            buf.readString(),
            Position.read(buf),
            buf.readString()
        );
    }

    public void write(PacketByteBuf buf) {
        buf.writeUuid(uuid);
        buf.writeString(name);
        position.write(buf);
        buf.writeString(worldName);
    }
} 