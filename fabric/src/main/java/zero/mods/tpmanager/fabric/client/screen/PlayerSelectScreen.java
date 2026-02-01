package zero.mods.tpmanager.fabric.client.screen;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import zero.mods.tpmanager.fabric.client.ClientNetworking;
import zero.mods.tpmanager.fabric.client.components.PlayerListScrollPanel;
import zero.mods.tpmanager.fabric.payload.AdminActionPayload;
import zero.mods.tpmanager.fabric.payload.PlayerListPayload;
import zero.mods.tpmanager.fabric.util.PlayerSkinCache;

import java.util.List;
import java.util.UUID;

public class PlayerSelectScreen extends Screen {
    // Constantes para las tarjetas de jugador (igual que AdminScreen)
    public static final int CARD_WIDTH = 250;
    public static final int CARD_HEIGHT = 40;
    private static final int HEAD_SIZE = 40;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int SECONDARY_COLOR = 0x808080;
    
    private final Screen parent;
    private final UUID sourceUuid;
    private final List<PlayerListPayload.PlayerInfo> players;
    private PlayerListScrollPanel playerListScrollPanel;

    public PlayerSelectScreen(Screen parent, UUID sourceUuid, List<PlayerListPayload.PlayerInfo> players) {
        super(Text.translatable("gui.playerSelect.title"));
        this.parent = parent;
        this.sourceUuid = sourceUuid;
        this.players = players;
    }

    /**
     * Implementación de PlayerListItem para las tarjetas de selección
     */
    public class PlayerSelectCardItem implements PlayerListScrollPanel.PlayerListItem {
        private final PlayerListPayload.PlayerInfo player;
        private final int panelX;

        public PlayerSelectCardItem(PlayerListPayload.PlayerInfo player, int panelX) {
            this.player = player;
            this.panelX = panelX;
        }

        @Override
        public void render(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float delta) {
            // Centrar la tarjeta en el ancho del panel
            int cardX = x + (width - CARD_WIDTH) / 2;
            
            // Renderizar la tarjeta del jugador
            renderPlayerCard(context, player, cardX, y);
            
            // Mostrar borde si el mouse está sobre la tarjeta
            if (isMouseOverCard(mouseX, mouseY, cardX, y)) {
                context.drawBorder(cardX, y, CARD_WIDTH, CARD_HEIGHT, 0xFFFFFF);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            // La lógica de clic se maneja en el panel principal
            return false;
        }
        
        private boolean isMouseOverCard(double mouseX, double mouseY, int cardX, int cardY) {
            return mouseX >= cardX && mouseX <= cardX + CARD_WIDTH &&
                   mouseY >= cardY && mouseY <= cardY + CARD_HEIGHT;
        }
        
        public PlayerListPayload.PlayerInfo getPlayer() {
            return player;
        }
    }

    @Override
    protected void init() {
        super.init();
        
        int altPanelWidth = width - 100;
        // Optimizar altura del panel para tarjetas de jugador
        int altPanelHeight = height - 75; // Espacio eficiente para tarjetas
        int panelX = (this.width - altPanelWidth) / 2;
        int panelY = 30;

        // Usar PlayerListScrollPanel con tarjetas de jugador
        this.playerListScrollPanel = new PlayerListScrollPanel(panelX, panelY, altPanelWidth, altPanelHeight, CARD_HEIGHT, 2);
        
        // Configurar el callback de clic para manejar selección de jugador
        this.playerListScrollPanel.setOnItemClick(itemIndex -> {
            if (itemIndex >= 0 && itemIndex < players.size()) {
                PlayerListPayload.PlayerInfo selectedPlayer = players.get(itemIndex);
                // Enviar acción de teletransporte
                ClientNetworking.sendAdminAction(new AdminActionPayload(
                        sourceUuid,
                        AdminActionPayload.ActionType.TELEPORT_PLAYER_TO_PLAYER,
                        selectedPlayer.uuid(),
                        null
                ));
                close();
            }
        });
        
        // Poblar las tarjetas de jugador
        renderPlayerCards();

        // Botón Volver posicionado en la parte inferior
        int backButtonY = this.height - 30;
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.general.back"),
                button -> {
                    assert client != null;
                    client.setScreen(parent);
                }
        ).dimensions(this.width / 2 - 100, backButtonY, 200, 20).build());
    }

    /**
     * Renderizar las tarjetas de jugador en el panel
     */
    private void renderPlayerCards() {
        playerListScrollPanel.clearItems();
        int panelWidth = width - 100;
        int panelX = (this.width - panelWidth) / 2;

        for (PlayerListPayload.PlayerInfo player : players) {
            PlayerSelectCardItem cardItem = new PlayerSelectCardItem(player, panelX);
            playerListScrollPanel.addItem(cardItem);
        }
    }

    /**
     * Renderizar tarjeta de jugador (igual que AdminScreen)
     */
    private void renderPlayerCard(DrawContext context, PlayerListPayload.PlayerInfo player, int x, int y) {
        context.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, 0xFF121212);
        renderPlayerHead(context, x, y, player);
        drawPlayerInfo(context, x + HEAD_SIZE + 15, y + 10, player);
    }

    /**
     * Renderizar cabeza del jugador (igual que AdminScreen)
     */
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

    /**
     * Dibujar información del jugador (igual que AdminScreen)
     */
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

    /**
     * Obtener nombre de dimensión (igual que AdminScreen)
     */
    private MutableText getDimensionName(String worldId) {
        return switch (worldId) {
            case "minecraft:overworld" -> Text.translatable("gui.general.dimension.overworld");
            case "minecraft:the_nether" -> Text.translatable("gui.general.dimension.nether");
            case "minecraft:the_end" -> Text.translatable("gui.general.dimension.end");
            default -> Text.translatable("gui.general.dimension.unknown");
        };
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFF);
        playerListScrollPanel.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (playerListScrollPanel.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (playerListScrollPanel.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (playerListScrollPanel.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (playerListScrollPanel.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }
}
