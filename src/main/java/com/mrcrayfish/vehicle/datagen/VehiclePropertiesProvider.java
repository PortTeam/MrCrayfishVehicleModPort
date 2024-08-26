package com.mrcrayfish.vehicle.datagen;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Author: MrCrayfish
 */
public abstract class VehiclePropertiesProvider implements DataProvider
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(VehicleProperties.class, new VehicleProperties.Serializer()).create();

    private final DataGenerator generator;
    private final Map<ResourceLocation, VehicleProperties> vehiclePropertiesMap = new HashMap<>();
    private boolean scaleWheels = false;

    // Placeholder cache implementation
    private final Map<Path, String> cache = new HashMap<>();

    protected VehiclePropertiesProvider(DataGenerator generator)
    {
        this.generator = generator;
    }

    public void setScaleWheels(boolean scaleWheels)
    {
        this.scaleWheels = scaleWheels;
    }

    protected final void add(EntityType<? extends VehicleEntity> type, VehicleProperties.Builder builder)
    {
        this.add(EntityType.getKey(type), builder); // Use getKey to get the correct ResourceLocation
    }

    protected final void add(ResourceLocation id, VehicleProperties.Builder builder)
    {
        this.vehiclePropertiesMap.put(id, builder.build(this.scaleWheels));
    }

    public Map<ResourceLocation, VehicleProperties> getVehiclePropertiesMap()
    {
        return ImmutableMap.copyOf(this.vehiclePropertiesMap);
    }

    public abstract void registerProperties();

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        return CompletableFuture.runAsync(() -> {
            this.vehiclePropertiesMap.clear();
            this.registerProperties();
            this.vehiclePropertiesMap.forEach((id, properties) -> {
                String modId = id.getNamespace();
                String vehicleId = id.getPath();
                Path path = this.generator.getPackOutput().getOutputFolder().resolve("data/" + modId + "/vehicles/properties/" + vehicleId + ".json");
                saveJsonToFile(path, GSON.toJson(properties));

                if (properties.getCosmetics().isEmpty()) {
                    return;
                }

                path = this.generator.getPackOutput().getOutputFolder().resolve("data/" + modId + "/vehicles/cosmetics/" + vehicleId + ".json");
                JsonObject object = new JsonObject();
                object.addProperty("replace", false);
                JsonObject validModels = new JsonObject();
                properties.getCosmetics().forEach((cosmeticId, cosmeticProperties) -> {
                    JsonArray array = new JsonArray();
                    cosmeticProperties.getModelLocations().forEach(location -> {
                        List<ResourceLocation> disabledCosmetics = cosmeticProperties.getDisabledCosmetics().getOrDefault(location, Collections.emptyList());
                        if (disabledCosmetics.isEmpty()) {
                            array.add(location.toString());
                        } else {
                            JsonObject modelObject = new JsonObject();
                            modelObject.addProperty("model", location.toString());
                            JsonArray disables = new JsonArray();
                            disabledCosmetics.forEach(disabledCosmeticId -> disables.add(disabledCosmeticId.toString()));
                            modelObject.add("disables", disables);
                            array.add(modelObject);
                        }
                    });
                    validModels.add(cosmeticId.toString(), array);
                });
                object.add("valid_models", validModels);
                saveJsonToFile(path, GSON.toJson(object));
            });
        });
    }

    private void saveJsonToFile(Path path, String json) {
        try {
            String hash = sha1Hash(json);
            if (!Objects.equals(cache.get(path), hash) || !Files.exists(path)) {
                Files.createDirectories(path.getParent());
                try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                    writer.write(json);
                }
                cache.put(path, hash);
            }
        } catch (IOException e) {
            LOGGER.error("Couldn't save vehicle properties to {}", path, e);
        }
    }

    private String sha1Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not found", e);
        }
    }

    @Nonnull
    @Override
    public String getName()
    {
        return "VehicleProperties";
    }
}
