package com.mrcrayfish.vehicle.fluid;

/**
 * Author: MrCrayfish
 */
public abstract class EnderSap //extends ForgeFlowingFluid
{
    /*
    public EnderSap()
    {
        super(new Properties(() -> ModFluids.ENDER_SAP.get(), () -> ModFluids.FLOWING_ENDER_SAP.get(), FluidAttributes.builder(new ResourceLocation(Reference.MOD_ID, "block/ender_sap_still"), new ResourceLocation(Reference.MOD_ID, "block/ender_sap_flowing")).viscosity(3000).sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY)).block(() -> ModBlocks.ENDER_SAP.get()));
    }

    @Override
    public Item getBucket()
    {
        return ModItems.ENDER_SAP_BUCKET.get();
    }

    public static class Source extends EnderSap
    {
        @Override
        public boolean isSource(FluidState state)
        {
            return true;
        }

        @Override
        public int getAmount(FluidState state)
        {
            return 8;
        }
    }

    public static class Flowing extends EnderSap
    {
        @Override
        protected void createFluidStateDefinition(StateContainer.Builder<Fluid, FluidState> builder)
        {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state)
        {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state)
        {
            return false;
        }
    }

     */
}
