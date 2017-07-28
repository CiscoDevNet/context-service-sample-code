package com.cisco.thunderhead.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Used by Jersey to handle exceptions thrown by the application.
 */
@Provider
public class ContextExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextExceptionMapper.class);

    @Override
    public Response toResponse(Throwable throwable) {
        String response = Utils.getError(throwable.getMessage());
        Response.Status status;
        if (throwable instanceof ContextException) {
            status = Response.Status.BAD_REQUEST;
        } else {
            status = Response.Status.INTERNAL_SERVER_ERROR;
        }
        LOGGER.error("exception thrown", throwable);
        return Response.status(status).entity(response).build();
    }
}
