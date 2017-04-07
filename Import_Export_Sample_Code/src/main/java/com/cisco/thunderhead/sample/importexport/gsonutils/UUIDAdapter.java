package com.cisco.thunderhead.sample.importexport.gsonutils;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.UUID;

/**
 * Class to serialize/deserialize the UUID.
 */
public class UUIDAdapter implements JsonSerializer<UUID>, JsonDeserializer<UUID>
{
    /**
     * Converts a JSON element into a UUID.
     *
     * @param json is the JSON element to deserialize
     * @param typeOfT is the type (UUID)
     * @param context is the context used to deserialize
     * @return UUID that has been converted
     * @throws JsonParseException when it can not be converted
     */
    @Override
    public UUID deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
            JsonParseException {

        return UUID.fromString(json.getAsJsonPrimitive().getAsString());
    }

    /**
     * Converts a UUID into a JSON element.
     *
     * @param uuid is the UUID to serialize
     * @param typeOfT is the type (UUID)
     * @param context is the context used to serialize
     * @return JsonElement which is the converted UUID
     */
    @Override
    public JsonElement serialize(UUID uuid, Type typeOfT, JsonSerializationContext context) {
        return context.serialize(uuid.toString());
    }
}
