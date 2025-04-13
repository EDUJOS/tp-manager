package zero.mods.tpmanager.fabric.client.screen;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import zero.mods.tpmanager.fabric.client.components.Positionable;
import zero.mods.tpmanager.fabric.client.components.ScrollPanel;
import zero.mods.tpmanager.fabric.payload.PlayerListPayload;
import java.util.List;
import java.util.UUID;
import zero.mods.tpmanager.fabric.util.PlayerSkinCache;
import net.minecraft.client.gui.Element;

public class AdminScreen extends Screen {
    public static final int CARD_WIDTH = 250;
    public static final int CARD_HEIGHT = 40;
    private static final int HEAD_SIZE = 40;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int SECONDARY_COLOR = 0x808080;
    private List<PlayerListPayload.PlayerInfo> players;
    private ScrollPanel scrollPanel;

    public AdminScreen(List<PlayerListPayload.PlayerInfo> players) {
        super(Text.translatable("gui.admin.title"));
        this.players = players;
        ClientPlayNetworking.send(new PlayerListPayload(List.of()));
    }

    public void updatePlayerList(List<PlayerListPayload.PlayerInfo> players) {
        this.players = players;
        clearAndInit();
    }

    public class PlayerCardWidget implements Drawable, Element, Positionable {
        private final PlayerListPayload.PlayerInfo player;
        private int x;
        private int y;
        private boolean focused;

        public PlayerCardWidget(int x, int y, PlayerListPayload.PlayerInfo player) {
            this.x = x;
            this.y = y;
            this.player = player;
        }

        public void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void setFocused(boolean focused) {
            this.focused = focused;
        }

        @Override
        public boolean isFocused() {
            return this.focused;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + CARD_WIDTH &&
                    mouseY >= y && mouseY <= y + CARD_HEIGHT;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            renderPlayerCard(context, player, this.x, this.y);

            if (isMouseOver(mouseX, mouseY)) {
                context.drawBorder(x, y, CARD_WIDTH, CARD_HEIGHT, 0xFFFFFF);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isMouseOver(mouseX, mouseY)) {
                openPlayerActionsScreen(player.uuid());
                return true;
            }
            return false;
        }
    }

    @Override
    protected void init() {
        super.init();
        int panelWidth = (int) (this.width * 0.8);
        int panelHeight = Math.max((int) (this.height * 0.65), 175);
        int panelX = (this.width - panelWidth) / 2;
        int panelY = 30;
        int itemHeight = CARD_HEIGHT;// + 10;

        this.scrollPanel = new ScrollPanel(panelX, panelY, panelWidth, panelHeight, itemHeight, ScrollPanel.Alignment.LEFT, 50);
        this.addDrawableChild(this.scrollPanel);
        this.renderPlayerList();

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.general.close"),
                button -> this.close()
        ).dimensions(width / 2 - 100, panelY + panelHeight + 10, 200, 20).build());
    }

    protected void renderPlayerList() {
        scrollPanel.clearEntries();
        // int yPos = 0;
        for (PlayerListPayload.PlayerInfo player : players) {
            PlayerCardWidget card = new PlayerCardWidget(0, 0, player);
            scrollPanel.addEntry(new ScrollPanel.Entry(card));
            // yPos += scrollPanel.itemHeight;
        }
    }

    private void renderPlayerCard(DrawContext context, PlayerListPayload.PlayerInfo player, int x, int y) {
        context.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, 0xFF121212);
        renderPlayerHead(context, x, y, player);
        drawPlayerInfo(context, x + HEAD_SIZE + 15, y + 10, player);
    }

    private void renderPlayerHead(DrawContext context, int x, int y, PlayerListPayload.PlayerInfo player) {
        Identifier skin = PlayerSkinCache.getTexture(
                player.uuid(),
                () -> new GameProfile(player.uuid(), player.name())
        );
        context.drawTexture(
                RenderLayer::getGuiTextured,
                skin,
                x, y,
                8, 8,
                HEAD_SIZE, HEAD_SIZE,
                8, 8,
                64, 64
        );
    }

    private void drawPlayerInfo(DrawContext context, int x, int y, PlayerListPayload.PlayerInfo player) {
        context.drawTextWithShadow(
                textRenderer,
                Text.literal(player.name()).formatted(Formatting.BOLD),
                x, y, TEXT_COLOR
        );
        String coordinates = String.format("%.1f  %.1f  %.1f", player.position().x(), player.position().y(), player.position().z());
        MutableText worldName = getDimensionName(player.position().worldId());
        context.drawTextWithShadow(
                textRenderer,
                worldName.append(": " + coordinates),
                x, y + 10, SECONDARY_COLOR
        );
    }

    private MutableText getDimensionName(String worldId) {
        return switch (worldId) {
            case "minecraft:overworld" -> Text.translatable("gui.general.dimension.overworld");
            case "minecraft:the_nether" -> Text.translatable("gui.general.dimension.nether");
            case "minecraft:the_end" -> Text.translatable("gui.general.dimension.end");
            default -> Text.translatable("gui.general.dimension.unknown");
        };
    }

    private void openPlayerActionsScreen(UUID targetUuid) {
        if (client != null) {
            client.setScreen(new PlayerActionsScreen(this, targetUuid, players));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFF);
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(null); // Cerrar y volver al juego
        }
    }
} 