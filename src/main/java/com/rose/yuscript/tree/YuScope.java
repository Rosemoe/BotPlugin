/**
 * This Java File is Created By Rose
 */
package com.rose.yuscript.tree;

/**
 * @author Rose
 *
 */
public class YuScope extends YuCodeBlock implements YuNode {

	public YuScope() {
		
	}
	
	@Override
	public <T, R> R accept(YuTreeVisitor<R, T> visitor, T value) {
		return visitor.visitScope(this, value);
	}

}
