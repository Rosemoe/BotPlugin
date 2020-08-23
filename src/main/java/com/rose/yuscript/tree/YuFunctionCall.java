/**
 * This Java File is Created By Rose
 */
package com.rose.yuscript.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rose
 *
 */
public class YuFunctionCall implements YuNode {

	private String functionName;
	
	private List<YuExpression> arguments;
	
	@Override
	public <T, R> R accept(YuTreeVisitor<R, T> visitor, T value) {
		return visitor.visitFunctionCall(this, value);
	}
	
	public YuFunctionCall() {
		arguments = new ArrayList<YuExpression>();
	}
	
	public void addArgument(YuExpression expression) {
		arguments.add(expression);
	}
	
	/**
	 * @return the arguments
	 */
	public List<YuExpression> getArguments() {
		return arguments;
	}
	
	/**
	 * @param functionName the functionName to set
	 */
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}
	
	/**
	 * @return the functionName
	 */
	public String getFunctionName() {
		return functionName;
	}

}
