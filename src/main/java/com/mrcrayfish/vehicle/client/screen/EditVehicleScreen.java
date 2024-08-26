package com.mrcrayfish.vehicle.client.screen;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mrcrayfish.vehicle.client.render.AbstractVehicleRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.client.render.CachedVehicle;
import com.mrcrayfish.vehicle.common.entity.Transform;
import com.mrcrayfish.vehicle.entity.EngineType;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.inventory.container.EditVehicleContainer;
import com.mrcrayfish.vehicle.util.CommonUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.Collections;

/**
 * Author: MrCrayfish
 */
//TODO::FIX THIS
public class EditVehicleScreen extends AbstractContainerScreen<EditVehicleContainer>
{
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("vehicle:textures/gui/edit_vehicle.png");

    private final Inventory playerInventory;
    private final Container vehicleInventory;
    private final CachedVehicle cachedVehicle;

    private RenderTarget framebuffer;
    private boolean showHelp = true;
    private int windowZoom = 10;
    private int windowX, windowY;
    private float windowRotationX, windowRotationY;
    private boolean mouseGrabbed;
    private int mouseGrabbedButton;
    private int mouseClickedX, mouseClickedY;

    public EditVehicleScreen(EditVehicleContainer container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        this.playerInventory = playerInventory;
        this.vehicleInventory = container.getVehicleInventory();
        this.cachedVehicle = new CachedVehicle(container.getVehicle());
        this.imageHeight = 184;
    }

