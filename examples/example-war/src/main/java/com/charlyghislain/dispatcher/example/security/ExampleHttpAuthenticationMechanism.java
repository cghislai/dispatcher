package com.charlyghislain.dispatcher.example.security;

import com.charlyghislain.dispatcher.management.api.security.DispatcherManagementRoles;

import javax.security.enterprise.AuthenticationException;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ExampleHttpAuthenticationMechanism implements HttpAuthenticationMechanism {

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthenticationException {
        String userParam = request.getParameter("user");
        String user = Optional.ofNullable(userParam)
                .orElse("example-user");
        Set<String> groups = new HashSet<>();
        groups.add("user");
        groups.add(DispatcherManagementRoles.ROLE_MANAGER);

        httpMessageContext.notifyContainerAboutLogin(user, groups);
        return AuthenticationStatus.SUCCESS;
    }
}
