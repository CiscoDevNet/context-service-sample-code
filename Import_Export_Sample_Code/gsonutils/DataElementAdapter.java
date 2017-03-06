package com.cisco.thunderhead.sample.importexport.gsonutils;

import com.cisco.thunderhead.DataElement;
import com.cisco.thunderhead.datatypes.ElementDataType;
import com.cisco.thunderhead.errors.ApiError;
import com.cisco.thunderhead.errors.ApiErrorType;
import com.cisco.thunderhead.errors.ApiException;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Make sure the data types are serialized and deserialized correctly.
 */
@SuppressWarnings("squid:S1161") // Can't @Override interface in Java 1.6
public class DataElementAdapter implements JsonDeserializer<DataElement>, JsonSerializer<DataElement> {

    public static final String TYPE = "type";
    public static final String CLASSIFICATION = "classification";


    private String deserializeElementDataType(JsonObject jobject) throws ApiException {

        //Deserialize ElementDataType
        String elementDataType = null;
        JsonElement typeElement = jobject.get(TYPE);
        if (typeElement != null) {
            try {
                elementDataType = typeElement.getAsString();
            } catch (Exception e) {
                ApiError apiError = new ApiError(ApiErrorType.ENUM_CONSTANT_NOT_PRESENT_TYPE_ERROR, TYPE,
                        String.format("%s is not a valid Metadata Type.", typeElement.toString()));
                throw new ApiException(apiError, e);
            }
        }
        else {
            throw new ApiException(new ApiError(ApiErrorType.DATAELEMENTS_DATA_MISSING_DATA_ERROR, "DataElement invalid",
                    "Missing the Type in Metadata"));
        }

        return elementDataType;
    }


    public DataElement deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jobject = json.getAsJsonObject();
        String elementDataType = null;
        String key = null;
        Object value = null;

        try {
            elementDataType = deserializeElementDataType(jobject);

            for (Map.Entry<String, JsonElement> entry : jobject.entrySet()) {
                String entryKey = entry.getKey();
                if (!entryKey.equals(TYPE) && !entryKey.equals(CLASSIFICATION)) {
                    // We have the key/value pair
                    key = entryKey;
                    value = getValueAsType(elementDataType, entry);
                }
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception exc) {
            throw new ApiException(new ApiError(ApiErrorType.ELEMENT_DATA_TYPE_ERROR, "DataElements type missmatch",
                    String.format("Unable to get the %s value of %s.", elementDataType, key)), exc);
        }
        return new DataElement(key, value, elementDataType);
    }

    public String deserializeElementClassification(JsonObject jobject) {
        String elementClassification = null;
        try {
            JsonElement classificationElement = jobject.get(CLASSIFICATION);
            if (classificationElement != null) {
                 elementClassification = classificationElement.toString();
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception exc) {
            throw new ApiException(new ApiError(ApiErrorType.ELEMENT_CLASSIFICATION_ERROR, CLASSIFICATION,
                    "Invalid Classification."), exc);
        }
        return elementClassification;
    }


    public JsonElement serialize(DataElement dataElement, Type typeOfSrc, JsonSerializationContext context) {
        if(typeOfSrc.equals(DataElement.class)) {
            Map<String, Object> serializedMap = new HashMap<String, Object>();
            serializedMap.put(dataElement.getDataKey(), dataElement.getDataValue());
            serializedMap.put(TYPE, dataElement.getType());
            return context.serialize(serializedMap, HashMap.class);
        }

        return null;
    }

    private Object getValueAsType(final String dataType, Map.Entry<String, JsonElement> entry) {
        Object value = null;
        if (ElementDataType.STRING.equals(dataType)) {
            value = entry.getValue().getAsString();
        } else if (ElementDataType.INTEGER.equals(dataType)) {
            value = Integer.valueOf(entry.getValue().getAsInt());
        } else if (ElementDataType.LONG.equals(dataType)) {
            value = Long.valueOf(entry.getValue().getAsLong());
        } else if (ElementDataType.DOUBLE.equals(dataType)) {
            value = Double.valueOf(entry.getValue().getAsDouble());
        } else if (ElementDataType.BOOLEAN.equals(dataType)) {
            value = Boolean.valueOf(entry.getValue().getAsBoolean());
        }
        return value;
    }
}
