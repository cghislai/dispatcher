package com.charlyghislain.dispatcher.util;

import com.charlyghislain.dispatcher.api.exception.DispatcherRuntimeException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;

public class FieldAccessUtils {

    public static Field getFieldNamed(String fieldName, Class<?> objectType) {
        try {
            return objectType.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            String message = MessageFormat.format("Failed to find a field named {0} on object {1}",
                    fieldName, objectType.getName());
            throw new DispatcherRuntimeException(message);
        }
    }

    public static void setFieldValue(Field field, Object instance, Object value) {
        try {
            field.setAccessible(true);
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            String message = MessageFormat.format("Failed to set dispatchingOptions for field {0} on object {1}",
                    field.getName(), instance.getClass().getName());
            throw new DispatcherRuntimeException(message);
        }
    }

    public static Object getFieldValue(Field field, Object instance) {
        try {
            field.setAccessible(true);
            return field.get(instance);
        } catch (IllegalAccessException e) {
            String message = MessageFormat.format("Failed to read dispatchingOptions for field {0} on object {1}",
                    field.getName(), instance.getClass().getName());
            throw new DispatcherRuntimeException(message);
        }
    }


    public static Object invokeGetter(Method getter, Object instance) {
        try {
            getter.setAccessible(true);
            return getter.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            String message = MessageFormat.format("Failed to invoke getter {0} on object {1}",
                    getter.getName(), instance.getClass().getName());
            throw new DispatcherRuntimeException(message);
        }
    }

    public static Method getMethodNamed(String name, Class<?> typeClass) {
        try {
            Method method = typeClass.getMethod(name);
            return method;
        } catch (NoSuchMethodException e) {
            String message = MessageFormat.format("Failed to find a method named {0} on class {1}",
                    name, typeClass.getName());
            throw new DispatcherRuntimeException(message);
        }
    }

}
