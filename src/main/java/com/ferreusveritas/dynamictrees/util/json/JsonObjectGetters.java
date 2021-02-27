package com.ferreusveritas.dynamictrees.util.json;

import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatures;
import com.ferreusveritas.dynamictrees.systems.genfeatures.VinesGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.GenFeatureProperty;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.GenFeaturePropertyValue;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Holds {@link IJsonObjectGetter} objects, which can be used to obtain objects from
 * {@link JsonElement} objects.
 *
 * @author Harley O'Connor
 */
public final class JsonObjectGetters {

    private static final Set<JsonObjectGetterHolder<?>> OBJECT_GETTERS = Sets.newHashSet();

    /** Returned by {@link #getObjectGetter(Class)} if an object getter wasn't found. */
    public static final class NullObjectGetter<T> implements IJsonObjectGetter<T> {
        @Override
        public boolean isValidGetter() {
            return false;
        }

        @Override
        public ObjectFetchResult<T> get(final JsonElement jsonElement) {
            return ObjectFetchResult.failure("Could not get Json object getter for json element: " + jsonElement.toString() + ".");
        }
    }

    /**
     * Gets the {@link IJsonObjectGetter} for the given class type.
     *
     * @param objectClass The {@link Class} of the object to get.
     * @param <T> The type of the object.
     * @return The {@link IJsonObjectGetter} for the class, or {@link NullObjectGetter} if it wasn't found.
     */
    @SuppressWarnings("unchecked")
    public static <T> IJsonObjectGetter<T> getObjectGetter (final Class<T> objectClass) {
        return OBJECT_GETTERS.stream().filter(jsonObjectGetterHolder -> jsonObjectGetterHolder.objectClass.equals(objectClass))
                .findFirst().map(jsonObjectGetterHolder -> (IJsonObjectGetter<T>) jsonObjectGetterHolder.objectGetter).orElse(new NullObjectGetter<>());
    }

    /**
     * Registers an {@link IJsonObjectGetter} to the registry.
     *
     * @param objectClass The {@link Class} of the object that will be obtained.
     * @param objectGetter The {@link IJsonObjectGetter} to register.
     * @param <T> The type of the object getter.
     * @return The {@link IJsonObjectGetter} given.
     */
    public static <T> IJsonObjectGetter<T> register(final Class<T> objectClass, final IJsonObjectGetter<T> objectGetter) {
        OBJECT_GETTERS.add(new JsonObjectGetterHolder<>(objectClass, objectGetter));
        return objectGetter;
    }

    /**
     * Holds an {@link IJsonObjectGetter} for the relevant class.
     *
     * @param <T> The type of the object getter.
     */
    private static final class JsonObjectGetterHolder<T> {
        private final Class<T> objectClass;
        private final IJsonObjectGetter<T> objectGetter;

        public JsonObjectGetterHolder(final Class<T> objectClass, final IJsonObjectGetter<T> objectGetter) {
            this.objectClass = objectClass;
            this.objectGetter = objectGetter;
        }
    }

    public static final IJsonObjectGetter<String> STRING_GETTER = register(String.class, jsonElement -> {
        if (!jsonElement.isJsonPrimitive() || !jsonElement.getAsJsonPrimitive().isString())
            return ObjectFetchResult.failure("Json element was not a string.");

        return ObjectFetchResult.success(jsonElement.getAsString());
    });

    public static final IJsonObjectGetter<Boolean> BOOLEAN_GETTER = register(Boolean.class, jsonElement -> {
        if (!jsonElement.isJsonPrimitive() || !jsonElement.getAsJsonPrimitive().isBoolean())
            return ObjectFetchResult.failure("Json element was not a boolean.");

        return ObjectFetchResult.success(jsonElement.getAsBoolean());
    });

    public static final IJsonObjectGetter<Number> NUMBER_GETTER = register(Number.class, jsonElement -> {
        if (!jsonElement.isJsonPrimitive() || !jsonElement.getAsJsonPrimitive().isNumber())
            return ObjectFetchResult.failure("Json element was not a number.");

        return ObjectFetchResult.success(jsonElement.getAsNumber());
    });

    public static final IJsonObjectGetter<Integer> INTEGER_GETTER = register(Integer.class, jsonElement -> {
        final ObjectFetchResult<Number> numberFetch = NUMBER_GETTER.get(jsonElement);

        if (!numberFetch.wasSuccessful())
            return ObjectFetchResult.failureFromOther(numberFetch);

        return ObjectFetchResult.success(numberFetch.getValue().intValue());
    });

