package com.charlyghislain.dispatcher.management.provider;

import com.charlyghislain.dispatcher.management.api.domain.WsManagementError;
import com.charlyghislain.dispatcher.management.api.error.DispatcherWebError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private static final Logger LOG = LoggerFactory.getLogger(WebApplicationExceptionMapper.class);


    @Override
    public Response toResponse(WebApplicationException exception) {
        LOG.warn("Uncaught exception while serving request", exception);


        WsManagementError wsManagementError = new WsManagementError();
        wsManagementError.setCode(DispatcherWebError.UNEXPECTED_ERROR.name());
        wsManagementError.setDescription(exception.getMessage());

        return Response
                .status(exception.getResponse().getStatus())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(wsManagementError)
                .build();
    }
}
