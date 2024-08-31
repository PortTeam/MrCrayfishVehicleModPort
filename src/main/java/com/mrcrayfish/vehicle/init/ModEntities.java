package com.mrcrayfish.vehicle.init;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.entity.EntityJack;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.trailer.*;
import com.mrcrayfish.vehicle.entity.vehicle.*;
import com.mrcrayfish.vehicle.util.VehicleUtil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BiFunction;
import java.util.function.Supplier;


public class ModEntities
{
    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Reference.MOD_ID);

    public static final RegistryObject<EntityType<QuadBikeEntity>> QUAD_BIKE = VehicleUtil.registerVehicleEntity(REGISTER,"quad_bike",QuadBikeEntity::new, 1.5F, 1.0F);
    public static final RegistryObject<EntityType<SportsCarEntity>> SPORTS_CAR = VehicleUtil.registerVehicleEntity(REGISTER,"sports_car", SportsCarEntity::new, 1.5F, 1.0F);
    public static final RegistryObject<EntityType<GoKartEntity>> GO_KART = VehicleUtil.registerVehicleEntity(REGISTER,"go_kart", GoKartEntity::new, 1.5F, 0.5F);
    public static final RegistryObject<EntityType<JetSkiEntity>> JET_SKI = VehicleUtil.registerVehicleEntity(REGISTER, "jet_ski", JetSkiEntity::new, 1.5F, 1.0F);
    public static final RegistryObject<EntityType<LawnMowerEntity>> LAWN_MOWER = VehicleUtil.registerVehicleEntity(REGISTER, "lawn_mower", LawnMowerEntity::new, 1.2F, 1.0F);
    public static final RegistryObject<EntityType<MopedEntity>> MOPED = VehicleUtil.registerVehicleEntity(REGISTER, "moped", MopedEntity::new, 1.0F, 1.0F);
    public static final RegistryObject<EntityType<SportsPlaneEntity>> SPORTS_PLANE = VehicleUtil.registerVehicleEntity( REGISTER,"sports_plane", SportsPlaneEntity::new, 3.0F, 1.6875F);
    public static final RegistryObject<EntityType<GolfCartEntity>> GOLF_CART = VehicleUtil.registerVehicleEntity(REGISTER, "golf_cart", GolfCartEntity::new, 2.0F, 1.0F);
    public static final RegistryObject<EntityType<OffRoaderEntity>> OFF_ROADER = VehicleUtil.registerVehicleEntity(REGISTER, "off_roader", OffRoaderEntity::new, 2.0F, 1.0F);
    public static final RegistryObject<EntityType<TractorEntity>> TRACTOR = VehicleUtil.registerVehicleEntity( REGISTER,"tractor", TractorEntity::new, 1.5F, 1.5F);
    public static final RegistryObject<EntityType<MiniBusEntity>> MINI_BUS = VehicleUtil.registerVehicleEntity( REGISTER,"mini_bus", MiniBusEntity::new, 2.0F, 2.0F);
    public static final RegistryObject<EntityType<DirtBikeEntity>> DIRT_BIKE = VehicleUtil.registerVehicleEntity(REGISTER, "dirt_bike", DirtBikeEntity::new, 1.0F, 1.5F);
    public static final RegistryObject<EntityType<CompactHelicopterEntity>> COMPACT_HELICOPTER = VehicleUtil.registerVehicleEntity(REGISTER, "compact_helicopter", CompactHelicopterEntity::new, 2.0F, 2.0F);

    /* Trailers */
    public static final RegistryObject<EntityType<VehicleTrailerEntity>> VEHICLE_TRAILER = VehicleUtil.registerVehicleEntity(REGISTER, "vehicle_trailer", VehicleTrailerEntity::new, 1.5F, 0.75F);
    public static final RegistryObject<EntityType<StorageTrailerEntity>> STORAGE_TRAILER = VehicleUtil.registerVehicleEntity(REGISTER, "storage_trailer", StorageTrailerEntity::new, 1.0F, 1.0F);
    public static final RegistryObject<EntityType<FluidTrailerEntity>> FLUID_TRAILER = VehicleUtil.registerVehicleEntity(REGISTER, "fluid_trailer", FluidTrailerEntity::new, 1.5F, 1.5F);
    public static final RegistryObject<EntityType<SeederTrailerEntity>> SEEDER = VehicleUtil.registerVehicleEntity(REGISTER, "seeder", SeederTrailerEntity::new, 1.5F, 1.0F);
    public static final RegistryObject<EntityType<FertilizerTrailerEntity>> FERTILIZER = VehicleUtil.registerVehicleEntity(REGISTER, "fertilizer", FertilizerTrailerEntity::new, 1.5F, 1.0F);

    /* Special Vehicles */
// Assuming SofacopterEntity::new is a valid constructor or method reference
    public static final RegistryObject<EntityType<SofacopterEntity>> SOFACOPTER = VehicleUtil.createModDependentEntityType(
            REGISTER,
            "cfm",
            "sofacopter",
            SofacopterEntity::new,
            1.0F,
            1.0F,
            false
    );

    /* Other */
//    public static final RegistryObject<EntityType<EntityJack>> JACK = REGISTER.register("jack", () ->
//            EntityType.Builder.of(EntityJack::new, MobCategory.MISC)
//                    .sized(0.0F, 0.0F)  // Set the entity's width and height
//                    .setTrackingRange(256)  // Set the tracking range
//                    .setUpdateInterval(1)  // Set the update interval
//                    .noSummon()  // Prevents the entity from being summoned
//                    .fireImmune()  // Makes the entity immune to fire
//                    .setShouldReceiveVelocityUpdates(true)  // Entity will receive velocity updates
//                    .build("jack")  // Build the EntityType with the given id
//    );

    public static void register(IEventBus bus)
    {
        REGISTER.register(bus);
    }

}
