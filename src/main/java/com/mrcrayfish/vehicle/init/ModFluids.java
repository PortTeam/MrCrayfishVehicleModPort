package com.mrcrayfish.vehicle.init;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.fluid.BlazeJuice;
import com.mrcrayfish.vehicle.fluid.EnderSap;
import com.mrcrayfish.vehicle.fluid.Fuelium;

import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.WaterFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Author: MrCrayfish
 */
public class ModFluids
{
    public static final DeferredRegister<Fluid> REGISTER = DeferredRegister.create(ForgeRegistries.FLUIDS, Reference.MOD_ID);

    public static final RegistryObject<Fluid> FUELIUM = REGISTER.register("fuelium", WaterFluid.Source::new);
    public static final RegistryObject<FlowingFluid> FLOWING_FUELIUM = REGISTER.register("flowing_fuelium", WaterFluid.Flowing::new);
    public static final RegistryObject<Fluid> ENDER_SAP = REGISTER.register("ender_sap", WaterFluid.Source::new);
    public static final RegistryObject<FlowingFluid> FLOWING_ENDER_SAP = REGISTER.register("flowing_ender_sap", WaterFluid.Flowing::new);
    public static final RegistryObject<Fluid> BLAZE_JUICE = REGISTER.register("blaze_juice", WaterFluid.Source::new);
    public static final RegistryObject<FlowingFluid> FLOWING_BLAZE_JUICE = REGISTER.register("flowing_blaze_juice", WaterFluid.Flowing::new);
}