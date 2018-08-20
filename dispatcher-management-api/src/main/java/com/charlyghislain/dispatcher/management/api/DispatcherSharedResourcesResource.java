package com.charlyghislain.dispatcher.management.api;


import com.charlyghislain.dispatcher.management.api.domain.WsResultList;
import com.charlyghislain.dispatcher.management.api.domain.WsSharedResource;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;

@Path("/dispatcher/management/shared-resources")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DispatcherSharedResourcesResource {

    @GET
    @Path("/list")
    WsResultList<WsSharedResource> listAllSharedResources();

    @GET
    @Path("/{path}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    StreamingOutput streamSharedResource(@PathParam("path") String resourcePath, @Context HttpServletResponse servletResponse);

    @GET
    @Path("/{path}")
    WsSharedResource getSharedResource(@PathParam("path") String resourcePath);


    @PUT
    @Path("/{path}}")
    @Consumes(MediaType.WILDCARD)
    void uploadSharedResource(@PathParam("path") String resourcePath,
                              @HeaderParam("Content-Type") String contentType,
                              InputStream contentStream);

}
