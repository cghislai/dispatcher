package com.charlyghislain.dispatcher.management.api;


import com.charlyghislain.dispatcher.management.api.domain.WsDispatcherMessage;
import com.charlyghislain.dispatcher.management.api.domain.WsDispatcherMessageFilter;
import com.charlyghislain.dispatcher.management.api.domain.WsDispatchingOption;
import com.charlyghislain.dispatcher.management.api.domain.WsMailHeaders;
import com.charlyghislain.dispatcher.management.api.domain.WsMessageTemplateVariable;
import com.charlyghislain.dispatcher.management.api.domain.WsResultList;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.util.List;

@Path("/dispatcher/management/message")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DispatcherMessagesResource {

    @GET
    @Path("/list")
    WsResultList<WsDispatcherMessage> listAllMessages(@BeanParam WsDispatcherMessageFilter messageFilter);

    @GET
    @Path("/{simpleName}")
    WsDispatcherMessage getMessage(@PathParam("simpleName") String name);

    @GET
    @Path("/{simpleName}/template/{type}")
    @Produces(MediaType.TEXT_PLAIN)
    StreamingOutput streamMessageTemplate(@PathParam("simpleName") String name, @PathParam("type") WsDispatchingOption wsDispatchingOption,
                                          @Context HttpHeaders httpHeaders);

    @PUT
    @Path("/{simpleName}/template/{type}/{locale}")
    @Consumes(MediaType.TEXT_PLAIN)
    void updateMessageTemplate(@PathParam("simpleName") String name, @PathParam("type") WsDispatchingOption wsDispatchingOption,
                               @PathParam("locale") String languageTag, InputStream body);

    @PUT
    @Path("/{simpleName}/template/{type}")
    @Consumes(MediaType.TEXT_PLAIN)
    void updateRootLocaleMessageTemplate(@PathParam("simpleName") String name, @PathParam("type") WsDispatchingOption wsDispatchingOption,
                                         InputStream body);

    @GET
    @Path("/{simpleName}/template/{type}/rendered")
    @Produces(MediaType.TEXT_HTML)
    StreamingOutput streamRenderedMessageTemplateExampleHtml(@PathParam("simpleName") String name, @PathParam("type") WsDispatchingOption wsDispatchingOption,
                                                             @Context HttpHeaders httpHeaders);

    @GET
    @Path("/{simpleName}/template/{type}/rendered")
    @Produces(MediaType.TEXT_PLAIN)
    StreamingOutput steamRenderedMessageTemplateExamplePlainText(@PathParam("simpleName") String name, @PathParam("type") WsDispatchingOption wsDispatchingOption,
                                                                 @Context HttpHeaders httpHeaders);


    @GET
    @Path("/{simpleName}/template/{type}/locale/list")
    List<String> getAvailableLocalesWithContent(@PathParam("simpleName") String name, @PathParam("type") WsDispatchingOption wsDispatchingOption);

    @GET
    @Path("/{simpleName}/template/MAIL/headers")
    WsMailHeaders getMailHeadersTemplate(@PathParam("simpleName") String name,
                                         @Context HttpHeaders httpHeaders);

    @PUT
    @Path("/{simpleName}/template/MAIL/headers/{locale}")
    void updateMailHeadersTemplate(@PathParam("simpleName") String name, @PathParam("locale") String languageTag,
                                   WsMailHeaders templateMailHeaders);

    @PUT
    @Path("/{simpleName}/template/MAIL/headers")
    void updateRootLocaleMailHeadersTemplate(@PathParam("simpleName") String name,
                                             WsMailHeaders templateMailHeaders);


    @GET
    @Path("/{simpleName}/template/MAIL/headers/rendered")
    WsMailHeaders getRenderedMailHeadersExample(@PathParam("simpleName") String name,
                                                @Context HttpHeaders httpHeaders);

    @GET
    @Path("/{simpleName}/template/variables")
    List<WsMessageTemplateVariable> getMessageTemplateVariableDescriptions(@PathParam("simpleName") String name);


}
