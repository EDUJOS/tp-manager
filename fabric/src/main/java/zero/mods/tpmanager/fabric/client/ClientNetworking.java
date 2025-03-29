package zero.mods.tpmanager.fabric.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import zero.mods.tpmanager.fabric.payload.AdminActionPayload;
import zero.mods.tpmanager.fabric.payload.PlayerListPayload;
import zero.mods.tpmanager.fabric.client.screen.AdminScreen;
import java.util.ArrayList;

public class ClientNetworking {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(
                PlayerListPayload.ID,
                (payload, context) -> context.client().execute(() ->
                    context.client().setScreen(new AdminScreen(payload.players()))
                )
        );
    }

    public static void requestPlayerList() {
        ClientPlayNetworking.send(new PlayerListPayload(new ArrayList<>()));
    }

    public static void sendAdminAction(AdminActionPayload payload) {
        ClientPlayNetworking.send(payload);
    }
} 