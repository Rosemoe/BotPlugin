/**
 * This Java File is Created By Rose
 */
package com.rose.yuscript.functions;

import java.util.List;

import com.rose.yuscript.YuContext;
import com.rose.yuscript.YuInterpreter;
import com.rose.yuscript.tree.YuExpression;

/**
 * @author Rose
 *
 */
public interface Function {

	String getName();
	
	/**
	 * -1 for unspecified (Any count)
	 */
	int getArgumentCount();
	
	void invoke(List<YuExpression> arguments, YuContext context, YuInterpreter interpreter) throws Throwable;
	
}
