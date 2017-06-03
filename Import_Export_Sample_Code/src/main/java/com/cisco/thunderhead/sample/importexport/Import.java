package com.cisco.thunderhead.sample.importexport;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.cisco.thunderhead.ContextBean;
import com.cisco.thunderhead.client.ClientResponse;
import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.customer.Customer;
import com.cisco.thunderhead.pod.Pod;
import com.cisco.thunderhead.request.Request;
import com.cisco.thunderhead.rest.FlushStatusBean;
import com.cisco.thunderhead.sample.importexport.gsonutils.CSGsonFactory;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Import: Sample program to import objects (pod, customer, request) saved using the Export sample.
 * Note that although the relationships between those objects will be preserved on import, the
 * objects' IDs and creation/update timestamps are not preserved.
 *
 * Program arguments :
 * -c, --connection    Specify the connection data as Base64 string
 * -f, --flush         Specify whether or not to flush all workgroup data (disabled by default)
 * -d, --dir           Specify the directory containing the exported data (pod.json, customer.json, request.json)
 * -o, --output        Specify the output directory which will contain summary file and error files
 *
 *
 * Program output :
 * Summary of the import is written to the console and `summary.txt`.  The output
 * includes the number of imported objects of each type (pod, customer, request) as well
 * as the reason why any object failed to be imported.
 * Total number of objects imported.
 * Total number of objects that failed to be imported.
 *
 * Any objects that failed to be imported will be copied to the output directory into the
 * appropriate file (pod_error.json, customer_error.json, request_error.json).
 */
public class Import {

    private static Logger LOGGER = Logger.getLogger("importexport");
    private static FileHandler logHandler;
    private static ContextServiceClient contextServiceClient;
    private static Gson gson = CSGsonFactory.getCSJson();

    // Map of bean type to file writer
    private static HashMap<Class<? extends ContextBean>,JsonArrayWriter> writerMap = new HashMap<>();
    // Mapping between the original (exported) object IDs and their new IDs after import.
    private static Map<UUID, UUID> idMap = new HashMap<>();

    private static int numberOfImportedEntities = 0;
    private static int numberOfFailedEntities = 0;

    private static File podFile;
    private static File custFile;
    private static File reqFile;
    private static File summary;

    public static void main(String args[]) throws Exception {

        Arguments arguments = new Arguments(args);

        try{
            // validate the arguments
            Path inputDirectoryPath = Paths.get(arguments.inputDirectory);
            Path outputDirectoryPath = Paths.get(arguments.outputDirectory);
            setupAndVerifyFiles(inputDirectoryPath, outputDirectoryPath);

            // initialize the Context Service SDK using the connection data
            contextServiceClient = Utils.initContextServiceClient(arguments.connection);

            // create error files
            createFailedObjectFiles(outputDirectoryPath, "pod_error", "customer_error", "request_error");

            // write summary and error log to summary.txt
            setupLogger();

            // flush all workgroup data from the database
            if (arguments.flush) {
                flush();
            }

            // Open input streams to the import object files. Read each json object from the file.
            // Create context objects. Log summary of results.
            try(BufferedReader pod = new BufferedReader(new FileReader(podFile));
                BufferedReader cust = new BufferedReader(new FileReader(custFile));
                BufferedReader req = new BufferedReader(new FileReader(reqFile));
            ){
                long lStartTime = System.currentTimeMillis();
                readAndVisit(cust, Customer.class, createAndMapCustomerAndRequest);
                readAndVisit(req, Request.class, createAndMapCustomerAndRequest);
                readAndVisit(pod, Pod.class, createPodEntity);
                long lEndTime = System.currentTimeMillis();

                LOGGER.info("Total number of objects imported : " + numberOfImportedEntities);
                LOGGER.info("Total number of objects that failed to import : " + numberOfFailedEntities);
                LOGGER.info("Total time elapsed in importing : " + (lEndTime - lStartTime) + " milliseconds");
            }
        }
        finally {
            cleanup();
        }
    }

