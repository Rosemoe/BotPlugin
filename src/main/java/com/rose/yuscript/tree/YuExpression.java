/**
 * This Java File is Created By Rose
 */
package com.rose.yuscript.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.rose.yuscript.YuContext;
import com.rose.yuscript.YuTokens;

/**
 * @author Rose
 *
 */
public class YuExpression extends YuValue implements YuNode {

	private List<YuValue> children;
	
	private List<YuTokens> operators;
	
	public YuExpression() {
		children = new ArrayList<>();
		operators = new ArrayList<>();
	}
	
	@Override
	public <T, R> R accept(YuTreeVisitor<R, T> visitor, T value) {
		return visitor.visitExpression(this, value);
	}
	
	public void addChild(YuValue child) {
		if(!children.isEmpty()) {
			throw new IllegalStateException();
		}
		children.add(Objects.requireNonNull(child));
	}
	
	public void addExpression(YuTokens op,YuValue child) {
		children.add(Objects.requireNonNull(child));
		operators.add(Objects.requireNonNull(op));
	}
	
	/**
	 * @return the children
	 */
	public List<YuValue> getChildren() {
		return children;
	}
	
	/**
	 * @return the operators
	 */
	public List<YuTokens> getOperators() {
		return operators;
	}

	@Override
	public Object getValue(YuContext context) {
		if(operators.isEmpty()) {
			return children.get(0).getValue(context);
		}
		StringBuilder sb = new StringBuilder().append(children.get(0).getValue(context));
		for(int i = 0;i < operators.size();i++) {
			if(operators.get(i) != YuTokens.PLUS) {
				throw new YuSyntaxError();
			}
			sb.append(children.get(i + 1).getValue(context));
		}
		return sb.toString();
		
	}
}
