/**
 * This Java File is Created By Rose
 */
package com.rose.yuscript.tree;

import static com.rose.yuscript.YuTokens.*;

import com.rose.yuscript.YuTokens;
import com.rose.yuscript.util.MyCharacter;
import com.rose.yuscript.util.TrieTree;

/**
 * @author Rose
 *
 */
public class YuTokenizer {

    private static TrieTree<YuTokens> keywords;

    static {
        doStaticInit();
    }

    private String source;
    protected int bufferLen;
    private int line;
    private int column;
    private int index;
    protected int offset;
    protected int length;
    private YuTokens currToken;
    private boolean lcCal;
    private boolean skipWS;
    private boolean skipComment;

    public YuTokenizer(String src) {
        if(src == null) {
            throw new IllegalArgumentException("src can not be null");
        }
        this.source = src;
        init();
    }

    private void init() {
        line = 0;
        column = 0;
        length = 0;
        index = 0;
        currToken = WHITESPACE;
        lcCal = false;
        skipWS = false;
        skipComment = false;
        this.bufferLen = source.length();
    }

    public void setCalculateLineColumn(boolean cal) {
        this.lcCal = cal;
    }

    public void setSkipWhitespace(boolean skip) {
        this.skipWS = skip;
    }

    public void setSkipComment(boolean skip) {
        this.skipComment = skip;
    }

    public void pushBack(int length) {
        if (length > getTokenLength()) {
            throw new IllegalArgumentException("pushBack length too large");
        }
        this.length -= length;
    }

    private boolean isIdentifierPart(char ch) {
        return MyCharacter.isJavaIdentifierPart(ch);
    }

    private boolean isIdentifierStart(char ch) {
        return MyCharacter.isJavaIdentifierStart(ch);
    }

    public String getTokenString() {
        return source.substring(offset, offset + length);
    }

