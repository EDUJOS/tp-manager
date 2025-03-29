package zero.mods.tpmanager.fabric.payload;

import java.util.UUID;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.codec.PacketCodec;
import static zero.mods.tpmanager.TpManager.MOD_ID;

public record AdminActionPayload(
    UUID targetUuid,
    ActionType action,
    // Datos adicionales según el tipo de acción
    UUID destinationPlayerUuid,  // Para teletransportar a otro jugador
    Position customPosition      // Para teletransportar a coordenadas
) implements CustomPayload {
    public static final Id<AdminActionPayload> ID =
        new Id<>(Identifier.of(MOD_ID, "admin_action"));

    public static final PacketCodec<PacketByteBuf, AdminActionPayload> CODEC = PacketCodec.of(
        (payload, buf) -> {
            buf.writeUuid(payload.targetUuid());
            buf.writeEnumConstant(payload.action());

            if (payload.destinationPlayerUuid() != null) {
                buf.writeBoolean(true);
                buf.writeUuid(payload.destinationPlayerUuid());
            } else {
                buf.writeBoolean(false);
            }

            if (payload.customPosition() != null) {
                buf.writeBoolean(true);
                payload.customPosition().write(buf);
            } else {
                buf.writeBoolean(false);
            }
        },
        buf -> {
            UUID targetUuid = buf.readUuid();
            ActionType action = buf.readEnumConstant(ActionType.class);
            UUID destinationUuid = buf.readBoolean() ? buf.readUuid() : null;
            Position position = buf.readBoolean() ? Position.read(buf) : null;
            return new AdminActionPayload(targetUuid, action, destinationUuid, position);
        }
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public enum ActionType {
        TELEPORT_TO_PLAYER,
        TELEPORT_TO_ADMIN,
        TELEPORT_PLAYER_TO_PLAYER,
        TELEPORT_TO_SPAWN,
        TELEPORT_TO_COORDINATES
    }
} 