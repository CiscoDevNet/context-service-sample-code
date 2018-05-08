package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.ContextObject;
import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.util.DataElementUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CreateEntities {

    /**
     * Create POD with default fields and fieldsets.
     * @param contextServiceClient an initialized ContextServiceClient
     * @return a newly-created pod with the cisco.base.pod fieldset
     */
    public static ContextObject createPodWithBaseFieldset(ContextServiceClient contextServiceClient) {
        ContextObject pod = new ContextObject(ContextObject.Types.POD);
        pod.setDataElements(
                DataElementUtils.convertDataMapToSet(
                        new HashMap<String, Object>() {{
                            put("Context_Notes", "Notes about this context.");
                            put("Context_POD_Activity_Link", "http://myservice.example.com/service/ID/xxxx");
                        }}
                )
        );
        pod.setFieldsets(Arrays.asList("cisco.base.pod"));
        contextServiceClient.create(pod);
        return pod;
    }

    /**
     * Create a Customer with default fields and fieldsets.
     * @param contextServiceClient an initialized ContextServiceClient
     * @return a newly-created customer with the cisco.base.customer fieldset
     */
    public static ContextObject createCustomerWithBaseFieldset(ContextServiceClient contextServiceClient) {
        ContextObject customer = new ContextObject(ContextObject.Types.CUSTOMER);
        customer.setDataElements(
                DataElementUtils.convertDataMapToSet(
                        new HashMap<String, Object>() {{
                            put("Context_Work_Email", "john.doe@example.com");
                            put("Context_Work_Phone", "555-555-5555");
                            put("Context_First_Name", "John");
                            put("Context_Last_Name", "Doe");
                            put("Context_Street_Address_1", "123 Sesame Street");
                            put("Context_City", "Detroit");
                            put("Context_State", "MI");
                            put("Context_Country", "US");
                            put("Context_ZIP", "90210");
                        }}
                )
        );
        customer.setFieldsets(Arrays.asList("cisco.base.customer"));
        contextServiceClient.create(customer);
        return customer;
    }

    /**
     * Create a Request with default fields and fieldsets.
     * @param contextServiceClient an initialized ContextServiceClient
     * @return a newly-created request with the cisco.base.request fieldset
     */
    public static ContextObject createRequestWithBaseFieldset(ContextServiceClient contextServiceClient, ContextObject customer) {
        ContextObject request = new ContextObject(ContextObject.Types.REQUEST);
        request.setDataElements(
                DataElementUtils.convertDataMapToSet(
                        new HashMap<String, Object>() {{
                            put("Context_Description", "Request1 Description");
                            put("Context_Title", "Request1 Title");
                        }}
                )
        );
        request.setFieldsets(Arrays.asList("cisco.base.request"));
        request.setCustomerId(customer.getId());
        contextServiceClient.create(request);
        return request;
    }

    /**
     * Create a POD and associate a customer to the POD.
     * @param contextServiceClient an initialized ContextServiceClient
     * @param customer a pre-existing Customer object
     * @return a POD associated with the Customer
     */
    public static ContextObject createPodWithCustomer(ContextServiceClient contextServiceClient, ContextObject customer) {
        ContextObject pod = new ContextObject(ContextObject.Types.POD);
        pod.setDataElements(
                DataElementUtils.convertDataMapToSet(
                        new HashMap<String, Object>() {{
                            put("Context_Notes", "Notes about this context.");
                            put("Context_POD_Activity_Link", "http://myservice.example.com/service/ID/xxxx");
                        }}
                )
        );
        pod.setFieldsets(Arrays.asList("cisco.base.pod"));
        pod.setCustomerId(customer.getId());
        contextServiceClient.create(pod);
        return pod;
    }

    /**
     * Create a POD with an associated request.
     * @param contextServiceClient an initialized ContextServiceClient
     * @param request a pre-existing Request object
     * @return a pod associated with the Request
     */
    public static ContextObject createPodWithRequest(ContextServiceClient contextServiceClient, ContextObject request) {
        ContextObject pod = new ContextObject(ContextObject.Types.POD);
        pod.setDataElements(
                DataElementUtils.convertDataMapToSet(
                        new HashMap<String, Object>() {{
                            put("Context_Notes", "Notes about this context.");
                            put("Context_POD_Activity_Link", "http://myservice.example.com/service/ID/xxxx");
                        }}
                )
        );
        pod.setFieldsets(Arrays.asList("cisco.base.pod"));
        pod.setParentId(request.getId());
        contextServiceClient.create(pod);
        return pod;
    }

    /**
     * Create a POD with an associated customer and an associated request.
     * @param contextServiceClient an initialized ContextServiceClient
     * @param customer pre-existing Customer object
     * @param request pre-existing Request object
     * @return a pod associated with both a customer and a request
     */
    public static ContextObject createPodWithCustomerAndRequest(ContextServiceClient contextServiceClient, ContextObject customer, ContextObject request) {
        ContextObject pod = new ContextObject(ContextObject.Types.POD);
        pod.setDataElements(
                DataElementUtils.convertDataMapToSet(
                        new HashMap<String, Object>() {{
                            put("Context_Notes", "Notes about this context.");
                            put("Context_POD_Activity_Link", "http://myservice.example.com/service/ID/xxxx");
                        }}
                )
        );
        pod.setFieldsets(Arrays.asList("cisco.base.pod"));
        pod.setCustomerId(customer.getId());
        pod.setParentId(request.getId());
        contextServiceClient.create(pod);
        return pod;
    }

    /**
     * Create multiple PODS associated with one customer.
     * One Customer can have many Pods associated with it.
     * @param contextServiceClient an initialized ContextServiceClient
     * @param customer a pre-existing Customer object
     * @return a List of Pods associated with the same Customer
     */
    public static List<ContextObject> createMultiplePodsWithSameCustomer(ContextServiceClient contextServiceClient, ContextObject customer) {
        List<ContextObject> pods = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            pods.add(createPodWithCustomer(contextServiceClient, customer));
        }
        return pods;
    }

    /**
     * Demonstrate the many-to-one relationship between Customers and Pods
     * One Customer can have many Pods associated with it.
     * @param contextServiceClient an initialized ContextServiceClient
     * @param request a pre-existing Request object
     * @return a List of Pods associated with the same Request
     */
    public static List<ContextObject> createMultiplePodsWithSameRequest(ContextServiceClient contextServiceClient, ContextObject request) {
        List<ContextObject> pods = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            pods.add(createPodWithRequest(contextServiceClient, request));
        }
        return pods;
    }

    /**
     * Create detail.comment with default field and fieldsets
     * @param contextServiceClient
     * @param pod
     * @return a newly-created detail.comment with cisco.base.comment fieldset
     */
    public static ContextObject createDetailCommentWithBaseFieldset(ContextServiceClient contextServiceClient, ContextObject pod) {
        ContextObject detailComment = new ContextObject("detail.comment");
        detailComment.setDataElements(
                DataElementUtils.convertDataMapToSet(
                        new HashMap<String, Object>() {{
                            put("Context_Comment", "Detailed context comment.");
                            put("Context_Visible", true);
                            put("Context_DisplayName", "Display name");
                        }}
                )
        );
        detailComment.setFieldsets(Arrays.asList("cisco.base.comment"));
        detailComment.setParentId(pod.getId());
        contextServiceClient.create(detailComment);
        return detailComment;
    }

    /**
     * Create detail.feedback with default field and fieldsets
     * @param contextServiceClient
     * @param pod
     * @return a newly-created detail.feedback with cisco.base.comment fieldset
     */
    public static ContextObject createDetailFeedbackWithBaseFieldset(ContextServiceClient contextServiceClient, ContextObject pod) {
        ContextObject detailComment = new ContextObject("detail.feedback");
        detailComment.setDataElements(
                DataElementUtils.convertDataMapToSet(
                        new HashMap<String, Object>() {{
                            put("Context_Comment", "Detailed context feedback.");
                            put("Context_Visible", true);
                            put("Context_DisplayName", "Display name");
                        }}
                )
        );
        detailComment.setFieldsets(Arrays.asList("cisco.base.comment"));
        detailComment.setParentId(pod.getId());
        contextServiceClient.create(detailComment);
        return detailComment;
    }
}
