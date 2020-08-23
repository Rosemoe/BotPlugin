/**
 * This Java File is Created By Rose
 */
package com.rose.yuscript.tree;

import java.util.Objects;

import com.rose.yuscript.*;
import static com.rose.yuscript.YuTokens.*;

/**
 * @author Rose
 *
 */
public final class YuTree {
	
	private YuTokenizer tokenizer;
	private YuScope root;

	public YuTree(YuTokenizer tokenizer) throws YuSyntaxError {
		this.tokenizer = Objects.requireNonNull(tokenizer);
		tokenizer.setCalculateLineColumn(true);
		tokenizer.setSkipComment(true);
		tokenizer.setSkipWhitespace(true);
		root = (YuScope)parseCodeBlock(true);
	}
	
	public YuScope getRoot() {
		return root;
	}
	
	private YuCodeBlock parseCodeBlock(boolean outside) throws YuSyntaxError {
		YuCodeBlock block = outside ? new YuScope() : new YuCodeBlock();
		while(tokenizer.nextToken() != EOF) {
			switch(tokenizer.getToken()) {
			case LBRACE:{
				block.addChild(parseCodeBlock(false));
				break;
			}
			case RBRACE:{
				if(!outside) {
					return block;
				}else {
					throw new YuSyntaxError();
				}
			}
			case VARIABLE_PREFIX:{
				tokenizer.pushBack(tokenizer.getTokenLength());
				block.addChild(parseAssignment());
				break;
			}
			case IDENTIFIER:{
				tokenizer.pushBack(tokenizer.getTokenLength());
				block.addChild(parseFunctionCall());
				break;
			}
			case IF:{
				tokenizer.pushBack(tokenizer.getTokenLength());
				block.addChild(parseIfTree());
				break;
			}
			case WHILE:{
				tokenizer.pushBack(tokenizer.getTokenLength());
				block.addChild(parseWhileTree());
				break;
			}
			case FOR:{
				tokenizer.pushBack(tokenizer.getTokenLength());
				block.addChild(parseForTree());
				break;
			}
			case ENDCODE:{
				block.addChild(new YuEndcode());
				break;
			}
			case BREAK:{
				block.addChild(new YuBreak());
				break;
			}
			case EOF:{
				if(outside) {
					return block;
				}else {
					throw new YuSyntaxError();
				}
			}
			default:{
				System.out.println(tokenizer.getToken());
				throw new YuSyntaxError();
			}
			}
		}
		return block;
	}
	
	private YuForTree parseForTree() throws YuSyntaxError {
		YuForTree tree = new YuForTree();
		if(tokenizer.nextToken() != FOR) {
			throw new YuSyntaxError();
		}
		if(tokenizer.nextToken() != LPAREN) {
			throw new YuSyntaxError();
		}
		tokenizer.nextToken();
		tree.setDest(parseValue());
		if(tokenizer.nextToken() != SEMI) {
			throw new YuSyntaxError();
		}
		tokenizer.nextToken();
		tree.setSrc(parseValue());
		if(tokenizer.nextToken() != RPAREN) {
			throw new YuSyntaxError();
		}
		if(tokenizer.nextToken() != LBRACE) {
			throw new YuSyntaxError();
		}
		tree.setCodeBlock(parseCodeBlock(false));
		return tree;
	}
	
	private YuWhileTree parseWhileTree() throws YuSyntaxError {
		YuWhileTree tree = new YuWhileTree();
		if(tokenizer.nextToken() != WHILE) {
			throw new YuSyntaxError();
		}
		if(tokenizer.nextToken() != LPAREN) {
			throw new YuSyntaxError();
		}
		tree.setCondition(parseConditionalExpression());
		if(tokenizer.nextToken() != RPAREN) {
			throw new YuSyntaxError();
		}
		if(tokenizer.nextToken() != LBRACE) {
			throw new YuSyntaxError();
		}
		tree.setCodeBlock(parseCodeBlock(false));
		return tree;
	}
	
