package zero.mods.tpmanager.fabric.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import zero.mods.tpmanager.fabric.payload.PlayerListPayload;
import zero.mods.tpmanager.fabric.payload.TeleportRequestPayload;

import java.util.List;

public class TeleportGui extends Screen {
    private List<PlayerListPayload.PlayerInfo> players;

    public TeleportGui() {
        super(Text.of("Jugadores Online"));
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
            this.addDrawableChild(ButtonWidget.builder(Text.of("TP a " + player.name()), button -> {
                ClientPlayNetworking.send(new TeleportRequestPayload(player.uuid()));
                this.close();
            }).position(20, y).size(120, 20).build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}
