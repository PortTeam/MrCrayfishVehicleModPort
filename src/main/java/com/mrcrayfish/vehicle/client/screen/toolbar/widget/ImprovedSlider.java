package com.mrcrayfish.vehicle.client.screen.toolbar.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * A class that fixes a bug with the Forge slider. The slider won't stop sliding if the mouse
 * was released outside of the slider's area.
 *
 * Author: MrCrayfish
 */
public class ImprovedSlider //extends AbstractSliderButton
{
    /*
    private final AbstractSliderButton handler;

    public ImprovedSlider(int xPos, int yPos, int width, int height, String prefix, String suffix, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, AbstractSliderButton handler)
    {
        super(xPos, yPos, width, height, Component.literal(prefix + suffix), minVal, maxVal, currentVal, showDec, drawStr);
        this.handler = handler;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            this.onRelease(mouseX, mouseY);
            //this.dragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

//    @Override
//    protected void updateSlider()
//    {
//        super.updateSlider();
//        this.handler.onChangeSliderValue(this);
//
//        /* Fixes the slider not being released when mouse is released outside of the slider area */
//        Minecraft mc = Minecraft.getInstance();
//        if(this.dragging && GLFW.glfwGetMouseButton(mc.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_RELEASE)
//        {
//            double mouseX = mc.mouseHandler.xpos() * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
//            double mouseY = mc.mouseHandler.ypos() * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
//            this.onRelease(mouseX, mouseY);
//            this.dragging = false;
//        }
//    }


}
