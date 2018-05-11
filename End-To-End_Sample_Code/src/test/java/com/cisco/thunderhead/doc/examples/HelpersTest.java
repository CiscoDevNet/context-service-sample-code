package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.ContextObject;
import com.cisco.thunderhead.util.DataElementUtils;
import com.cisco.thunderhead.util.RFC3339Date;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class HelpersTest extends BaseExamplesTest {
    @Test
    public void testGetIdFromCreatePodResponse() {
        ContextObject pod = createLocalPod();
        String id = Helpers.getIdFromCreatePodResponse(contextServiceClient, pod);
        assertEquals(pod.getId().toString(), id);
    }

    @Test
    public void testGetIdFromCreatePod() {
        ContextObject pod = createLocalPod();
        String id = Helpers.getIdFromCreatePod(contextServiceClient, pod);
        assertEquals(pod.getId().toString(), id);
    }

    @Test
    public void testGetLastUpdatedFromCreatePodResponse() {
        ContextObject pod = createLocalPod();
        RFC3339Date lastUpdated = Helpers.getLastUpdatedFromCreatePodResponse(contextServiceClient, pod);
        assertEquals(pod.getLastUpdated(), lastUpdated);
    }

    @Test
    public void testGetLastUpdatedFromCreatePod() {
        ContextObject pod = createLocalPod();
        RFC3339Date lastUpdated = Helpers.getLastUpdatedFromCreatePod(contextServiceClient, pod);
        assertEquals(pod.getLastUpdated(), lastUpdated);
    }

    // create a local pod that hasn't been created in context service
    private ContextObject createLocalPod() {
        ContextObject pod = new ContextObject(ContextObject.Types.POD);

        pod.setDataElements(
                DataElementUtils.convertDataMapToSet(
                        new HashMap<String, Object>() {{
                            put("Context_Notes", "Notes about this context.");
                        }}
                )
        );
        pod.setFieldsets(Arrays.asList("cisco.base.pod"));
        return pod;
    }
}
