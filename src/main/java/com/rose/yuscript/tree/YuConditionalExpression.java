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
public class YuConditionalExpression implements YuNode {

	private List<YuCondition> children;
	
	private List<YuTokens> operators;
	
	@Override
	public <T, R> R accept(YuTreeVisitor<R, T> visitor, T value) {
		return visitor.visitConditionalExpression(this, value);
	}
	
	public YuConditionalExpression() {
		children = new ArrayList<>();
		operators = new ArrayList<>();
	}
	
	public void addChild(YuCondition child) {
		if(!children.isEmpty()) {
			throw new IllegalStateException();
		}
		children.add(Objects.requireNonNull(child));
	}
	
	public void addExpression(YuTokens op,YuCondition child) {
		children.add(Objects.requireNonNull(child));
		operators.add(Objects.requireNonNull(op));
	}
	
	/**
	 * @return the children
	 */
	public List<YuCondition> getChildren() {
		return children;
	}
	
	/**
	 * @return the operators
	 */
	public List<YuTokens> getOperators() {
		return operators;
	}
	
	@SuppressWarnings("incomplete-switch")
	public boolean getValue(YuContext context) {
		boolean condition = getChildren().get(0).getValue(context);
		loop:for(int i = 0;i < operators.size();i++) {
			YuTokens operator = operators.get(i);
			switch(operator) {
			case ANDAND:
				if(condition) {
					condition = condition && getChildren().get(i + 1).getValue(context);
				}else {
					break loop;
				}
				break;
			case OROR:
				if(condition) {
					break loop;
				}else {
					condition = condition || getChildren().get(i + 1).getValue(context);
				}
			}
		}
		return condition;
	}

}
