/**
 * This Java File is Created By Rose
 */
package com.rose.yuscript;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import com.rose.yuscript.tree.YuCodeBlock;

/**
 * The context used to save and manage variables
 * @author Rose
 */
public class YuContext {

	private static Map<Long,Map<String,Object>> sessionVariableMaps;
	
	private static Map<String,Object> globalVariables;

	static {
		globalVariables = new ConcurrentHashMap<>();
		sessionVariableMaps = new ConcurrentHashMap<>();
	}
	
	private synchronized static Map<String,Object> getSessionVariableMap(long session) {
		Map<String,Object> map = sessionVariableMaps.get(session);
		if(map == null) {
			map = new ConcurrentHashMap<>();
			sessionVariableMaps.put(session, map);
		}
		return map;
	}
	
	public synchronized static void clear(long session) {
	    Map<String,Object> map = getSessionVariableMap(session);
		map.clear();
	}
	
	public static void clear() {
	    globalVariables.clear();
	}
	
	private Map<String,Object> sessionVariables;
	private Map<String,Object> localVariables;
	private Stack<YuCodeBlock> stack;
	private Stack<Boolean> usage;
	private long session;
	private boolean stopFlag = false;
	public final static YuCodeBlock NO_CODE_BLOCK = new YuCodeBlock();
	private YuInterpreter declaringInterpreter;
	private final Stack<BoolWrapper> loopEnv = new Stack<>();

	/**
	 * Wrapper class
	 */
	private static class BoolWrapper {
		public boolean value = false;
	}

	/**
	 * Enter a new loop
	 */
	public void enterLoop() {
		loopEnv.add(new BoolWrapper());
	}

	/**
	 * Exit from loop
	 */
	public void exitLoop() {
		loopEnv.pop();
	}

	/**
	 * Whether in a loop
	 * @return whether in a loop
	 */
	public boolean isInLoop() {
		return !loopEnv.empty();
	}

	/**
	 * Break current loop
	 */
	public void loopBreak() {
		loopEnv.peek().value = true;
	}
	
	public void addCodeBlock(YuCodeBlock block) {
		stack.push(block);
		usage.push(false);
	}
	
	public boolean isCodeBlockUsed() {
		return usage.lastElement();
	}
	
	public boolean hasCodeBlock() {
		return (!stack.isEmpty()) && stack.lastElement() != NO_CODE_BLOCK;
	}
	
	public YuCodeBlock getCodeBlock() {
		YuCodeBlock block = hasCodeBlock() ? stack.lastElement() : null;
		if(block != null) {
			usage.pop();
			usage.push(true);
		}
		return block;
	}
	
	public void popCodeBlock() {
		stack.pop();
		usage.pop();
	}
	
	/**
	 * @param declaringInterpreter the declaringInterpreter to set
	 */
	public void setDeclaringInterpreter(YuInterpreter declaringInterpreter) {
		this.declaringInterpreter = declaringInterpreter;
	}
	
	/**
	 * @return the declaringInterpreter
	 */
	public YuInterpreter getDeclaringInterpreter() {
		return declaringInterpreter;
	}
	
	/**
	 * @param stopFlag the stopFlag to set
	 */
	public void setStopFlag(boolean stopFlag) {
		this.stopFlag = stopFlag;
	}
	
	/**
	 * @return the stopFlag
	 */
	public boolean isStopFlagSet() {
		return stopFlag;
	}
	
	/**
	 * Create a empty context with the given session
	 * @param session Session for variable management
	 */
	public YuContext(long session) {
		sessionVariables = getSessionVariableMap(session);
		localVariables = new HashMap<>();
		stack = new Stack<>();
		usage = new Stack<>();
		this.session = session;
	}

	/**
	 * Create a new context from the given context
	 * @param context The source context.Can not be null
	 */
	public YuContext(YuContext context) {
		this(context.getSession());
		localVariables.putAll(context.localVariables);
		declaringInterpreter = context.declaringInterpreter;
	}
	
	/**
	 * Get session of this context
	 * @return Session of this context
	 */
	public long getSession() {
		return session;
	}
	
	/**
	 * Get the variable map for the given prefix
	 * @param prefix The name of map
	 * @return The variable map
	 */
	private Map<String,Object> getVariableMapForPrefix(String prefix) {
		switch(prefix) {
		case "s":
			return localVariables;
		case "ss":
			return sessionVariables;
		case "sss":
			return globalVariables;
		}
		throw new IllegalArgumentException("Not a valid variable type:" + prefix);
	}
	
	/**
	 * Set variable value
	 * @param prefix The name of map
	 * @param name The name of variable without its map's name
	 * @param value The value of variable
	 */
	public void setVariable(String prefix,String name,Object value) {
		Map<String,Object> map = getVariableMapForPrefix(prefix);
		if(name.contains(".")) {
			throw new IllegalArgumentException("dot in name is a syntax error");
		}
		if(value == null) {
			map.remove(name);
			return;
		}
		map.put(name, value);
	}
	
	public void setVariable(String fullName,Object val) {
		int dot = fullName.indexOf(".");
		if(dot == -1) {
			setVariable("s", fullName,val);
			return;
		}
		int lastDot = fullName.lastIndexOf(".");
		if(dot != lastDot) {
			throw new IllegalArgumentException("two or more dots in name is a syntax error");
		}
		setVariable(fullName.substring(0,dot), fullName.substring(dot + 1),val);
	}
	
	/**
	 * Get variable value
	 * @param prefix The name of map
	 * @param name The name of variable without its map's name
	 * @return The value of variable
	 */
	public Object getVariable(String prefix,String name) {
		return getVariableMapForPrefix(prefix).get(name);
	}
	
	/**
	 * Get variable value
	 * @param fullName The full name of variable such as ss.var
	 * @return The value of variable
	 */
	public Object getVariable(String fullName) {
		int dot = fullName.indexOf(".");
		if(dot == -1) {
			return getVariable("s", fullName);
		}
		int lastDot = fullName.lastIndexOf(".");
		if(dot != lastDot) {
			throw new IllegalArgumentException("two or more dots in name is a syntax error");
		}
		return getVariable(fullName.substring(0,dot), fullName.substring(dot + 1));
	}

}