    /**
     * Flushes (deletes) all of the objects in an organization's workgroup.
     */
    private static void flush() throws InterruptedException, TimeoutException {
        LOGGER.info("Flushing workgroup data...");

        contextServiceClient.flush(Pod.class);
        contextServiceClient.flush(Request.class);
        contextServiceClient.flush(Customer.class);

        FlushStatusBean status = null;

        // Use SDK to wait for flush to complete.  In this case, allow up to 30 seconds...
        status = contextServiceClient.waitForFlushComplete(Pod.class, 30);
        if (status.isCompleted()) {
            LOGGER.info("Flush of pods complete. Flushed " + status.getNumberFlushed() + " pods.");
        } else {
            LOGGER.info("Flush of pods not complete. Flushed " + status.getNumberFlushed() + " pods. " + (!StringUtils.isEmpty(status.getMessage()) ? status.getMessage() : ""));
            throw new TimeoutException();
        }

        status = contextServiceClient.waitForFlushComplete(Request.class, 30);
        if (status.isCompleted()) {
            LOGGER.info("Flush of requests complete. Flushed " + status.getNumberFlushed() + " requests.");
        } else {
            LOGGER.info("Flush of requests not complete. Flushed " + status.getNumberFlushed() + " requests. " + (!StringUtils.isEmpty(status.getMessage()) ? status.getMessage() : ""));
            throw new TimeoutException();
        }

        status = contextServiceClient.waitForFlushComplete(Customer.class, 30);
        if (status.isCompleted()) {
            LOGGER.info("Flush of customers complete. Flushed " + status.getNumberFlushed() + " customers.");
        } else {
            LOGGER.info("Flush of customers not complete. Flushed " + status.getNumberFlushed() + " customers. " + (!StringUtils.isEmpty(status.getMessage()) ? status.getMessage() : ""));
            throw new TimeoutException();
        }

        LOGGER.info("Flushed workgroup data.");

    }

    /**
     * This method reads the specified file and invokes the specified callback function for each
     * object contained in the file.
     *
     * @param  bufferedReader  Input stream of objects to import.
     * @param  type  Type of object to import.
     * @param  visitor  Consumer lambda which gets invoked for each object.
     */
    private static <T extends ContextBean> void readAndVisit(BufferedReader bufferedReader, Class<T> type, Function visitor) throws Exception {

        int totalReadEntities = 0;
        int totalCreatedEntities = 0;

        try (JsonReader reader = new JsonReader(bufferedReader)) {
            reader.beginArray();
            while (reader.hasNext()) {
                // deserialize a bean from json and invoke the callback function
                totalCreatedEntities += (int) visitor.apply(gson.fromJson(reader, type));
                totalReadEntities++;
            }
            reader.endArray();
        }

        LOGGER.info("Total number of " + type.getSimpleName() + " created : " + totalCreatedEntities);

        // Count number of imports failed and log them
        int createFailCount = totalReadEntities - totalCreatedEntities;
        if (createFailCount > 0) {
            numberOfFailedEntities += createFailCount;
            LOGGER.info("Failed to create " + createFailCount +" "+ type.getSimpleName());
        }

        // Add number of objects created to total number of imports
        numberOfImportedEntities += totalCreatedEntities;
    }

