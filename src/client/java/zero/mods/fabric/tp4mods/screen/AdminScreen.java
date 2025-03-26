package zero.mods.fabric.tp4mods.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import zero.mods.fabric.tp4mods.payload.PlayerListPayload;

import java.util.List;
import java.util.UUID;

public class AdminScreen extends Screen {
    private List<PlayerListPayload.PlayerInfo> players;

    public AdminScreen(List<PlayerListPayload.PlayerInfo> players) {
        super(Text.translatable("gui.admin.title"));
        this.players = players;
        ClientPlayNetworking.send(new PlayerListPayload(List.of()));
    }

    public void updatePlayerList(List<PlayerListPayload.PlayerInfo> players) {
        this.players = players;
        clearAndInit();
    }

    @Override
    protected void init() {
        int y = 30;
        for (PlayerListPayload.PlayerInfo player : players) {
            addDrawableChild(ButtonWidget.builder(
                Text.literal(player.name()),
                button -> openPlayerActionsScreen(player.uuid())
            ).dimensions(20, y, 200, 20).build());
            y += 25;
        }
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.general.close"),
                button -> close()
        ).dimensions(width / 2 - 100, height - 40, 200, 20).build());
    }

    private void openPlayerActionsScreen(UUID targetUuid) {
        if (client != null) {
            client.setScreen(new PlayerActionsScreen(this, targetUuid, players));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(null); // Cerrar y volver al juego
        }
    }
} 