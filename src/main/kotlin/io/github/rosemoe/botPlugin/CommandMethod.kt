package io.github.rosemoe.botPlugin

@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CommandMethod(val path: String, val asFallbackMethod: Boolean = false)