    public static final IJsonObjectGetter<Double> DOUBLE_GETTER = register(Double.class, jsonElement -> {
        final ObjectFetchResult<Number> numberFetch = NUMBER_GETTER.get(jsonElement);

        if (!numberFetch.wasSuccessful())
            return ObjectFetchResult.failureFromOther(numberFetch);

        return ObjectFetchResult.success(numberFetch.getValue().doubleValue());
    });

    public static final IJsonObjectGetter<Float> FLOAT_GETTER = register(Float.class, jsonElement -> {
        final ObjectFetchResult<Number> numberFetch = NUMBER_GETTER.get(jsonElement);

        if (!numberFetch.wasSuccessful())
            return ObjectFetchResult.failureFromOther(numberFetch);

        return ObjectFetchResult.success(numberFetch.getValue().floatValue());
    });

    public static final IJsonObjectGetter<JsonObject> JSON_OBJECT_GETTER = register(JsonObject.class, jsonElement -> {
        if (!jsonElement.isJsonObject())
            return ObjectFetchResult.failure("Json element was not a json object.");

        return ObjectFetchResult.success(jsonElement.getAsJsonObject());
    });

    public static final IJsonObjectGetter<JsonArray> JSON_ARRAY_GETTER = register(JsonArray.class, jsonElement -> {
        if (!jsonElement.isJsonArray())
            return ObjectFetchResult.failure("Json element was not a json array.");

        return ObjectFetchResult.success(jsonElement.getAsJsonArray());
    });

    public static final IJsonObjectGetter<ResourceLocation> RESOURCE_LOCATION_GETTER = register(ResourceLocation.class, jsonElement -> {
        final ObjectFetchResult<String> stringFetchResult = STRING_GETTER.get(jsonElement);

        if (!stringFetchResult.wasSuccessful())
            return ObjectFetchResult.failureFromOther(stringFetchResult);

        try {
            return ObjectFetchResult.success(new ResourceLocation(stringFetchResult.getValue()));
        } catch (ResourceLocationException e) {
            return ObjectFetchResult.failure("Json element was not a valid resource location: " + e.getMessage());
        }
    });

    public static final IJsonObjectGetter<Block> BLOCK_GETTER = register(Block.class, new ForgeRegistryEntryGetter<>(ForgeRegistries.BLOCKS, "block"));
    public static final IJsonObjectGetter<Item> ITEM_GETTER = register(Item.class, new ForgeRegistryEntryGetter<>(ForgeRegistries.ITEMS, "item"));

    public static final IJsonObjectGetter<LeavesProperties> LEAVES_PROPERTIES_GETTER = register(LeavesProperties.class, new ForgeRegistryEntryGetter<>(LeavesProperties.REGISTRY, "leaves properties"));
    public static final IJsonObjectGetter<GrowthLogicKit> GROWTH_LOGIC_KIT_GETTER = register(GrowthLogicKit.class, new ForgeRegistryEntryGetter<>(GrowthLogicKit.REGISTRY, "growth logic kit"));
    public static final IJsonObjectGetter<GenFeature> GEN_FEATURE_GETTER = register(GenFeature.class, new ForgeRegistryEntryGetter<>(GenFeature.REGISTRY, "gen feature"));
    public static final IJsonObjectGetter<TreeFamily> TREE_FAMILY_GETTER = register(TreeFamily.class, new ForgeRegistryEntryGetter<>(TreeFamily.REGISTRY, "tree family"));
    public static final IJsonObjectGetter<Species> SPECIES_GETTER = register(Species.class, new ForgeRegistryEntryGetter<>(Species.REGISTRY, "species"));

    public static final IJsonObjectGetter<ConfiguredGenFeature<GenFeature>> CONFIGURED_GEN_FEATURE_GETTER = register(ConfiguredGenFeature.NULL_CONFIGURED_FEATURE_CLASS, new ConfiguredGenFeatureGetter());

    public static final IJsonObjectGetter<Seed> SEED_GETTER = register(Seed.class, jsonElement -> {
        final ObjectFetchResult<Item> itemFetchResult = ITEM_GETTER.get(jsonElement);

        if (!itemFetchResult.wasSuccessful())
            return ObjectFetchResult.failureFromOther(itemFetchResult);

        final Item item = itemFetchResult.getValue();

        if (!(item instanceof Seed))
            return ObjectFetchResult.failure("Item '" + item.getRegistryName() + "' was not a Seed.");

        return ObjectFetchResult.success(((Seed) item));
    });

    // Random enum getters.
    public static final IJsonObjectGetter<VinesGenFeature.VineType> VINE_TYPE_GETTER = register(VinesGenFeature.VineType.class, new EnumGetter<>(VinesGenFeature.VineType.class));

}
