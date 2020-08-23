/**
 * This Java File is Created By Rose
 */
package com.rose.yuscript.tree;

/**
 * @author Rose
 *
 */
public class YuEndcode implements YuNode {

	public YuEndcode() {
		
	}

	
	@Override
	public <T, R> R accept(YuTreeVisitor<R, T> visitor, T value) {
		return visitor.visitEndcode(this, value);
	}
}
