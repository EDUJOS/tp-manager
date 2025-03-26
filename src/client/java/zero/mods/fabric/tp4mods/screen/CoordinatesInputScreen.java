package zero.mods.fabric.tp4mods.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import zero.mods.fabric.tp4mods.client.ClientNetworking;
import zero.mods.fabric.tp4mods.payload.AdminActionPayload;
import zero.mods.fabric.tp4mods.payload.Position;
import java.util.UUID;
import zero.mods.fabric.tp4mods.util.Utils;

public class CoordinatesInputScreen extends Screen {
    private final Screen parent;
    private final UUID targetUuid;
    private TextFieldWidget xField, yField, zField;

    public CoordinatesInputScreen(Screen parent, UUID targetUuid) {
        super(Text.translatable("gui.coordinatesInput.title"));
        this.parent = parent;
        this.targetUuid = targetUuid;
    }

    @Override
    protected void init() {
        int y = height / 2 - 30;
        xField = addDrawableChild(new TextFieldWidget(textRenderer, width / 2 - 90, y, 50, 20, Text.literal("X")));
        yField = addDrawableChild(new TextFieldWidget(textRenderer, width / 2 - 25, y, 50, 20, Text.literal("Y")));
        zField = addDrawableChild(new TextFieldWidget(textRenderer, width / 2 + 40, y, 50, 20, Text.literal("Z")));

        addDrawableChild(ButtonWidget.builder(Text.literal("Teletransportar"), button -> {
            try {
                double x = Double.parseDouble(xField.getText());
                double yPos = Double.parseDouble(yField.getText());
                double z = Double.parseDouble(zField.getText());
                Utils.LOGGER.info("Coordenadas: x: {} y: {} z: {}", x, yPos, z);
                Position pos = new Position("minecraft:overworld", x, yPos, z, 0.0f, 0.0f);
                Utils.LOGGER.info("Posición destino: {}", pos);
                ClientNetworking.sendAdminAction(new AdminActionPayload(
                    targetUuid, AdminActionPayload.ActionType.TELEPORT_TO_COORDINATES, null, pos));
                assert client != null;
                client.setScreen(parent);
            } catch (NumberFormatException e) {
                // Ignorar entrada inválida
            }
        }).dimensions(width / 2 - 100, height / 2 + 30, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Cancelar"), 
            button -> {
                assert client != null;
                client.setScreen(parent);
            }
        ).dimensions(width / 2 - 100, height / 2 + 60, 200, 20).build());
    }
} 