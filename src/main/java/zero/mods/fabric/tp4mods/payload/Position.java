package zero.mods.fabric.tp4mods.payload;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.PacketByteBuf;

public record Position(
    String worldId,
    double x,
    double y,
    double z,
    float yaw,
    float pitch
) {
    public static Position fromPlayer(ServerPlayerEntity player) {
        return new Position(
            player.getWorld().getRegistryKey().getValue().toString(),
            player.getX(),
            player.getY(),
            player.getZ(),
            player.getYaw(),
            player.getPitch()
        );
    }

    public static Position read(PacketByteBuf buf) {
        return new Position(
            buf.readString(),
            buf.readDouble(),
            buf.readDouble(),
            buf.readDouble(),
            buf.readFloat(),
            buf.readFloat()
        );
    }

    public void write(PacketByteBuf buf) {
        buf.writeString(worldId);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
    }
} 