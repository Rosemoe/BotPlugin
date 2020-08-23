/**
 * This Java File is Created By Rose
 */
package com.rose.yuscript.functions;

import java.lang.reflect.Method;
import java.util.List;

import com.rose.yuscript.YuContext;
import com.rose.yuscript.YuInterpreter;
import com.rose.yuscript.annotation.ScriptMethod;
import com.rose.yuscript.tree.YuExpression;
import com.rose.yuscript.tree.YuSyntaxError;
import com.rose.yuscript.tree.YuValue;

public class JavaFunction implements Function {

	private final Method method;
	private final Class<?>[] params;
	private int argCount;
	private String name;
	private final boolean firstRtv;

	public JavaFunction(Method method) {
		this.method = method;
		params = method.getParameterTypes();
		argCount = params.length;
		for(Class<?> clazz : params) {
			if(clazz == YuContext.class) {
				if(argCount != -1)
					argCount--;
			}else if(clazz == Object[].class || clazz == YuExpression[].class) {
				if(argCount == -1) {
					throw new IllegalArgumentException("too many arrays");
				}
				argCount = -1;
			}else if(clazz != YuExpression.class && clazz != Object.class) {
				throw new IllegalArgumentException("bad type in parameters");
			}
		}
		if(argCount != -1 && method.getReturnType() != void.class) {
			argCount++;
		}
		firstRtv = method.getAnnotation(ScriptMethod.class).returnValueAtBegin();
		String specialName = method.getAnnotation(ScriptMethod.class).scriptEnvName();
		this.name = specialName.equals("@DEFAULT") ? method.getName() : specialName;
	}

	public JavaFunction(Method method,String name) {
		this(method);
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int getArgumentCount() {
		return argCount;
	}

	private static Object get(YuExpression expr,Class<?> clazz,YuContext context) {
		if(clazz == YuExpression.class) {
			return expr;
		}else if(clazz == Object.class) {
			return expr.getValue(context);
		}else if(clazz == YuContext.class) {
			return context;
		}
		return null;
	}

	@Override
	public void invoke(List<YuExpression> arguments, YuContext context, YuInterpreter interpreter) throws Throwable {
		Object[] args = new Object[params.length];
		if(method.getReturnType() != void.class) {
			YuExpression rt;
			Object value;
			if(firstRtv) {
				int pointerArgument = 1;
				for(int i = 0;i < params.length;i++) {
					if(params[i].isArray()) {
						if(params[i] == Object[].class) {
							Object[] array = new Object[arguments.size() - 1];
							for(int j = 1;j < array.length + 1;j++) {
								array[j - 1] = arguments.get(j).getValue(context);
							}
							args[i] = array;
						}else {
							YuExpression[] array = new YuExpression[arguments.size() - 1];
							for(int j = 1;j < array.length + 1;j++) {
								array[j - 1] = arguments.get(j);
							}
							args[i] = array;
						}
					}else {
						YuExpression expr = arguments.get(pointerArgument);
						if(params[i] == YuExpression.class) {
							args[i] = expr;
							pointerArgument++;
						}else if(params[i] == Object.class) {
							args[i] = expr.getValue(context);
							pointerArgument++;
						}else if(params[i] == YuContext.class) {
							args[i] = context;
						}
					}
				}
				rt = arguments.get(0);
			}else {
				int pointerArgument = 0;
				for(int i = 0;i < params.length;i++) {
					if(params[i].isArray()) {
						if(params[i] == Object[].class) {
							Object[] array = new Object[arguments.size() - 1];
							for(int j = 0;j < array.length - 1;j++) {
								array[j] = arguments.get(j).getValue(context);
							}
							args[i] = array;
						}else {
							YuExpression[] array = new YuExpression[arguments.size() - 1];
							for(int j = 0;j < array.length - 1;j++) {
								array[j] = arguments.get(j);
							}
							args[i] = array;
						}
					}else {
						YuExpression expr = arguments.get(pointerArgument);
						if(params[i] == YuExpression.class) {
							args[i] = expr;
							pointerArgument++;
						}else if(params[i] == Object.class) {
							args[i] = expr.getValue(context);
							pointerArgument++;
						}else if(params[i] == YuContext.class) {
							args[i] = context;
						}
					}
				}
				rt = arguments.get(arguments.size() - 1);
			}
			value = method.invoke(null, args);
			if(!rt.getOperators().isEmpty()) {
				throw new YuSyntaxError();
			}
			YuValue val = rt.getChildren().get(0);
			if(val.isInvert()) {
				throw new YuSyntaxError();
			}
			if(val.getType() == YuValue.TYPE_VAR) {
				context.setVariable(val.getVariableName(), value);
			}
		} else {
			for(int i = 0;i < params.length;i++) {
				if(params[i].isArray()) {
					if(params[i] == Object[].class) {
						Object[] array = new Object[arguments.size()];
						for(int j = 0;j < array.length;j++) {
							array[j] = arguments.get(j).getValue(context);
						}
						args[i] = array;
					}else {
						args[i] = arguments.toArray(new YuExpression[0]);
					}
				}else {
					args[i] = get(i >= arguments.size() ? null : arguments.get(i),  params[i], context);
				}
			}
			method.invoke(null, args);
		}
	}

}