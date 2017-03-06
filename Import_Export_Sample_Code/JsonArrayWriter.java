package com.cisco.thunderhead.sample.importexport;

import com.cisco.thunderhead.sample.importexport.gsonutils.CSGsonFactory;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


/**+
 * Writes JSON entities to a file in an array.
 * Allows you to write the array to file over time, instead of needing to hold the entire array in memory.
 */
public class JsonArrayWriter {
    private static final String EXTENSION = ".json";

    private Gson gsonHandler;
    private FileWriter writer;
    private BufferedWriter bufferedWriter;

    private int numberOfEntities = 0;
    private static Logger LOGGER = LoggerFactory.getLogger(JsonArrayWriter.class);

    /**
     * Writes JSON entities to a file
     * @param name name of the file, without the .json extension. (e.g. name="bob" will output to "bob.json")
     * @param outputDir Output directory to write the file
     * @param printPretty Whether or not to pretty print the output json
     * @param deleteExisting Whether or to overwrite existing file. If set to false and the file already exists, will throw an exception.
     * @throws IOException
     */
    JsonArrayWriter(String name, Path outputDir, boolean printPretty, boolean deleteExisting) throws IOException {

        gsonHandler = CSGsonFactory.getCSJson(printPretty);

        Path filePath = createFile(name, outputDir, deleteExisting);
        initWriter(filePath);
    }

    /**+
     * writeEntity : This method writes a single entity in the list to the file
     * @param jsonObject : json list of entities
     * @param <T> : Type of the entity
     */
    public <T> void writeEntity(T jsonObject) throws IOException {
        if(numberOfEntities!=0 ){
            bufferedWriter.write(",");
        }
        bufferedWriter.write(gsonHandler.toJson(jsonObject).toString());
        numberOfEntities++;
    }

    /**
     * writeEntities : This method writes each entity in the list to the file
     * @param jsonList : json list of entities
     * @param <T> : Type of the entity
     */
    public <T> void writeEntities(List<T> jsonList) throws IOException {
        for(T elem : jsonList){
            writeEntity(elem);
        }
    }

    /**
     * close : Write last character to the file. Then close all the streams.
     */
    public void close() throws IOException {
        try {
            bufferedWriter.write("]");
            bufferedWriter.flush();
        } finally {
            bufferedWriter.close();
            writer.close();
        }
    }

    public int getNumberOfEntities(){
        return numberOfEntities;
    }

    private Path createFile(String name, Path outputDir, boolean deleteExisting) throws IOException {
        Path filePath = outputDir.resolve(name + EXTENSION);

        if (deleteExisting) {
            Files.deleteIfExists(filePath);
        } else if (Files.exists(filePath)) {
            throw new IOException("File already exists: " + filePath.toString());
        }

        Files.createFile(filePath);
        return filePath;
    }

    private void initWriter(Path filePath) throws IOException {
        writer = new FileWriter(filePath.toString(), true);
        bufferedWriter = new BufferedWriter(writer);
        bufferedWriter.write("[");
    }
}
