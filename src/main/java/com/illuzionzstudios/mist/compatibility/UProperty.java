/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.compatibility;

import com.illuzionzstudios.mist.util.TextUtil;
import com.illuzionzstudios.mist.util.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;

@RequiredArgsConstructor
public enum UProperty {

    // ItemMeta
    /**
     * The unbreakable property of ItemMeta
     */
    UNBREAKABLE(ItemMeta.class, boolean.class),

    // Entity
    /**
     * The glowing entity property, currently only support white color
     */
    GLOWING(Entity.class, boolean.class),

    /**
     * The AI navigator entity property
     */
    AI(Entity.class, boolean.class),

    /**
     * The gravity entity property
     */
    GRAVITY(Entity.class, boolean.class),

    /**
     * Silent entity property that controls if the entity emits sounds
     */
    SILENT(Entity.class, boolean.class),

    /**
     * The god mode entity property
     */
    INVULNERABLE(Entity.class, boolean.class);

    /**
     * The class that this enum applies for, for example {@link Entity}
     */
    @Getter
    private final Class<?> requiredClass;

    /**
     * The "setter" field type, for example setSilent method accepts boolean
     */
    private final Class<?> setterMethodType;

    /**
     * Apply the property to the entity. Class must be compatible with {@link #requiredClass}
     *
     * Example: SILENT.apply(myZombieEntity, true)
     *
     * @param instance
     * @param key
     */
    public final void apply(Object instance, Object key) {
        Valid.checkBoolean(requiredClass.isAssignableFrom(instance.getClass()), this + " accepts " + requiredClass.getSimpleName() + ", not " + instance.getClass().getSimpleName());

        try {
            final Method m = getMethod(instance.getClass());
            m.setAccessible(true);

            m.invoke(instance, key);

        } catch (final ReflectiveOperationException e) {
            if (e instanceof NoSuchMethodException && ServerVersion.olderThan(ServerVersion.V.values()[0])) {
                // Pass through TODO
            } else
                e.printStackTrace();
        }
    }

    /**
     * Can this property be used on this server for the given class? Class must be compatible with {@link #requiredClass}
     *
     * @param clazz
     * @return
     */
    public final boolean isAvailable(Class<?> clazz) {
        try {
            getMethod(clazz);
        } catch (final ReflectiveOperationException e) {
            if (e instanceof NoSuchMethodException && ServerVersion.olderThan(ServerVersion.V.values()[0]))
                return false;
        }

        return true;
    }

    // Automatically returns the correct getter or setter method for class
    private final Method getMethod(Class<?> clazz) throws ReflectiveOperationException {
        return clazz.getMethod("set" + (toString().equals("AI") ? "AI" : TextUtil.formatText(toString().toLowerCase(), true)), setterMethodType);
    }

}
