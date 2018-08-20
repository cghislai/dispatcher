package com.charlyghislain.dispatcher.management;

import org.eclipse.microprofile.auth.LoginConfig;

import javax.annotation.security.DeclareRoles;
import javax.ws.rs.ApplicationPath;

import static com.charlyghislain.dispatcher.management.api.security.DispatcherManagementRoles.ROLE_MANAGER;

@ApplicationPath("")
@LoginConfig(authMethod = "MP-JWT")
@DeclareRoles(ROLE_MANAGER)
public class DispatcherManagementApplication {
}
