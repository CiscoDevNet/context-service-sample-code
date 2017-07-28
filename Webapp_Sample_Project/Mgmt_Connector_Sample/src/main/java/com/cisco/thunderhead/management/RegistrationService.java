package com.cisco.thunderhead.management;

import com.cisco.thunderhead.connector.ManagementConnector;
import com.cisco.thunderhead.connector.RegisteringApplication;
import com.cisco.thunderhead.errors.ApiException;
import com.cisco.thunderhead.rest.Utils;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to manage registration/deregistration.
 */
@Path("/")
public class RegistrationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationService.class);

    private ManagementConnector managementConnector = ManagementContextListener.getManagementConnector();

    private static final String REGISTRATION_CALLBACK_PATH = "callback";
    private static final String DEREGISTRATION_CALLBACK_PATH = "deregistrationcallback";
    private static final String STATUS_PROP = "status";

    private RegisteringApplication registerApp = ManagementContextListener.getRegisterApp();

    /**
     * The registration URL is used to redirect and ultimately send back
     * the connection data string to this application.
     */
    @GET
    @Path("/register")
    public Response getRegistrationUrl(@Context UriInfo uriInfo) {
        String APPLICATION_TYPE = "custom";

        String callbackUrl = getCallbackUrl(uriInfo, 1, REGISTRATION_CALLBACK_PATH);

        try {
            String registrationURL = registerApp.createRegistrationRequest(callbackUrl, APPLICATION_TYPE);

            LOGGER.info("Generated registration url: " + registrationURL);
            return Response.ok(registrationURL).build();
        } catch (RuntimeException e) {
            LOGGER.error("Problem creating registration request", e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("failed to create registration URL: " + e.getMessage()).build();
        }
    }

    /**
     * This is called back when registration is complete to
     * save the connection data to persistent storage.
     */
    @GET
    @Path("/" + REGISTRATION_CALLBACK_PATH)
    public Response saveConnectionData(@Context UriInfo uriInfo,
                                       @QueryParam("connectionData") String connectionData) throws URISyntaxException {
        if (!Utils.saveConnectionData(connectionData)) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("failed to save connection data; check server settings").build();
        }

        ManagementContextListener.initManagementConnector(connectionData);
        String redirectUrl = getCallbackUrl(uriInfo, 2, "registrationComplete.html");

        // Once the connection data is saved, redirect to successful completion page.
        return Response.temporaryRedirect(new URI(redirectUrl)).build();
    }

    /**
     * The deregistration URL is used to deregister the application.
     */
    @GET
    @Path("/deregister")
    public Response getDeregistrationUrl(@Context UriInfo uriInfo) throws URISyntaxException {
        if (managementConnector==null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("management connector not started").build();
        }

        try {
            String callbackUrl = getCallbackUrl(uriInfo, 1, DEREGISTRATION_CALLBACK_PATH);
            String deregistrationURL = managementConnector.deregister(callbackUrl);
            LOGGER.info("Generated deregistration url: " + deregistrationURL);
            return Response.ok(deregistrationURL).build();
        } catch (ApiException e) {
            LOGGER.info("Error creating de-registration Url! The Error is: " + e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("failed to create de-registration URL: " + e.getMessage()).build();
        }
    }

    /**
     * Callback to clear out registration callback data
     */
    @GET
    @Path("/" + DEREGISTRATION_CALLBACK_PATH)
    public Response deregistrationCallback(@Context UriInfo uriInfo) throws URISyntaxException {

        // clear connection data
        Utils.saveConnectionData(null);
        String redirectUrl = getCallbackUrl(uriInfo, 2, "deregistrationComplete.html");

        // Once the connection data is saved, redirect to successful completion page.
        return Response.temporaryRedirect(new URI(redirectUrl)).build();
    }

    /**
     * This returns whether the connection data file is configured properly.
     */
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRegistrationStatus() {
        Map<String,String> status = new HashMap<>();
        String state;
        if (Utils.getConnectionData()!=null) {
            state = "registered";
        } else {
            state = "not registered";
        }
        status.put(STATUS_PROP, state);
        String response = new Gson().toJson(status);
        return Response.ok(response).build();
    }

    /**
     * Gets the callback URL
     * @param uriInfo the URI
     * @param count the number of slashes to go back
     * @param page the actual page
     */
    private String getCallbackUrl(UriInfo uriInfo, int count, String page) {
        String url = uriInfo.getAbsolutePath().toString();
        for (int i=0; i<count; i++) {
            url = url.substring(0,url.lastIndexOf('/'));
        }
        return url + "/" + page;
    }
}
