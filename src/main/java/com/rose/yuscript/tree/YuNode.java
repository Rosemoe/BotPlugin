/**
 * This Java File is Created By Rose
 */
package com.rose.yuscript.tree;

/**
 * @author Rose
 *
 */
public interface YuNode {

	<T,R> R accept(YuTreeVisitor<R, T> visitor, T value);
	
}
