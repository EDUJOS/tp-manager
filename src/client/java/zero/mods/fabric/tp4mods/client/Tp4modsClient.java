package zero.mods.fabric.tp4mods.client;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import zero.mods.fabric.tp4mods.Tp4Mods;
import zero.mods.fabric.tp4mods.payload.PlayerListPayload;
import zero.mods.fabric.tp4mods.payload.RefreshSkinsPayload;
import zero.mods.fabric.tp4mods.payload.TeleportResponsePayload;
import zero.mods.fabric.tp4mods.screen.AdminScreen;
import zero.mods.fabric.tp4mods.util.PlayerSkinCache;

import java.util.List;

import static zero.mods.fabric.tp4mods.client.PlayerHeadManager.client;

public class Tp4modsClient implements ClientModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static Item modItem;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing TP 4 Mods (Client)");
        modItem = Registries.ITEM.get(Identifier.of(Tp4Mods.MOD_ID, "tp_manager_item"));
        registerNetworkReceivers();
    }

    private void registerNetworkReceivers () {
        ClientPlayNetworking.registerGlobalReceiver(
                PlayerListPayload.ID,
                (payload, context) -> {
                    List<PlayerListPayload.PlayerInfo> players = payload.players();
                    client.execute(() -> {
                        if (client.currentScreen instanceof AdminScreen) {
                            ((AdminScreen) client.currentScreen).updatePlayerList(players);
                        } else {
                            openTeleportGui(players);
                        }
                    });
                });

        ClientPlayNetworking.registerGlobalReceiver(
                TeleportResponsePayload.ID,
                (payload, context) -> {
                    Boolean success = payload.success();
                    String message = payload.message();
                    client.execute(() -> {
                        if (MinecraftClient.getInstance().player != null) {
                            Text text = Text.literal(message).formatted(success ? Formatting.GREEN : Formatting.RED);
                            MinecraftClient.getInstance().player.sendMessage(text, false);
                        }
                    });
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                RefreshSkinsPayload.ID,
                ((refreshSkinsPayload, context) -> {
                    if (refreshSkinsPayload.refreshSkins()) {
                        PlayerEntity player = context.player();
                        PlayerSkinCache.clearCache();
                        player.sendMessage(Text.literal("Se ha limpiado la cache de skins"), false);
                    }
                })
        );
    }

    public static void openTeleportGui(List<PlayerListPayload.PlayerInfo> players) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen == null) {
            client.setScreen(new AdminScreen(players));
        }
    }
}
