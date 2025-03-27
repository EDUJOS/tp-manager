package zero.mods.fabric.tp4mods.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class PlayerSkinCache {
    private static final Map<UUID, Identifier> SKIN_CACHE = new HashMap<>();
    private static final Identifier DEFAULT_SKIN = Identifier.of("textures/entity/player/wide/steve.png");

    public static Identifier getTexture(UUID uuid, Supplier<GameProfile> profileSupplier) {
        return SKIN_CACHE.computeIfAbsent(uuid, id -> {
            try {
                GameProfile profile = profileSupplier.get();
                SkinTextures textures = MinecraftClient.getInstance()
                        .getSkinProvider()
                        .getSkinTextures(profile);
                return textures.texture();
            } catch (Exception e) {
                return DEFAULT_SKIN;
            }
        });
    }

    public static void clearCacheEntry(UUID uuid) {
        SKIN_CACHE.remove(uuid);
    }

    public static void clearCache() {
        SKIN_CACHE.clear();
    }
}
