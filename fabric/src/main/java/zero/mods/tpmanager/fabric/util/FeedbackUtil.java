package zero.mods.tpmanager.fabric.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import zero.mods.tpmanager.fabric.payload.AdminActionPayload;

import java.util.Arrays;

public class FeedbackUtil {
    public static void sendFeedback(ServerPlayerEntity player, String message, Formatting... formatting) {
        Text text = Text.literal(message).formatted(formatting);
        player.sendMessage(text, false);
    }

    public static void sendTranslationFeedback(ServerPlayerEntity player, String key, Formatting[] formatting, Object... args) {
        Text text = args.length >= 1 ? Text.translatable(key, args).formatted(formatting) : Text.translatable(key).formatted(formatting);
        player.sendMessage(text, false);
    }

    public static void sendActionFeedback(ServerPlayerEntity admin, ServerPlayerEntity target, AdminActionPayload.ActionType actionType, ServerPlayerEntity... playerTarget) {
        switch (actionType) {
            case TELEPORT_TO_PLAYER -> sendTranslationFeedback(
                    admin,
                    "utils.sendFeedback.actions.admin.teleportToPlayer",
                    new Formatting[]{Formatting.GRAY},
                    Text.literal(target.getName().getString()).formatted(Formatting.YELLOW)
            );
            case TELEPORT_TO_ADMIN -> {
                sendTranslationFeedback(
                        admin,
                        "utils.sendFeedback.actions.admin.teleportToAdmin",
                        new Formatting[]{Formatting.WHITE},
                        Text.literal(target.getName().getString()).formatted(Formatting.YELLOW)
                );
                sendTranslationFeedback(
                        target,
                        "utils.sendFeedback.actions.target.teleportToAdmin",
                        new Formatting[]{Formatting.GOLD}
                );
            }
            case TELEPORT_TO_SPAWN -> {
                sendTranslationFeedback(
                        admin,
                        "utils.sendFeedback.actions.admin.teleportToSpawn",
                        new Formatting[]{Formatting.WHITE},
                        Text.literal(target.getName().getString()).formatted(Formatting.YELLOW)
                );
                sendTranslationFeedback(
                        target,
                        "utils.sendFeedback.actions.target.teleportToSpawn",
                        new Formatting[]{Formatting.GOLD}
                );
            }
            case TELEPORT_PLAYER_TO_PLAYER -> {
                sendTranslationFeedback(
                        admin,
                        "utils.sendFeedback.actions.admin.teleportPlayerToPlayer",
                        new Formatting[]{},
                        Text.literal(target.getName().getString()).formatted(Formatting.AQUA),
                        Text.literal(Arrays.stream(playerTarget).toList().getFirst().getName().getString()).formatted(Formatting.LIGHT_PURPLE)
                );
                sendTranslationFeedback(
                        target,
                        "utils.sendFeedback.actions.target.teleportPlayerToPlayer",
                        new Formatting[]{Formatting.WHITE},
                        Text.literal(Arrays.stream(playerTarget).toList().getFirst().getName().getString()).formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC)
                );
            }
            case TELEPORT_TO_COORDINATES -> {
                sendTranslationFeedback(
                        admin,
                        "utils.sendFeedback.actions.admin.teleportToCoordinates",
                        new Formatting[]{Formatting.WHITE},
                        Text.literal("x: " + target.getX()+ " y: " + target.getY() + " z: " + target.getZ()).formatted(Formatting.ITALIC)
                );
                sendTranslationFeedback(
                        target,
                        "utils.sendFeedback.actions.target.teleportToCoordinates",
                        new Formatting[]{Formatting.WHITE},
                        Text.literal("x: " + target.getX()+ " y: " + target.getY() + " z: " + target.getZ()).formatted(Formatting.ITALIC)
                );
            }
        }
    }
} 