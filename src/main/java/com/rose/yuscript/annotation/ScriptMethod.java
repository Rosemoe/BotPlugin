/**
 * This Java File is Created By Rose
 */
package com.rose.yuscript.annotation;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(METHOD)
/*
  @author Rose

 */
public @interface ScriptMethod {

	/**
	 * the position of return value
	 * @return begin of end
	 */
	boolean returnValueAtBegin() default false;

	/**
	 * Name in script env.
	 * default is method's name
	 * This can support special character used in iyu such as s+ and s-
	 * @return name
	 */
	String scriptEnvName() default "@DEFAULT";

}