    /**
     * This lambda function is invoked for each imported customer and request.  It both creates
     * the object and maintains the mapping between an object's original (exported) ID and its
     * new (after import) ID.
     * The function returns the number of objects created. 1 or 0
     */
    private static Function<ContextBean, Integer> createAndMapCustomerAndRequest = (bean) -> {
        try {
            UUID oldId = bean.getId();
            ClientResponse res = contextServiceClient.create(bean);
            // If bean successfully created
            if (res.getStatus() == 201) {
                UUID newId = bean.getId();
                idMap.put(oldId,newId);
                return 1; // success!
            }
            LOGGER.info("Error while creating " + bean.toString()+ " , " + res.getStatus());
        }
        catch(Exception e){
            // display error and continue
            LOGGER.log(Level.SEVERE,"Exception while creating " + bean.toString(), e);
        }

        // A problem occurred importing the object.  Record the object in the output directory.
        try {
            writerMap.get(bean.getClass()).writeEntity(bean);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,"Exception while writing " + bean.toString(), e);
        }
        return 0;
    };

    /**
     * This lambda function is invoked for each imported pod.  Before importing the pod, the function
     * replaces the original (exported) customer and request IDs in the pod with the new (imported) IDs
     * for those objects.
     * The function returns the number of objects created. 1 or 0
     */
    private static Function<Pod, Integer> createPodEntity = (podBean) -> {
        try {
            // Map the original customer and request IDs to their newly imported values.
            UUID customerId = podBean.getCustomerId();
            UUID requestId = podBean.getRequestId();

            if (idMap.containsKey(customerId)) {
                podBean.setCustomerId(idMap.get(customerId));
            }

            if (idMap.containsKey(requestId)) {
                podBean.setRequestId(idMap.get(requestId));
            }

            // Create the pod
            ClientResponse res = contextServiceClient.create(podBean);
            //If bean successfully created
            if (res.getStatus() == 201) {
                return 1; // success!
            }

            LOGGER.log(Level.SEVERE,"Error while creating " + podBean.toString()+ " , " + res.getStatus());
        }
        catch (Exception e){
            LOGGER.log(Level.SEVERE,"Exception while creating " + podBean.toString(), e);
        }

        //When there is error while creating, write to summary file and return 0
        try {
            writerMap.get(podBean.getClass()).writeEntity(podBean);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,"Exception while writing " + podBean.toString(), e);
        }
        return 0;
    };

    /**
     * Verify that the input directory contains exported json files and that the output directory
     * is empty.
     *
     * @param inputDirectoryPath Path to directory containing the files from the Export sample
     * @param outputDirectoryPath Path to directory in which to record the import attempt
     * @throws IOException the input or output directory is not valid
     */
    public static void setupAndVerifyFiles(Path inputDirectoryPath, Path outputDirectoryPath) throws IOException {
        File inputDirectory = inputDirectoryPath.toFile();
        File outputDirectory = outputDirectoryPath.toFile();
        podFile  = inputDirectoryPath.resolve("pod.json").toFile();
        custFile = inputDirectoryPath.resolve("customer.json").toFile();
        reqFile = inputDirectoryPath.resolve("request.json").toFile();

        //Throw error if input directory does not exist or does not contain exported files
        if(!inputDirectory.exists() || !inputDirectory.isDirectory()
                || !podFile.exists() || !custFile.exists() || !reqFile.exists()
                || !podFile.isFile() || !custFile.isFile() || !reqFile.isFile()){
            throw new FileNotFoundException("The specified input directory does not appear to be from an export");
        }

        boolean exist = outputDirectory.listFiles((File pathname) -> (pathname.getName().endsWith(".json") || pathname.getName().endsWith(".txt"))).length != 0 ;
        //Throw error if output directory is not empty or does not exist
        if(!outputDirectory.exists() || !outputDirectory.isDirectory() || exist){
            throw new FileNotFoundException("The specified directory does not exist or already contains files - "+outputDirectoryPath.toString());
        }

        //create summary file
        summary = Files.createFile(outputDirectoryPath.resolve("summary.txt")).toFile();
    }

    /**
     * Creates files to store the json objects which failed to import.
     *
     * @param outputDirectoryPath directory in which to create the files
     * @param podFile name of file which will contain pod objects
     * @param customerFile name of file which will contain customer objects
     * @param requestFile name of file which will contain request objects
     * @throws IOException unable to create one of the files
     */
    public static void createFailedObjectFiles(Path outputDirectoryPath, String podFile, String customerFile, String requestFile) throws IOException {
        JsonArrayWriter pErrWriter = new JsonArrayWriter(podFile,outputDirectoryPath,false,true);
        writerMap.put(Pod.class,pErrWriter);
        JsonArrayWriter cErrWriter = new JsonArrayWriter(customerFile,outputDirectoryPath,false,true);
        writerMap.put(Customer.class,cErrWriter);
        JsonArrayWriter rErrWriter = new JsonArrayWriter(requestFile,outputDirectoryPath,false,true);
        writerMap.put(Request.class,rErrWriter);
    }

    /**
     * Configure the logger to write to both the console and the log file.
     * @throws IOException
     */
    private static void setupLogger() throws IOException {
        logHandler = new FileHandler(summary.toString(),true);
        logHandler.setFormatter(new SimpleFormatter());
        logHandler.setLevel(Level.ALL);
        LOGGER.addHandler(logHandler);
    }

    /**
     * Close our opened files, our log handler, and destroy the contextServiceClient
     * @throws IOException
     */
    private static void cleanup() throws IOException {
        if(logHandler != null){
            logHandler.flush();
            LOGGER.removeHandler(logHandler);
        }

        for (JsonArrayWriter elem : writerMap.values()) {
            elem.close();
        }

        if(contextServiceClient!= null) {
            contextServiceClient.destroy();
        }
    }

    /**
     * Command line argument specifications
     */
    private static class Arguments {

        Arguments(String []args) throws IOException {
            parseArguments(args);
        }

        @Parameter(names = {"-c", "--connection"}, description = "Specify the connection data as Base64 string", required = true)
        String connection;

        @Parameter(names = {"-f", "--flush"}, description = "Causes all existing workgroup data to be flushed, disabled by default")
        boolean flush = false;

        @Parameter(names = {"-d", "--dir"}, description = "Specify the directory containing the data to be imported", required = true)
        String inputDirectory;

        @Parameter(names = {"-o", "--output"}, description = "Specify the directory in which to record the summary log and objects that failed to import", required = true)
        String outputDirectory;

        /**+
         * parseArguments : parse the arguments using JCommander
         * @param args
         */
        private void parseArguments(String args[]) throws IOException {
            try {
                JCommander jc = new JCommander(this, args);
                LOGGER.info("Arguments- " + "Flush: " + flush + ", Input directory: " + inputDirectory + ", Output directory: "+outputDirectory+", Connection String: " + connection);
            }
            catch (ParameterException e) {
                // Display usage - this will catch the case where no args are specified as well as the case where required args are missing
                JCommander jCommander = new JCommander(this);
                jCommander.usage();
                throw (e);
            }
        }
    }

}


