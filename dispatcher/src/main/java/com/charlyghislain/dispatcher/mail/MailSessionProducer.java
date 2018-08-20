package com.charlyghislain.dispatcher.mail;

import com.charlyghislain.dispatcher.api.configuration.ConfigConstants;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Optional;
import java.util.Properties;

public class MailSessionProducer {

    @Inject
    @ConfigProperty(name = ConfigConstants.MAIL_TRANSPORT_PROTOCOL,
            defaultValue = ConfigConstants.MAIL_TRANSPORT_PROTOCOL_DEFAULT_VALUE)
    private String transportProtocol;
    @Inject
    @ConfigProperty(name = ConfigConstants.MAIL_HOST,
            defaultValue = ConfigConstants.MAIL_HOST_DEFAULT_VALUE)
    private String host;
    @Inject
    @ConfigProperty(name = ConfigConstants.MAIL_USER,
            defaultValue = ConfigConstants.MAIL_USER_DEFAULT_VALUE)
    private String user;
    @Inject
    @ConfigProperty(name = ConfigConstants.MAIL_FROM,
            defaultValue = ConfigConstants.MAIL_FROM_DEFAULT_VALUE)
    private String from;
    @Inject
    @ConfigProperty(name = ConfigConstants.MAIL_DEBUG,
            defaultValue = ConfigConstants.MAIL_DEBUG_DEFAULT_VALUE)
    private boolean debug;
    @Inject
    @ConfigProperty(name = ConfigConstants.MAIL_AUTH_USER)
    private Optional<String> authUser;
    @Inject
    @ConfigProperty(name = ConfigConstants.MAIL_AUTH_PASSWORD)
    private Optional<String> authPassword;
    @Inject
    @ConfigProperty(name = ConfigConstants.MAIL_SMTP_STARTTLS_ENABLE,
            defaultValue = ConfigConstants.MAIL_SMTP_STARTTLS_ENABLE_DEFAULT_VALUE)
    private boolean enableSmtpStartTls;
    @Inject
    @ConfigProperty(name = ConfigConstants.MAIL_SMTP_PORT,
            defaultValue = ConfigConstants.MAIL_SMTP_PORT_DEFAULT_VALUE)
    private int smtpPort;


    @Produces
    @MailSession
    public Session provideMailSession() {
        Properties properties = new Properties();
        properties.put("mail.transport.protocol", transportProtocol);
        properties.put("mail.host", host);
        properties.put("mail.user", user);
        properties.put("mail.from", from);
        properties.put("mail.debug", debug);
        properties.put("mail.smtp.starttls.enable", enableSmtpStartTls);
        properties.put("mail.smtp.port", smtpPort);

        return this.createSession(properties);
    }

    private Session createSession(Properties properties) {
        if (authUser.isPresent()) {
            properties.put("mail.smtp.auth", "true");
            if (!enableSmtpStartTls) {
                properties.put("mail.smtp.socketFactory.port", smtpPort);
                properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                properties.put("mail.smtp.socketFactory.fallback", "false");
            }
            MailAuthenticator mailAuthenticator = new MailAuthenticator(authUser.get(), authPassword.orElse(null));
            return Session.getInstance(properties, mailAuthenticator);
        } else {
            return Session.getInstance(properties);
        }
    }

    private static class MailAuthenticator extends Authenticator {

        private final PasswordAuthentication authentication;

        public MailAuthenticator(String user, String password) {
            this.authentication = new PasswordAuthentication(user, password);
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return this.authentication;
        }
    }
}