    public int getTokenLength() {
        return length;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public int getIndex() {
        return index;
    }

    public YuTokens getToken() {
        return currToken;
    }

    public String yyDesc() {
        return " line " + line + " column " + column;
    }

    private char charAt(int i) {
        return source.charAt(i);
    }

    private char charAt() {
        return source.charAt(offset + length);
    }

    public YuTokens nextToken() {
        YuTokens token;
        do {
            token = directNextToken();
        } while ((skipWS && (token == WHITESPACE || token == NEWLINE)) || (skipComment && token == COMMENT));
        currToken = token;
        return token;
    }

    public YuTokens directNextToken() {
        if (lcCal) {
            boolean r = false;
            for (int i = offset; i < offset + length; i++) {
                char ch = charAt(i);
                if (ch == '\r') {
                    r = true;
                    line++;
                    column = 0;
                } else if (ch == '\n') {
                    if (r) {
                        r = false;
                        continue;
                    }
                    line++;
                    column = 0;
                } else {
                    r = false;
                    column++;
                }
            }
        }
        index = index + length;
        offset = offset + length;
        if (offset == bufferLen) {
            return EOF;
        }
        char ch = source.charAt(offset);
        length = 1;
        if (ch == '\n') {
            return NEWLINE;
        } else if (ch == '\r') {
            scanNewline();
            return NEWLINE;
        } else if (isWhitespace(ch)) {
            char chLocal;
            while (offset + length < bufferLen && isWhitespace(chLocal = charAt(offset + length)) ) {
                if (chLocal == '\r' || chLocal == '\n') {
                    break;
                }
                length++;
            }
            return WHITESPACE;
        } else {
            if (isIdentifierStart(ch)) {
                return scanIdentifier(ch);
            }
            if (isPrimeDigit(ch)) {
                scanNumber();
                return NUMBER;
            }
            if(ch == '(') {
                return LPAREN;
            }else if(ch == ')') {
                return RPAREN;
            }else if(ch == '<') {
                return scanOperatorTwo('=', LT, LTEQ);
            }else if(ch == '>') {
                return scanOperatorTwo('=', GT, GTEQ);
            }
            switch (ch) {
                case '=':
                    return scanOperatorTwo('=', EQ, EQEQ);
                case '.':
                	if(column == 0) {
                		while(offset + length < bufferLen && charAt() != '\r' && charAt() != '\n') {
                			length++;
                		}
                		return COMMENT;
                	}
                    return DOT;
                case '{':
                    return LBRACE;
                case '}':
                    return RBRACE;
                case '/':
                    return scanDIV();
                case '*':
                    return scanOperatorTwo('?', MULTIPLY, ENDS_WITH);
                case '-':
                    return MINUS;
                case '+':
                    return PLUS;
                case ',':
                    return COMMA;
                case '!':
                    return scanOperatorTwo('=', NOT, NOTEQ);
                case '?':
                    return scanOperatorTwo('*', CONTAINS, STARTS_WITH);
                case '\"':
                    scanString();
                    return STRING;
                case ';':
                	return SEMI;
                case '|':
                	if(scanOperatorTwo('|', UNKNOWN, OROR) == OROR){
                		return OROR;
                	}
                	bad();
                	return UNKNOWN;
                case '&':
                	if(scanOperatorTwo('&', UNKNOWN, ANDAND) == ANDAND){
                		return ANDAND;
                	}
                	bad();
                	return UNKNOWN;
                default:
                    bad();
                    return UNKNOWN;
            }
        }
    }

    protected final void throwIfNeeded() {
        if(offset + length == bufferLen) {
            bad();
        }
    }

    protected void scanNewline() {
        if (offset + length < bufferLen && charAt(offset + length) == '\n') {
            length++;
        }
    }

    protected YuTokens scanIdentifier(char ch) {
        TrieTree.Node<YuTokens> n = keywords.root.map.get(ch);
        while (offset + length < bufferLen && isIdentifierPart(ch = charAt(offset + length))) {
            length++;
            n = n == null ? null : n.map.get(ch);
        }
        YuTokens rt = (n == null ? IDENTIFIER : (n.token == null ? IDENTIFIER : n.token));
        //Predicate
        if(rt == VARIABLE_PREFIX && offset + length < bufferLen) {
        	//Skip White spaces to get next valid character
        	int extraOffset = 0;
        	char predicate_ch = '\n';
        	while(offset + length + extraOffset < bufferLen && isWhitespace(predicate_ch = charAt(offset + length + extraOffset))) {
        		extraOffset++;
        	}
        	if(!isWhitespace(predicate_ch)) {
        		//For iyu language ugly function name
        		if(length == 1 && extraOffset == 0 && (predicate_ch == '2' || predicate_ch == '+' || predicate_ch == '-' || predicate_ch == '*' || predicate_ch == '/')) {
        			extraOffset++;
        			while(offset + length + extraOffset < bufferLen && isWhitespace(predicate_ch = charAt(offset + length + extraOffset))) {
                		extraOffset++;
                	}
        			if(predicate_ch == '(') {
        				length++;
        				return IDENTIFIER;
        			}
        		}
        		//
        		if(predicate_ch != '.' && !isIdentifierStart(predicate_ch)) {
        			return IDENTIFIER;
        		}
        	}
        }
        if(rt == WHILE || rt == FOR || rt == IF) {
        	int extraOffset = 0;
        	char predicate_ch = '\n';
        	while(offset + length + extraOffset < bufferLen && isWhitespace(predicate_ch = charAt(offset + length + extraOffset))) {
        		extraOffset++;
        	}
        	if(predicate_ch != '(') {
        		return IDENTIFIER;
        	}
        }
        if(rt == ELSE) {
        	int extraOffset = 0;
        	char predicate_ch = '\n';
        	while(offset + length + extraOffset < bufferLen && isWhitespace(predicate_ch = charAt(offset + length + extraOffset))) {
        		extraOffset++;
        	}
        	if(predicate_ch != '(' && !(predicate_ch == 'i' && bufferLen + length + extraOffset < bufferLen + 1 && charAt(bufferLen + length + extraOffset + 1) == 'f')) {
        		return IDENTIFIER;
        	}
        }
        return rt;
    }
    
    private void bad() {
    	throw new IllegalArgumentException("syntax error:token start at " + yyDesc());
    }

    protected void scanTrans() {
        throwIfNeeded();
        char ch = charAt(offset + length);
        if (ch == '\\' || ch == 't' || ch == 'f' || ch == 'n' || ch == 'r' || ch == '0' || ch == '\"' || ch == '\''
                || ch == 'b') {
            length++;
        } else if (ch == 'u') {
            length++;
            for (int i = 0; i < 4; i++) {
                throwIfNeeded();
                if (!isDigit(charAt(offset + length))) {
                	bad();
                    return;
                }
                length++;
            }
        } else {
        }
    }

    protected void scanString() {
        throwIfNeeded();
        char ch;
        while (offset + length < bufferLen && (ch = charAt(offset + length)) != '\"') {
            if (ch == '\\') {
                length++;
                scanTrans();
            } else {
                if (ch == '\n') {
                	bad();
                    return;
                }
                length++;
                throwIfNeeded();
            }
        }
        if (offset + length == bufferLen) {
           bad();
        } else {
            length++;
        }
    }

    protected void scanNumber() {
        while(offset + length < bufferLen && isPrimeDigit(charAt())) {
        	length++;
        }
    }

    protected YuTokens scanDIV() {
        if (offset + 1 == bufferLen) {
            return DIVIDE;
        }
        char ch = charAt();
        if (ch == '/') {
            length++;
            while (offset + length < bufferLen && charAt() != '\n') {
                length++;
            }
            return COMMENT;
        } else if (ch == '.') {
            length++;
            char pre, curr = '?';
            boolean breakFromLoop = false;
            while (offset + length < bufferLen) {
                pre = curr;
                curr = charAt();
                if (curr == '/' && pre == '.') {
                    length++;
                    breakFromLoop = true;
                    break;
                }
                length++;
            }
            if (!breakFromLoop) {
               bad();
            }
            return COMMENT;
        } else {
            return DIVIDE;
        }
    }

    protected YuTokens scanOperatorTwo(char expected, YuTokens ifWrong, YuTokens ifRight) {
        if(offset + length == bufferLen) {
        	return ifWrong;
        }
        if(charAt() == expected) {
        	length++;
        	return ifRight;
        }
    	return ifWrong;
    }

    public void reset(String src) {
        if(src == null) {
            throw new IllegalArgumentException();
        }
        this.source = src;
        line = 0;
        column = 0;
        length = 0;
        index = 0;
        offset = 0;
        currToken = WHITESPACE;
        bufferLen = src.length();
    }

    protected static void doStaticInit() {
        keywords = new TrieTree<>();
        keywords.put("s", VARIABLE_PREFIX);
        keywords.put("ss", VARIABLE_PREFIX);
        keywords.put("sss", VARIABLE_PREFIX);
        keywords.put("endcode", ENDCODE);
        keywords.put("break", BREAK);
        keywords.put("w", WHILE);
        keywords.put("f",IF);
        keywords.put("else", ELSE);
        keywords.put("for", FOR);
        keywords.put("null", NULL);
        keywords.put("true", TRUE);
        keywords.put("false",FALSE);
        MyCharacter.initMap();
    }

    protected static boolean isDigit(char c) {
        return ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f'));
    }

    protected static boolean isPrimeDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    protected static boolean isWhitespace(char c) {
        return (c == '\t' || c == ' ' || c == '\f' || c == '\n' || c == '\r');
    }
}
