package com.mrcrayfish.vehicle.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.block.FluidPumpBlock;
import com.mrcrayfish.vehicle.client.raytrace.EntityRayTracer;
import com.mrcrayfish.vehicle.init.ModItems;
import com.mrcrayfish.vehicle.tileentity.PumpTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class FluidPumpRenderer implements BlockEntityRenderer<PumpTileEntity> {

    private final BlockEntityRendererProvider.Context context;

    public FluidPumpRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(PumpTileEntity tileEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        Entity entity = Minecraft.getInstance().cameraEntity;
        if (!(entity instanceof Player player))
            return;

        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.getItem() != ModItems.WRENCH.get())
            return;

        this.renderInteractableBox(tileEntity, matrixStack, bufferSource);

        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK)
            return;

        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        if (!blockHitResult.getBlockPos().equals(tileEntity.getBlockPos()))
            return;

        BlockPos pos = tileEntity.getBlockPos();
        BlockState state = tileEntity.getBlockState();
        FluidPumpBlock fluidPumpBlock = (FluidPumpBlock) state.getBlock();
        if (!fluidPumpBlock.isLookingAtHousing(state, blockHitResult.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ())))
            return;

        matrixStack.pushPose();
        matrixStack.translate(0.5, 0.5, 0.5);

        var direction = state.getValue(FluidPumpBlock.DIRECTION);
        matrixStack.translate(-direction.getStepX() * 0.35, -direction.getStepY() * 0.35, -direction.getStepZ() * 0.35);

        matrixStack.mulPose(this.context.getEntityRenderer().cameraOrientation());
        matrixStack.scale(-0.015F, -0.015F, 0.015F);

        var matrix4f = matrixStack.last().pose();
        var fontRenderer = this.context.getFont();
        var text = tileEntity.getPowerMode().getKey();
        var x = (float) (-fontRenderer.width(text) / 2);
        fontRenderer.drawInBatch(text, x, 0, -1, true, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, combinedLight);
        matrixStack.popPose();
    }

    private void renderInteractableBox(PumpTileEntity tileEntity, PoseStack matrixStack, MultiBufferSource bufferSource) {
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            if (blockHitResult.getBlockPos().equals(tileEntity.getBlockPos())) {
                BlockPos pos = tileEntity.getBlockPos();
                BlockState state = tileEntity.getBlockState();
                FluidPumpBlock fluidPumpBlock = (FluidPumpBlock) state.getBlock();
                if (fluidPumpBlock.isLookingAtHousing(state, blockHitResult.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ()))) {
                    return;
                }
            }
        }

        BlockState state = tileEntity.getBlockState();
        var shape = FluidPumpBlock.PUMP_BOX[state.getValue(FluidPumpBlock.DIRECTION).getOpposite().get3DDataValue()];
        var builder = bufferSource.getBuffer(RenderType.lines());
        EntityRayTracer.renderShape(matrixStack, builder, shape, 1.0F, 0.77F, 0.29F, 1.0F);
    }
}
