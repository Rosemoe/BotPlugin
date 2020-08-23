/**
 * This Java File is Created By Rose
 */
package com.rose.yuscript.tree;

/**
 * @author Rose
 *
 */
public class YuForTree implements YuNode {

	private YuValue dest;
	
	private YuValue src;
	
	private YuCodeBlock codeBlock;
	
	@Override
	public <T, R> R accept(YuTreeVisitor<R, T> visitor, T value) {
		return visitor.visitForTree(this, value);
	}
	
	/**
	 * @param dest the dest to set
	 */
	public void setDest(YuValue dest) {
		this.dest = dest;
	}
	
	/**
	 * @param src the src to set
	 */
	public void setSrc(YuValue src) {
		this.src = src;
	}
	
	/**
	 * @return the dest
	 */
	public YuValue getDest() {
		return dest;
	}
	
	/**
	 * @return the src
	 */
	public YuValue getSrc() {
		return src;
	}
	
	/**
	 * @param codeBlock the codeBlock to set
	 */
	public void setCodeBlock(YuCodeBlock codeBlock) {
		this.codeBlock = codeBlock;
	}
	
	/**
	 * @return the codeBlock
	 */
	public YuCodeBlock getCodeBlock() {
		return codeBlock;
	}

}
