package com.cisco.thunderhead.sample.importexport.gsonutils;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * This class transforms dates into the number of seconds that have elapsed since January 1, 1970 (midnight UTC/GMT).
 */
public class DateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new Date(json.getAsJsonPrimitive().getAsLong());
    }

    @Override
    public JsonElement serialize(Date date, Type typeOfT, JsonSerializationContext context) {
        return context.serialize(date.getTime());
    }

}