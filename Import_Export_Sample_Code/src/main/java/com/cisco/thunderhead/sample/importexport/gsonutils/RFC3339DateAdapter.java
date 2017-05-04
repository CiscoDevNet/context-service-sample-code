package com.cisco.thunderhead.sample.importexport.gsonutils;

import com.cisco.thunderhead.util.RFC3339Date;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.ParseException;

/**
 * Adapter to convert between a RFC3339Date object and a String.
 */
public class RFC3339DateAdapter implements JsonSerializer<RFC3339Date>, JsonDeserializer<RFC3339Date> {

    @Override
    public RFC3339Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return new RFC3339Date(json.getAsJsonPrimitive().getAsString());
        } catch (ParseException e) {
            throw new JsonParseException(e);
        }
    }

    @Override
    public JsonElement serialize(RFC3339Date date, Type typeOfT, JsonSerializationContext context) {
        return context.serialize(date.toString());
    }
}
