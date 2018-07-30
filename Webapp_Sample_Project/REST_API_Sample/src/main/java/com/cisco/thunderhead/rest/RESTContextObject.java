package com.cisco.thunderhead.rest;

import com.cisco.thunderhead.ContextObject;
import com.cisco.thunderhead.DataElement;
import com.cisco.thunderhead.ExposeMember;
import com.cisco.thunderhead.datatypes.ElementDataType;
import com.cisco.thunderhead.datatypes.PodMediaType;
import com.cisco.thunderhead.util.DataElementUtils;
import com.cisco.thunderhead.util.RFC3339Date;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a context service object.
 */
public class RESTContextObject {
    @ExposeMember private UUID id;
    @ExposeMember private String type;
    @ExposeMember private UUID customerId;
    @ExposeMember private UUID parentId;
    @ExposeMember private String mediaType;
    @ExposeMember private List<String> fieldsets = new LinkedList<String>();
    @ExposeMember private List<ContextDataElement> dataElements = new ArrayList<>();
    @ExposeMember private RFC3339Date created;
    @ExposeMember private RFC3339Date lastUpdated;

    private static final Map<Class<?>, String> DATA_TYPE_MAP = new HashMap<Class<?>, String>();
    static {
        DATA_TYPE_MAP.put(Integer.class, ElementDataType.INTEGER);
        DATA_TYPE_MAP.put(Double.class, ElementDataType.DOUBLE);
        DATA_TYPE_MAP.put(Boolean.class, ElementDataType.BOOLEAN);
        DATA_TYPE_MAP.put(String.class, ElementDataType.STRING);
    }

    public RESTContextObject() {
    }

    /**
     * Converts from a CS SDK ContextObject to the REST version.
     * @param bean
     */
    public RESTContextObject(ContextObject bean) {
        type = bean.getType();
        customerId = bean.getCustomerId();
        parentId = bean.getParentId();
        mediaType = bean.getMediaType();
        for (DataElement dataElement : bean.getDataElements()) {
            ContextDataElement contextDataElement = new ContextDataElement(
                    dataElement.getDataKey(),
                    dataElement.getDataValue(),
                    dataElement.getType());
            dataElements.add(contextDataElement);
        }
        fieldsets = bean.getFieldsets();
        created = bean.getCreated();
        lastUpdated = bean.getLastUpdated();
        id = bean.getId();
    }

    public UUID getId() {
        return id;
    }

    public String getType() {
        return type;
    }
    public UUID getCustomerId() {
        return this.customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getParentId() {
        return this.parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public void setType(String type) {
        this.type = type != null ? type.toLowerCase() : null;
    }

    public List<String> getFieldsets() { return fieldsets; }

    public void setFieldsets(List<String> fieldsets) {
        this.fieldsets = fieldsets;
    }

    public List<ContextDataElement> getDataElements() {
        return dataElements;
    }

    public void setDataElements(List<ContextDataElement> dataElements) {
        this.dataElements = dataElements;
    }

    public RFC3339Date getCreated() {
        return created;
    }

    public RFC3339Date getLastUpdated() {
        return lastUpdated;
    }

    public static class ContextDataElement {
        @ExposeMember private String key;
        @ExposeMember private Object value;
        @ExposeMember private final String type;

        public ContextDataElement(String key, Object value, String type) {
            this.key = key;
            this.value = value;
            this.type = type;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String getType() {
            return type;
        }

    }

    public static void copyToContextBean(ContextObject dest, RESTContextObject src) {
        if (!dest.getType().equals(src.getType())) {
            throw new ContextException("Object types do not match");
        }

        if (src.mediaType != null && ContextObject.Types.POD.equals(src.getType()) && !PodMediaType.isValidType(src.mediaType)) {
            throw new ContextException("Media type: " + src.mediaType + " for activity object is invalid.");
        }

        dest.setParentId(src.parentId);
        dest.setCustomerId(src.customerId);
        dest.setCreated(src.getCreated());
        dest.setLastUpdated(src.getLastUpdated());
        dest.setMediaType(src.mediaType);
        dest.setFieldsets(src.getFieldsets());
        dest.setId(src.getId());
        Map<String,Object> dataElements = new HashMap<>();
        for (ContextDataElement dataElement : src.dataElements) {
            fixDataElementType(dataElement);
            dataElements.put(dataElement.key, dataElement.value);
        }

        dest.setDataElements(DataElementUtils.convertDataMapToSet(dataElements));
    }

    /**
     * GsonBuilder is automatically guessing that integers are double values, so it is converting 1 to 1.0.  This method
     * detects that the type should be integer and then converts from a double to an integer before attempting to create
     * or update the ContextObject.
     * @param dataElement
     * @throws ContextException
     */
    private static void fixDataElementType(ContextDataElement dataElement) throws ContextException {
        String type = dataElement.getType();
        String typeDerivedFromValue = DATA_TYPE_MAP.get(dataElement.getValue().getClass());

        if(type == null && !typeDerivedFromValue.equalsIgnoreCase(ElementDataType.STRING))
        {
            throw new ContextException("Must set data element type for non-string data element fields, data element " + dataElement.getKey() + " type was null.");
        }else if(type != null && type.equalsIgnoreCase(ElementDataType.INTEGER))
        {
            Double dataElementValue = (Double)dataElement.getValue();
            dataElement.setValue(Integer.valueOf(dataElementValue.intValue()));
        }
    }
}
