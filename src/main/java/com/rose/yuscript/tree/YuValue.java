/**
 * This Java File is Created By Rose
 */
package com.rose.yuscript.tree;

import com.rose.yuscript.YuContext;

/**
 * @author Rose
 *
 */
public class YuValue implements YuNode {

	public final static int TYPE_VAR = 0,TYPE_NUM = 1,TYPE_STR = 2,TYPE_BOOL = 3,TYPE_NULL = 4;
	
	private String variableName;
	private String string;
	private Long number;
	private boolean bool;
	private int type = -1;
	private boolean invert = false;
	
	@Override
	public <T, R> R accept(YuTreeVisitor<R, T> visitor, T value) {
		return visitor.visitValue(this, value);
	}
	
	/**
	 * @param invert the invert to set
	 */
	public void setInvert(boolean invert) {
		this.invert = invert;
	}
	
	/**
	 * @return the invert
	 */
	public boolean isInvert() {
		return invert;
	}

	/**
	 * @param variableName the variableName to set
	 */
	public void setVariableName(String variableName) {
		this.variableName = variableName;
		type = TYPE_VAR;
	}
	
	/**
	 * @param number the number to set
	 */
	public void setNumber(String number) {
		this.number = Long.parseLong(number);
		type = TYPE_NUM;
	}
	
	/**
	 * @param string the string to set
	 */
	public void setString(String string) {
		StringBuilder sb = new StringBuilder();
		for(int i = 1;i < string.length() - 1;i++) {
			if(string.charAt(i) == '\\') {
				char next = string.charAt(i + 1);
				if(next == 'n') {
					sb.append('\n');
				}else {
					sb.append(next);
				}
				i++;
			}else {
				sb.append(string.charAt(i));
			}
		}
		this.string = sb.toString();
		type = TYPE_STR;
	}
	
	/**
	 * @return the string
	 */
	public String getString() {
		return string;
	}
	
	/**
	 * @return the number
	 */
	public Long getNumber() {
		return number;
	}
	
	/**
	 * @return the variableName
	 */
	public String getVariableName() {
		return variableName;
	}
	
	/**
	 * @param bool the bool to set
	 */
	public void setBool(boolean bool) {
		this.bool = bool;
		type = TYPE_BOOL;
	}
	
	/**
	 * @return the bool
	 */
	public boolean getBool() {
		return bool;
	}
	
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	
	public void setNull() {
		type = TYPE_NULL;
	}
	
	/**
	 * Get value
	 * @param context
	 * @return
	 */
	public Object getValue(YuContext context) {
		switch(getType()) {
		case TYPE_NUM:
			return number;
		case TYPE_STR:
			return string;
		case TYPE_VAR:
			return context.getVariable(variableName);
		case TYPE_BOOL:
			return bool;
		case TYPE_NULL:
			return null;
		default:
			throw new IllegalStateException();
		}
	}
}
