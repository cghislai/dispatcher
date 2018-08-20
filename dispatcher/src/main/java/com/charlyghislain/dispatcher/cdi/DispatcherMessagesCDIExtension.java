package com.charlyghislain.dispatcher.cdi;


import com.charlyghislain.dispatcher.api.context.TemplateContext;
import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.exception.DispatcherRuntimeException;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;
import com.charlyghislain.dispatcher.api.message.Message;
import com.charlyghislain.dispatcher.api.message.MessageDefinition;
import com.charlyghislain.dispatcher.util.CharacterSequences;
import com.charlyghislain.dispatcher.util.CharacterValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.enterprise.util.AnnotationLiteral;
import javax.validation.constraints.NotNull;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Scans all {@link MessageDefinition} in the classpath, performs sanity checks and register beans using the
 * {@link Message} qualifier. Application-scoped instances of  {@link DispatcherMessage}s can be injected in the code
 * using the following constructs:
 * <pre>
 * {@code
 *
 * @Inject
 * @Message(MessageDefinitionType.class)
 * private DispatcherMessage message;
 *
 * @Inject
 * @Message
 * private Instance<DispatcherMessage> allMessages;
 *
 * }
 * </pre>
 */
public class DispatcherMessagesCDIExtension implements Extension {

    private static final Logger LOG = LoggerFactory.getLogger(DispatcherMessagesCDIExtension.class);

    private Set<DispatcherMessage> messagesSet = new HashSet<>();

    public DispatcherMessagesCDIExtension() {
    }

    <T> void processAnnotatedType(@Observes @WithAnnotations({MessageDefinition.class}) ProcessAnnotatedType<T> processAnnotatedType) {
        AnnotatedType<T> annotatedType = processAnnotatedType.getAnnotatedType();
        Class<T> javaClass = annotatedType.getJavaClass();
        MessageDefinition annotation = annotatedType.getAnnotation(MessageDefinition.class);

        DispatcherMessage message = createMessage(javaClass, annotation);
        if (messagesSet.contains(message)) {
            String errorMessage = getDuplicateNameErrorMessage(javaClass, message, messagesSet);
            throw new DispatcherRuntimeException(errorMessage);
        } else {
            messagesSet.add(message);
            LOG.debug("Processed message " + message.getQualifiedName());
        }
        processAnnotatedType.veto();
    }

    private <T> String getDuplicateNameErrorMessage(Class<T> javaClass, DispatcherMessage message, Set<DispatcherMessage> messagesSet) {
        DispatcherMessage duplicateMessage = messagesSet.stream()
                .filter(msg -> msg.getName().equals(message.getName()))
                .findAny()
                .orElseThrow(IllegalStateException::new);
        return MessageFormat.format("Multiple messages named {0} found in the classpath : {1} and {2}. Make sure the archive containing the messages is only present once and all messages use an unique name.",
                message.getName(), duplicateMessage.getQualifiedName(), javaClass.getName());
    }

