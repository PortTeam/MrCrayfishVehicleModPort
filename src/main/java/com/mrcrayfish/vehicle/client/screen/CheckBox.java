package com.mrcrayfish.vehicle.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Author: MrCrayfish
 */
@OnlyIn(Dist.CLIENT)
public class CheckBox extends AbstractWidget
{
    private static final ResourceLocation GUI = new ResourceLocation("vehicle:textures/gui/components.png");

    private boolean toggled = false;

    public CheckBox(int left, int top, Component title)
    {
        super(left, top, 8, 8, title);
    }

    public void setToggled(boolean toggled)
    {
        this.toggled = toggled;
    }

    public boolean isToggled()
    {
        return this.toggled;
    }



    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bindForSetup(GUI);
        guiGraphics.blit(GUI, this.x, this.y, 0, 0, 8, 8);
        if(this.toggled)
        {
            guiGraphics.blit(GUI, this.x, this.y - 1, 8, 0, 9, 8);
        }
        //minecraft.font.drawInBatch(this.getMessage().getString(), this.x + 12, this.y, 0xFFFFFF);
    }

    @Override
    public void onClick(double mouseX, double mouseY)
    {
        this.toggled = !this.toggled;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
