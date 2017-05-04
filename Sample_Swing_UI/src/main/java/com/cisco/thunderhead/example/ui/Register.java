package com.cisco.thunderhead.example.ui;

import com.cisco.thunderhead.connector.RegisteringApplication;
import com.cisco.thunderhead.plugin.ConnectorFactory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Example of how to create a registration request URL.
 */
public class Register {
    private static final Logger LOGGER = LoggerFactory.getLogger(Register.class);

    private static HttpServer server;
    public static void main(String args[]) throws InterruptedException {
        new Thread(Register::startWebServer).start();
        RegisteringApplication registerApp = ConnectorFactory.getConnector(RegisteringApplication.class);

        try{
            String productCallbackUrl = "http://localhost:8000/test";
            String APPLICATION_TYPE = "custom";
            String registrationURL = registerApp.createRegistrationRequest(productCallbackUrl, APPLICATION_TYPE);
            LOGGER.info("*** Opening Context Service Registration URL: " + registrationURL);
            Desktop.getDesktop().browse(new URL(registrationURL).toURI());
        } catch (Exception e) {
            LOGGER.error("Error Creating Request! The Error is: " + e);
        }
    }

    public static void startWebServer()  {

        LOGGER.info("*** Starting embedded web server for Context Service Registration callback");
        try {
            server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/test", new RegistrationCallbackHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException e) {
            LOGGER.error("Failed to start web server",e);
        }
    }

    static class RegistrationCallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            Pattern pattern = Pattern.compile("/test\\?connectionData=(.*)");
            Matcher matcher = pattern.matcher(t.getRequestURI().toString());
            if (matcher.find()) {
                String connectionData = matcher.group(1);
                writeConnectionData(connectionData, t);
            } else {
                LOGGER.error("There was a problem retrieving the connection data string from the callback URI");
                LOGGER.error("URI: " + t.getRequestURI().toString());
                String htmlMessage = "<h1>There was a problem retrieving the connection data</h1>URI:<br>" +
                        "<textarea rows=25 cols=100>" + t.getRequestURI().toString() + "</textarea>";
                writeResponseBody(t, htmlMessage, HttpURLConnection.HTTP_INTERNAL_ERROR);
            }
            LOGGER.info("*** Stopping embedded web server");
            server.stop(0);
        }
    }
    private static void writeConnectionData(String connectionData, HttpExchange t) throws IOException {
        LOGGER.info("*** Connection data string is: " + connectionData);

        String htmlMessage = "This is the connection data string.  Use this to initialize the connection to the Context Service.<br>" +
                "<textarea rows=25 cols=100>" + connectionData + "</textarea>";
        writeResponseBody(t, htmlMessage, HttpURLConnection.HTTP_OK);
        if (!new File(ConnectionData.connectionDataFileName).exists()) {
            Path path = Paths.get(ConnectionData.connectionDataFileName);
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write(connectionData);
            }
            LOGGER.info("*** Wrote connection data string to: " + ConnectionData.connectionDataFileName);
        }
    }

    private static void writeResponseBody(HttpExchange t, String htmlMessage, int code) throws IOException {
        t.getResponseHeaders().add("Content-Type", "text/html");
        t.sendResponseHeaders(code, htmlMessage.length());
        t.getResponseBody().write(htmlMessage.getBytes());
        t.close();
    }
}
