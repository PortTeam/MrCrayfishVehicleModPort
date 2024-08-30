package com.mrcrayfish.vehicle.crafting;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.VehicleMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Author: MrCrayfish
 */
public class RecipeTypes
{
    public static final DeferredRegister<RecipeType<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Reference.MOD_ID);

    public static final RegistryObject<RecipeType<FluidExtractorRecipe>> FLUID_EXTRACTOR = register("fluid_extractor");
    public static final RegistryObject<RecipeType<FluidMixerRecipe>> FLUID_MIXER = register("fluid_mixer");
    public static final RegistryObject<RecipeType<WorkstationRecipe>> WORKSTATION = register("workstation");

    private static <T extends Recipe<?>> RegistryObject<RecipeType<T>> register(final String key)
    {
        return RECIPES.register(key, () -> new RecipeType<>() {
            @Override
            public String toString() {
                return key;
            }
        });
    }

    // Does nothing, just forces static fields to initialize
    public static void init() {}

    public static void register(IEventBus eventBus)
    {
        VehicleMod.LOGGER.info("Registered RecipeTypes");

        RECIPES.register(eventBus);
    }
}
