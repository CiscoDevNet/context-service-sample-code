package com.cisco.thunderhead.rest;

import com.cisco.thunderhead.ContextBean;
import com.cisco.thunderhead.ContextObject;
import com.cisco.thunderhead.client.ClientResponse;
import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.client.SearchParameters;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simple REST interface for Context Service.
 */
@Path("/")
public class ContextService {
    private ContextServiceClient contextServiceClient = RestApiContextListener.getContextServiceClient();

    /**
     * Creates a new object in Context Service.
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(RESTContextObject restContextObject) throws ContextException {
        final String type = restContextObject.getType();

        try {
            validateType(type);

            ContextObject contextBean = new ContextObject(restContextObject.getType());
            RESTContextObject.copyToContextBean(contextBean, restContextObject);
            contextServiceClient.create(contextBean);

            String uri = type + "/" + contextBean.getId().toString();
            return Response.created(new URI(uri)).build();
        } catch (Exception e) {
            throw new ContextException("failed to create class of type " + type, e);
        }
    }

    /**
     * Updates an existing object in Context Service.
     */
    @PUT
    @Path("/{type}/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("type") String type, @PathParam("id") String id, RESTContextObject restContextObject) throws ContextException {

        validateType(type, restContextObject);

        if (restContextObject.getId()!=null
                && StringUtils.isNotBlank(restContextObject.getId().toString())
                && !StringUtils.equals(restContextObject.getId().toString(), id)) {
            throw new ContextException("context object ID '" + restContextObject.getId() + "' does not match query parameter id '" + id + "'");
        }
        ContextObject contextBean = contextServiceClient.getContextObject(type, id);
        RESTContextObject.copyToContextBean(contextBean, restContextObject);
        ClientResponse update = contextServiceClient.update(contextBean);
        return Response.accepted().build();
    }

    /**
     * Returns the specified object from Context Service.
     */
    @GET
    @Path("/{type}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("type") String type, @PathParam("id") String id) {
        validateType(type);

        ContextObject contextBean = contextServiceClient.getContextObject(type, id);
        return Response.ok().entity(new RESTContextObject(contextBean)).build();
    }

    /**
     * Deletes the specified item from Context Service.
     */
    @DELETE
    @Path("/{type}/{id}")
    public Response delete(@PathParam("type") String type, @PathParam("id") String id) {
        validateType(type);

        // first have to do a GET
        ContextObject contextBean = contextServiceClient.getContextObject(type, id);

        // now we can do the delete.
        contextServiceClient.delete(contextBean);
        return Response.accepted().build();
    }

    /**
     * Search for objects
     */
    @POST
    @Path("/search/{type}")
    public Response search(@PathParam("type") String type, SearchParams searchParams) {

        validateType(type);

        if (searchParams.getOperation()==null) {
            throw new ContextException("invalid search parameter operation " + searchParams.operation);
        }

        SearchParameters searchParameters = searchParams.getSearchParameters();
        if (searchParameters == null) {
            throw new ContextException("invalid query option");
        }
        // need to make sure we search for objects of the correct type
        // for ContextObject, the search parameters must include the type
        if (searchParameters.containsKey("type")) {
            final List<String> searchTypes = searchParameters.get("type");
            // there must only be 1, and it must match the type specified in the URL
            if (searchTypes.size() != 1 || !StringUtils.equals(searchTypes.get(0), type)) {
                throw new ContextException("searchParam type does not match expected type " + type);
            }
        } else {
            // add the type to the search parameters
            searchParameters.put("type", Arrays.asList(type));
        }

        List<? extends ContextBean> contextBeans = contextServiceClient.search(ContextObject.class, searchParameters, searchParams.getOperation());
        List<RESTContextObject> restContextObjects = new ArrayList<>();
        for (ContextBean contextBean : contextBeans) {
            restContextObjects.add(new RESTContextObject((ContextObject) contextBean));
        }
        return Response.ok().entity(restContextObjects).build();
    }

    /**
     * Returns status of the Context Service client.
     */
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus() {
        String status = contextServiceClient.getStatus();
        return Response.ok().entity(status).build();
    }

    private static void validateType(String type) throws ContextException {
        switch (type) {
            case ContextObject.Types.REQUEST:
            case ContextObject.Types.POD:
            case ContextObject.Types.CUSTOMER:
                return;
        }

        throw new ContextException("Unsupported object type " + type);
    }

    private static void validateType(String type, RESTContextObject restContextObject) throws ContextException {
        validateType(type);
        if (StringUtils.isNotBlank(restContextObject.getType()) && StringUtils.equals(type, restContextObject.getType())) {
            throw new ContextException("context object type '" + restContextObject.getType() + "' does not match expected type '" + type + "'");
        }
    }
}
