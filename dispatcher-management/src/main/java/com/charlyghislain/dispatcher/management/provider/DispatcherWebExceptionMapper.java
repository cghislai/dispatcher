package com.charlyghislain.dispatcher.management.provider;


import com.charlyghislain.dispatcher.management.api.domain.WsManagementError;
import com.charlyghislain.dispatcher.management.error.DispatcherWebException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
public class DispatcherWebExceptionMapper implements ExceptionMapper<DispatcherWebException> {


    @Override
    public Response toResponse(DispatcherWebException exception) {
        String code = exception.getCode();
        int status = exception.getStatus();
        String description = exception.getDescription();

        WsManagementError wsManagementError = new WsManagementError();
        wsManagementError.setCode(code);
        wsManagementError.setDescription(description);

        return Response
                .status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(wsManagementError)
                .build();
    }
}
