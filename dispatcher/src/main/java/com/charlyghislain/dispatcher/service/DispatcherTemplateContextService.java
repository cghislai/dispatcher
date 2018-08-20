package com.charlyghislain.dispatcher.service;

import com.charlyghislain.dispatcher.api.context.ProducedTemplateContext;
import com.charlyghislain.dispatcher.api.context.TemplateContext;
import com.charlyghislain.dispatcher.api.context.TemplateContextObject;
import com.charlyghislain.dispatcher.api.context.TemplateField;
import com.charlyghislain.dispatcher.api.context.TemplateVariableDescription;
import com.charlyghislain.dispatcher.api.exception.DispatcherRuntimeException;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;
import com.charlyghislain.dispatcher.api.service.TemplateContextsService;
import com.charlyghislain.dispatcher.util.FieldAccessUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class DispatcherTemplateContextService implements TemplateContextsService {

    private static final Logger LOG = LoggerFactory.getLogger(DispatcherTemplateContextService.class);

    @Override
    public List<TemplateContextObject> createTemplateContexts(DispatcherMessage message, Object... providedContextObjects) {
        return streamMessageTemplateContexts(message)
                .map(objectClass -> this.getTemplateContextObject(objectClass, providedContextObjects))
                .collect(Collectors.toList());
    }


    @Override
    public List<TemplateContextObject> createExampleTemplateContexts(DispatcherMessage message) {
        return streamMessageTemplateContexts(message)
                .map(this::createExampleTemplateContextObject)
                .collect(Collectors.toList());
    }

    @Override
    public List<TemplateVariableDescription> listTemplateVariableDescriptions(DispatcherMessage message) {
        List<TemplateVariableDescription> variableDescriptions = streamMessageTemplateContexts(message)
                .flatMap(this::streamVariablesDescription)
                .collect(Collectors.toList());
        List<TemplateVariableDescription> toolVariables = this.getToolVariableDescriptions();
        variableDescriptions.addAll(toolVariables);
        return variableDescriptions;
    }

    private List<TemplateVariableDescription> getToolVariableDescriptions() {
        List<TemplateVariableDescription> toolDescriptions = new ArrayList<>();

        TemplateVariableDescription dateToolDescription = new TemplateVariableDescription();
        dateToolDescription.setVariableName("date");
        dateToolDescription.setDescription("Date formatting tool. See Apache velocity DateTool documentation");
        dateToolDescription.setExample("${date.format('medium', ${dateVariable})}");
        toolDescriptions.add(dateToolDescription);

        TemplateVariableDescription resToolDescription = new TemplateVariableDescription();
        resToolDescription.setVariableName("res");
        resToolDescription.setDescription("Tool to include inline resources such as images in the template");
        resToolDescription.setExample("${res.load('res/image.png')}");
        toolDescriptions.add(resToolDescription);

        return toolDescriptions;
    }

    private TemplateContextObject createKeyedContextObject(Class<?> objectClass, Object objectInstance) {
        TemplateContext annotation = objectClass.getAnnotation(TemplateContext.class);
        String key = annotation.key();

        TemplateContextObject contextObject = new TemplateContextObject();
        contextObject.setKey(key);
        contextObject.setValue(objectInstance);
        return contextObject;
    }


    private Stream<Class<?>> streamMessageTemplateContexts(DispatcherMessage message) {
        Stream<Class<?>> contentTemplateContexts = message.getTemplateContexts().stream();
        Stream<Class<?>> headerTemplateContexts = message.getHeader()
                .map(DispatcherMessage::getTemplateContexts)
                .map(List::stream)
                .orElseGet(Stream::empty);
        Stream<Class<?>> footerTemplateContexts = message.getFooter()
                .map(DispatcherMessage::getTemplateContexts)
                .map(List::stream)
                .orElseGet(Stream::empty);
        return Stream.concat(contentTemplateContexts, Stream.concat(headerTemplateContexts, footerTemplateContexts))
                .distinct();
    }

    private TemplateContextObject getTemplateContextObject(Class<?> contextObjectClass, Object... providedContextObjects) {
        Object objectInstance = Arrays.stream(providedContextObjects)
                .filter(object -> object.getClass().equals(contextObjectClass))
                .findAny()
                .orElseGet(() -> this.createTemplateContextObject(contextObjectClass));
        return createKeyedContextObject(contextObjectClass, objectInstance);
    }

    private Object createTemplateContextObject(Class<?> contextObjectClass) {
        TemplateContext templateContextAnnotation = contextObjectClass.getAnnotation(TemplateContext.class);
        boolean produced = templateContextAnnotation.produced();
        if (!produced) {
            String message = MessageFormat.format("Template context {0} is not produced but wasn't provided", contextObjectClass.getName());
            throw new DispatcherRuntimeException(message);
        }
        return lookupTemplateContextObject(contextObjectClass)
                .orElseThrow(() -> new DispatcherRuntimeException("No template context object found for class " + contextObjectClass.getName()));
    }

    private Optional<Object> lookupTemplateContextObject(Class<?> contextObjectClass) {
        try {
            InitialContext initialContext = new InitialContext();
            BeanManager beanManager = (BeanManager) initialContext.lookup("java:comp/BeanManager");
            Set<Bean<?>> contextBeans = beanManager.getBeans(contextObjectClass, new ProducedTemplateAnnotationLiteral());
            return Optional.ofNullable(beanManager.resolve(contextBeans))
                    .map(bean -> beanManager.getReference(bean, contextObjectClass, beanManager.createCreationalContext(bean)));
        } catch (NamingException e) {
            throw new DispatcherRuntimeException("Failed to initialize template context object lookup");
        }
    }


    private Stream<TemplateVariableDescription> streamVariablesDescription(Class<?> templateContextObjectClass) {
        TemplateContext templateContextObjectClassAnnotation = templateContextObjectClass.getAnnotation(TemplateContext.class);
        String key = templateContextObjectClassAnnotation.key();
        return this.streamTemplateVariableDescriptions(templateContextObjectClass, new String[]{key});
    }


    private <T> Stream<TemplateVariableDescription> streamTemplateVariableDescriptions(Class<T> templateType, String[] variablePath) {
        return Arrays.stream(templateType.getDeclaredFields())
                .flatMap(field -> this.streamTemplateFieldVariable(variablePath, field));
    }


    private Stream<TemplateVariableDescription> streamTemplateFieldVariable(String[] variablePath, Field field) {
        String name = field.getName();
        List<String> variablePaths = new ArrayList<>(Arrays.asList(variablePath));
        variablePaths.add(name);
        String[] newVariablePath = variablePaths.toArray(new String[0]);

        TemplateField annotation = field.getAnnotation(TemplateField.class);
        if (annotation == null) {
            Class<?> fieldType = field.getType();
            return this.streamTemplateVariableDescriptions(fieldType, newVariablePath);
        }

        String variableName = this.createVariableName(newVariablePath);
        String description = annotation.description();
        String example = annotation.example();

        TemplateVariableDescription variableDescription = new TemplateVariableDescription();
        variableDescription.setDescription(description);
        variableDescription.setExample(example);
        variableDescription.setVariableName(variableName);
        return Stream.of(variableDescription);

    }

    private String createVariableName(String[] variablePath) {
        return Arrays.stream(variablePath)
                .reduce((a, b) -> a + "." + b)
                .orElseThrow(() -> new DispatcherRuntimeException("Failed to build variable path"));
    }


    private <T> TemplateContextObject createExampleTemplateContextObject(Class<T> templateType) {
        try {
            T exampleTemplate = templateType.newInstance();
            Arrays.stream(templateType.getDeclaredFields())
                    .forEach(field -> this.fillExampleTemplate(exampleTemplate, field));
            return createKeyedContextObject(templateType, exampleTemplate);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new DispatcherRuntimeException("Failed to create example template instance", e);
        }
    }

    private <T> void fillExampleTemplate(T exampleTemplate, Field field) {
        TemplateField fieldAnnotation = field.getAnnotation(TemplateField.class);
        if (fieldAnnotation != null) {
            this.fillTemplateField(exampleTemplate, field, fieldAnnotation);
        } else {
            Class<?> fieldType = field.getType();
            Object fieldValueExample = this.createExampleTemplateContextObject(fieldType);
            FieldAccessUtils.setFieldValue(field, exampleTemplate, fieldValueExample);
        }
    }

    private <T> void fillTemplateField(T template, Field field, TemplateField tf) {
        String exampleStringValue = tf.example();
        Class<?> fieldType = field.getType();
        if (exampleStringValue.trim().isEmpty()) {
            Object exampleValue = castValue(fieldType, null);
            FieldAccessUtils.setFieldValue(field, template, exampleValue);
        } else {
            Object exampleValue = castValue(fieldType, exampleStringValue);
            FieldAccessUtils.setFieldValue(field, template, exampleValue);
        }

    }

    private <T> Object castValue(Class<T> fieldType, String stringValue) {
        if (fieldType.isPrimitive()) {
            if (Boolean.TYPE.equals(fieldType)) {
                return stringValue == null ? true : Boolean.valueOf(stringValue);
            } else if (Character.TYPE.equals(fieldType)) {
                return stringValue == null ? 'a' : stringValue.charAt(0);
            } else if (Byte.TYPE.equals(fieldType)) {
                return stringValue == null ? 0 : Byte.valueOf(stringValue);
            } else if (Short.TYPE.equals(fieldType)) {
                return stringValue == null ? 0 : Short.valueOf(stringValue);
            } else if (Integer.TYPE.equals(fieldType)) {
                return stringValue == null ? 0 : Integer.valueOf(stringValue);
            } else if (Long.TYPE.equals(fieldType)) {
                return stringValue == null ? 0 : Long.valueOf(stringValue);
            } else if (Float.TYPE.equals(fieldType)) {
                return stringValue == null ? 0 : Float.valueOf(stringValue);
            } else if (Double.TYPE.equals(fieldType)) {
                return stringValue == null ? 0 : Double.valueOf(stringValue);
            } else if (Void.TYPE.equals(fieldType)) {
                return null;
            }
        } else {
            if (Date.class.equals(fieldType)) {
                Instant instant = ZonedDateTime.parse(stringValue).toInstant();
                return Date.from(instant);
            }

            try {
                Constructor<T> constructor = fieldType.getConstructor(String.class);
                if (constructor != null) {
                    return constructor.newInstance(stringValue);
                }
            } catch (Exception e) {
                // try next
            }

            try {
                if (stringValue == null) {
                    return fieldType.newInstance();
                } else {
                    return (T) stringValue;
                }
            } catch (Exception e) {
                // try next
            }
        }

        LOG.warn("Failed to case template example value " + stringValue + " to " + fieldType.getName());
        return null;
    }


    public class ProducedTemplateAnnotationLiteral extends AnnotationLiteral<ProducedTemplateContext> implements ProducedTemplateContext {

    }
}