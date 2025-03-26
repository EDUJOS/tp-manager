package zero.mods.fabric.tp4mods.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.UUID;
import zero.mods.fabric.tp4mods.client.ClientNetworking;
import zero.mods.fabric.tp4mods.payload.AdminActionPayload;
import zero.mods.fabric.tp4mods.payload.PlayerListPayload;

public class PlayerSelectScreen extends Screen {
    private final Screen parent;
    private final UUID sourceUuid;
    private final List<PlayerListPayload.PlayerInfo> players;

    public PlayerSelectScreen(Screen parent, UUID sourceUuid, List<PlayerListPayload.PlayerInfo> players) {
        super(Text.translatable("gui.playerSelect.title"));
        this.parent = parent;
        this.sourceUuid = sourceUuid;
        this.players = players;
    }

    @Override
    protected void init() {
        // Añadir botones para cada jugador que permitan teletransportar
        int y = 30;
        assert client != null;
        for (PlayerListPayload.PlayerInfo player : players) {
            UUID targetUuid = player.uuid();
            String playerName = player.name();
            addDrawableChild(ButtonWidget.builder(
                Text.literal(playerName),
                button -> {
                    ClientNetworking.sendAdminAction(new AdminActionPayload(
                        sourceUuid, 
                        AdminActionPayload.ActionType.TELEPORT_PLAYER_TO_PLAYER,
                        targetUuid,
                        null
                    ));
                    client.setScreen(parent);
                }
            ).dimensions(20, y, 200, 20).build());
            y += 25;
        }

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.general.back"),
                button -> {
                    assert client != null;
                    client.setScreen(parent);
                }
        ).dimensions(width / 2 - 100, height - 30, 200, 20).build());
    }
}