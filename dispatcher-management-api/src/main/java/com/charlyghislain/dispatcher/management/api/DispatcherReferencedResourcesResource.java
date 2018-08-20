package com.charlyghislain.dispatcher.management.api;


import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

@Path("/dispatcher/referenced-resource")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DispatcherReferencedResourcesResource {

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    StreamingOutput streamReferencedResource(@QueryParam("resourceId") String resourceId, @Context HttpServletResponse servletResponse);

}
