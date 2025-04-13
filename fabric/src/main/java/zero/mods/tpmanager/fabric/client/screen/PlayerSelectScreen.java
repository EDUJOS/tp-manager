package zero.mods.tpmanager.fabric.client.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.UUID;
import zero.mods.tpmanager.fabric.client.ClientNetworking;
import zero.mods.tpmanager.fabric.client.components.ScrollPanel;
import zero.mods.tpmanager.fabric.payload.AdminActionPayload;
import zero.mods.tpmanager.fabric.payload.PlayerListPayload;

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
        super.init();
        // Añadir botones para cada jugador que permitan teletransportar
        //int y = 30;
        int panelWidth = (int) (this.width * 0.8);
        int panelHeight = Math.max((int) (this.height * 0.65), 175);
        int panelX = (this.width - panelWidth) / 2;
        int panelY = 30;
        int itemHeight = 20;// + 10;
        int itemWidth = 150;

        ScrollPanel scrollPanel = new ScrollPanel(panelX, panelY, panelWidth, panelHeight, itemHeight, ScrollPanel.Alignment.CENTER, 0);
        assert client != null;
        // int yPos = 0;
        scrollPanel.clearEntries();
        for (PlayerListPayload.PlayerInfo player : players) {
            UUID targetUuid = player.uuid();
            String playerName = player.name();
            ButtonWidget button = ButtonWidget.builder(
                    Text.literal(playerName),
                    btn -> {
                        ClientNetworking.sendAdminAction(new AdminActionPayload(
                                sourceUuid,
                                AdminActionPayload.ActionType.TELEPORT_PLAYER_TO_PLAYER,
                                targetUuid,
                                null
                        ));
                        close();
                    }
            ).dimensions(0, 0, itemWidth, 20).build();
            scrollPanel.addEntry(new ScrollPanel.Entry(button));
            // yPos += itemHeight;

        }

        this.addDrawableChild(scrollPanel);

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.general.back"),
                button -> {
                    assert client != null;
                    client.setScreen(parent);
                }
        ).dimensions(width / 2 - 100, height - 30, 200, 20).build());
    }
}