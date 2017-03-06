package com.cisco.thunderhead.doc.examples;


import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.client.SearchParameters;
import com.cisco.thunderhead.datatypes.ElementClassification;
import com.cisco.thunderhead.datatypes.ElementDataType;
import com.cisco.thunderhead.datatypes.LanguageType;
import com.cisco.thunderhead.dictionary.Field;
import com.cisco.thunderhead.dictionary.FieldSet;
import com.cisco.thunderhead.pod.Pod;
import com.cisco.thunderhead.util.DataElementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.cisco.thunderhead.client.Operation.OR;

/**
 * This class shows how to create new fields and field sets.
 */
public class FieldSets {

    private final static Logger LOGGER = LoggerFactory.getLogger(FieldSets.class);

    /**
     * Create a field with translations.
     *
     * @param an initialized Context Service Client
     * @return a field with translations
     */
    public static Field createFieldWithTranslations(ContextServiceClient contextServiceClient){

        Map<String, Object> translations = new HashMap<>();
        translations.put(LanguageType.EN_US, "First Name");
        translations.put(LanguageType.FR, "Prenom");

        List<String> locales = new ArrayList<>();
        locales.add("en_US");
        locales.add("en_GB");
        locales.add("zh_CN");

        Field field = new Field("sdkExample_fieldOne", ElementClassification.PII, ElementDataType.STRING, false, translations, "true", locales);

        contextServiceClient.create(field);
        LOGGER.info("Created field: "+field.getId()+" with translations: "+translations.toString() + " and locales: " + String.join(", ", locales));

        return field;
    }

    /**
     * Create two new fields and add them to a field set.
     *
     * @param initialized Context Service Client
     * @return a field set
     */
    public static FieldSet createFieldSet(ContextServiceClient contextServiceClient){

        Field field1 = new Field("sdkExample_fieldOne", ElementClassification.UNENCRYPTED, ElementDataType.STRING, false, null);
        contextServiceClient.create(field1);

        Field field2 = new Field("sdkExample_fieldTwo", ElementClassification.UNENCRYPTED, ElementDataType.STRING, false, null);
        contextServiceClient.create(field2);

        FieldSet fieldset = new FieldSet("sdkExample_fieldSet", new HashSet<>(Arrays.asList(field1.getIdentifier(), field2.getIdentifier())), false);
        contextServiceClient.create(fieldset);

        LOGGER.info("Created fieldset: "+fieldset.getId()+" with fields: "+fieldset.getFields().toString());
        return fieldset;
    }

    /**
     * Add a new field to the specified field set.
     * 
     * @param initialized Context Service Client
     * @param field set a field set to be updated
     * @return a field set
     */
    public static FieldSet updateFieldSet(ContextServiceClient contextServiceClient, FieldSet fieldSet){

        Field field = new Field("sdkExample_fieldThree", ElementClassification.UNENCRYPTED, ElementDataType.STRING, false, null);

        Set<String> fields = fieldSet.getFields();
        fields.add(field.getIdentifier());
        fieldSet.setFields(fields);

        LOGGER.info("Updated fieldSet: "+fieldSet.getId()+" by adding field "+field.getId());
        return fieldSet;
    }

    /**
     * Delete the specified field set.
     * 
     * @param initialized Context Service Client
     * @param field set a field set
     */
    public static void deleteFieldSet(ContextServiceClient contextServiceClient, FieldSet fieldSet){
        contextServiceClient.delete(fieldSet);
        LOGGER.info("Deleted fieldSet: "+fieldSet.getId());
    }

    /**
     * Search field sets using a search query.
     *
     * @param initialized Context Service Client
     * @return List<FieldSet> a list of field set which matches the search query
     */
    public static List<FieldSet> searchFieldSet(ContextServiceClient contextServiceClient){

        LOGGER.info("Constructing search query ...");
        SearchParameters params =  new SearchParameters();
        params.add("id","sdkExample_fieldSet");

        LOGGER.info("Searching for fieldSet in ContextService based on query: " + params.toString());
        List<FieldSet> fieldsets = contextServiceClient.search(FieldSet.class, params, OR);
        for (FieldSet fieldset : fieldsets) {
            LOGGER.info("Found Field Set " + fieldset.getId());
        }
        return fieldsets;
    }

    /**
     * Search fields using a search query.
     * 
     * @param initialized Context Service Client
     * @return List<Field> a list of fields which matches the search query
     */
    public static List<Field> searchField(ContextServiceClient contextServiceClient){

        LOGGER.info("Constructing search query ...");
        SearchParameters params =  new SearchParameters();
        params.add("id", "sdkExample_fieldOne");
        params.add("id", "sdkExample_fieldTwo");
        params.add("id", "sdkExample_fieldThree");

        LOGGER.info("Searching for fields in ContextService based on query: " + params.toString());
        List<Field> fields = contextServiceClient.search(Field.class, params, OR);
        for (Field field : fields) {
            LOGGER.info("Found Field " + field.getId());
        }
        return fields;
    }

