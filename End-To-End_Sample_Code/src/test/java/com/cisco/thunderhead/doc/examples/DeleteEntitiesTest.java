package com.cisco.thunderhead.doc.examples;


import com.cisco.thunderhead.customer.Customer;
import com.cisco.thunderhead.pod.Pod;
import com.cisco.thunderhead.request.Request;
import org.junit.Test;

public class DeleteEntitiesTest extends BaseExamplesTest{

    @Test
    public void deletePodTest(){
        Pod pod = CreateEntities.createPodWithBaseFieldset(contextServiceClient);
        DeleteEntities.deletePod(contextServiceClient,pod);
    }

    @Test
    public void deleteCustomerTest(){
        Customer customer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        DeleteEntities.deleteCustomer(contextServiceClient,customer);
    }

    @Test
    public void deleteRequestTest(){
        Request request = CreateEntities.createRequestWithBaseFieldset(contextServiceClient);
        DeleteEntities.deleteRequest(contextServiceClient,request);
    }
}
