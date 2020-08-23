/**
 * This Java File is Created By Rose
 */
package com.rose.yuscript.tree;

/**
 * @author Rose
 *
 */
public class YuAssignment implements YuNode {

	private String variableType;
	private String variableName;
	private YuExpression value;
	
	@Override
	public <T, R> R accept(YuTreeVisitor<R, T> visitor, T value) {
		return visitor.visitAssignment(this, value);
	}
	
	/**
	 * @param value the value to set
	 */
	public void setValue(YuExpression value) {
		this.value = value;
	}
	
	/**
	 * @return the value
	 */
	public YuExpression getValue() {
		return value;
	}
	
	/**
	 * @param variableName the variableName to set
	 */
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}
	
	/**
	 * @return the variableName
	 */
	public String getVariableName() {
		return variableName;
	}
	
	/**
	 * @param variableType the variableType to set
	 */
	public void setVariableType(String variableType) {
		this.variableType = variableType;
	}
	
	/**
	 * @return the variableType
	 */
	public String getVariableType() {
		return variableType;
	}

}
