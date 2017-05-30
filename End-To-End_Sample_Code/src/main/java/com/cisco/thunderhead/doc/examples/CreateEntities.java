package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.customer.Customer;
import com.cisco.thunderhead.pod.Pod;
import com.cisco.thunderhead.request.Request;
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
    public static Pod createPodWithBaseFieldset(ContextServiceClient contextServiceClient) {
        Pod pod = new Pod(
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
    public static Customer createCustomerWithBaseFieldset(ContextServiceClient contextServiceClient) {
        Customer customer = new Customer(
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
    public static Request createRequestWithBaseFieldset(ContextServiceClient contextServiceClient) {
        Request request = new Request(
                DataElementUtils.convertDataMapToSet(
                        new HashMap<String, Object>() {{
                            put("Context_Description", "Request1 Description");
                            put("Context_Title", "Request1 Title");
                        }}
                )
        );
        request.setFieldsets(Arrays.asList("cisco.base.request"));
        contextServiceClient.create(request);
        return request;
    }

    /**
     * Create a POD and associate a customer to the POD.
     * @param contextServiceClient an initialized ContextServiceClient
     * @param customer a pre-existing Customer object
     * @return a POD associated with the Customer
     */
    public static Pod createPodWithCustomer(ContextServiceClient contextServiceClient, Customer customer) {
        Pod pod = new Pod(
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
    public static Pod createPodWithRequest(ContextServiceClient contextServiceClient, Request request) {
        Pod pod = new Pod(
                DataElementUtils.convertDataMapToSet(
                        new HashMap<String, Object>() {{
                            put("Context_Notes", "Notes about this context.");
                            put("Context_POD_Activity_Link", "http://myservice.example.com/service/ID/xxxx");
                        }}
                )
        );
        pod.setFieldsets(Arrays.asList("cisco.base.pod"));
        pod.setRequestId(request.getId());
        contextServiceClient.create(pod);
        return pod;
    }

    /**
     * Create a POD with an associated customer and an associated request.
     * @param an initialized ContextServiceClient
     * @param pre-existing Customer object
     * @param pre-existing Request object
     * @return a pod associated with both a customer and a request
     */
    public static Pod createPodWithCustomerAndRequest(ContextServiceClient contextServiceClient, Customer customer, Request request) {
        Pod pod = new Pod(
                DataElementUtils.convertDataMapToSet(
                        new HashMap<String, Object>() {{
                            put("Context_Notes", "Notes about this context.");
                            put("Context_POD_Activity_Link", "http://myservice.example.com/service/ID/xxxx");
                        }}
                )
        );
        pod.setFieldsets(Arrays.asList("cisco.base.pod"));
        pod.setCustomerId(customer.getId());
        pod.setRequestId(request.getId());
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
    public static List<Pod> createMultiplePodsWithSameCustomer(ContextServiceClient contextServiceClient, Customer customer) {
        List<Pod> pods = new ArrayList<>();
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
    public static List<Pod> createMultiplePodsWithSameRequest(ContextServiceClient contextServiceClient, Request request) {
        List<Pod> pods = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            pods.add(createPodWithRequest(contextServiceClient, request));
        }
        return pods;
    }
}
