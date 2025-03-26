package zero.mods.fabric.tp4mods.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import zero.mods.fabric.tp4mods.payload.AdminActionPayload;
import zero.mods.fabric.tp4mods.payload.PlayerListPayload;
import zero.mods.fabric.tp4mods.screen.AdminScreen;
import java.util.ArrayList;

public class ClientNetworking {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(
                PlayerListPayload.ID,
                (payload, context) -> context.client().execute(() ->
                    context.client().setScreen(new AdminScreen(payload.players()))
                )
        );

//        ClientPlayNetworking.registerGlobalReceiver(
//                TeleportResponsePayload.ID,
//                (payload, context) -> context.client().execute(() ->
//                    Objects.requireNonNull(context.client().player).sendMessage(Text.literal(payload.message()), false)
//                )
//        );
    }

    public static void requestPlayerList() {
        ClientPlayNetworking.send(new PlayerListPayload(new ArrayList<>()));
    }

    public static void sendAdminAction(AdminActionPayload payload) {
        ClientPlayNetworking.send(payload);
    }
} 