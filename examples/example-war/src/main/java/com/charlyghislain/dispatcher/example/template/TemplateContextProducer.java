package com.charlyghislain.dispatcher.example.template;


import com.charlyghislain.dispatcher.api.context.ProducedTemplateContext;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;

public class TemplateContextProducer {

    @Produces
    @Dependent
    @ProducedTemplateContext
    public AppContext producesAppContext() {
        AppContext appContext = new AppContext();
        appContext.setName("ApplicationNameAtRuntime");
        appContext.setVersion(2);
        return appContext;
    }

    @Produces
    @Dependent
    @ProducedTemplateContext
    public RequestContext producesRequestContext(HttpServletRequest servletRequest) {
        String pathInfo = servletRequest.getPathInfo();
        String remoteUser = servletRequest.getRemoteUser();

        RequestContext requestContext = new RequestContext();
        requestContext.setRequestPath(pathInfo);
        requestContext.setUserName(remoteUser);
        return requestContext;
    }

}
