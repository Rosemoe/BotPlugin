/**
 * This Java File is Created By Rose
 */
package com.rose.yuscript.functions;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rose.yuscript.annotation.ScriptMethod;

/**
 * @author Rose
 * A manager that holds all script functions which will used by interpreter
 */
public class FunctionManager {

	private final Map<String,List<Function>> functionMap;

	/**
	 * Create a FunctionManager and add all basic functions
	 */
	public FunctionManager() {
		functionMap = new HashMap<>();
		addFunctionsFromClass(YuMethod.class);
	}

	/**
	 * Add all script functions of a class
	 * @param clazz The class
	 */
	public void addFunctionsFromClass(Class<?> clazz) {
		Method[] methods = clazz.getMethods();
		for(Method method : methods) {
			if(method.isAnnotationPresent(ScriptMethod.class)) {
				addFunctionFromMethod(method);
			}
		}
	}

	/**
	 * Make this method as a script function
	 * NOTE:
	 * The method must be public,static and non-abstract and with a ScriptMethod annotation.
	 * Otherwise,it will throw a IllegalArgumentException.
	 * NOTE:
	 * One method could be added for several times
	 * DOT NOT add a method twice or more
	 * @param method The method to add
	 */
	public void addFunctionFromMethod(Method method) {
		if((method.getModifiers() & Modifier.PUBLIC) == 0) {
			throw new IllegalArgumentException("Only public method can be added");
		}
		if((method.getModifiers() & Modifier.STATIC) == 0) {
			throw new IllegalArgumentException("Only static method can be added");
		}
		if((method.getModifiers() & Modifier.ABSTRACT) != 0) {
			throw new IllegalArgumentException("Only non-abstract method can be added");
		}
		if(!method.isAnnotationPresent(ScriptMethod.class)) {
			throw new IllegalArgumentException("Method must represent ScriptMethod annotation");
		}
		addFunction(new JavaFunction(method));
	}

	/**
	 * Remove all functions with the given name
	 * @param name function name
	 */
	public void removeFunctions(String name) {
		functionMap.remove(name);
	}

	/**
	 * Remove the given function
	 * @param function Function to remove
	 */
	public void removeFunction(Function function) {
		List<Function> list = functionMap.get(function.getName());
		if(list == null) {
			return;
		}
		list.remove(function);
	}

	/**
	 * Add a new function
	 * @param function New function
	 */
	public void addFunction(Function function) {
		List<Function> list = functionMap.computeIfAbsent(function.getName(), k -> new ArrayList<>());
		if(list.contains(function)) {
			throw new IllegalArgumentException("Function has been added");
		}
		list.add(function);
	}

	/**
	 * Get functions with the given name
	 * @param name function name
	 * @return An unmodifiable list of functions with the given name
	 */
	public List<Function> getFunctions(String name) {
		List<Function> functions = functionMap.get(name);
		return Collections.unmodifiableList(functions != null ? functions : Collections.emptyList());
	}

}