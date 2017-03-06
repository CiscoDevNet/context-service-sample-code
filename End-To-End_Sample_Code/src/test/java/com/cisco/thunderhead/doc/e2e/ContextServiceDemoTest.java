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

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ContextServiceDemoTest {
    private ContextServiceClient contextServiceClient;

    @After
    public void flushAll() throws TimeoutException, InterruptedException {
        FlushEntities.flushAllEntities(contextServiceClient);
        contextServiceClient.destroy();
    }

    @Test
    public void testContextServiceDemo() {
        ContextServiceDemo.main();
        contextServiceClient = ConfigurationAndInitialization.createAndInitContextServiceClientWithCustomConfiguration(ConnectionData.getConnectionData());
        try {
            sleep(2000);    // wait 2 seconds to allow data to propagate
        } catch (InterruptedException e) {
            fail("sleep interrupted");
        }
        List<Pod> pods = contextServiceClient.search(Pod.class, new SearchParameters(){{ add("Context_Notes", "Context Service Demo POD"); }}, Operation.AND);
        assertEquals("Pod was created", 1, pods.size());
    }
}
