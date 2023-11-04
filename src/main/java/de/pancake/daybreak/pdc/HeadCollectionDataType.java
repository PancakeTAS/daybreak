package de.pancake.daybreak.pdc;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistent data type for head collections.
 * @author Pancake
 */
public class HeadCollectionDataType implements PersistentDataType<String, Map> {

    /**
     * Convert a complex value to a primitive value.
     * @param complex the complex object instance
     * @param context the context this operation is running in
     * @return the primitive value
     */
    @Override
    public @NotNull String toPrimitive(@NotNull Map complex, @NotNull PersistentDataAdapterContext context) {
        return ((Map<UUID, Integer>) complex).entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining(","));
    }

    /**
     * Convert a primitive value to a complex value.
     * @param primitive the primitive value
     * @param context the context this operation is running in
     * @return the complex object instance
     */
    @Override
    public @NotNull Map fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        var map = new HashMap<UUID, Integer>();
        for (var entry : primitive.split(",")) {
            var split = entry.split(":");
            map.put(UUID.fromString(split[0]), Integer.parseInt(split[1]));
        }
        return map;
    }

    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<Map> getComplexType() {
        return Map.class;
    }

}
