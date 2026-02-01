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
        ServerWorld overworldSpawn = Objects.requireNonNull(target.getServer()).getOverworld();
        ServerWorld currentWorld = target.getServerWorld();
        BlockPos spawnPos = overworldSpawn.getSpawnPos();
        Position spawnPosition = new Position(
            overworldSpawn.getRegistryKey().getValue().toString(),
            spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(),
                target.getYaw(), target.getPitch()
        );

        Utils.LOGGER.info("El jugador @{} será teletransportado a:\nMundo: {}\nCordenadas del Spawn: x={}, y={}, z={}, yaw={}, pitch={}", target.getName().getString(), spawnPosition.worldId(), spawnPosition.x(), spawnPosition.y(), spawnPosition.z(), spawnPosition.yaw(), spawnPosition.pitch());

        teleportToPosition(target, spawnPosition, currentWorld);
        FeedbackUtil.sendActionFeedback(admin, target, AdminActionPayload.ActionType.TELEPORT_TO_SPAWN);
    }

    public static void teleportToPosition(ServerPlayerEntity player, Position position, ServerWorld currentWorld) {
        try {
            ServerWorld targetWorld = Objects.requireNonNull(player.getServer()).getWorld(
                    RegistryKey.of(RegistryKeys.WORLD, Identifier.of(position.worldId()))
            );

            if (targetWorld != null) {
                Utils.LOGGER.info("Teleportando jugador {} desde {} hacia {}: X={}, Y={}, Z={}, Yaw={}, Pitch={}",
                        player.getName().getString(),
                        currentWorld.getRegistryKey().getValue(),
                        targetWorld.getRegistryKey().getValue(),
                        position.x(), position.y(), position.z(),
                        position.yaw(), position.pitch()
                );
                
                // Si el jugador ya está en el mundo de destino, solo teleportar las coordenadas
                if (currentWorld == targetWorld) {
                    BlockPos targetPos = new BlockPos((int)position.x(), (int)position.y(), (int)position.z());

                    if (!targetWorld.getBlockState(targetPos.down()).isSolidBlock(targetWorld, targetPos)){
                        BlockPos safePos = findSafePosition(targetWorld, targetPos);
                        if (safePos != null) {
                            Utils.LOGGER.info("Ajustando posición Y de {} a {} para evitar caída", position.y(), safePos.getY() + 1);
                            player.requestTeleport(position.x(), safePos.getY() + 1, position.z());
                        } else {
                            player.requestTeleport(position.x(), position.y(), position.z());
                        }
                    } else {
                        player.requestTeleport(position.x(), position.y(), position.z());
                    }
                    player.networkHandler.syncWithPlayerPosition();
                } else {
                    // Teleportar entre dimensiones
                    Utils.LOGGER.info("Teleportando entre dimensiones: {} -> {}", 
                            currentWorld.getRegistryKey().getValue(), 
                            targetWorld.getRegistryKey().getValue());
                    
                    // Deshabilitar temporalmente la validación de movimiento para evitar "Player moved too quickly!"
                    boolean wasInvulnerable = player.isInvulnerable();
                    player.setInvulnerable(true);
                    
                    try {
                        // Usar las coordenadas exactas del spawn sin validación adicional
                        // ya que el spawn debe ser seguro por defecto
                        player.teleport(targetWorld,
                                position.x(), position.y(), position.z(),
                                Set.of(), // Sin flags de posición para evitar validaciones
                                position.yaw(), position.pitch(),
                                false
                        );
                        
                        // Esperar un tick para que el teleporte se complete
                        player.getServer().execute(() -> {
                            // Restaurar la invulnerabilidad original después del teleporte
                            player.setInvulnerable(wasInvulnerable);
                            // Sincronizar la posición después del teleporte
                            player.networkHandler.syncWithPlayerPosition();
                            Utils.LOGGER.info("Teleporte interdimensional completado para {}", player.getName().getString());
                        });
                        
                    } catch (Exception teleportException) {
                        // Restaurar invulnerabilidad en caso de error
                        player.setInvulnerable(wasInvulnerable);
                        Utils.LOGGER.error("Error durante teleporte interdimensional: {}", teleportException.getMessage());
                        throw teleportException;
                    }
                }
            } else {
                Utils.LOGGER.error("No se pudo encontrar el mundo: {}", position.worldId());
            }
        } catch (Exception e) {
            Utils.LOGGER.error("Error al teleportar jugador {}: {}", player.getName().getString(), e.getMessage(), e);
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

    /**
     * Verifica si una posición es segura para teleporte (no hay bloques sólidos en los pies y cabeza del jugador)
     */
    private static boolean isPositionSafe(ServerWorld world, BlockPos pos) {
        // Verificar que los bloques donde estarán los pies y la cabeza del jugador estén libres
        BlockPos feetPos = pos;
        BlockPos headPos = pos.up();
        BlockPos groundPos = pos.down();
        
        // Los pies y la cabeza deben estar libres (no sólidos)
        boolean feetClear = !world.getBlockState(feetPos).isSolidBlock(world, feetPos);
        boolean headClear = !world.getBlockState(headPos).isSolidBlock(world, headPos);
        // Debe haber un bloque sólido debajo para pararse
        boolean hasGround = world.getBlockState(groundPos).isSolidBlock(world, groundPos);
        
        return feetClear && headClear && hasGround;
    }

    /**
     * Encuentra una posición segura para teleporte interdimensional, con búsqueda más amplia
     */
    private static BlockPos findSafePositionForDimension(ServerWorld world, BlockPos originalPos) {
        // Primero intentar en la posición exacta
        if (isPositionSafe(world, originalPos)) {
            return originalPos;
        }
        
        // Buscar en un radio de 3x3 alrededor de la posición original
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int zOffset = -1; zOffset <= 1; zOffset++) {
                if (xOffset == 0 && zOffset == 0) continue; // Ya probamos la posición original
                
                BlockPos testPos = originalPos.add(xOffset, 0, zOffset);
                if (isPositionSafe(world, testPos)) {
                    Utils.LOGGER.info("Posición segura encontrada en offset ({}, 0, {})", xOffset, zOffset);
                    return testPos;
                }
            }
        }
        
        // Si no encontramos posición segura horizontal, buscar hacia arriba
        for (int yOffset = 1; yOffset <= 5; yOffset++) {
            BlockPos testPos = originalPos.up(yOffset);
            if (isPositionSafe(world, testPos)) {
                Utils.LOGGER.info("Posición segura encontrada {} bloques arriba", yOffset);
                return testPos;
            }
        }
        
        // Como último recurso, buscar hacia abajo (similar al método original)
        for (int y = originalPos.getY() - 1; y > originalPos.getY() - 20 && y > world.getBottomY(); y--) {
            BlockPos testPos = new BlockPos(originalPos.getX(), y, originalPos.getZ());
            if (isPositionSafe(world, testPos)) {
                Utils.LOGGER.info("Posición segura encontrada {} bloques abajo", originalPos.getY() - y);
                return testPos;
            }
        }
        
        Utils.LOGGER.warn("No se pudo encontrar una posición segura, usando posición original");
        return originalPos; // Si no encontramos nada, usar la posición original
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