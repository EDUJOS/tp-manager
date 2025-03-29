package zero.mods.tpmanager.fabric.client.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.util.math.MathHelper;
import java.util.List;

import java.util.ArrayList;

public class ScrollPanel extends ElementListWidget<ScrollPanel.Entry> {
    private final List<Entry> children = new ArrayList<>();
    private final int minHeight;

    public ScrollPanel(int x, int y, int width, int height, int minHeight) {
        super(MinecraftClient.getInstance(), width, height, y, y + height, 20);
        this.minHeight = minHeight;
        this.setPosition(x, y);
    }

    private int getPanelTop() {
        return this.getY(); // Posición Y inicial del panel
    }

    private int getScrollbarPositionX() {
        return this.getRight() - 6;
    }

    public int getMaxScroll() {
        //return Math.max(0, this.getMaxScrollY() - this.height + this.minHeight);
        return Math.max(0, this.getEntryCount() * this.itemHeight - this.height + this.minHeight);
    }


    @Override
    public void renderList(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x66000000);
        super.renderList(context, mouseX, mouseY, delta);

        if (this.getMaxScroll() > 0) {
            int scrollbarHeight = (int) ((float) this.height * this.height / this.getMaxScroll());
            scrollbarHeight = MathHelper.clamp(scrollbarHeight, 32, this.height);

            int scrollbarY = (int) (this.getScrollY() * (this.height - scrollbarHeight) / this.getMaxScroll());
            context.fill(this.getScrollbarPositionX(), this.getPanelTop() + scrollbarY,
                    this.getScrollbarPositionX() + 4, this.getPanelTop() + scrollbarY + scrollbarHeight,
                    0xFFAAAAAA
            );
        }
    }

    public static class Entry extends ElementListWidget.Entry<Entry> {
        private final Element element;

        public Entry(Element element) {
            this.element = element;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
            if (this.element instanceof Drawable drawable) {
                drawable.render(context, mouseX, mouseY, delta);
            }
        }

        @Override
        public List<? extends Element> children() {
            return List.of(this.element);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return this.element instanceof Selectable ? List.of((Selectable) this.element) : List.of();
        }
    }
}
