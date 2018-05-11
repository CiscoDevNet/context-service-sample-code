package com.cisco.thunderhead.rest;

import com.cisco.thunderhead.ContextObject;
import com.cisco.thunderhead.DataElement;
import com.cisco.thunderhead.ExposeMember;
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
    @ExposeMember private List<String> fieldsets = new LinkedList<String>();
    @ExposeMember private List<ContextDataElement> dataElements = new ArrayList<>();
    @ExposeMember private RFC3339Date created;
    @ExposeMember private RFC3339Date lastUpdated;

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
        for (DataElement dataElement : bean.getDataElements()) {
            ContextDataElement contextDataElement = new ContextDataElement(
                    dataElement.getDataKey(),
                    dataElement.getDataValue().toString(),
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
        @ExposeMember private final String value;
        @ExposeMember private final String type;

        public ContextDataElement(String key, String value, String type) {
            this.key = key;
            this.value = value;
            this.type = type;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    public static void copyToContextBean(ContextObject dest, RESTContextObject src) {
        if (!dest.getType().equals(src.getType())) {
            throw new ContextException("Object types do not match");
        }

        dest.setParentId(src.parentId);
        dest.setCustomerId(src.customerId);
        dest.setCreated(src.getCreated());
        dest.setLastUpdated(src.getLastUpdated());
        dest.setFieldsets(src.getFieldsets());
        dest.setId(src.getId());
        Map<String,Object> dataElements = new HashMap<>();
        for (ContextDataElement dataElement : src.dataElements) {
            dataElements.put(dataElement.key, dataElement.value);
        }
        dest.setDataElements(DataElementUtils.convertDataMapToSet(dataElements));
    }
}
