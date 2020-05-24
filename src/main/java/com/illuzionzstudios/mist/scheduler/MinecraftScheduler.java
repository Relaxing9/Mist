/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.scheduler;

import com.illuzionzstudios.mist.Logger;
import com.illuzionzstudios.mist.Mist;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import com.illuzionzstudios.mist.scheduler.rate.Async;
import com.illuzionzstudios.mist.scheduler.rate.Rate;
import com.illuzionzstudios.mist.scheduler.timer.PresetCooldown;
import com.sun.corba.se.impl.orbutil.concurrent.Sync;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * An instance of a scheduler that ticks objects. This will handle
 * safely ticking objects in order to avoid errors and leaks. To use this
 * we can directly call methods to delay tasks or run tasks on certain threads,
 * or we can make use of the {@link Sync} and {@link Async} annotations.
 *
 * These {@link Annotation} can annotate either {@link Class} {@link Field} or {@link Method}
 * Below is explained what each does
 *
 * {@link Class} Any {@link Tickable} objects in the class will be invoked with the set rate
 * {@link Field} If this is an instance of {@link Tickable}, will be invoked with set rate
 * {@link Method} The method will be invoked with a set rate
 *
 * When invoking a {@link Tickable} object, if the field is an instance of {@link Iterable}
 * or {@link Map}, will check the elements for {@link Tickable} and invoke those
 */
public abstract class MinecraftScheduler {

    /**
     * This holds the set of registered synchronization services that are
     * currently ticking. Objects must be dismissed after being used
     */
    protected volatile static Set<SynchronizationService> SYNC_SERVICE_REGISTRATION;

    /**
     * These are the amount of synchronized ticks the application
     * has undergone. Async ticks are not counted as this will keep track
     * of total application time
     */
    private static volatile AtomicLong synchronousTicks;

    /**
     * Instance of the {@link MinecraftScheduler}
     */
    private static MinecraftScheduler INSTANCE;

    /**
     * @return Our instance of the {@link MinecraftScheduler}
     */
    public static MinecraftScheduler get() {
        return INSTANCE;
    }

    /**
     * @return The current amount of ticks the application has undergone
     */
    public static long getCurrentTick() {
        return synchronousTicks.get();
    }

    /**
     * Start up our scheduler
     * Called in the {@link SpigotPlugin#onEnable()}
     */
    public void initialize() {
        INSTANCE = this;
        SYNC_SERVICE_REGISTRATION = Collections.newSetFromMap(new ConcurrentHashMap<>());
        start();
    }

    /**
     * Stop all tickers
     * Called in the {@link SpigotPlugin#onDisable()}
     */
    public void stopInvocation() {
        stop();

        SYNC_SERVICE_REGISTRATION.clear();
    }

    /**
     * Implemented to start the scheduler
     */
    protected abstract void start();

    /**
     * Implemented to stop the scheduler
     */
    protected abstract void stop();

