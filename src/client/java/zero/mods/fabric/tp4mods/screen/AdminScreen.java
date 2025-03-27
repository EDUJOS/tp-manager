package zero.mods.fabric.tp4mods.screen;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import zero.mods.fabric.tp4mods.payload.PlayerListPayload;

import java.util.List;
import java.util.UUID;
import zero.mods.fabric.tp4mods.util.PlayerSkinCache;

public class AdminScreen extends Screen {
    private static final int CARD_WIDTH = 250;
    private static final int CARD_HEIGHT = 40;
    private static final int HEAD_SIZE = 40;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int SECONDARY_COLOR = 0x808080;
    private static final int VISIBLE_ROWS = 5;
    private List<PlayerListPayload.PlayerInfo> players;
    private int scrollOffset;

    public AdminScreen(List<PlayerListPayload.PlayerInfo> players) {
        super(Text.translatable("gui.admin.title"));
        this.players = players;
        this.scrollOffset = 0;
        ClientPlayNetworking.send(new PlayerListPayload(List.of()));
    }

    public void updatePlayerList(List<PlayerListPayload.PlayerInfo> players) {
        this.players = players;
        clearAndInit();
    }

    @Override
    protected void init() {
        this.renderPlayerList();
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.general.close"),
                button -> this.close()
        ).dimensions(width / 2 - 100, height - 40, 200, 20).build());
    }

    protected void renderPlayerList() {
        int startIndex = Math.max(0, Math.min(scrollOffset, players.size() - 5)); // scrollOffset; ----  Math.min(scrollOffset, players.size());
        // int endIndex = Math.min(startIndex + VISIBLE_ROWS, players.size());
        int yPos = 30;

        for (int i = startIndex; i < startIndex + 5 && i < players.size(); i++) { // (int i = startIndex; i < endIndex; i++) {
            PlayerListPayload.PlayerInfo player = players.get(i);
            this.addDrawableChild(createPlayerCardButton(20, yPos, player)); //this.addPlayerButton(player, 20, yPos);
            yPos += CARD_HEIGHT + 10; // yPos += 25;
        }
    }

    private ButtonWidget createPlayerCardButton(int x, int y, PlayerListPayload.PlayerInfo player) {
        return ButtonWidget.builder(Text.empty(), button -> openPlayerActionsScreen(player.uuid()))
                .dimensions(x, y, CARD_WIDTH, CARD_HEIGHT)
                .build();
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
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        this.scrollOffset = Math.max(0, Math.min(
                scrollOffset - (int) vertical,
                players.size() - VISIBLE_ROWS
        ));
        this.clearAndInit();
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        int startIndex = Math.max(0, Math.min(scrollOffset, players.size() - 5));
        int yPos = 30;

        for (int i = startIndex; i < startIndex + 5 && i < players.size(); i++) {
            PlayerListPayload.PlayerInfo player = players.get(i);
            renderPlayerCard(context, player, 20, yPos);
            yPos += CARD_HEIGHT + 10;
        }

        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFF);
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(null); // Cerrar y volver al juego
        }
    }
} 