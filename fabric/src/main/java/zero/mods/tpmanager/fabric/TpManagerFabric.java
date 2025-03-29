package zero.mods.tpmanager.fabric;

import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.Item;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import java.util.Objects;

import zero.mods.tpmanager.fabric.item.ModItems;
import zero.mods.tpmanager.fabric.payload.*;
import zero.mods.tpmanager.fabric.util.*;
import net.minecraft.util.Rarity;

public class TpManagerFabric implements ModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        LOGGER.info("Setting up TP Manager (Server)");

        Item CUSTOM_ITEM = ModItems.registerItem("tp_manager_item",
                Item::new,
                new Item.Settings()
                        .maxCount(1)
                        .fireproof()
                        .rarity(Rarity.EPIC)
        );
        ModItems.registerModItems(CUSTOM_ITEM);

        try {
            PayloadRegister.typesRegister(PlayerListPayload.ID, PlayerListPayload.CODEC, true);
            PayloadRegister.typesRegister(PlayerListPayload.ID, PlayerListPayload.CODEC, false);
            PayloadRegister.typesRegister(TeleportRequestPayload.ID, TeleportRequestPayload.CODEC, false);
            PayloadRegister.typesRegister(TeleportResponsePayload.ID, TeleportResponsePayload.CODEC, true);
            PayloadRegister.typesRegister(AdminActionPayload.ID, AdminActionPayload.CODEC, false);
            PayloadRegister.typesRegister(RefreshSkinsPayload.ID, RefreshSkinsPayload.CODEC, true);
        } catch (Exception e) {
            LOGGER.info("No se han podido registrar los tipos de los Payloads debido a: {}", e.getMessage());
        }

        ServerPlayNetworking.registerGlobalReceiver(
                PlayerListPayload.ID,
                (playerListPayload, context) -> {
                    ServerPlayerEntity admin = context.player();
                    List<ServerPlayerEntity> onlinePlayers = getOnlinePlayers(admin);

                    List<PlayerListPayload.PlayerInfo> playerInfos = onlinePlayers.stream()
                            .map(PlayerListPayload.PlayerInfo::fromPlayer)
                            .toList();
                    ServerPlayNetworking.send(admin, new PlayerListPayload(playerInfos));
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                TeleportRequestPayload.ID,
                ((teleportRequestPayload, context) -> {
                    ServerPlayerEntity player = context.player();
                    ServerPlayerEntity target = Objects.requireNonNull(player.getServer()).getPlayerManager().getPlayer(teleportRequestPayload.targetUuid());

                    if (target != null) {
                        try {
                            player.requestTeleport(target.getX(), target.getY(), target.getZ());
                            player.teleport(target.getServerWorld(), target.getX(), target.getY(), target.getZ(), PositionFlag.ROT, target.getYaw(), target.getPitch(), true);
                            ServerPlayNetworking.send(player, new TeleportResponsePayload("Teletransportado a " + target.getName().getString(), true));
                        } catch (Exception e) {
                            ServerPlayNetworking.send(player, new TeleportResponsePayload("Error: " + e.getMessage(), false));
                        }
                    } else {
                        ServerPlayNetworking.send(player, new TeleportResponsePayload("Jugador no encontrado", false));
                    }
                })
        );

        ServerPlayNetworking.registerGlobalReceiver(
                AdminActionPayload.ID,
                ((payload, context) -> {
                    ServerPlayerEntity admin = context.player();
                    if (!admin.hasPermissionLevel(2)) {
                        FeedbackUtil.sendFeedback(admin, "No tienes permisos para esta acción",
                                Formatting.RED);
                        return;
                    }

                    ServerPlayerEntity target = Objects.requireNonNull(admin.getServer()).getPlayerManager()
                            .getPlayer(payload.targetUuid());

                    if (target == null) {
                        FeedbackUtil.sendFeedback(admin, "Jugador objetivo no encontrado",
                                Formatting.RED);
                        return;
                    }

                    switch (payload.action()) {
                        case TELEPORT_TO_PLAYER ->
                                TeleportUtil.teleportAdminToPlayer(admin, target);
                        case TELEPORT_TO_ADMIN ->
                                TeleportUtil.teleportPlayerToAdmin(admin, target);
                        case TELEPORT_PLAYER_TO_PLAYER ->
                                TeleportUtil.teleportPlayerToPlayer(admin, target,
                                        payload.destinationPlayerUuid());
                        case TELEPORT_TO_SPAWN ->
                                TeleportUtil.teleportPlayerToSpawn(admin, target);
                        case TELEPORT_TO_COORDINATES ->
                                TeleportUtil.teleportToPosition(target, payload.customPosition(), target.getServerWorld());
                    }
                })
        );

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("tp4mods")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (player != null) {
                                if (Utils.isServerSide(player)) {
                                    ServerPlayNetworking.send(player, new PlayerListPayload(List.of()));
                                } else {
                                    player.sendMessage(Text.literal("Lo siento pero no hay un servidor activo").formatted(Formatting.GRAY));
                                }
                            }
                            return 1;
                        })
                        .then(CommandManager.literal("refreshSkins")
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    if (player != null) {
                                        ServerPlayNetworking.send(player, new RefreshSkinsPayload(true));
                                    }
                                    return 1;
                                })
                        )
                )
        );
        zero.mods.tpmanager.TpManager.init();
    }

    public static List<ServerPlayerEntity> getOnlinePlayers(ServerPlayerEntity player) {
        return Objects.requireNonNull(player.getServer()).getPlayerManager().getPlayerList();
    }
}
