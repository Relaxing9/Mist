package com.illuzionzstudios.mist.scheduler.rate

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FIELD,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS
)
@Retention(
    RetentionPolicy.RUNTIME
)
annotation class Async(
    /**
     * @return The rate of refresh
     */
    val rate: Rate = Rate.INSTANT
)