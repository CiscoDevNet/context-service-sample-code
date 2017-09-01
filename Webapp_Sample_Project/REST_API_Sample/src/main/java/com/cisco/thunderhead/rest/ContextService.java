package com.cisco.thunderhead.rest;

import com.cisco.thunderhead.ContextBean;
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
import java.net.URISyntaxException;
import java.util.ArrayList;
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
    public Response create(ContextObject contextObject) throws URISyntaxException {
        Class<? extends ContextBean> clazz = validateTypeAndGetClass(contextObject.getType());

        try {
            ContextBean contextBean = clazz.newInstance();
            ContextObject.copyToContextBean(contextBean, contextObject);
            contextServiceClient.create(contextBean);

            String uri = contextObject.getType() + "/" + contextBean.getId().toString();
            return Response.created(new URI(uri)).build();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ContextException("failed to create class of type " + contextObject.getType(), e);
        }
    }

    /**
     * Updates an existing object in Context Service.
     */
    @PUT
    @Path("/{type}/{id}")
    public Response put(@PathParam("type") String type, @PathParam("id") String id, ContextObject contextObject) {
        Class<? extends ContextBean> clazz = validateTypeAndGetClass(type);

        if (contextObject.getId()!=null
                && StringUtils.isNotBlank(contextObject.getId().toString())
                && !StringUtils.equals(contextObject.getId().toString(), id)) {
            throw new ContextException("context object ID '" + contextObject.getId() + "' does not match query parameter id '" + id + "'");
        }
        ContextBean contextBean = contextServiceClient.get(clazz, id);
        ContextObject.copyToContextBean(contextBean, contextObject);
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
        Class<? extends ContextBean> clazz = validateTypeAndGetClass(type);

        ContextBean contextBean = contextServiceClient.get(clazz, id);
        return Response.ok().entity(new ContextObject(contextBean)).build();
    }

    /**
     * Deletes the specified item from Context Service.
     */
    @DELETE
    @Path("/{type}/{id}")
    public Response delete(@PathParam("type") String type, @PathParam("id") String id) {
        Class<? extends ContextBean> clazz = validateTypeAndGetClass(type);

        // first have to do a GET
        ContextBean contextBean = contextServiceClient.get(clazz, id);

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
        Class<? extends ContextBean> clazz = validateTypeAndGetClass(type);

        if (searchParams.getOperation()==null) {
            throw new ContextException("invalid search parameter operation " + searchParams.operation);
        }

        SearchParameters searchParameters = searchParams.getSearchParameters();
        if (searchParameters == null) {
            throw new ContextException("invalid query option");
        }

        List<? extends ContextBean> contextBeans = contextServiceClient.search(clazz, searchParameters, searchParams.getOperation());
        List<ContextObject> contextObjects = new ArrayList<>();
        for (ContextBean contextBean : contextBeans) {
            contextObjects.add(new ContextObject(contextBean));
        }
        return Response.ok().entity(contextObjects).build();
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

    private Class<? extends ContextBean> validateTypeAndGetClass(String type) {
        Class<? extends ContextBean> clazz = ContextObject.determineTypeClass(type);
        if (clazz==null) {
            throw new ContextException("invalid type: " + type);
        }
        return clazz;
    }
}