    /**
     * This method describes usage of Cisco FieldSets to create a Pod in ContextService.
     * Similarly, Customer and Request objects can be created in ContextService.
     * @param initialized Context Service Client
     * @return a pod uses Cisco base field set
     */
    public static Pod ciscoBaseFieldSetUsage(ContextServiceClient contextServiceClient){

        Pod pod = new Pod();
        pod.setDataElements(DataElementUtils.convertDataMapToSet(
                new HashMap<String, Object>() {{
                    put("Context_Notes", "Notes about this context.");
                    put("Context_POD_Activity_Link", "http://myservice.example.com/service/ID/xxxx");
                }}
        ));
        pod.setFieldsets(Arrays.asList("cisco.base.pod"));

        contextServiceClient.create(pod);
        LOGGER.info("Created Pod in ContextService using Cisco FieldSet. "+pod.toString());

        return pod;

    }

    /**
     * This method describe usage of Cisco FieldSets and Custom FieldSet to create a Pod
     * in ContextService. Similarly, Customer and Request objects can be created.
     * @param initialized Context Service Client
     * @return a pod uses Custom FieldSet
     */
    public static Pod customFieldSetUsage(ContextServiceClient contextServiceClient){

        LOGGER.info("Creating custom fieldSet ...");

        Field field1 = new Field("sdkExample_fieldOne", ElementClassification.UNENCRYPTED, ElementDataType.STRING, false, null);
        contextServiceClient.create(field1);

        Field field2 = new Field("sdkExample_fieldTwo", ElementClassification.UNENCRYPTED, ElementDataType.STRING, false, null);
        contextServiceClient.create(field2);

        FieldSet fieldset = new FieldSet("sdkExample_fieldSet", new HashSet<>(Arrays.asList(field1.getIdentifier(), field2.getIdentifier())), false);
        contextServiceClient.create(fieldset);

        LOGGER.info("Created custom fieldSet " + fieldset.getId() + " containing fields: " + field1.getId() + " " + field2.getId());

        Pod pod = new Pod();
        pod.setFieldsets(Arrays.asList(fieldset.getId()));
        pod.setDataElements( DataElementUtils.convertDataMapToSet(
                new HashMap<String, Object>() {{
                    put("sdkExample_fieldOne", "receipt of purchase");
                    put("sdkExample_fieldTwo", "10 million dollars");
                }}
        ));

        contextServiceClient.create(pod);
        LOGGER.info("Created Pod in ContextService that uses fieldSet "+fieldset.getId()+". Pod: "+pod.toString());

        return pod;
    }

    /**
     * This method describes usage of Cisco base field set and Custom field set to create a Pod
     * in ContextService. Similarly, Customer and Request can be created.
     * @param initialized Context Service Client
     * @return a pod which uses Cisco Base FieldSet and Custom FieldSet
     */
    public static Pod customAndCiscoFieldSetUsage(ContextServiceClient contextServiceClient){

        LOGGER.info("Creating custom fieldSet ...");

        Field field1 = new Field("sdkExample_fieldOne", ElementClassification.UNENCRYPTED, ElementDataType.STRING, false, null);
        contextServiceClient.create(field1);

        Field field2 = new Field("sdkExample_fieldTwo", ElementClassification.UNENCRYPTED, ElementDataType.STRING, false, null);
        contextServiceClient.create(field2);

        FieldSet fieldset = new FieldSet("sdkExample_fieldSet", new HashSet<>(Arrays.asList(field1.getIdentifier(), field2.getIdentifier())), false);
        contextServiceClient.create(fieldset);

        LOGGER.info("Created custom fieldSet "+fieldset.getId()+" containing field: "+field1.getId());

        Pod pod = new Pod();
        pod.setFieldsets(Arrays.asList(fieldset.getId(), "cisco.base.pod"));
        pod.setDataElements(DataElementUtils.convertDataMapToSet(
                new HashMap<String, Object>() {{
                    put("Context_Notes", "Notes about this context.");
                    put("sdkExample_fieldOne", "Receipt of purchase");
                }}
        ));

        contextServiceClient.create(pod);
        LOGGER.info("Created Pod in ContextService that uses fieldSet "+fieldset.getId()+" and cisco.base.pod . Pod: "+pod.toString());

        return pod;
    }

}