    public void afterDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery) {
        this.messagesSet.forEach(this::setMessageCompositionReferences);
        this.messagesSet.forEach(this::checkDependenciesLoop);
        this.messagesSet.forEach(msg -> this.registerBean(msg, afterBeanDiscovery));
    }


    private void setMessageCompositionReferences(DispatcherMessage dispatcherMessage) {
        Class<?> messageType = dispatcherMessage.getMessageType();
        MessageDefinition messageDefinitionTypeAnnotation = messageType.getAnnotation(MessageDefinition.class);
        setHeader(dispatcherMessage, messageDefinitionTypeAnnotation);
        setFooter(dispatcherMessage, messageDefinitionTypeAnnotation);
    }

    private void setHeader(DispatcherMessage dispatcherMessage, MessageDefinition messageDefinitionTypeAnnotation) {
        Class<?> headerClass = messageDefinitionTypeAnnotation.header();
        if (headerClass.equals(Void.class)) {
            return;
        }
        DispatcherMessage headerMessage = checkComposition(dispatcherMessage, headerClass, "header");
        dispatcherMessage.setHeader(headerMessage);
    }


    private void setFooter(DispatcherMessage dispatcherMessage, MessageDefinition messageDefinitionTypeAnnotation) {
        Class<?> footerClass = messageDefinitionTypeAnnotation.footer();
        if (footerClass.equals(Void.class)) {
            return;
        }
        DispatcherMessage footerMessage = checkComposition(dispatcherMessage, footerClass, "footer");
        dispatcherMessage.setFooter(footerMessage);
    }


    @NotNull
    private DispatcherMessage checkComposition(DispatcherMessage message, Class<?> compositionItemClass, String compositionName) {
        DispatcherMessage compositionMessage = this.messagesSet.stream()
                .filter(m -> m.getMessageType().equals(compositionItemClass))
                .findFirst()
                .orElseThrow(() -> this.newCompositionItemNotFoundError(compositionName, message, compositionItemClass));

        // check dispatching options
        Set<DispatchingOption> dispatchingOptions = message.getDispatchingOptions();
        boolean isMissingDispatchingOptions = dispatchingOptions.stream()
                .anyMatch(option -> !compositionMessage.getDispatchingOptions().contains(option));
        if (isMissingDispatchingOptions) {
            String errorMessage = MessageFormat.format("MessageDefinition {0} supports dispatching options {1}"
                            + " and reference {2} composition item {3}, however this latter only supports dispatching options {4}."
                            + " Make sure the composition items support all required dispatching options.",
                    message.getQualifiedName(), debugDispatchingOptions(message), compositionName,
                    compositionItemClass.getName(), debugDispatchingOptions(compositionMessage));
            throw new DispatcherRuntimeException(errorMessage);
        }
        return compositionMessage;
    }

    private DispatcherRuntimeException newCompositionItemNotFoundError(String compositionName, DispatcherMessage message, Class<?> compositionItemClass) {
        String errorMessage = MessageFormat.format("MessageDefinition {0} reference {1} composition items {2}, which is not found",
                message, compositionName, compositionItemClass.getName());
        throw new DispatcherRuntimeException(errorMessage);
    }

    private DispatcherMessage createMessage(Class<?> messageClass, MessageDefinition messageDefinitionAnnotation) {

        Class<?>[] templateContexts = messageDefinitionAnnotation.templateContexts();
        List<Class<?>> templateContextsList = Arrays.asList(templateContexts);
        String qualifiedName = messageClass.getName();
        String simpleName = getSimpleName(messageClass);
        String description = getNonEmptyString(messageDefinitionAnnotation.description())
                .orElse(null);
        Set<DispatchingOption> dispatchingOptions = Arrays.stream(messageDefinitionAnnotation.dispatchingOptions())
                .collect(Collectors.toSet());
        boolean compositionItem = messageDefinitionAnnotation.compositionItem();

        DispatcherMessage dispatcherMessage = new DispatcherMessage();
        dispatcherMessage.setDescription(description);
        dispatcherMessage.setDispatchingOptions(dispatchingOptions);
        dispatcherMessage.setMessageType(messageClass);
        dispatcherMessage.setQualifiedName(qualifiedName);
        dispatcherMessage.setName(simpleName);
        dispatcherMessage.setTemplateContexts(templateContextsList);
        dispatcherMessage.setCompositionItem(compositionItem);

        performSanityChecks(dispatcherMessage);
        return dispatcherMessage;
    }

    @NotNull
    private String getSimpleName(Class<?> messageClass) {
        MessageDefinition messageDefinitionAnnotation = this.getMessageAnnotation(messageClass);
        return getNonEmptyString(messageDefinitionAnnotation.name())
                .orElse(messageClass.getSimpleName());
    }

    private void performSanityChecks(DispatcherMessage dispatcherMessage) {
        String name = dispatcherMessage.getName();
        boolean validName = CharacterValidationUtils.allMatch(name, CharacterSequences.ALPHANUMERIC_WITH_DASH_AND_SLASH);
        if (!validName) {
            String errorMessage = MessageFormat.format("MessageDefinition name {0} is not valid. Only alphanumeric and dashes are allowed",
                    name);
            throw new DispatcherRuntimeException(errorMessage);
        }

        dispatcherMessage.getTemplateContexts()
                .forEach(this::verifyValidTemplateContextObject);
    }


    private void verifyValidTemplateContextObject(Class<?> aClass) {
        TemplateContext annotation = aClass.getAnnotation(TemplateContext.class);
        if (annotation == null) {
            String errorMessage = MessageFormat.format("Referenced template class {0} is not annotated with @TemplateContext",
                    aClass.getName());
            throw new DispatcherRuntimeException(errorMessage);
        }
    }

    private MessageDefinition getMessageAnnotation(Class<?> messageType) {
        MessageDefinition annotation = messageType.getAnnotation(MessageDefinition.class);
        if (annotation == null) {
            throw new IllegalStateException("No @MessageDefinition annotation found on bean");
        }
        return annotation;
    }

    private Optional<String> getNonEmptyString(String value) {
        if (value.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(value);
    }


    private void checkDependenciesLoop(DispatcherMessage dispatcherMessage) {
        List<DispatcherMessage> dependencyChain = new ArrayList<>();
        this.checkDependenciesLoop(dependencyChain, dispatcherMessage);
    }

    private void checkDependenciesLoop(List<DispatcherMessage> curChain, DispatcherMessage nextMessage) {
        if (curChain.contains(nextMessage)) {
            throwDependencyLoopException(curChain, nextMessage);
        }
        ArrayList<DispatcherMessage> nextChain = new ArrayList<>(curChain);
        nextChain.add(nextMessage);

        nextMessage.getHeader()
                .ifPresent(header -> this.checkDependenciesLoop(nextChain, header));

        nextMessage.getFooter()
                .ifPresent(footer -> this.checkDependenciesLoop(nextChain, footer));
    }

    private void throwDependencyLoopException(List<DispatcherMessage> dependencyChain, DispatcherMessage nextMessage) {
        dependencyChain.add(nextMessage);
        String chainString = dependencyChain.stream()
                .map(DispatcherMessage::getQualifiedName)
                .reduce("", (cur, next) -> cur.isEmpty() ? next : cur + " -> " + next);
        throw new DispatcherRuntimeException("Dependency loop in composed messages: " + chainString);
    }


    private String debugDispatchingOptions(DispatcherMessage dispatcherMessage) {
        return dispatcherMessage.getDispatchingOptions().stream()
                .reduce("", (a, b) -> a.isEmpty() ? b.name() : a + ", " + b.name(), String::concat);
    }

    private void registerBean(DispatcherMessage message, AfterBeanDiscovery afterBeanDiscovery) {
        if (message.isCompositionItem()) {
            LOG.debug("Skipping bean creation for composition item " + message);
            return;
        }
        afterBeanDiscovery.addBean()
                .scope(ApplicationScoped.class)
                .addQualifier(new MessageLiteral(message.getMessageType()))
                .types(DispatcherMessage.class)
                .id(getBeanId(message))
                .createWith(creationalContext -> message);
        afterBeanDiscovery.addBean()
                .scope(ApplicationScoped.class)
                .addQualifier(new MessageLiteral(Void.class))
                .types(DispatcherMessage.class)
                .id(getGlobalBeanId(message))
                .createWith(creationalContext -> message);
        LOG.info("Created bean for " + message);
    }

    private String getBeanId(DispatcherMessage message) {
        return DispatcherMessagesCDIExtension.class.getName() + " :" + message.getName() + ":" + message.getMessageType().getSimpleName();
    }

    private String getGlobalBeanId(DispatcherMessage message) {
        return DispatcherMessagesCDIExtension.class.getName() + " :" + message.getName() + ":Void";
    }

    private class MessageLiteral extends AnnotationLiteral<Message> implements Message {

        private Class<?> value;

        public MessageLiteral(Class<?> value) {
            this.value = value;
        }

        @Override
        public Class<?> value() {
            return value;
        }
    }

}
