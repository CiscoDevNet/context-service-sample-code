package com.cisco.thunderhead.sample.importexport.gsonutils;

import com.cisco.thunderhead.DataElement;
import com.cisco.thunderhead.tag.Tag;
import com.cisco.thunderhead.util.RFC3339Date;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;
import java.util.UUID;

/**
 * Factory that can be used to obtain GSON object with Context Service custom marshallers
 * to convert CS Java Objects into their JSON representation.
 * It can also be used to convert a JSON string to an equivalent Java object.
 */
public class CSGsonFactory {

    private CSGsonFactory(){}

    /**
     * Obtain GSON object. PrintPretty is default=False
     * @return
     */
    public static Gson getCSJson(){
        return getCSJson(false);
    }

    /**
     * Obtain GSON object
     * @param printPretty can be set False or True
     * @return
     */
    public static Gson getCSJson(boolean printPretty){

        GsonBuilder gsonBuilder = new GsonBuilder();
        if (printPretty) gsonBuilder.setPrettyPrinting();

        return gsonBuilder
                .registerTypeAdapter(Date.class, new DateAdapter())
                .registerTypeAdapter(RFC3339Date.class, new RFC3339DateAdapter())
                .registerTypeAdapter(Tag.class, new TagAdapter())
                .registerTypeAdapter(DataElement.class, new DataElementAdapter())
                .registerTypeAdapter(UUID.class, new UUIDAdapter())
                .create();
    }
}
