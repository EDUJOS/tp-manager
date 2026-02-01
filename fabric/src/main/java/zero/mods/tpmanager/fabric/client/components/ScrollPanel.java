package zero.mods.tpmanager.fabric.client.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import zero.mods.tpmanager.fabric.client.TpManagerFabricClient;

import java.util.List;

public class ScrollPanel extends ElementListWidget<ScrollPanel.Entry> {
    public int itemHeight;

    public enum Alignment { LEFT, CENTER, RIGHT }
    private final Alignment contentAlignment;
    private final int panelWidth;
    private int scrollXPosition;
    private int padding;

    public ScrollPanel(int x, int y, int width, int height, int itemHeight, Alignment alignment, int padding) {
        super(MinecraftClient.getInstance(), width, height, y, y + height, itemHeight);
        this.contentAlignment = alignment;
        this.itemHeight = itemHeight;
        this.setPosition(x, y);
        this.panelWidth = width;
        this.padding = padding;
    }

    @Override
    public int addEntry(Entry entry) {
        int index = super.addEntry(entry);
        entry.setParentList(this);
        return index;
    }

    public void clearEntries() {
        this.children().clear();
    }

    public int getMaxScroll() {
        return Math.max(0, (this.getEntryCount() * this.itemHeight) - this.height);
    }

    // @Override
    // public int getRowTop(int index) {
    //     //return this.getY() + (index * this.itemHeight);
    //     return (int)(this.getY() - (int)this.getScrollY() + index * this.itemHeight + this.headerHeight) + 10;
    // }

//    @Override
//    protected void drawMenuListBackground(DrawContext context) {
//    }
//
//    @Override
//    protected void drawHeaderAndFooterSeparators(DrawContext context) {
//    }

    @Override
    protected void drawScrollbar(DrawContext context) {
        Identifier SCROLLER_TEXTURE = Identifier.ofVanilla("widget/scroller");
        Identifier SCROLLER_BACKGROUND_TEXTURE = Identifier.ofVanilla("widget/scroller_background");
        if (this.overflows()) {
            int i = this.scrollXPosition + panelWidth - 6; // this.getRight();
            int j = this.getScrollbarThumbHeight();
            int k = this.getScrollbarThumbY();
            context.drawGuiTexture(RenderLayer::getGuiTextured, SCROLLER_BACKGROUND_TEXTURE, i, this.getY(), 6, this.getHeight());
            context.drawGuiTexture(RenderLayer::getGuiTextured, SCROLLER_TEXTURE, i, k, 6, j);
        }
    }

    @Override
    public void renderList(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderList(context, mouseX, mouseY, delta);
        // if (this.getMaxScroll() > 0) {
        //     int scrollbarHeight = (int) ((float) this.height * this.height / (this.getMaxScroll() + this.height));
        //     scrollbarHeight = MathHelper.clamp(scrollbarHeight, 32, this.height);

        //     int scrollbarY = (int) (this.getScrollY() * (this.height - scrollbarHeight) / this.getMaxScroll());
        //     context.fill(this.getRight(), this.getY() + scrollbarY,
        //             this.getRight() - 2, this.getY() + scrollbarY + scrollbarHeight,
        //             0xFFAAAAAA
        //     );
        //     TpManagerFabricClient.LOGGER.info("Scrollbar info:\nX position: {}px, width: {}px", this.getX(), this.getX() + this.width);
        // }
    }

    public static class Entry extends ElementListWidget.Entry<Entry> {
        private final Element element;
        private ScrollPanel parentList;

        public Entry(Element element) {
            this.element = element;
        }

        void setParentList(ScrollPanel parent) {
            this.parentList = parent;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return this.element.isMouseOver(mouseX, mouseY);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
            int contentX = x;
            int absoluteX = this.parentList.getX();
            //TpManagerFabricClient.LOGGER.info("AbsoluteX: {}, Entry width: {}", absoluteX, entryWidth);
            if (parentList != null) {
                this.parentList.scrollXPosition = absoluteX;
                contentX = switch (parentList.contentAlignment) {
                    case CENTER -> absoluteX + (entryWidth / 2) - 10;// + (entryWidth - AdminScreen.CARD_WIDTH) / 2;
                    case RIGHT -> absoluteX + (parentList.panelWidth - entryWidth);// + entryWidth - AdminScreen.CARD_WIDTH;
                    default -> absoluteX + this.parentList.padding; // + 40;
                };
            }
            //TpManagerFabricClient.LOGGER.info("ContentX: {}", contentX);
            if (this.element instanceof ButtonWidget button) {
                button.setY(y);
                button.setX(contentX);
                button.render(context, mouseX, mouseY, delta);
            }
            if (this.element instanceof Positionable pos) {
                pos.setPosition(contentX, y);
            }
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
//    @Override
//    public void setScrollY(double value) {
//        if (this.getMaxScroll() <= 0) {
//            super.setScrollY(0);
//        } else {
//            super.setScrollY(value);
//        }
//    }
}
