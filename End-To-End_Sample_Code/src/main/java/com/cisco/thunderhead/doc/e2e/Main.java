package com.cisco.thunderhead.doc.e2e;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.cisco.thunderhead.connector.RegisteringApplication;
import com.cisco.thunderhead.plugin.ConnectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * This is a simple example for running different commands (currently just one, register)
 */
public class Main {
    private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String REGISTER = "register";

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

    /**
     * This parses the command line arguments
     */
    private void parseAndRunCommand(String[] args) {
        CommandRegister register = new CommandRegister();
        JCommander jc = JCommander.newBuilder()
                .addCommand(REGISTER, register)
                .programName("Main")
                .build();

        try {
            jc.parse(args);
            if (jc.getParsedCommand()!=null && jc.getParsedCommand().equals(REGISTER)) {
                register(register);
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
}
