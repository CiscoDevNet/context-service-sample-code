package com.cisco.thunderhead.doc.e2e;

import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.client.Operation;
import com.cisco.thunderhead.client.SearchParameters;
import com.cisco.thunderhead.doc.examples.ConfigurationAndInitialization;
import com.cisco.thunderhead.doc.examples.ConnectionData;
import com.cisco.thunderhead.doc.examples.FlushEntities;
import com.cisco.thunderhead.pod.Pod;
import org.junit.After;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

public class ContextServiceDemoTest {
    private ContextServiceClient contextServiceClient;

    @After
    public void flushAll() throws TimeoutException, InterruptedException {
        FlushEntities.flushAllEntities(contextServiceClient);
        ConfigurationAndInitialization.beforeDestroyCSConnector(contextServiceClient);
        contextServiceClient.destroy();
    }

    @Test
    public void testContextServiceDemo() {
        //Set up connectors to create a test pod and cleanup after
        ContextServiceDemo.main();

        //Create CS connector and search for earlier created pod
        contextServiceClient = ConfigurationAndInitialization.createAndInitContextServiceClientWithCustomConfiguration(ConnectionData.getConnectionData());

        List<Pod> pods = contextServiceClient.search(Pod.class, new SearchParameters(){{ add("Context_Notes", "Context Service Demo POD - " + System.currentTimeMillis()); }}, Operation.AND);
        assertEquals("Pod was created", 1, pods.size());
    }
}
