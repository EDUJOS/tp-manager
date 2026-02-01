package zero.mods.tpmanager.fabric.client.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * ScrollPanel optimizado específicamente para listas de jugadores
 * Diseñado para ser más eficiente y tener mejor control del espaciado
 */
public class PlayerListScrollPanel implements Element {
    private boolean focused;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final List<PlayerListItem> items = new ArrayList<>();
    private int scrollPosition = 0;
    private boolean isDragging = false;
    private int lastMouseY;
    private final int itemHeight;
    private final int itemSpacing;
    private Consumer<Integer> onScroll;
    private Consumer<Integer> onItemClick;

    public PlayerListScrollPanel(int x, int y, int width, int height, int itemHeight, int itemSpacing) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.itemHeight = itemHeight;
        this.itemSpacing = itemSpacing;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dibujar fondo del panel
        context.fill(x, y, x + width, y + height, 0x80000000);
        
        // Configurar área de recorte para el contenido
        context.enableScissor(x, y, x + width, y + height);
        
        // Calcular qué elementos son visibles
        int firstVisibleIndex = Math.max(0, scrollPosition / (itemHeight + itemSpacing));
        int lastVisibleIndex = Math.min(items.size() - 1, 
            (scrollPosition + height) / (itemHeight + itemSpacing) + 1);
        
        // Renderizar solo los elementos visibles
        for (int i = firstVisibleIndex; i <= lastVisibleIndex && i < items.size(); i++) {
            PlayerListItem item = items.get(i);
            int itemY = y + (i * (itemHeight + itemSpacing)) - scrollPosition;
            
            // Solo renderizar si el elemento está realmente visible
            if (itemY + itemHeight >= y && itemY <= y + height) {
                item.render(context, x, itemY, width, itemHeight, mouseX, mouseY, delta);
            }
        }
        
        // Deshabilitar área de recorte
        context.disableScissor();
        
        // Dibujar barra de scroll si es necesario
        if (needsScrollbar()) {
            renderScrollbar(context);
        }
    }

    private boolean needsScrollbar() {
        return getContentHeight() > height;
    }

    private int getContentHeight() {
        return items.size() * (itemHeight + itemSpacing) - itemSpacing;
    }

    private void renderScrollbar(DrawContext context) {
        int contentHeight = getContentHeight();
        int scrollBarHeight = Math.max(20, height * height / contentHeight);
        int scrollBarY = y + (height - scrollBarHeight) * scrollPosition / (contentHeight - height);
        
        // Fondo de la barra
        context.fill(x + width - 8, y, x + width - 2, y + height, 0x40000000);
        
        // Barra de desplazamiento
        context.fill(x + width - 7, scrollBarY, x + width - 3, scrollBarY + scrollBarHeight, 0x80FFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }

        // Verificar si se hizo clic en la barra de scroll
        if (needsScrollbar() && mouseX >= x + width - 8 && mouseX <= x + width - 2) {
            isDragging = true;
            lastMouseY = (int) mouseY;
            return true;
        }

        // Verificar si se hizo clic en algún elemento
        int relativeY = (int) (mouseY - y + scrollPosition);
        int itemIndex = relativeY / (itemHeight + itemSpacing);
        
        if (itemIndex >= 0 && itemIndex < items.size()) {
            int itemTopY = itemIndex * (itemHeight + itemSpacing);
            int itemBottomY = itemTopY + itemHeight;
            
            if (relativeY >= itemTopY && relativeY <= itemBottomY) {
                // Llamar al callback de clic si está configurado
                if (onItemClick != null) {
                    onItemClick.accept(itemIndex);
                    return true;
                }
                return items.get(itemIndex).mouseClicked(mouseX, mouseY, button);
            }
        }

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isDragging) {
            isDragging = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDragging) {
            int deltaScroll = (int) (mouseY - lastMouseY);
            lastMouseY = (int) mouseY;
            
            int maxScroll = Math.max(0, getContentHeight() - height);
            int newScrollPosition = MathHelper.clamp(scrollPosition + deltaScroll, 0, maxScroll);
            
            if (newScrollPosition != scrollPosition) {
                scrollPosition = newScrollPosition;
                if (onScroll != null) {
                    onScroll.accept(scrollPosition);
                }
            }
            
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }

        int scrollAmount = (int) (-verticalAmount * (itemHeight + itemSpacing));
        int maxScroll = Math.max(0, getContentHeight() - height);
        int newScrollPosition = MathHelper.clamp(scrollPosition + scrollAmount, 0, maxScroll);

        if (newScrollPosition != scrollPosition) {
            scrollPosition = newScrollPosition;
            if (onScroll != null) {
                onScroll.accept(scrollPosition);
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public boolean isFocused() {
        return this.focused;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public void addItem(PlayerListItem item) {
        items.add(item);
    }

    public void clearItems() {
        items.clear();
        scrollPosition = 0;
    }

    public void setOnScroll(Consumer<Integer> onScroll) {
        this.onScroll = onScroll;
    }
    
    public void setOnItemClick(Consumer<Integer> onItemClick) {
        this.onItemClick = onItemClick;
    }
    
    public List<PlayerListItem> getItems() {
        return items;
    }

    /**
     * Interfaz para elementos de la lista de jugadores
     */
    public interface PlayerListItem {
        void render(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float delta);
        boolean mouseClicked(double mouseX, double mouseY, int button);
    }
}
