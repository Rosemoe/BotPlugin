/**
 * This Java File is Created By Rose
 */
package com.rose.yuscript.tree;

import com.rose.yuscript.YuContext;
import com.rose.yuscript.YuTokens;
import static com.rose.yuscript.YuInterpreter.*;

/**
 * @author Rose
 *
 */
public class YuCondition implements YuNode {

	private YuExpression left;

	private YuTokens operator;

	private YuExpression right;

	@Override
	public <T, R> R accept(YuTreeVisitor<R, T> visitor, T value) {
		return visitor.visitCondition(this, value);
	}

	/**
	 * @param left the left to set
	 */
	public void setLeft(YuExpression left) {
		this.left = left;
	}

	/**
	 * @param operator the operator to set
	 */
	public void setOperator(YuTokens operator) {
		this.operator = operator;
	}

	/**
	 * @param right the right to set
	 */
	public void setRight(YuExpression right) {
		this.right = right;
	}

	/**
	 * @return the left
	 */
	public YuExpression getLeft() {
		return left;
	}

	/**
	 * @return the operator
	 */
	public YuTokens getOperator() {
		return operator;
	}

	/**
	 * @return the right
	 */
	public YuExpression getRight() {
		return right;
	}

	@SuppressWarnings("incomplete-switch")
	public boolean getValue(YuContext context) {
		if (operator != null) {
			try {
				switch (operator) {
				case EQEQ:
					return stringForm(left.getValue(context)).equals(stringForm(right.getValue(context)));
				case NOTEQ:
					return !stringForm(left.getValue(context)).equals(stringForm(right.getValue(context)));
				case LT:
					return Long.parseLong(stringForm(left.getValue(context))) < Long
							.parseLong(stringForm(right.getValue(context)));
				case GT:
					return Long.parseLong(stringForm(left.getValue(context))) > Long
							.parseLong(stringForm(right.getValue(context)));
				case LTEQ:
					return Long.parseLong(stringForm(left.getValue(context))) <= Long
							.parseLong(stringForm(right.getValue(context)));
				case GTEQ:
					return Long.parseLong(stringForm(left.getValue(context))) >= Long
							.parseLong(stringForm(right.getValue(context)));
				case STARTS_WITH:
					return stringForm(left.getValue(context)).startsWith(stringForm(right.getValue(context)));
				case CONTAINS:
					return stringForm(left.getValue(context)).contains(stringForm(right.getValue(context)));
				case ENDS_WITH:
					return stringForm(left.getValue(context)).endsWith(stringForm(right.getValue(context)));
				}
			} catch (NumberFormatException e) {
				return false;
			}
		} else {
			return stringForm(left.getValue(context)).equals("true");
		}
		return false;
	}

}
