/**
 * This Java File is Created By Rose
 */
package com.rose.yuscript;

/**
 * @author Rose
 */
public enum YuTokens {
	DOT,//.
	COMMA,//,
	LPAREN,//(
	RPAREN,//)
	LBRACE,//{
	RBRACE,//}
	
	ANDAND,//&&
	OROR,//||
	
	STARTS_WITH,//?*
	ENDS_WITH,//*?
	CONTAINS,//?
	
	EQEQ,//==
	NOTEQ,//!=
	LTEQ,//<=
	GTEQ,//>=
	EQ,//=
	NOT,//!
	LT,//<
	GT,//>
	SEMI,//;
	
	PLUS,// +
	MINUS,// -
	MULTIPLY,// *
	DIVIDE,// /
	
	//Constant expression
	STRING,
	NUMBER,
	
	ENDCODE,//end code
	BREAK,//break
	VARIABLE_PREFIX,//s,ss,sss
	
	COMMENT,
	WHITESPACE,
	NEWLINE,
	EOF,
	UNKNOWN,
	IDENTIFIER,
	IF,
	ELSE,
	WHILE,
	FOR,
	TRUE,FALSE,NULL
}