    protected <A extends Annotation> void heartbeat(Class<A> type) {
        for (SynchronizationService service : SYNC_SERVICE_REGISTRATION) {
            for (SynchronizedElement<?> element : service.elements) {
                // Make sure matches rate type
                if (!element.synchronizationClass.equals(type)) {
                    continue;
                }

                // Lets get all the refresh services

                // Use synchronous ticks to check if rate has elapsed so that if the
                // server thread blocks will still account for the time that we missed

                // Check timer and it's ready
                if ((type.equals(Sync.class) && element.timer.isReady())
                        || (type.equals(Async.class) && element.timer.isReadyRealTime())) {
                    element.timer.go();

                    // Invoke method
                    if (element.object instanceof Method) {
                        Method method = (Method) element.object;

                        // Determine if method should be fired based on the rate of refresh
                        final long start = System.currentTimeMillis();

                        // Call method
                        try {
                            method.invoke(service.source);
                        } catch (Exception e) {
                            Logger.severe("Interrupted synchronization invocation: ");
                            e.printStackTrace();
                        }

                        // Took too long
                        if (System.currentTimeMillis() - start > Mist.Scheduler.TIME_WARNING_THRESHOLD
                                && type.equals(Sync.class)) {
                            Logger.severe("WARNING: Synchronization block took way too long to invoke! (" + (System.currentTimeMillis() - start) + "ms)");
                            Logger.severe("Block " + method.getName() + "() in " + service.source.getClass());
                        }
                    } else {
                        // Invoke field
                        try {
                            Object object = (element.object instanceof Field) ? ((Field) element.object).get(service.source) : service.source;

                            if (object != null) {
                                if (object instanceof Tickable) {
                                    safelyTick((Tickable) object);
                                } else if (object instanceof Iterable || object instanceof Map) {
                                    Iterable<?> iterable = null;

                                    if (object instanceof Collection) {
                                        iterable = (Iterable<?>) object;
                                    } else if (object instanceof Map) {
                                        iterable = ((Map<?, ?>) object).values();
                                    }

                                    // If objects are maps that contain tickable objects
                                    // invoke those
                                    if (iterable != null) {
                                        iterable.forEach(fieldElement -> {
                                            if (fieldElement instanceof Tickable) {
                                                safelyTick((Tickable) fieldElement);
                                            }
                                        });
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Logger.displayError(e, "Interrupted synchronization invocation: in " + service.source.getClass());
                        }
                    }
                }
            }
        }

        // Increment current ticks passed
        if (type.equals(Sync.class)) {
            synchronousTicks.incrementAndGet();
        }
    }

    /**
     * Safely runs tick without throwing an exception
     *
     * @param tickable Tickable object
     * @return Returns if tick operation executed successfully
     */
    public boolean safelyTick(Tickable tickable) {
        try {
            if (tickable == null) {
                return false;
            }
            tickable.tick();
        } catch (Exception e) {
            Logger.displayError(e, "Interrupted tick call!:");
        }

        return true;
    }

    /**
     * @param occurrence The time in ticks we want to check
     * @return If time has elapsed
     */
    public boolean hasElapsed(double occurrence) {
        return synchronousTicks.get() % occurrence == 0;
    }

    /**
     * Checks if method is being ran on server thread
     */
    public abstract void validateMainThread();

    /**
     * Checks if method is not being ran on server thread
     */
    public abstract void validateNotMainThread();

    /**
     * Submits task into sync scheduler
     *
     * @param runnable The task
     */
    public int synchronize(Runnable runnable) {
        return synchronize(runnable, 0);
    }

    /**
     * Submits task into async scheduler
     *
     * @param runnable The task
     */
    public int desynchronize(Runnable runnable) {
        return desynchronize(runnable, 0);
    }

    /**
     * Submits task into sync scheduler
     *
     * @param runnable The task
     * @param time     The delay time
     */
    public abstract int synchronize(Runnable runnable, long time);

    /**
     * Submits task into async scheduler
     *
     * @param runnable The task
     * @param time     The delay time
     */
    public abstract int desynchronize(Runnable runnable, long time);

    /**
     * Asynchronous callback tool
     *
     * @param callable The future task
     * @param consumer The task callback
     * @param <T>      The type returned
     */
    public abstract <T> void desynchronize(Callable<T> callable, Consumer<Future<T>> consumer);

    /**
     * Cancel a running task with certain id
     *
     * @param id The id of the task to cancel
     */
    public abstract void stopTask(int id);

    /**
     * Registers a class as refresh service listener
     *
     * @param source This can be any object you want
     */
    public void registerSynchronizationService(Object source) {
        SYNC_SERVICE_REGISTRATION.add(new SynchronizationService(source));
    }

    /**
     * Removes a synchronized service
     *
     * @param source This can be any object you want
     */
    public void dismissSynchronizationService(Object source) {
        SYNC_SERVICE_REGISTRATION.removeIf(service -> service.source.equals(source));
    }

    /**
     * Cached Synchronization Element
     * This contains the method/field data that will be used such
     * as the ticking rate, invoked object
     */
    protected static class SynchronizedElement<A extends Annotation> {

        /**
         * The rate to tick by
         */
        protected final Rate rate;

        /**
         * The object to tick
         */
        protected final Object object;

        /**
         * The class of the tick rate type
         */
        protected final Class<A> synchronizationClass;

        /**
         * Timer to tick object
         */
        protected final PresetCooldown timer;

        protected SynchronizedElement(Rate rate, Object object, Class<A> synchronizationClass) {
            this.rate = rate;
            this.object = object;
            this.synchronizationClass = synchronizationClass;

            this.timer = new PresetCooldown((int) (rate.getTime() / 50));
        }

    }

    /**
     * A Cached Synchronization Service that should be ticked
     */
    protected static class SynchronizationService {

        /**
         * The object that is being ticked
         */
        protected Object source;

        /**
         * Elements to be ticked
         */
        protected Set<SynchronizedElement<?>> elements = new HashSet<>();

        private SynchronizationService(Object source) {
            this.source = source;

            try {
                // LOAD ELEMENTS //
                for (Class<? extends Annotation> clazz : getAnnotations()) {

                    if (source.getClass().isAnnotationPresent(clazz)) {
                        Rate rate = getRate(clazz, source.getClass());
                        elements.add(new SynchronizedElement<>(rate, source, clazz));
                    } else if (source.getClass().getSuperclass().isAnnotationPresent(clazz)) {
                        Rate rate = getRate(clazz, source.getClass().getSuperclass());
                        elements.add(new SynchronizedElement<>(rate, source, clazz));
                    }

                    for (Rate rate : Rate.values()) {
                        // LOAD METHODS //
                        elements.addAll(getElements
                                (getAllMethods(source.getClass()), clazz, rate));

                        // LOAD FIELDS //
                        elements.addAll(getElements
                                (getAllFields(source.getClass()), clazz, rate));
                    }

                }
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        public static Field[] getAllFields(Class<?> aClass) {
            List<Field> fields = new ArrayList<>();
            do {
                Collections.addAll(fields, aClass.getDeclaredFields());
                aClass = aClass.getSuperclass();
            } while (aClass != null);
            return fields.toArray(new Field[fields.size()]);
        }

        public static Method[] getAllMethods(Class<?> aClass) {
            List<Method> methods = new ArrayList<>();
            do {
                Collections.addAll(methods, aClass.getDeclaredMethods());
                aClass = aClass.getSuperclass();
            } while (aClass != null);
            return methods.toArray(new Method[methods.size()]);
        }

        private <A extends Annotation> Class<A>[] getAnnotations() {
            return new Class[]{Sync.class, Async.class};
        }

        private <A extends Annotation> Set<SynchronizedElement<A>> getElements(AccessibleObject[] objects, Class<A> synchronizationClass, Rate rate)
                throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            Set<SynchronizedElement<A>> elements = new HashSet<>();

            for (AccessibleObject object : objects) {
                // Set them to public if they are private for obvious reasons
                if (!object.isAccessible()) {
                    object.setAccessible(true);
                }

                Rate declaredRate = getRate(synchronizationClass, object);

                if (declaredRate != null && declaredRate.equals(rate)) {
                    elements.add(new SynchronizedElement<>(rate, object, synchronizationClass));
                }
            }

            return elements;
        }

        private <A extends Annotation> Rate getRate(Class<A> synchronizationClass, AnnotatedElement element) throws NoSuchMethodException,
                InvocationTargetException, IllegalAccessException {
            if (!element.isAnnotationPresent(synchronizationClass)) {
                return null;
            }

            // Get the annotation itself //
            A annotation = element.getAnnotation(synchronizationClass);

            // Get declared rate of refresh value is instant by default //
            Method getRate = annotation.annotationType()
                    .getDeclaredMethod("rate");

            if (!getRate.isAccessible()) {
                getRate.setAccessible(true);
            }

            return (Rate) getRate.invoke(annotation);
        }
    }

}
