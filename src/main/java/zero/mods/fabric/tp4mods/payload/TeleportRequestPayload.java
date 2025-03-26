package zero.mods.fabric.tp4mods.payload;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import zero.mods.fabric.tp4mods.Tp4Mods;

import java.util.Objects;
import java.util.UUID;

public record TeleportRequestPayload (UUID targetUuid) implements CustomPayload {
    public static final CustomPayload.Id<TeleportRequestPayload> ID =
            new CustomPayload.Id<>(Identifier.of(Tp4Mods.MOD_ID, "teleport_req"));

    public static final PacketCodec<PacketByteBuf, TeleportRequestPayload> CODEC =
            PacketCodec.of(
                    (payload, buf) -> buf.writeUuid(payload.targetUuid()),
                    buf -> new TeleportRequestPayload(buf.readUuid())
            );

    @Override public Id<? extends CustomPayload> getId() { return ID; }

//    public static void register() {
//        ServerPlayNetworking.registerGlobalReceiver(
//                ID,
//                (teleportRequestPayload, context) -> {
//                    if (teleportRequestPayload != null) {
//                        handleTeleportRequest(teleportRequestPayload, context.player());
//                    }
//                }
//        );
//    }
//
//    private static void handleTeleportRequest(TeleportRequestPayload payload, ServerPlayerEntity player) {
//        ServerPlayerEntity target = Objects.requireNonNull(player.getServer()).getPlayerManager().getPlayer(payload.targetUuid());
//        if (target != null) {
//            try {
//                player.teleport(target.getServerWorld(), target.getX(), target.getY(), target.getZ(), PositionFlag.ROT, target.getYaw(), target.getPitch(), true);
//                ServerPlayNetworking.send(player, new TeleportResponsePayload("Teletransportado a " + target.getName().getString(), true));
//            } catch (Exception e) {
//                ServerPlayNetworking.send(player, new TeleportResponsePayload("Error: " + e.getMessage(), false));
//            }
//        } else {
//            ServerPlayNetworking.send(player, new TeleportResponsePayload("Jugador no encontrado", false));
//        }
//    }

}
