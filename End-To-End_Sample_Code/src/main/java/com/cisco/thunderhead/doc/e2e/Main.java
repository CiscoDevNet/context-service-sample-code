package com.cisco.thunderhead.doc.e2e;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.cisco.thunderhead.ContextBean;
import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.client.ContextServiceClientConstants;
import com.cisco.thunderhead.client.Operation;
import com.cisco.thunderhead.client.SearchParameters;
import com.cisco.thunderhead.connector.ConnectorConfiguration;
import com.cisco.thunderhead.connector.ManagementConnector;
import com.cisco.thunderhead.connector.RegisteringApplication;
import com.cisco.thunderhead.connector.info.ConnectorInfoImpl;
import com.cisco.thunderhead.customer.Customer;
import com.cisco.thunderhead.doc.examples.ConnectionData;
import com.cisco.thunderhead.plugin.ConnectorFactory;
import com.cisco.thunderhead.pod.Pod;
import com.cisco.thunderhead.request.Request;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.cisco.thunderhead.doc.e2e.ContextServiceDemo.getNoManagementConnector;

/**
 * This is a simple example for running different commands (currently just one, register)
 */
public class Main {
    private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String REGISTER = "register";
    private static final String PARSE_CONNECTION_DATA = "parse";
    private static final String SEARCH = "search";

    public static void main(String[] args) {
        new Main().parseAndRunCommand(args);
    }

    /**
     * This generates a registration callback URL and prints it to the console.
     */
    private void register(CommandRegister register) {
        LOGGER.info("MAIN: callback url: " + register.callbackUrl);
        LOGGER.info("MAIN: appType: " + register.appType);
        LOGGER.info("MAIN: proxy: " + (register.proxy!=null?register.proxy:"not set"));

        if (register.proxy!=null) {
            System.setProperty("contextservice.proxyURL",register.proxy);
        }

        ConnectorFactory.initializeFactory((new File(".")).getAbsolutePath() + "/connector.property");
        RegisteringApplication registerApp = ConnectorFactory.getConnector(RegisteringApplication.class);
        String registrationURL = registerApp.createRegistrationRequest(register.callbackUrl, register.appType);
        LOGGER.info("MAIN: registrationURL: " + registrationURL);
    }

    private void parseConnectionData(CommandParseConnectionData commandParseConnectionData) {
        try {
            String json = new String(Base64.decode(URLDecoder.decode(commandParseConnectionData.connectionData, "UTF-8")));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Map jsonMap = gson.fromJson(json, Map.class);
            LOGGER.info("Connection Data:\n" + gson.toJson(jsonMap));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("failed to decode connection data", e);
        }
    }

    private ContextServiceClient getContextServiceClient(final String requestTimeout) {
        String connectionData = ConnectionData.getConnectionData();

        // initialize connector factory
        String pathToConnectorProperty = Paths.get("./connector.property").toAbsolutePath().toString();
        ConnectorFactory.initializeFactory(pathToConnectorProperty);
        LOGGER.info("Initialized Connector Factory");

        // initialize management connector
        ManagementConnector managementConnector = ConnectorFactory.getConnector(ManagementConnector.class);
        String hostname = "doctest.example.com";
        ConnectorInfoImpl connInfo = new ConnectorInfoImpl(hostname);
        ConnectorConfiguration configuration = new ConnectorConfiguration(){{
            addProperty("LAB_MODE", true); // exclude this line for prod mode
            addProperty("REQUEST_TIMEOUT", requestTimeout!=null ? Integer.parseInt(requestTimeout) : 10000);
            //----Test Only Begin - Not needed for production implementation ----
            addProperty(ContextServiceClientConstants.NO_MANAGEMENT_CONNECTOR, getNoManagementConnector());
            //----Test Only End - Not needed for production implementation ----
        }};
        managementConnector.init(connectionData, connInfo, configuration);
        LOGGER.info("Initialized management connector");

        // initialize context service client
        ContextServiceClient contextServiceClient = ConnectorFactory.getConnector(ContextServiceClient.class);
        // reuse configuration we used for management connector
        contextServiceClient.init(connectionData, connInfo, configuration);
        return contextServiceClient;
    }

    private void search(CommandSearch search) {
        ContextServiceClient client = getContextServiceClient(search.requestTimeout);
        Class<? extends ContextBean> clazz;
        switch (search.type) {
            case "pod":
                clazz = Pod.class;
                break;
            case "customer":
                clazz = Customer.class;
                break;
            case "request":
                clazz = Request.class;
                break;
            default:
                throw new RuntimeException("unknown type " + search.type);
        }
        String[] terms = search.queryString.split(" ");
        SearchParameters sp = new SearchParameters();
        for (String term : terms) {
            int colonPos = term.indexOf(':');
            if (colonPos==-1) {
                throw new RuntimeException("badly formed query.  Terms must be in field:value form");
            }
            String key = term.substring(0, colonPos);
            String value = term.substring(colonPos+1);
            sp.add(key, value);
        }
        List<? extends ContextBean> response = client.search(clazz, sp, Operation.OR);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String responseJson = gson.toJson(response);

        if (StringUtils.isNotEmpty(search.outputFilename)) {
            try (FileWriter fileWriter = new FileWriter(search.outputFilename)) {
                fileWriter.append(responseJson);
                LOGGER.info("Wrote to file " + search.outputFilename);
            } catch (IOException e) {
                LOGGER.error("failed writing to file", e);
            }
        } else {
            LOGGER.info("Response:\n" + responseJson);
        }
    }

    /**
     * This parses the command line arguments
     */
    private void parseAndRunCommand(String[] args) {
        CommandRegister register = new CommandRegister();
        CommandParseConnectionData parseConnectionData = new CommandParseConnectionData();
        CommandSearch search = new CommandSearch();
        JCommander jc = JCommander.newBuilder()
                .addCommand(REGISTER, register)
                .addCommand(PARSE_CONNECTION_DATA, parseConnectionData)
                .addCommand(SEARCH, search)
                .programName("Main")
                .build();

        try {
            jc.parse(args);
            if (jc.getParsedCommand()!=null) {
                switch (jc.getParsedCommand()) {
                    case REGISTER:
                        register(register);
                        break;
                    case PARSE_CONNECTION_DATA:
                        parseConnectionData(parseConnectionData);
                        break;
                    case SEARCH:
                        search(search);
                        break;
                }
            } else {
                jc.usage();
            }
        } catch (ParameterException e) {
            jc.usage();
        }
    }

    @Parameters(commandDescription = "Generate registration callback URL")
    private class CommandRegister {
        @Parameter(names="--url", description = "Registration callback URL", required = true)
        private String callbackUrl;

        @Parameter(names="--appType", description = "Application type (optional)")
        private String appType = "custom";

        @Parameter(names="--proxy", description = "Proxy URL (optional), i.e. http://host:80")
        private String proxy;
    }

    @Parameters(commandDescription = "Parse connection data string")
    private class CommandParseConnectionData {
        @Parameter(names="--data", description = "Connection data string", required = true)
        private String connectionData;
    }

    @Parameters(commandDescription = "Search objects")
    private class CommandSearch {
        @Parameter(names="--query", description = "Query string, space-separated terms in key:value format", required = true)
        private String queryString;

        @Parameter(names="--type", description = "pod, customer, request", required = true)
        private String type;

        @Parameter(names="--output", description = "Output file (optional)")
        private String outputFilename;

        @Parameter(names="--timeout", description = "Request timeout (optional)")
        private String requestTimeout;
    }
}
