/**
 * This Java File is Created By Rose
 */
package com.rose.yuscript.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Rose
 *
 */
public class YuCodeBlock implements YuNode {

	private List<YuNode> children;
	
	@Override
	public <T, R> R accept(YuTreeVisitor<R, T> visitor, T value) {
		return visitor.visitCodeBlock(this, value);
	}
	
	public YuCodeBlock() {
		children = new ArrayList<>();
	}

	public void addChild(YuNode child) {
		children.add(Objects.requireNonNull(child));
	}
	
	public List<YuNode> getChildren() {
		return children;
	}
}
