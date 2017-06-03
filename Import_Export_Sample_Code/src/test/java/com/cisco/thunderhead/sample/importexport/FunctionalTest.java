package com.cisco.thunderhead.sample.importexport;

import com.cisco.thunderhead.client.ClientResponse;
import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.customer.Customer;
import com.cisco.thunderhead.util.DataElementUtils;
import com.cisco.thunderhead.util.RFC3339Date;
import com.cisco.thunderhead.util.SDKUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

/**
 * Tests functionality
 */
public class FunctionalTest {

    @Test
    public void testExport() throws Exception {
        assumeTrue("must have connectiondata.txt file to test this", Utils.getConnectionData().length() > 0);
        ContextServiceClient contextServiceClient = Utils.initContextServiceClient(Utils.getConnectionData());
        Customer customer = createCustomerAndWait(contextServiceClient);
        Path dir = Files.createTempDirectory("export");

        try {
            RFC3339Date startDate = new RFC3339Date(new Date().getTime() - TimeUnit.MINUTES.toMillis(1));
            RFC3339Date endDate = new RFC3339Date(new Date().getTime());

            Export.doExport(contextServiceClient, dir.toFile().getAbsolutePath(), true, true, 50, startDate, endDate, 1000);

            System.out.println("Output directory is: " + dir.toAbsolutePath());

            // Validate size of customer JSON file
            File file = new File(dir.toFile(), "customer.json");
            FileReader fr = new FileReader(file);
            JsonArray customers = new Gson().fromJson(fr, JsonArray.class);
            assertEquals("wrong number of customers", 1, customers.size());
        } finally {
            // cleanup
            contextServiceClient.delete(customer);
//            Files.delete(dir);
        }
    }

    /**
     * Creates a customer and waits for it to be searchable.
     */
    private Customer createCustomerAndWait(ContextServiceClient contextServiceClient) throws InterruptedException {
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
        customer.setFieldsets(Collections.singletonList("cisco.base.customer"));
        ClientResponse clientResponse = contextServiceClient.create(customer);

        String id = SDKUtils.getIdFromResponse(clientResponse);
        Utils.waitForSearchable(contextServiceClient, Collections.singletonList(id), Customer.class);
        Thread.sleep(5000);
        return customer;
    }
}