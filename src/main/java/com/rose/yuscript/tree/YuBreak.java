/**
 * This Java File is Created By Rose
 */
package com.rose.yuscript.tree;

/**
 * @author Rose
 *
 */
public class YuBreak implements YuNode {

	public YuBreak() {
		
	}

	@Override
	public <T, R> R accept(YuTreeVisitor<R, T> visitor, T value) {
		return visitor.visitBreak(this, value);
	}
	
}