    @Override
    protected void renderBg(GuiGraphics matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bindForSetup(GUI_TEXTURES);
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        matrixStack.blit(GUI_TEXTURES, left, top, 0, 0, this.imageWidth, this.imageHeight);

        // Render engine icon if applicable
        if(this.cachedVehicle.getProperties().getExtended(PoweredProperties.class).getEngineType() != EngineType.NONE)
        {
            if(this.vehicleInventory.getItem(0).isEmpty())
            {
                matrixStack.blit(GUI_TEXTURES, left + 8, top + 17, 176, 0, 16, 16);
            }
        }
        else if(this.vehicleInventory.getItem(0).isEmpty())
        {
            matrixStack.blit(GUI_TEXTURES, left + 8, top + 17, 176, 32, 16, 16);
        }

        // Render wheel icon if applicable
        if(this.cachedVehicle.getProperties().canChangeWheels())
        {
            if(this.vehicleInventory.getItem(1).isEmpty())
            {
                matrixStack.blit(GUI_TEXTURES, left + 8, top + 35, 176, 16, 16, 16);
            }
        }
        else if(this.vehicleInventory.getItem(1).isEmpty())
        {
            matrixStack.blit(GUI_TEXTURES, left + 8, top + 35, 176, 32, 16, 16);
        }

        // Render framebuffer content
        if(this.framebuffer != null)
        {
            this.framebuffer.bindRead();
            int startX = left + 26;
            int startY = top + 17;
            RenderSystem.disableCull();
            PoseStack pose = matrixStack.pose();
            BufferBuilder builder = Tesselator.getInstance().getBuilder();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.vertex(pose.last().pose(), startX, startY + 70, 0).uv(0, 1).endVertex();
            builder.vertex(pose.last().pose(), startX + 142, startY + 70, 0).uv(1, 1).endVertex();
            builder.vertex(pose.last().pose(), startX + 142, startY,0).uv(1, 0).endVertex();
            builder.vertex(pose.last().pose(), startX, startY, 0).uv(0, 0).endVertex();
            builder.end();
            RenderSystem.enableDepthTest();
            //BufferUploader.(builder);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics matrixStack, int mouseX, int mouseY)
    {
        Minecraft minecraft = Minecraft.getInstance();
        //minecraft.font.draw(matrixStack, this.title.getString(), 8, 6, 4210752);
        //minecraft.font.draw(matrixStack, this.playerInventory.getDisplayName().getString(), 8, this.imageHeight - 96 + 2, 4210752);

        if(this.showHelp)
        {
            PoseStack poseStack = matrixStack.pose();
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            //minecraft.font.draw(matrixStack, "Use the mouse to rotate and zoom the vehicle", 56, 38, 0xFFFFFF);
            poseStack.popPose();
        }
    }

    private void renderVehicleToBuffer(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        PoseStack poseStack = matrixStack.pose();
        RenderSystem.setProjectionMatrix(new Matrix4f().setPerspective(30, 142.0F / 70.0F, 0.5F, 200.0F),VertexSorting.ORTHOGRAPHIC_Z);
        RenderSystem.setProjectionMatrix(new Matrix4f().identity(),VertexSorting.ORTHOGRAPHIC_Z);

        AbstractVehicleRenderer renderer = this.cachedVehicle.getRenderer();
        if(renderer != null)
        {
            this.bindFrameBuffer();

            poseStack.pushPose();
            poseStack.translate(0, -20, -150);
            poseStack.translate(this.windowX + (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseX - this.mouseClickedX : 0), 0, 0);
            poseStack.translate(0, this.windowY - (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseY - this.mouseClickedY : 0), 0);

            Quaternionf quaternion = Axis.POSITIVE_X.rotationDegrees(20F);
            quaternion.mul(Axis.NEGATIVE_X.rotationDegrees(this.windowRotationY - (this.mouseGrabbed && this.mouseGrabbedButton == 1 ? mouseY - this.mouseClickedY : 0)));
            quaternion.mul(Axis.POSITIVE_Y.rotationDegrees(this.windowRotationX + (this.mouseGrabbed && this.mouseGrabbedButton == 1 ? mouseX - this.mouseClickedX : 0)));
            quaternion.mul(Axis.POSITIVE_Y.rotationDegrees(45F));
            poseStack.mulPose(quaternion);

            poseStack.scale(this.windowZoom / 10F, this.windowZoom / 10F, this.windowZoom / 10F);
            poseStack.scale(22F, 22F, 22F);

            Transform position = this.cachedVehicle.getProperties().getDisplayTransform();
            poseStack.scale((float) position.getScale(), (float) position.getScale(), (float) position.getScale());
            poseStack.mulPose(Axis.POSITIVE_X.rotationDegrees((float) position.getRotX()));
            poseStack.mulPose(Axis.POSITIVE_Y.rotationDegrees((float) position.getRotY()));
            poseStack.mulPose(Axis.POSITIVE_Z.rotationDegrees((float) position.getRotZ()));
            poseStack.translate(position.getX(), position.getY(), position.getZ());

            MultiBufferSource.BufferSource renderTypeBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
            renderer.setupTransformsAndRender(this.menu.getVehicle(), poseStack, renderTypeBuffer, Minecraft.getInstance().getFrameTime(), 15728880);
            renderTypeBuffer.endBatch();

            poseStack.popPose();

            this.unbindFrameBuffer();
        }

        RenderSystem.setProjectionMatrix(new Matrix4f().identity(),VertexSorting.ORTHOGRAPHIC_Z);
        RenderSystem.setTextureMatrix(new Matrix4f().identity());
        RenderSystem.applyModelViewMatrix();
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;
        if(mouseX > startX + 26 && mouseX < startX + 168 && mouseY > startY + 17 && mouseY < startY + 87)
        {
            this.windowZoom += scroll;
            this.windowZoom = Math.max(5, Math.min(30, this.windowZoom));
            return true;
        }
        return false;
    }

    private void bindFrameBuffer()
    {
        if(this.framebuffer != null)
        {
            this.framebuffer.bindWrite(true);
        }
    }

    private void unbindFrameBuffer()
    {
        if(this.framebuffer != null)
        {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;
        if(mouseX > startX + 26 && mouseX < startX + 168 && mouseY > startY + 17 && mouseY < startY + 87)
        {
            this.mouseGrabbed = true;
            this.mouseGrabbedButton = button;
            this.mouseClickedX = (int) mouseX;
            this.mouseClickedY = (int) mouseY;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if(this.mouseGrabbed && this.mouseGrabbedButton == button)
        {
            this.mouseGrabbed = false;
            return true;
        }
        return false;
    }

    @Override
    protected void init()
    {
        super.init();

        // Framebuffer Initialization
        Window window = this.minecraft.getWindow();
        int framebufferWidth = (int) (142 * this.minecraft.getWindow().getGuiScale());
        int framebufferHeight = (int) (70 * this.minecraft.getWindow().getGuiScale());
        this.framebuffer = new TextureTarget(framebufferWidth, framebufferHeight, true, Minecraft.ON_OSX);
        this.framebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.framebuffer.clear(Minecraft.ON_OSX);
        this.windowZoom = 10;
        this.windowX = 0;
        this.windowY = 0;
        this.windowRotationX = 0;
        this.windowRotationY = 0;
    }

    @Override
    public void removed()
    {
        super.removed();
        if(this.framebuffer != null)
        {
            this.framebuffer.destroyBuffers();
            this.framebuffer = null;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if(this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode)))
        {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
