package zero.mods.tpmanager.fabric.client;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import com.mojang.authlib.GameProfile;

public class PlayerHeadManager {
    private static final Map<UUID, Identifier> playerHeadTextures = new HashMap<>();
    static final MinecraftClient client = MinecraftClient.getInstance();

    public static Identifier getPlayerHead(UUID playerUuid, String playerName) {
        return playerHeadTextures.computeIfAbsent(playerUuid, uuid -> {
            GameProfile profile = new GameProfile(uuid, playerName);
            return client.getSkinProvider().getSkinTextures(profile).texture();
        });
    }
} 