	private YuIfTree parseIfTree() throws YuSyntaxError {
		YuIfTree tree = new YuIfTree();
		if(tokenizer.nextToken() != IF) {
			throw new YuSyntaxError();
		}
		if(tokenizer.nextToken() != LPAREN) {
			throw new YuSyntaxError();
		}
		tree.setCondition(parseConditionalExpression());
		if(tokenizer.nextToken() != RPAREN) {
			throw new YuSyntaxError();
		}
		if(tokenizer.nextToken() != LBRACE) {
			throw new YuSyntaxError();
		}
		tree.setCodeBlock(parseCodeBlock(false));
		YuTokens next = tokenizer.nextToken();
		if(next != ELSE) {
			tokenizer.pushBack(tokenizer.getTokenLength());
			return tree;
		}
		next = tokenizer.nextToken();
		if(next == IF) {
			YuCodeBlock block = new YuCodeBlock();
			tokenizer.pushBack(tokenizer.getTokenLength());
			block.addChild(parseIfTree());
			tree.setFallbackCodeBlock(block);
		}else if(next == LBRACE) {
			tree.setFallbackCodeBlock(parseCodeBlock(false));
		}else {
			throw new YuSyntaxError();
		}
		return tree;
	}
	
	private YuConditionalExpression parseConditionalExpression() throws YuSyntaxError {
		YuConditionalExpression expr = new YuConditionalExpression();
		YuCondition condition = parseCondition();
		expr.addChild(condition);
		while(true) {
			YuTokens next = tokenizer.nextToken();
			if(next == ANDAND || next == OROR) {
				expr.addExpression(next, parseCondition());
			}else {
				tokenizer.pushBack(tokenizer.getTokenLength());
				break;
			}
		}
		return expr;
	}
	
	private YuCondition parseCondition() throws YuSyntaxError {
		YuCondition condition = new YuCondition();
		condition.setLeft(parseExpression());
		YuTokens token = tokenizer.nextToken();
		switch(token) {
		case EQEQ:
		case LT:
		case LTEQ:
		case GT:
		case GTEQ:
		case STARTS_WITH:
		case ENDS_WITH:
		case CONTAINS:
		case NOTEQ:
			condition.setOperator(token);
			break;
		default:
			throw new YuSyntaxError();
		}
		condition.setRight(parseExpression());
		return condition;
	}
	
	private YuAssignment parseAssignment() throws YuSyntaxError {
		YuAssignment assignment = new YuAssignment();
		if(tokenizer.nextToken() != VARIABLE_PREFIX) {
			throw new YuSyntaxError();
		}
		assignment.setVariableType(tokenizer.getTokenString());
		if(tokenizer.nextToken() == IDENTIFIER) {
			assignment.setVariableName(tokenizer.getTokenString());
		}else {
			throw new YuSyntaxError();
		}
		if(tokenizer.nextToken() != EQ) {
			throw new YuSyntaxError();
		}
		assignment.setValue(parseExpression());
		return assignment;
	}
	
	private YuFunctionCall parseFunctionCall() throws YuSyntaxError {
		YuFunctionCall call = new YuFunctionCall();
		if(tokenizer.nextToken() != IDENTIFIER) {
			throw new YuSyntaxError();
		}
		call.setFunctionName(tokenizer.getTokenString());
		if(tokenizer.nextToken() != LPAREN) {
			throw new YuSyntaxError();
		}
		if(tokenizer.nextToken() == RPAREN) {
			return call;
		}
		tokenizer.pushBack(tokenizer.getTokenLength());
		while(true) {
			YuExpression expr = parseExpression();
			call.addArgument(expr);
			tokenizer.nextToken();
			if(tokenizer.getToken() == COMMA) {
				
			}else if(tokenizer.getToken() == RPAREN) {
				break;
			}else {
				throw new YuSyntaxError();
			}
		}
		return call;
	}

