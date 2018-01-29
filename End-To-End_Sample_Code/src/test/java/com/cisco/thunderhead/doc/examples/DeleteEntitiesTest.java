package com.cisco.thunderhead.doc.examples;


import com.cisco.thunderhead.ContextObject;
import org.junit.Test;

public class DeleteEntitiesTest extends BaseExamplesTest{

    @Test
    public void deletePodTest(){
        ContextObject pod = CreateEntities.createPodWithBaseFieldset(contextServiceClient);
        DeleteEntities.deletePod(contextServiceClient,pod);
    }

    @Test
    public void deleteCustomerTest(){
        ContextObject customer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        DeleteEntities.deleteCustomer(contextServiceClient,customer);
    }

    @Test
    public void deleteRequestTest(){
        ContextObject request = CreateEntities.createRequestWithBaseFieldset(contextServiceClient);
        DeleteEntities.deleteRequest(contextServiceClient,request);
    }
}
