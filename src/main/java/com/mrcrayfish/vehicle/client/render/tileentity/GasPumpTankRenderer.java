package com.mrcrayfish.vehicle.client.render.tileentity;

/**
 * Author: MrCrayfish
 */
public class GasPumpTankRenderer //extends TileEntityRenderer<GasPumpTankTileEntity>
{
//    private static final FluidUtils.FluidSides FLUID_SIDES = new FluidUtils.FluidSides(Direction.NORTH, Direction.SOUTH, Direction.UP);
//
//    public GasPumpTankRenderer(TileEntityRendererDispatcher dispatcher)
//    {
//        super(dispatcher);
//    }
//
//    @Override
//    public void render(GasPumpTankTileEntity gasPump, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int light, int overlay)
//    {
//        World world = gasPump.getLevel();
//        BlockState state = gasPump.getBlockState();
//        if(state.getBlock() != ModBlocks.GAS_PUMP.get())
//            return;
//
//        FluidTank tank = gasPump.getFluidTank();
//        if(tank.isEmpty())
//            return;
//
//        matrixStack.pushPose();
//        Direction direction = state.getValue(RotatedObjectBlock.DIRECTION);
//        matrixStack.translate(0.5, 0.5, 0.5);
//        matrixStack.mulPose(Axis.POSITIVE_Y.rotationDegrees(direction.get2DDataValue() * -90F - 90F));
//        matrixStack.translate(-0.5, -0.5, -0.5);
//        float height = 11.0F * (tank.getFluidAmount() / (float) tank.getCapacity());
//        FluidUtils.drawFluidInWorld(tank, world, gasPump.getBlockPos(), matrixStack, renderTypeBuffer, 2.01F * 0.0625F, 4F * 0.0625F, 5F * 0.0625F, (12 - 0.02F) * 0.0625F, height * 0.0625F, 6F * 0.0625F, light, FLUID_SIDES);
//        matrixStack.popPose();
//    }
}