	private YuExpression parseExpression() throws YuSyntaxError {
		YuExpression expression = new YuExpression();
		YuTokens token = tokenizer.nextToken();
		boolean invert = token == NOT;
		if(invert) {
			token = tokenizer.nextToken();
		}
		switch(token) {
		case LPAREN:{
			YuExpression expr = parseExpression();
			expr.setInvert(invert);
			expression.addChild(expr);
			if(tokenizer.nextToken() != RPAREN) {
				throw new YuSyntaxError();
			}
			break;
		}
		case NUMBER:
		case STRING:
		case VARIABLE_PREFIX:
		case IDENTIFIER:
		case TRUE:
		case FALSE:
		case NULL:
		{
			YuValue value = parseValue();
			value.setInvert(invert);
			expression.addChild(value);
			break;
		}
		case MINUS:
		{
			token = tokenizer.nextToken();
			if(token != NUMBER && token != VARIABLE_PREFIX && token != IDENTIFIER) {
				throw new YuSyntaxError();
			}
			YuValue val = new YuValue();
			val.setInvert(invert);
			val.setNumber("-" + tokenizer.getTokenString());
			expression.addChild(val);
			break;
		}
		default:
			throw new YuSyntaxError();
		}
		token = tokenizer.nextToken();
		while(true) {
			invert = false;
			if(token == PLUS || token == MINUS || token == MULTIPLY || token == DIVIDE || token == NOT) {
				if(token == NOT) {
					invert = true;
					token = tokenizer.nextToken();
					if(!(token == PLUS || token == MINUS || token == MULTIPLY || token == DIVIDE)) {
						throw new YuSyntaxError();
					}
				}
				YuTokens op = tokenizer.getToken();
				token = tokenizer.nextToken();
				switch(token) {
				case LPAREN:{
					YuExpression expr = parseExpression();
					expr.setInvert(invert);
					expression.addExpression(op,expr);
					if(tokenizer.getToken() != RPAREN) {
						throw new YuSyntaxError();
					}
					break;
				}
				case NUMBER:
				case STRING:
				case VARIABLE_PREFIX:
				case IDENTIFIER:
				case TRUE:
				case FALSE:
				case NULL:
				{
					YuValue val = parseValue();
					val.setInvert(invert);
					expression.addExpression(op,val);
					break;
				}
				case MINUS:
				{
					token = tokenizer.nextToken();
					if(token != NUMBER) {
						throw new YuSyntaxError();
					}
					YuValue val = new YuValue();
					val.setInvert(invert);
					val.setNumber("-" + tokenizer.getTokenString());
					expression.addExpression(op,val);
					break;
				}
				default:
					throw new YuSyntaxError();
				}
				token = tokenizer.nextToken();
			}else{
				tokenizer.pushBack(tokenizer.getTokenLength());
				break;
			}
		}
		return expression;
	}
	
	private YuValue parseValue() throws YuSyntaxError {
		YuValue value = new YuValue();
		switch(tokenizer.getToken()) {
		case NUMBER:
			value.setNumber(tokenizer.getTokenString());
			break;
		case STRING:
			value.setString(tokenizer.getTokenString());
			break;
		case VARIABLE_PREFIX:
			String prefix = tokenizer.getTokenString();
			if(tokenizer.nextToken()!=DOT) {
				throw new YuSyntaxError();
			}
			if(tokenizer.nextToken()!=IDENTIFIER) {
				throw new YuSyntaxError();
			}
			value.setVariableName(prefix + "." + tokenizer.getTokenString());
			break;
		case IDENTIFIER:
			value.setVariableName(tokenizer.getTokenString());
			break;
		case TRUE:
			value.setBool(true);
			break;
		case FALSE:
			value.setBool(false);
			break;
		case NULL:
			value.setNull();
			break;
		default:
			throw new YuSyntaxError();
		}
		return value;
	}
	
}
