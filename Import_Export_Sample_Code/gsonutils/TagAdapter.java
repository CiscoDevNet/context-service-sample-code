package com.cisco.thunderhead.sample.importexport.gsonutils;

import com.cisco.thunderhead.tag.Tag;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Remove the object wrapper on tag, so that tags are just a list of tag names (string).
 */
public class TagAdapter implements JsonDeserializer<Tag>, JsonSerializer<Tag> {
    @Override
    public Tag deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new Tag(json.getAsString());
    }

    @Override
    public JsonElement serialize(Tag tag, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(tag.getName());
    }
}
