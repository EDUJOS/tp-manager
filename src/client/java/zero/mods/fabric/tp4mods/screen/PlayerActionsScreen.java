package zero.mods.fabric.tp4mods.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import zero.mods.fabric.tp4mods.payload.AdminActionPayload;
import zero.mods.fabric.tp4mods.client.ClientNetworking;
import zero.mods.fabric.tp4mods.payload.PlayerListPayload;

import java.util.List;
import java.util.UUID;

public class PlayerActionsScreen extends Screen {
    private final Screen parent;
    private final UUID targetUuid;
    private final List<PlayerListPayload.PlayerInfo> players;

    public PlayerActionsScreen(Screen parent, UUID targetUuid, List<PlayerListPayload.PlayerInfo> players) {
        super(Text.translatable("gui.playerActions.title"));
        this.parent = parent;
        this.targetUuid = targetUuid;
        this.players = players;
    }

    @Override
    protected void init() {
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 24;
        int startY = height / 2 - (spacing * 2);

        // Teletransportarse al jugador
        addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.playerActions.teleportToPlayer"),
            button -> sendAdminAction(AdminActionPayload.ActionType.TELEPORT_TO_PLAYER)
        ).dimensions(width / 2 - buttonWidth / 2, startY, buttonWidth, buttonHeight).build());

        // Teletransportar al Admin
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.playerActions.teleportToAdmin"),
                button -> sendAdminAction(AdminActionPayload.ActionType.TELEPORT_TO_ADMIN)
        ).dimensions(width / 2 - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight).build());

        // Teletransportar al spawn
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.playerActions.teleportToSpawn"),
            button -> sendAdminAction(AdminActionPayload.ActionType.TELEPORT_TO_SPAWN)
        ).dimensions(width / 2 - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight).build());

        // Teletransportar a otro jugador
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.playerActions.teleportPlayerToPlayer"),
            button -> {
                assert client != null;
                client.setScreen(new PlayerSelectScreen(this, targetUuid, players));
            }
        ).dimensions(width / 2 - buttonWidth / 2, startY + spacing * 3, buttonWidth, buttonHeight).build());

        // Botón Volver
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.general.back"),
            button -> {
                assert client != null;
                client.setScreen(parent);
            }
        ).dimensions(width / 2 - buttonWidth / 2, startY + spacing * 4, buttonWidth, buttonHeight).build());
    }

    private void sendAdminAction(AdminActionPayload.ActionType actionType) {
        AdminActionPayload payload = null;
        if (actionType == AdminActionPayload.ActionType.TELEPORT_TO_PLAYER ||
            actionType == AdminActionPayload.ActionType.TELEPORT_TO_ADMIN ||
            actionType == AdminActionPayload.ActionType.TELEPORT_TO_SPAWN) {
            payload = new AdminActionPayload(targetUuid, actionType, null, null);
        }
        ClientNetworking.sendAdminAction(payload);
        assert client != null;
        client.setScreen(null);
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }
} 