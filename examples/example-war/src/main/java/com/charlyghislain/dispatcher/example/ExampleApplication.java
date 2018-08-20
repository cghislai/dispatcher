package com.charlyghislain.dispatcher.example;

import com.charlyghislain.dispatcher.example.resource.ExampleResourceController;
import com.charlyghislain.dispatcher.management.api.security.DispatcherManagementRoles;
import com.charlyghislain.dispatcher.management.provider.DispatcherWebExceptionMapper;
import com.charlyghislain.dispatcher.management.provider.WebApplicationExceptionMapper;
import com.charlyghislain.dispatcher.management.web.DispatcherMessagesResourceController;
import com.charlyghislain.dispatcher.management.web.DispatcherReferencedResourcesResourceController;
import com.charlyghislain.dispatcher.management.web.DispatcherSharedResourcesResourceController;

import javax.annotation.security.DeclareRoles;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("")
@DeclareRoles({"user", DispatcherManagementRoles.ROLE_MANAGER})
public class ExampleApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(DispatcherSharedResourcesResourceController.class);
        classes.add(DispatcherMessagesResourceController.class);
        classes.add(DispatcherReferencedResourcesResourceController.class);

        classes.add(ExampleResourceController.class);

        classes.add(DispatcherWebExceptionMapper.class);
        classes.add(WebApplicationExceptionMapper.class);

        return classes;
    }
}
