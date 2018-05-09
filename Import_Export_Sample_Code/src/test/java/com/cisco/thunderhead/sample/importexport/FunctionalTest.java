package com.cisco.thunderhead.sample.importexport;

import com.cisco.thunderhead.ContextObject;
import com.cisco.thunderhead.client.ClientResponse;
import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.client.Operation;
import com.cisco.thunderhead.client.SearchParameters;
import com.cisco.thunderhead.util.DataElementUtils;
import com.cisco.thunderhead.util.RFC3339Date;
import com.cisco.thunderhead.util.SDKUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Tests import/export works
 */
public class FunctionalTest {
    private static Logger LOGGER = LoggerFactory.getLogger(Export.class);
    static ContextServiceClient contextServiceClient;

    @BeforeClass
    public static void beforeClass() {
        assumeTrue("must have connectiondata.txt file to test this", Utils.getConnectionData().length() > 0);
        contextServiceClient = Utils.initContextServiceClient(Utils.getConnectionData());
    }

    /**
     * Ensure import works.  Approach:
     * - Search for all customers to take note of how many there are.
     * - Import a customer
     * - Search again and wait for the customer to show up in search results.
     */
    @Test
    public void testImport() throws Exception {
        assumeTrue("must have connectiondata.txt file to test this", Utils.getConnectionData().length() > 0);

        // use the test/main/resources directory of files for import
        URL customerJson = FunctionalTest.class.getClassLoader().getResource("customer.json");
        Path inputDir = Paths.get(customerJson.toURI()).getParent();
        Path outputDir = Files.createTempDirectory("errors");

        // find all customers before.
        SearchParameters sp = new SearchParameters() {{
            add("type","customer");
        }};
        List<ContextObject> beforeCustomers = contextServiceClient.search(ContextObject.class, sp, Operation.OR);

        // Do the import!
        Import.doImport(contextServiceClient, inputDir.toFile().getAbsolutePath(), outputDir.toFile().getAbsolutePath(), false);

        assertEquals("number imported is incorrect", 1, Import.getNumberOfImportedEntities());
        assertEquals("number failed to import is incorrect", 0, Import.getNumberOfFailedEntities());

        List<ContextObject> afterCustomers = waitForCustomerToBeCreated(contextServiceClient, beforeCustomers, sp);
        assertEquals("should be an extra customer", 1, afterCustomers.size());

        // cleanup
        contextServiceClient.delete(afterCustomers.get(0));
        deleteDir(outputDir);
    }

    /**
     * This waits for the customer to be created.
     */
    private List<ContextObject> waitForCustomerToBeCreated(ContextServiceClient contextServiceClient, List<ContextObject> beforeCustomers, SearchParameters sp) throws InterruptedException {
        int attempts = 0;
        List<ContextObject> afterCustomers = Collections.emptyList();
        while (attempts<10) {
            afterCustomers = contextServiceClient.search(ContextObject.class, sp, Operation.OR);
            if (afterCustomers.size()>beforeCustomers.size()) {
                break;
            }
            Thread.sleep(1000);
            attempts++;
        }
        afterCustomers.removeAll(beforeCustomers);
        return afterCustomers;
    }

    /**
     * Ensures export works.  Approach:
     * - Create a customer
     * - Export anything created within last minute (should be just this customer)
     * - Ensure the exported customer matches the one we just created.
     */
    @Test
    public void testExport() throws Exception {
        assumeTrue("must have connectiondata.txt file to test this", Utils.getConnectionData().length() > 0);
        Map<String,Object> map = new HashMap<String, Object>() {{
            put("Context_Work_Email", "john.doe@example.com");
            put("Context_Work_Phone", "555-555-5555");
            put("Context_First_Name", "John");
            put("Context_Last_Name", "Doe");
            put("Context_Street_Address_1", "123 Sesame Street");
            put("Context_City", "Detroit");
            put("Context_State", "MI");
            put("Context_Country", "US");
            put("Context_ZIP", "90210");
        }};

        ContextObject customer = createCustomerAndWait(contextServiceClient, map);
        Path dir = Files.createTempDirectory("export");

        try {
            RFC3339Date startDate = new RFC3339Date(new Date().getTime() - TimeUnit.MINUTES.toMillis(1));
            RFC3339Date endDate = new RFC3339Date(new Date().getTime());

            // Do the export!!
            Export.doExport(contextServiceClient, dir.toFile().getAbsolutePath(), true, true, 50, startDate, endDate, 1000);

            System.out.println("Output directory is: " + dir.toAbsolutePath());

            // Validate size of customer JSON file
            File file = new File(dir.toFile(), "customer.json");
            FileReader fr = new FileReader(file);
            JsonArray customers = new Gson().fromJson(fr, JsonArray.class);
            assertEquals("wrong number of customers", 1, customers.size());

            validateCustomers(customers,map);

        } finally {
            // cleanup
            contextServiceClient.delete(customer);
            deleteDir(dir);
        }
    }

    /**
     * Deletes a directory and any of the files within it
     */
    private void deleteDir(Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

        });
    }

    /**
     * Validates the exported customer data matches the customer we created
     */
    private void validateCustomers(JsonArray customers, Map<String, Object> map) {
        JsonArray dataElementsList = customers.get(0).getAsJsonObject().getAsJsonArray("dataElements");
        assertEquals(map.size(), dataElementsList.size());
        for (int i=0; i<dataElementsList.size(); i++) {
            JsonObject jsonObject = dataElementsList.get(i).getAsJsonObject();
            for (Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
                if (e.getKey().equals("type"))
                    continue;
                assertEquals(e.getValue().getAsString(), map.get(e.getKey()).toString());
            }
            System.out.println(jsonObject);
        }
    }

    /**
     * Creates a customer and waits for it to be searchable.
     */
    private ContextObject createCustomerAndWait(ContextServiceClient contextServiceClient, Map<String, Object> map) throws InterruptedException {
        ContextObject customer = new ContextObject(ContextObject.Types.CUSTOMER);
        customer.setDataElements(
                DataElementUtils.convertDataMapToSet(map)
        );
        customer.setFieldsets(Collections.singletonList("cisco.base.customer"));
        ClientResponse clientResponse = contextServiceClient.create(customer);

        String id = SDKUtils.getIdFromResponse(clientResponse);
        LOGGER.info("********************createCustomerAndWait*********customerId=" + id);
        Utils.waitForSearchable(contextServiceClient, Collections.singletonList(id), ContextObject.class, ContextObject.Types.CUSTOMER);
        Thread.sleep(10000);
        return customer;
    }
}