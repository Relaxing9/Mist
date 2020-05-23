/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.util;

import com.illuzionzstudios.mist.compatibility.ServerVersion;
import com.illuzionzstudios.mist.exception.PluginException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * A utils class for help with reflection. Useful for NMS and
 * other things we may need to do
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtils {

    /**
     * The full package name for NMS
     */
    public static final String NMS = "net.minecraft.server";

    /**
     * The package name for Craftbukkit
     */
    public static final String CRAFTBUKKIT = "org.bukkit.craftbukkit";

    /**
     * Find a class in net.minecraft.server package, adding the version
     * automatically
     *
     * @param name
     * @return
     */
    public static Class<?> getNMSClass(final String name) {
        return ReflectionUtils.lookupClass(NMS + "." + ServerVersion.getServerVersion() + "." + name);
    }

    /**
     * Find a class in org.bukkit.craftbukkit package, adding the version
     * automatically
     *
     * @param name
     * @return
     */
    public static Class<?> getOBCClass(final String name) {
        return ReflectionUtils.lookupClass(CRAFTBUKKIT + "." + ServerVersion.getServerVersion() + "." + name);
    }

    /**
     * Set the static field to the given value
     *
     * @param clazz
     * @param fieldName
     * @param fieldValue
     */
    public static void setStaticField(@NonNull final Class<?> clazz, final String fieldName, final Object fieldValue) {
        try {
            final Field field = getDeclaredField(clazz, fieldName);

            field.set(null, fieldValue);

        } catch (final Throwable t) {
            throw new PluginException(t, "Could not set " + fieldName + " in " + clazz + " to " + fieldValue);
        }
    }

    /**
     * Set the static field to the given value
     *
     * @param object
     * @param fieldName
     * @param fieldValue
     */
    public static void setStaticField(@NonNull final Object object, final String fieldName, final Object fieldValue) {
        try {
            final Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);

            field.set(object, fieldValue);

        } catch (final Throwable t) {
            throw new PluginException(t, "Could not set " + fieldName + " in " + object + " to " + fieldValue);
        }
    }

    /**
     * Convenience method for getting a static field content.
     *
     * @param <T>
     * @param clazz
     * @param field
     * @return
     */
    public static <T> T getStaticFieldContent(@NonNull final Class<?> clazz, final String field) {
        return getFieldContent(clazz, field, null);
    }

    /**
     * Return a constructor for the given NMS class. We prepend the class name
     * with the {@link #NMS} so you only have to give in the name of the class.
     *
     * @param nmsClass
     * @param params
     * @return
     */
    public static Constructor<?> getNMSConstructor(@NonNull final String nmsClass, final Class<?>... params) {
        return getConstructor(getNMSClass(nmsClass), params);
    }

    /**
     * Return a constructor for the given OBC class. We prepend the class name
     * with the OBC so you only have to give in the name of the class.
     *
     * @param obcClass
     * @param params
     * @return
     */
    public static Constructor<?> getOBCConstructor(@NonNull final String obcClass, final Class<?>... params) {
        return getConstructor(getOBCClass(obcClass), params);
    }

    /**
     * Return a constructor for the given fully qualified class path such as
     * org.mineacademy.boss.BossPlugin
     *
     * @param classPath
     * @param params
     * @return
     */
    public static Constructor<?> getConstructor(@NonNull final String classPath, final Class<?>... params) {
        final Class<?> clazz = lookupClass(classPath);

        return getConstructor(clazz, params);
    }

    /**
     * Return a constructor for the given class
     *
     * @param clazz
     * @param params
     * @return
     */
    public static Constructor<?> getConstructor(@NonNull final Class<?> clazz, final Class<?>... params) {
        try {
            final Constructor<?> constructor = clazz.getConstructor(params);
            constructor.setAccessible(true);

            return constructor;

        } catch (final ReflectiveOperationException ex) {
            throw new PluginException(ex, "Could not get constructor of " + clazz + " with parameters " + params);
        }
    }

    /**
     * Get the field content
     *
     * @param instance
     * @param field
     * @return
     */
    public static <T> T getFieldContent(final Object instance, final String field) {
        return getFieldContent(instance.getClass(), field, instance);
    }

    /**
     * Get the field content
     *
     * @param <T>
     * @param clazz
     * @param field
     * @param instance
     * @param type
     * @return
     */
    public static <T> T getFieldContent(Class<?> clazz, final String field, final Object instance) {
        final String originalClassName = clazz.getSimpleName();

        do
            // note: getDeclaredFields() fails if any of the fields are classes that cannot be loaded
            for (final Field f : clazz.getDeclaredFields())
                if (f.getName().equals(field))
                    return (T) getFieldContent(f, instance);
        while (!(clazz = clazz.getSuperclass()).isAssignableFrom(Object.class));

        throw new ReflectionException("No such field " + field + " in " + originalClassName + " or its superclasses");
    }

    /**
     * Get the field content
     *
     * @param field
     * @param instance
     * @return
     */
    public static Object getFieldContent(final Field field, final Object instance) {
        try {
            field.setAccessible(true);

            return field.get(instance);

        } catch (final ReflectiveOperationException e) {
            throw new ReflectionException("Could not get field " + field.getName() + " in instance " + (instance != null ? instance : field).getClass().getSimpleName());
        }
    }

    /**
     * Get all fields from the class and its super classes
     *
     * @param clazz
     * @return
     */
    public static Field[] getAllFields(Class<?> clazz) {
        final List<Field> list = new ArrayList<>();

        do
            list.addAll(Arrays.asList(clazz.getDeclaredFields()));
        while (!(clazz = clazz.getSuperclass()).isAssignableFrom(Object.class));

        return list.toArray(new Field[list.size()]);
    }

    /**
     * Gets the declared field in class by its name
     *
     * @param clazz
     * @param fieldName
     * @return
     */
    public static Field getDeclaredField(final Class<?> clazz, final String fieldName) {
        try {
            final Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);

            return field;

        } catch (final ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets a class method
     *
     * @param clazz
     * @param methodName
     * @param args
     * @return
     */
    public static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>... args) {
        for (final Method method : clazz.getMethods())
            if (method.getName().equals(methodName) && isClassListEqual(args, method.getParameterTypes())) {
                method.setAccessible(true);

                return method;
            }

        return null;
    }

    // Compares class lists
    private static boolean isClassListEqual(final Class<?>[] first, final Class<?>[] second) {
        if (first.length != second.length)
            return false;

        for (int i = 0; i < first.length; i++)
            if (first[i] != second[i])
                return false;

        return true;
    }

    /**
     * Gets a class method
     *
     * @param clazz
     * @param methodName
     * @param args
     * @return
     */
    public static Method getMethod(final Class<?> clazz, final String methodName, final Integer args) {
        for (final Method method : clazz.getMethods())
            if (method.getName().equals(methodName) && args.equals(new Integer(method.getParameterTypes().length))) {
                method.setAccessible(true);

                return method;
            }

        return null;
    }

    /**
     * Gets a class method
     *
     * @param clazz
     * @param methodName
     * @return
     */
    public static Method getMethod(final Class<?> clazz, final String methodName) {
        for (final Method method : clazz.getMethods())
            if (method.getName().equals(methodName)) {
                method.setAccessible(true);
                return method;
            }

        return null;
    }

    /**
     * Wrapper for Class.forName
     *
     * @param path
     * @param type
     * @return
     */
    public static <T> Class<T> lookupClass(final String path, final Class<T> type) {
        return (Class<T>) lookupClass(path);
    }

    /**
     * Wrapper for Class.forName
     *
     * @param path
     * @return
     */
    public static Class<?> lookupClass(final String path) {
        try {
            return Class.forName(path);

        } catch (final ClassNotFoundException ex) {
            throw new ReflectionException("Could not find class: " + path);
        }
    }

    /**
     * Represents an exception during reflection operation
     */
    public static final class ReflectionException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ReflectionException(final String msg) {
            super(msg);
        }

        public ReflectionException(final String msg, final Exception ex) {
            super(msg, ex);
        }
    }
    
}
