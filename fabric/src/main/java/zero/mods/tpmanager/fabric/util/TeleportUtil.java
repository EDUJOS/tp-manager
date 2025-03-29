package zero.mods.tpmanager.fabric.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.util.Formatting;

import java.util.Objects;
import java.util.UUID;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import zero.mods.tpmanager.fabric.payload.AdminActionPayload;
import zero.mods.tpmanager.fabric.payload.Position;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import java.util.Set;

public class TeleportUtil {
    public static void teleportAdminToPlayer(ServerPlayerEntity admin, ServerPlayerEntity target) {
        admin.teleportTo(createTeleportTarget(target));
        admin.networkHandler.syncWithPlayerPosition();
        FeedbackUtil.sendActionFeedback(admin, target, AdminActionPayload.ActionType.TELEPORT_TO_PLAYER);
    }

    public static void teleportPlayerToAdmin(ServerPlayerEntity admin, ServerPlayerEntity target) {
        target.teleportTo(createTeleportTarget(admin));
        target.networkHandler.syncWithPlayerPosition();
        FeedbackUtil.sendActionFeedback(admin, target, AdminActionPayload.ActionType.TELEPORT_TO_ADMIN);
    }

    public static void teleportPlayerToPlayer(ServerPlayerEntity admin, ServerPlayerEntity target, 
            UUID destinationPlayerUuid) {
        ServerPlayerEntity destination = Objects.requireNonNull(admin.getServer()).getPlayerManager()
            .getPlayer(destinationPlayerUuid);
        
        if (destination == null) {
            FeedbackUtil.sendFeedback(admin, "Jugador destino no encontrado", Formatting.RED);
            return;
        }
        target.teleportTo(createTeleportTarget(destination));
        FeedbackUtil.sendActionFeedback(admin, target, AdminActionPayload.ActionType.TELEPORT_PLAYER_TO_PLAYER, destination);
    }

    public static void teleportPlayerToSpawn(ServerPlayerEntity admin, ServerPlayerEntity target) {
        ServerWorld world = Objects.requireNonNull(target.getServer()).getOverworld();
        BlockPos spawnPos = world.getSpawnPos();
        Position spawnPosition = new Position(
            world.getRegistryKey().getValue().toString(),
            spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(),
                target.getYaw(), target.getPitch()
        );

        Utils.LOGGER.info("El jugador @{} será teletransportado a:\nMundo: {}\nCordenadas del Spawn: x={}, y={}, z={}, yaw={}, pitch={}", target.getName().getString(), spawnPosition.worldId(), spawnPosition.x(), spawnPosition.y(), spawnPosition.z(), spawnPosition.yaw(), spawnPosition.pitch());

        teleportToPosition(target, spawnPosition, world);
        FeedbackUtil.sendActionFeedback(admin, target, AdminActionPayload.ActionType.TELEPORT_TO_SPAWN);
    }

    public static void teleportToPosition(ServerPlayerEntity player, Position position, ServerWorld currentWorld) {
        try {
            // Pasarle el mundo como argumento!! [Completar después de la práctica #2 del gp Australia]
            ServerWorld targetWorld = Objects.requireNonNull(player.getServer()).getWorld(
                    RegistryKey.of(RegistryKeys.WORLD, Identifier.of(position.worldId()))
            );
            ServerWorld targetWorld2 = player.getServerWorld();

            if (targetWorld != null) {
                Utils.LOGGER.info("Teleportando jugador {} a: Mundo={}, X={}, Y={}, Z={}, Yaw={}, Pitch={}\n\nTarget World: {}\nTarget World #2: {}",
                        player.getName().getString(), position.worldId(),
                        position.x(), position.y(), position.z(),
                        position.yaw(), position.pitch(),
                        targetWorld.getRegistryKey().getValue(), targetWorld2.getRegistryKey().getValue()
                );

                Utils.LOGGER.info("{}", currentWorld.getRegistryKey());
                if (currentWorld == targetWorld) {
                    BlockPos targetPos = new BlockPos((int)position.x(), (int)position.y(), (int)position.z());

                    if (!targetWorld.getBlockState(targetPos.down()).isSolidBlock(targetWorld, targetPos)){
                        BlockPos safePos = findSafePosition(targetWorld, targetPos);
                        if (safePos != null) {
                            Utils.LOGGER.info("Ajustando posición Y de {} a {} para evitar caída", position.y(), safePos.getY() + 1);
                            player.requestTeleport(position.x(), safePos.getY() + 1, position.z());
                            player.networkHandler.syncWithPlayerPosition();
                            return;
                        }
                    } else {
                        player.requestTeleport(position.x(), position.y(), position.z());
                        player.networkHandler.syncWithPlayerPosition();
                        return;
                    }
                }
//                player.tryUsePortal();
                // El problema está en que no estamos manejando bien el mundo, revisar si los datos recibidos está bien y propios de un mundo
                player.teleport(targetWorld,
                        position.x(), position.y(), position.z(),
                        Set.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z),
                        position.yaw(), position.pitch(),
                        false
                );
                player.networkHandler.syncWithPlayerPosition();
            } else {
                Utils.LOGGER.info("No se pudo encontrar el mundo: {}", position.worldId());
            }
        } catch (Exception e) {
            Utils.LOGGER.error("Error al teleportar: {}", e.getMessage());
        }
    }

    private static BlockPos findSafePosition(ServerWorld world, BlockPos pos) {
        // 20 bloques hacia abajo jeje
        for (int y = pos.getY(); y > pos.getY() - 20 && y > world.getBottomY(); y--) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (world.getBlockState(checkPos).isSolidBlock(world, pos)) {
                return checkPos;
            }
        }
        return null;
    }

    private static TeleportTarget createTeleportTarget(ServerPlayerEntity targetPlayer) {
        Vec3d position = new Vec3d(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ());
        float yaw = targetPlayer.getYaw();
        float pitch = targetPlayer.getPitch();
        ServerWorld targetWorld = (ServerWorld) targetPlayer.getWorld();
        Vec3d velocity = Vec3d.ZERO;

        return new TeleportTarget(targetWorld, position, velocity, yaw, pitch, false, false, Set.of(),
                entity -> Utils.LOGGER.info("Teleport Target")
        );
    }
} 