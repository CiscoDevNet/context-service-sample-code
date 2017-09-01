package com.cisco.thunderhead.rest;

import com.cisco.thunderhead.ContextBean;
import com.cisco.thunderhead.DataElement;
import com.cisco.thunderhead.ExposeMember;
import com.cisco.thunderhead.customer.Customer;
import com.cisco.thunderhead.pod.Pod;
import com.cisco.thunderhead.request.Request;
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
public class ContextObject {
    @ExposeMember private UUID id;
    @ExposeMember private String type;
    @ExposeMember private List<String> fieldsets = new LinkedList<String>();
    @ExposeMember private List<ContextDataElement> dataElements = new ArrayList<>();
    @ExposeMember private RFC3339Date created;
    @ExposeMember private RFC3339Date lastUpdated;

    public ContextObject() {
    }

    /**
     * Converts from a CS SDK ContextBean to the REST version.
     */
    public ContextObject(ContextBean bean) {
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
        type = determineType(bean);
    }

    public UUID getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public static String determineType(ContextBean contextBean) {
        String type;
        if (contextBean instanceof Customer) {
            type = "customer";
        } else if (contextBean instanceof Pod) {
            type = "pod";
        } else if (contextBean instanceof Request) {
            type = "request";
        } else {
            type = "unknown";
        }
        return type;
    }

    public static Class<? extends ContextBean> determineTypeClass(String type) {
        Class<? extends ContextBean> clazz;
        switch (type.toLowerCase()) {
            case "customer":
                clazz = Customer.class;
                break;
            case "pod":
                clazz = Pod.class;
                break;
            case "request":
                clazz = Request.class;
                break;
            default:
                clazz = null;
        }
        return clazz;
    }

    public static void copyToContextBean(ContextBean dest, ContextObject src) {
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
