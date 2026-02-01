package zero.mods.tpmanager.fabric.client.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Componente ScrollPanel mejorado que puede contener diferentes tipos de elementos
 * incluyendo Drawable, Positionable, ButtonWidget y cualquier ClickableWidget
 */
public class AltScrollPanel implements Element {
    private boolean focused;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private int contentHeight;
    private final List<Object> children = new ArrayList<>();
    private int scrollPosition = 0;
    private boolean isDragging = false;
    private int lastMouseY;
    private boolean hasScrollBar;
    private Consumer<Integer> onScroll;

    public AltScrollPanel(int x, int y, int width, int height, int contentHeight) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.contentHeight = contentHeight;
        this.hasScrollBar = contentHeight > height;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dibujar fondo del panel
        context.fill(x, y, x + width, y + height, 0x80000000);
        
        // Configurar área de recorte
        context.enableScissor(x, y, x + width, y + height);
        
        // Renderizar cada elemento con el desplazamiento aplicado
        for (Object child : children) {
            if (child instanceof Drawable drawable) {
                if (child instanceof Positionable positionable) {
                    // Para elementos que implementan ambos Drawable y Positionable
                    int originalY = -1;
                    
                    // Técnicas para obtener/establecer la posición Y varían según el tipo
                    if (child instanceof ClickableWidget widget) {
                        originalY = widget.getY();
                        widget.setY(originalY - scrollPosition);
                        
                        // Solo dibujar si está en el área visible
                        if (isWithinVisibleArea(widget.getY(), widget.getHeight())) {
                            drawable.render(context, mouseX, mouseY, delta);
                        }
                        
                        // Restaurar la posición original
                        widget.setY(originalY);
                    } else {
                        // Para otros elementos Positionable, intentar obtener posición
                        // Nota: Como Positionable solo tiene getters, no podemos ajustar la posición
                        // directamente. Necesitamos usar transformaciones de contexto.
                        int itemY = ((Positionable) child).getY();
                        
                        // Ajustar el contexto de dibujo
                        context.getMatrices().push();
                        context.getMatrices().translate(0, -scrollPosition, 0);
                        
                        // Solo dibujar si está en el área visible
                        if (isWithinVisibleArea(itemY, getElementHeight(child))) {
                            drawable.render(context, mouseX, mouseY + scrollPosition, delta);
                        }
                        
                        // Restaurar el contexto
                        context.getMatrices().pop();
                    }
                } else {
                    // Para elementos que son solo Drawable pero no Positionable
                    // Estos serán dibujados en su posición absoluta con transformaciones
                    context.getMatrices().push();
                    context.getMatrices().translate(0, -scrollPosition, 0);
                    drawable.render(context, mouseX, mouseY + scrollPosition, delta);
                    context.getMatrices().pop();
                }
            }
        }
        
        // Deshabilitar área de recorte
        context.disableScissor();
        
