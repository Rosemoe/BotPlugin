/**
 * This Java File is Created By Rose
 */
package com.rose.yuscript.tree;

/**
 * @author Rose
 */
public interface YuTreeVisitor <R,T> {
	R visitAssignment(YuAssignment assign, T value);
	
	R visitScope(YuScope scope, T value);
	
	R visitCodeBlock(YuCodeBlock codeBlock, T value);
	
	R visitBreak(YuBreak codeBlock, T value);
	
	R visitCondition(YuCondition condition, T value);
	
	R visitConditionalExpression(YuConditionalExpression expr, T value);
	
	R visitEndcode(YuEndcode endcode, T value);
	
	R visitExpression(YuExpression expr, T value);
	
	R visitForTree(YuForTree tree, T value);
	
	R visitFunctionCall(YuFunctionCall call, T value);
	
	R visitIfTree(YuIfTree tree, T value);
	
	R visitValue(YuValue val, T value);
	
	R visitWhileTree(YuWhileTree tree, T value);

}
