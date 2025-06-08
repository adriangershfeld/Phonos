package io.github.foundationgames.phonos.screen.widget;

import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;

public class WBasicButton extends WWidget {
    private ClickFunction clicked = (button, x, y, mbutton) -> {};    private Object backgroundPainter = null; // Using Object to avoid client-side class loading on server
    private Object customPainter = null; // Using Object to avoid client-side class loading on server
    private ScrollFunction scroll = ((button, x, y, amount) -> InputResult.IGNORED);
    public boolean enabled = true;

    public WBasicButton(int width, int height) {
        this.width = width;
        this.height = height;
    }    @Override
    @Environment(EnvType.CLIENT)
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        super.paint(matrices, x, y, mouseX, mouseY);
        if (this.backgroundPainter != null && isBackgroundPainter(this.backgroundPainter)) {
            paintBackground(this.backgroundPainter, matrices, x, y, this);
        }
        if (this.customPainter != null && this.customPainter instanceof PaintFunction) {
            ((PaintFunction) this.customPainter).apply(this, matrices, x, y, mouseX, mouseY);
        }
    }

    public void setWhenClicked(ClickFunction f) {
        this.clicked = f;
    }

    @Environment(EnvType.CLIENT)
    public Object getBackgroundPainter() {
        return this.backgroundPainter;
    }

    @Environment(EnvType.CLIENT)
    public void setBackgroundPainter(Object painter) {
        this.backgroundPainter = painter;
    }

    @Environment(EnvType.CLIENT)
    public void setBackgroundPainter(PaintFunction painter) {
        this.customPainter = painter;
    }

    public void setWhenScrolledOver(ScrollFunction f) {
        this.scroll = f;
    }

    @Override
    public InputResult onMouseDown(int x, int y, int button) {
        if (isWithinBounds(x, y) && enabled) this.clicked.apply(this, x, y, button);
        return super.onMouseDown(x, y, button);
    }

    @Override
    public InputResult onMouseScroll(int x, int y, double amount) {
        super.onMouseScroll(x, y, amount);
        if(isWithinBounds(x, y)) {
            return scroll.apply(this, x, y, amount);
        }
        return super.onMouseScroll(x, y, amount);
    }

    @FunctionalInterface
    public interface ClickFunction {
        void apply(WBasicButton button, int x, int y, int mouseButton);
    }    @FunctionalInterface
    public interface ScrollFunction {
        InputResult apply(WBasicButton button, int x, int y, double amount);
    }    @FunctionalInterface
    public interface PaintFunction {
        void apply(WBasicButton button, MatrixStack matrices, int x, int y, int mouseX, int mouseY);
    }

    @Environment(EnvType.CLIENT)
    private static boolean isBackgroundPainter(Object obj) {
        try {
            Class<?> backgroundPainterClass = Class.forName("io.github.cottonmc.cotton.gui.client.BackgroundPainter");
            return backgroundPainterClass.isInstance(obj);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }    @Environment(EnvType.CLIENT)
    private static void paintBackground(Object painter, MatrixStack matrices, int x, int y, WWidget widget) {
        try {
            Class<?> backgroundPainterClass = Class.forName("io.github.cottonmc.cotton.gui.client.BackgroundPainter");
            Class<?> matrixStackClass = Class.forName("net.minecraft.client.util.math.MatrixStack");
            java.lang.reflect.Method paintMethod = backgroundPainterClass.getMethod("paintBackground", matrixStackClass, int.class, int.class, WWidget.class);
            paintMethod.invoke(painter, matrices, x, y, widget);
        } catch (Exception e) {
            // Silently fail if reflection doesn't work
        }
    }
}