        // Dibujar barra de desplazamiento si es necesario
        if (hasScrollBar) {
            int scrollBarHeight = Math.max(20, height * height / contentHeight);
            int scrollBarY = y;
            
            if (contentHeight > height) {
                scrollBarY = y + (height - scrollBarHeight) * scrollPosition / (contentHeight - height);
            }
            
            // Fondo de la barra
            context.fill(x + width - 8, y, x + width - 2, y + height, 0x40000000);
            
            // Barra de desplazamiento
            context.fill(x + width - 7, scrollBarY, x + width - 3, scrollBarY + scrollBarHeight, 0x80FFFFFF);
        }
    }
    
    private boolean isWithinVisibleArea(int elementY, int elementHeight) {
        int adjustedElementY = elementY - scrollPosition;
        return (adjustedElementY + elementHeight >= 0 && adjustedElementY <= height);
    }
    
    private int getElementHeight(Object element) {
        if (element instanceof ClickableWidget widget) {
            return widget.getHeight();
        } else if (element instanceof ButtonWidget button) {
            return button.getHeight();
        } else {
            // Si es un Widget, usamos un tamaño predeterminado o intentamos estimarlo
            return 20; // Altura estándar para widgets
        }
    }
    
    @Override
    public boolean isFocused() {
        return this.focused;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= x && mouseX <= x + width && 
            mouseY >= y && mouseY <= y + height) {
            
            // Verificar si se hizo clic en la barra de desplazamiento
            if (hasScrollBar && mouseX >= x + width - 8 && mouseX <= x + width - 2) {
                isDragging = true;
                lastMouseY = (int) mouseY;
                return true;
            }
            
            // Comprobar si se hizo clic en algún elemento
            for (Object child : children) {
                if (child instanceof Element element) {
                    // Ajustar coordenadas para clickables
                    if (child instanceof ClickableWidget widget) {
                        int originalY = widget.getY();
                        int adjustedY = originalY - scrollPosition;
                        
                        if (mouseX >= widget.getX() && mouseX <= widget.getX() + widget.getWidth() &&
                            mouseY >= adjustedY && mouseY <= adjustedY + widget.getHeight()) {
                            
                            // Enviar el evento de clic con coordenadas ajustadas
                            boolean result = element.mouseClicked(mouseX, mouseY - originalY + adjustedY, button);
                            return result;
                        }
                    } else {
                        // Para otros elementos que implementan Element
                        if (element.mouseClicked(mouseX, mouseY + scrollPosition, button)) {
                            return true;
                        }
                    }
                }
            }
            
            // Si no se hizo clic en ningún elemento, el panel maneja el clic
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isDragging) {
            isDragging = false;
            return true;
        }
        
        for (Object child : children) {
            if (child instanceof Element element) {
                // Ajuste similar para soltar el botón del ratón
                double adjustedMouseY = mouseY;
                
                if (child instanceof ClickableWidget) {
                    adjustedMouseY = mouseY + scrollPosition;
                }
                
                if (element.mouseReleased(mouseX, adjustedMouseY, button)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDragging) {
            int deltaScroll = (int) (mouseY - lastMouseY);
            lastMouseY = (int) mouseY;
            
            // Factor de velocidad para hacer el scroll más suave
            float scrollFactor = contentHeight > height * 3 ? 3.0f : 1.5f;
            
            int maxScroll = Math.max(0, contentHeight - height);
            int newScrollPosition = MathHelper.clamp(
                scrollPosition + (int)(deltaScroll * scrollFactor), 
                0, 
                maxScroll
            );
            
            if (newScrollPosition != scrollPosition) {
                scrollPosition = newScrollPosition;
                if (onScroll != null) {
                    onScroll.accept(scrollPosition);
                }
            }
            
            return true;
        }
        
        // Propagar el evento de arrastre a los elementos hijo
        for (Object child : children) {
            if (child instanceof Element element) {
                double adjustedMouseY = mouseY;
                
                if (child instanceof ClickableWidget widget) {
                    int originalY = widget.getY();
                    int adjustedY = originalY - scrollPosition;
                    
                    if (mouseX >= widget.getX() && mouseX <= widget.getX() + widget.getWidth() &&
                        mouseY >= adjustedY && mouseY <= adjustedY + widget.getHeight()) {
                        
                        if (element.mouseDragged(mouseX, mouseY - originalY + adjustedY, button, deltaX, deltaY)) {
                            return true;
                        }
                    }
                } else if (element.mouseDragged(mouseX, mouseY + scrollPosition, button, deltaX, deltaY)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            // Ajustar la velocidad de desplazamiento según el tamaño del contenido
            int scrollSpeed = Math.max(10, contentHeight / 20);
            int scrollAmount = (int) (-verticalAmount * scrollSpeed);
            
            int maxScroll = Math.max(0, contentHeight - height);
            int newScrollPosition = MathHelper.clamp(scrollPosition + scrollAmount, 0, maxScroll);
            
            if (newScrollPosition != scrollPosition) {
                scrollPosition = newScrollPosition;
                if (onScroll != null) {
                    onScroll.accept(scrollPosition);
                }
                return true;
            }
        }
        
        // Propagar el evento de scroll a los elementos hijo
        for (Object child : children) {
            if (child instanceof Element element) {
                if (element.mouseScrolled(mouseX, mouseY + scrollPosition, horizontalAmount, verticalAmount)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Añade un elemento hijo al panel de desplazamiento.
     * Soporta Drawable, Element, ClickableWidget, ButtonWidget y otros tipos.
     */
    public <T> void addChild(T child) {
        children.add(child);
        
        // Actualizar la altura del contenido basado en el nuevo elemento
        updateContentHeight();
    }
    
    /**
     * Añade varios elementos hijos al panel de desplazamiento.
     */
    public <T> void addChildren(List<T> newChildren) {
        children.addAll(newChildren);
        
        // Actualizar la altura del contenido
        updateContentHeight();
    }
    
    /**
     * Elimina todos los elementos hijos del panel.
     */
    public void clearChildren() {
        children.clear();
        scrollPosition = 0;
        contentHeight = 0;
        hasScrollBar = false;
    }
    
    /**
     * Actualiza la altura total del contenido basado en los elementos.
     */
    public void updateContentHeight() {
        int estimatedHeight = 0;
        int maxBottom = 0;
        
        // Calcular la altura basada en los elementos
        for (Object child : children) {
            if (child instanceof Positionable positionable) {
                int elementY = positionable.getY();
                int elementHeight = getElementHeight(child);
                int elementBottom = elementY + elementHeight;
                
                maxBottom = Math.max(maxBottom, elementBottom);
            } else {
                // Para elementos sin posición, añadir una altura predeterminada
                estimatedHeight += getElementHeight(child);
            }
        }
        
        // Usar el valor máximo entre la altura acumulada y el punto más bajo de los elementos posicionables
        estimatedHeight = Math.max(estimatedHeight, maxBottom);
        
        // Añadir un pequeño padding al final
        estimatedHeight += 5;
        
        this.contentHeight = estimatedHeight;
        this.hasScrollBar = contentHeight > height;
        
        // Asegurarse de que el scroll no exceda el máximo
        int maxScroll = Math.max(0, contentHeight - height);
        scrollPosition = MathHelper.clamp(scrollPosition, 0, maxScroll);
    }
    
    /**
     * Establece la función que se llama cuando cambia la posición de desplazamiento.
     */
    public void setOnScroll(Consumer<Integer> onScroll) {
        this.onScroll = onScroll;
    }
    
    /**
     * Obtiene la posición actual de desplazamiento.
     */
    public int getScrollPosition() {
        return scrollPosition;
    }
    
    /**
     * Establece la posición de desplazamiento.
     */
    public void setScrollPosition(int position) {
        int maxScroll = Math.max(0, contentHeight - height);
        this.scrollPosition = MathHelper.clamp(position, 0, maxScroll);
        
        if (onScroll != null) {
            onScroll.accept(scrollPosition);
        }
    }
    
    /**
     * Desplaza el panel para mostrar un elemento específico.
     */
    public void scrollToElement(Object element) {
        int elementY = 0;
        
        if (element instanceof Positionable positionable) {
            elementY = positionable.getY();
        } else {
            // Si no es posicionable, buscar su índice y estimar su posición
            int index = children.indexOf(element);
            if (index >= 0) {
                for (int i = 0; i < index; i++) {
                    elementY += getElementHeight(children.get(i));
                }
            }
        }
        
        // Ajustar el scroll para que el elemento sea visible
        int elementHeight = getElementHeight(element);
        
        // Si el elemento está por encima del área visible
        if (elementY < scrollPosition) {
            setScrollPosition(elementY);
        } 
        // Si el elemento está por debajo del área visible
        else if (elementY + elementHeight > scrollPosition + height) {
            setScrollPosition(elementY + elementHeight - height);
        }
    }
    
    /**
     * Obtiene la lista de elementos hijos.
     */
    public List<Object> getChildren() {
        return children;
    }
    
    /**
     * Verifica si el ratón está sobre el panel.
     */
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    
    /**
     * Obtiene la altura del contenido.
     */
    public int getContentHeight() {
        return contentHeight;
    }
    
    /**
     * Establece la altura del contenido manualmente.
     */
    public void setContentHeight(int contentHeight) {
        this.contentHeight = contentHeight;
        this.hasScrollBar = contentHeight > height;
        
        // Ajustar la posición de scroll si es necesario
        int maxScroll = Math.max(0, contentHeight - height);
        this.scrollPosition = MathHelper.clamp(scrollPosition, 0, maxScroll);
    }
    
    /**
     * Obtiene las coordenadas X del panel.
     */
    public int getX() {
        return x;
    }
    
    /**
     * Obtiene las coordenadas Y del panel.
     */
    public int getY() {
        return y;
    }
    
    /**
     * Obtiene el ancho del panel.
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Obtiene la altura del panel.
     */
    public int getHeight() {
        return height;
    }
}
