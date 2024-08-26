package com.mrcrayfish.vehicle.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.client.render.AbstractLandVehicleRenderer;
import com.mrcrayfish.vehicle.client.render.AbstractPoweredRenderer;
import com.mrcrayfish.vehicle.client.render.AbstractVehicleRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.client.render.CachedVehicle;
import com.mrcrayfish.vehicle.common.entity.Transform;
import com.mrcrayfish.vehicle.crafting.WorkstationIngredient;
import com.mrcrayfish.vehicle.crafting.WorkstationRecipe;
import com.mrcrayfish.vehicle.crafting.WorkstationRecipes;
import com.mrcrayfish.vehicle.entity.EngineType;
import com.mrcrayfish.vehicle.entity.IEngineType;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.inventory.container.WorkstationContainer;
import com.mrcrayfish.vehicle.item.EngineItem;
import com.mrcrayfish.vehicle.item.WheelItem;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageCraftVehicle;
import com.mrcrayfish.vehicle.tileentity.WorkstationTileEntity;
import com.mrcrayfish.vehicle.util.CommonUtils;
import com.mrcrayfish.vehicle.util.InventoryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.RenderTypeHelper;
import org.joml.Quaternionf;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorkstationScreen //extends AbstractContainerScreen<WorkstationContainer>
{
    /*
    private static final ResourceLocation GUI = new ResourceLocation("vehicle:textures/gui/workstation.png");
    private static CachedVehicle cachedVehicle;
    private static CachedVehicle prevCachedVehicle;
    private static int currentVehicle = 0;
    private static boolean showRemaining = false;

    private final List<EntityType<?>> vehicleTypes;
    private final List<MaterialItem> materials;
    private List<MaterialItem> filteredMaterials;
    private final Inventory playerInventory;
    private final WorkstationTileEntity workstation;
    private Button btnCraft;
    private CheckBox checkBoxMaterials;
    private boolean validEngine;
    private boolean transitioning;
    private int vehicleScale = 30;
    private int prevVehicleScale = 30;

    public WorkstationScreen(WorkstationContainer container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        this.playerInventory = playerInventory;
        this.workstation = container.getTileEntity();
        this.imageWidth = 256;
        this.imageHeight = 184;
        this.inventoryLabelY = this.imageHeight - 93;
        this.materials = new ArrayList<>();
        this.vehicleTypes = this.getVehicleTypes(playerInventory.player.level);
        this.vehicleTypes.sort(Comparator.comparing(type -> type.toString()));
    }

    private List<EntityType<?>> getVehicleTypes(Level world)
    {
        return world.getRecipeManager().getRecipes().stream()
                .filter(recipe -> recipe.getType() == RecipeType.WORKSTATION)
                .map(recipe -> (WorkstationRecipe) recipe)
                .map(WorkstationRecipe::getVehicle)
                .filter(entityType -> !Config.SERVER.disabledVehicles.get().contains(Objects.requireNonNull(entityType.getRegistryName()).toString()))
                .collect(Collectors.toList());
    }

    @Override
    public void init()
    {
        super.init();

        this.addRenderableWidget(new Button(this.leftPos + 9, this.topPos + 18, 15, 20, Component.literal("<"), button -> {
            this.loadVehicle(Math.floorMod(currentVehicle - 1, this.vehicleTypes.size()));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }));

        this.addRenderableWidget(new Button(this.leftPos + 153, this.topPos + 18, 15, 20, Component.literal(">"), button -> {
            this.loadVehicle(Math.floorMod(currentVehicle + 1, this.vehicleTypes.size()));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }));

        this.btnCraft = this.addRenderableWidget(new Button(this.leftPos + 172, this.topPos + 6, 97, 20, Component.translatable("gui.vehicle.craft"), button -> {
            ResourceLocation registryName = ResourceLocation.tryParse(this.vehicleTypes.get(currentVehicle).toString());
            Objects.requireNonNull(registryName, "Vehicle registry name must not be null!");
            PacketHandler.getChannel().sendToServer(new MessageCraftVehicle(registryName.toString(), this.workstation.getBlockPos()));
        }));

        this.btnCraft.active = false;
        this.checkBoxMaterials = this.addRenderableWidget(new CheckBox(this.leftPos + 172, this.topPos + 51, Component.translatable("gui.vehicle.show_remaining")));
        this.checkBoxMaterials.setToggled(WorkstationScreen.showRemaining);
        this.loadVehicle(currentVehicle);
    }

    @Override
    public void tick()
    {
        super.tick();

        this.validEngine = true;

        for (MaterialItem material : this.materials)
        {
            material.tick();
        }

        boolean canCraft = true;
        for (MaterialItem material : this.materials)
        {
            if (!material.isEnabled())
            {
                canCraft = false;
                break;
            }
        }

        if (cachedVehicle.getRenderer() instanceof AbstractPoweredRenderer)
        {
            AbstractPoweredRenderer<?> poweredRenderer = (AbstractPoweredRenderer<?>) cachedVehicle.getRenderer();
            VehicleProperties properties = cachedVehicle.getProperties();
            if (properties.getExtended(PoweredProperties.class).getEngineType() != EngineType.NONE)
            {
                ItemStack engine = this.workstation.getItem(1);
                if (!engine.isEmpty() && engine.getItem() instanceof EngineItem)
                {
                    EngineItem engineItem = (EngineItem) engine.getItem();
                    IEngineType engineType = engineItem.getEngineType();
                    if (properties.getExtended(PoweredProperties.class).getEngineType() == engineType)
                    {
                        poweredRenderer.setEngineStack(engine);
                    }
                    else
                    {
                        canCraft = false;
                        this.validEngine = false;
                        poweredRenderer.setEngineStack(ItemStack.EMPTY);
                    }
                }
                else
                {
                    canCraft = false;
                    this.validEngine = false;
                    poweredRenderer.setEngineStack(ItemStack.EMPTY);
                }
            }

            if (cachedVehicle.getProperties().canChangeWheels())
            {
                ItemStack wheels = this.workstation.getItem(2);
                if (!wheels.isEmpty() && wheels.getItem() instanceof WheelItem)
                {
                    poweredRenderer.setWheelStack(wheels);
                }
                else
                {
                    poweredRenderer.setWheelStack(ItemStack.EMPTY);
                    canCraft = false;
                }
            }
        }
        this.btnCraft.active = canCraft;

        this.prevVehicleScale = this.vehicleScale;
        if (this.transitioning)
        {
            if (this.vehicleScale > 0)
            {
                this.vehicleScale = Math.max(0, this.vehicleScale - 6);
            }
            else
            {
                this.transitioning = false;
            }
        }
        else if (this.vehicleScale < 30)
        {
            this.vehicleScale = Math.min(30, this.vehicleScale + 6);
        }

        this.updateVehicleColor();
    }

    private void updateVehicleColor()
    {
        if (cachedVehicle.getProperties().canBePainted())
        {
            AbstractVehicleRenderer<?> renderer = cachedVehicle.getRenderer();
            ItemStack dyeStack = this.workstation.getItem(0);
            if (dyeStack.getItem() instanceof DyeItem)
            {
                renderer.setColor(((DyeItem) dyeStack.getItem()).getDyeColor().getColorValue());
            }
            else
            {
                renderer.setColor(Color.WHITE.getRGB());
            }
        }
    }

    private void loadVehicle(int vehicleIndex)
    {
        currentVehicle = vehicleIndex;
        EntityType<?> vehicleType = this.vehicleTypes.get(currentVehicle);
        cachedVehicle = CachedVehicle.create(Minecraft.getInstance().level, vehicleType);
        prevCachedVehicle = CachedVehicle.create(Minecraft.getInstance().level, vehicleType);
        this.transitioning = true;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY)
    {
        RenderSystem.setShaderTexture(0, GUI);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if (this.cachedVehicle != null)
        {
            PoseStack matrixStack = RenderSystem.getModelViewStack();
            matrixStack.pushPose();
            matrixStack.translate(this.leftPos + 99, this.topPos + 110, 1000);
            matrixStack.scale(-this.vehicleScale / 30.0F, this.vehicleScale / 30.0F, this.vehicleScale / 30.0F);
            matrixStack.translate(0.0D, -1.0D, 0.0D);

            AbstractVehicleRenderer<?> renderer = this.cachedVehicle.getRenderer();
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            renderer.setRenderDispatcher(dispatcher);
            PoseStack modelViewStack = new PoseStack();
            modelViewStack.mulPoseMatrix(matrixStack.last().pose());

            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            renderer.render(this.cachedVehicle.getEntity(), 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, modelViewStack, bufferSource, Minecraft.getInstance().entityRenderDispatcher.getLightLevel(this.cachedVehicle.getEntity(), 0));
            bufferSource.endBatch();

            matrixStack.popPose();
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY)
    {
        super.renderLabels(poseStack, mouseX, mouseY);
        this.font.draw(poseStack, this.title, (float) this.titleLabelX, (float) this.titleLabelY, 4210752);
        this.font.draw(poseStack, this.inventoryTitle, (float) this.inventoryLabelX, (float) this.inventoryLabelY, 4210752);

        if (this.checkBoxMaterials.isToggled())
        {
            List<Component> tooltip = this.filteredMaterials.stream()
                    .map(material -> Component.literal(material.getName()).withStyle(ChatFormatting.GRAY))
                    .collect(Collectors.toList());

            if (!tooltip.isEmpty())
            {
                this.renderTooltip(poseStack, tooltip, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    @Override
    public void onClose()
    {
        super.onClose();
        //Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
    }

     */
}
