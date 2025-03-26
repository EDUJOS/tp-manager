package zero.mods.fabric.tp4mods.screen;

import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import java.util.List;
import zero.mods.fabric.tp4mods.client.PlayerHeadManager;

import java.util.Collections;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import zero.mods.fabric.tp4mods.payload.PlayerListPayload;

public class ScrollingPlayerList extends ElementListWidget<ScrollingPlayerList.PlayerEntry> {
    private final MinecraftClient client;
    private final Screen parent;
    private final List<PlayerListPayload.PlayerInfo> players;

    public ScrollingPlayerList(MinecraftClient client, Screen parent, int x, int y, int width, int height,
            int entryHeight, List<PlayerListPayload.PlayerInfo> players) {
        super(client, width, height, y, entryHeight);
        this.setX(x);
        this.client = client;
        this.parent = parent;
        this.players = players;
        
        players.forEach(player -> addEntry(new PlayerEntry(player)));
    }

    private void renderPlayerEntry(DrawContext context, PlayerListPayload.PlayerInfo player,
                                   int x, int y, int width, int height, int mouseX, int mouseY, float delta) {
        // Fondo
        context.fill(x, y, x + width, y + height, 0x80000000);
        
        // Cabeza del jugador
        Identifier headTexture = PlayerHeadManager.getPlayerHead(player.uuid(), player.name());
        context.drawTexture(
                RenderLayer::getEntityCutoutNoCull,
            headTexture,
            x + 2, y + 2,    // x, y
            0.0f, 0.0f,      // u, v
            28, 28,          // width, height
            64, 64           // textureWidth, textureHeight
        );

        // Nombre del jugador
        context.drawTextWithShadow(client.textRenderer,
            player.name(),
            x + 34, y + 2,
            0xFFFFFF);

        // Coordenadas
//        String coords = String.format("%.1f, %.1f, %.1f",
//            player.position().x(),
//            player.position().y(),
//            player.position().z());
//        context.drawTextWithShadow(client.textRenderer,
//            coords,
//            x + 34, y + 14,
//            0xAAAAAA);
//
//        // Mundo
//        context.drawTextWithShadow(client.textRenderer,
//            player.worldName(),
//            x + 34, y + 26,
//            0xAAAAAA);

        // Botón Administrar
        ButtonWidget button = ButtonWidget.builder(
            Text.literal("Administrar"),
            btn -> openPlayerActions(player)
        ).dimensions(x + width - 70, y + 6, 60, 20).build();
        button.render(context, mouseX, mouseY, delta);
    }

    private void openPlayerActions(PlayerListPayload.PlayerInfo player) {
        MinecraftClient.getInstance().setScreen(
            new PlayerActionsScreen(this.parent, player.uuid(), players));
    }

    public class PlayerEntry extends ElementListWidget.Entry<PlayerEntry> {
        private final PlayerListPayload.PlayerInfo player;

        public PlayerEntry(PlayerListPayload.PlayerInfo player) {
            this.player = player;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, 
                int mouseX, int mouseY, boolean hovered, float tickDelta) {
            renderPlayerEntry(context, player, x, y, entryWidth, entryHeight, mouseX, mouseY, tickDelta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                openPlayerActions(player);
                return true;
            }
            return false;
        }

        @Override
        public List<? extends Element> children() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return Collections.emptyList();
        }
    }
} 