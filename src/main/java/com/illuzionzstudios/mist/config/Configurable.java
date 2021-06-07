package com.illuzionzstudios.mist.config;

import com.cryptomorin.xseries.XMaterial;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Simple tag for fields which we want to configure for an object.
 * Examples through a GUI to expose certain values
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Configurable {

    /**
     * Node for the description of this value
     *
     * @return Message in locale with this node
     */
    String description();

    /**
     * Represents the material for this option should
     * we need to display in a menu
     *
     * @return {@link XMaterial} instance
     */
    XMaterial material() default XMaterial.PAPER;

}
