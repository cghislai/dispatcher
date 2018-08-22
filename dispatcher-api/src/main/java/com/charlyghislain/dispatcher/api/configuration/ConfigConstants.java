package com.charlyghislain.dispatcher.api.configuration;

public class ConfigConstants {

    public static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";
    public static final String MAIL_TRANSPORT_PROTOCOL_DEFAULT_VALUE = "smtp";
    public static final String MAIL_HOST = "mail.host";
    public static final String MAIL_HOST_DEFAULT_VALUE = "localohost";
    public static final String MAIL_USER = "mail.user";
    public static final String MAIL_USER_DEFAULT_VALUE = "";
    public static final String MAIL_FROM = "mail.from";
    public static final String MAIL_FROM_DEFAULT_VALUE = "";
    public static final String MAIL_DEBUG = "mail.debug";
    public static final String MAIL_DEBUG_DEFAULT_VALUE = "false";
    public static final String MAIL_AUTH_USER = "mail.auth.user";
    public static final String MAIL_AUTH_PASSWORD = "mail.auth.password";
    public static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    public static final String MAIL_SMTP_STARTTLS_ENABLE_DEFAULT_VALUE = "true";
    public static final String MAIL_SMTP_PORT = "mail.smtp.port";
    public static final String MAIL_SMTP_PORT_DEFAULT_VALUE = "25";


    public static final String MAIL_TRANSPORT_ENABLED
            = "com.charlyghislain.dispatcher.mail.transportEnabled";
    public static final String MAIL_TRANSPORT_ENABLED_DEFAULT_VALUE = "true";

    public static final String MAIL_TRANSPORT_WHITELISTED_ADDRESSES_REGEXP
            = "com.charlyghislain.dispatcher.mail.whitelistedAddressesRegexp";
    public static final String MAIL_TRANSPORT_WHITELISTED_ADDRESSES_REGEXP_DEFAULT_VALUE
            = "^.*$";


    /**
     * Writable path on the filesystem for message resources. This will take precedence over resources in the
     * application classpath. This can be disabled by setting the property to an empty value. In such case,
     * service methods updating the template (MessageResourcesUpdateService) will throw runtime exceptions.
     */
    public static final String FILESYSTEM_WRITABLE_RESOURCE_PATH
            = "com.charlyghislain.dispatcher.fileSystemWritableResourcePath";
    public static final String FILESYSTEM_WRITABLE_RESOURCE_PATH_DEFAULT_VALUE
            = "/var/run/templates";

    /**
     * Relative path under which to look for resources, both in the class path and on the filesystem.
     */
    public static final String RESOURCES_BASE_DIR
            = "com.charlyghislain.dispatcher.resourcesBaseDir";
    public static final String RESOURCES_BASE_DIR_DEFAULT_VALUE
            = "";

    /**
     * A directory besides messages resources directories to contains shared resources like images.
     * You can use underscores or dots in the name to prevent collision with message names.
     */
    public static final String SHARED_RESOURCES_PATH
            = "com.charlyghislain.dispatcher.sharedResourcesSubpath";
    public static final String SHARED_RESOURCES_PATH_DEFAULT_VALUE
            = "shared_resources";


    /**
     * An url to which the mail resources (images) will be made accessible for template previews.
     * The url will be called with a 'resourceId' query parameter.
     */
    public static final String WEB_ACCESSIBLE_RESOURCES_URL
            = "com.charlyghislain.dispatcher.webAccessibleResourcesUrl";
    public static final String WEB_ACCESSIBLE_RESOURCES_URL_DEFAULT_VALUE
            = "shared_resources";
}
