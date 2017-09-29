/* *
 * Scanner for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2017.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2017 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2017
  */

package cop5556fa17;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Scanner {
	
	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {
		
		int pos;

		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}
		
		public int getPos() { return pos; }

	}

//	public static enum Kind {
//		IDENTIFIER, INTEGER_LITERAL, BOOLEAN_LITERAL, STRING_LITERAL, 
//		KW_x/* x */, KW_X/* X */, KW_y/* y */, KW_Y/* Y */, KW_r/* r */, KW_R/* R */, KW_a/* a */, 
//		KW_A/* A */, KW_Z/* Z */, KW_DEF_X/* DEF_X */, KW_DEF_Y/* DEF_Y */, KW_SCREEN/* SCREEN */, 
//		KW_cart_x/* cart_x */, KW_cart_y/* cart_y */, KW_polar_a/* polar_a */, KW_polar_r/* polar_r */, 
//		KW_abs/* abs */, KW_sin/* sin */, KW_cos/* cos */, KW_atan/* atan */, KW_log/* log */, 
//		KW_image/* image */,  KW_int/* int */, 
//		KW_boolean/* boolean */, KW_url/* url */, KW_file/* file */, OP_ASSIGN/* = */, OP_GT/* > */, OP_LT/* < */, 
//		OP_EXCL/* ! */, OP_Q/* ? */, OP_COLON/* : */, OP_EQ/* == */, OP_NEQ/* != */, OP_GE/* >= */, OP_LE/* <= */, 
//		OP_AND/* & */, OP_OR/* | */, OP_PLUS/* + */, OP_MINUS/* - */, OP_TIMES/* * */, OP_DIV/* / */, OP_MOD/* % */, 
//		OP_POWER/* ** */, OP_AT/* @ */, OP_RARROW/* -> */, OP_LARROW/* <- */, LPAREN/* ( */, RPAREN/* ) */, 
//		LSQUARE/* [ */, RSQUARE/* ] */, SEMI/* ; */, COMMA/* , */, EOF;
//	}
	
	/** Enum for Keyword Values
	 * 
	 * @author jcyar
	 *
	 */
	public static enum Kind {
		IDENTIFIER(""), INTEGER_LITERAL(""), BOOLEAN_LITERAL(""), STRING_LITERAL(""), 
		KW_x("x")/* x */, KW_X("X")/* X */, KW_y("y")/* y */, KW_Y("Y")/* Y */, KW_r("r")/* r */, KW_R("R")/* R */, KW_a("a")/* a */, 
		KW_A("A")/* A */, KW_Z("Z")/* Z */, KW_DEF_X("DEF_X")/* DEF_X */, KW_DEF_Y("DEF_Y")/* DEF_Y */, KW_SCREEN("SCREEN")/* SCREEN */, 
		KW_cart_x("cart_x")/* cart_x */, KW_cart_y("cart_y")/* cart_y */, KW_polar_a("polar_a")/* polar_a */, KW_polar_r("polar_r")/* polar_r */, 
		KW_abs("abs")/* abs */, KW_sin("sin")/* sin */, KW_cos("cos")/* cos */, KW_atan("atan")/* atan */, KW_log("log")/* log */, 
		KW_image("image")/* image */,  KW_int("int")/* int */, 
		KW_boolean("boolean")/* boolean */, KW_url("url")/* url */, KW_file("file")/* file */, OP_ASSIGN("=")/* = */, OP_GT(">")/* > */, OP_LT("<")/* < */, 
		OP_EXCL("!")/* ! */, OP_Q("?")/* ? */, OP_COLON(":")/* : */, OP_EQ("==")/* == */, OP_NEQ("!=")/* != */, OP_GE(">=")/* >= */, OP_LE("<=")/* <= */, 
		OP_AND("&")/* & */, OP_OR("|")/* | */, OP_PLUS("+")/* + */, OP_MINUS("-")/* - */, OP_TIMES("*")/* * */, OP_DIV("/")/* / */, OP_MOD("%")/* % */, 
		OP_POWER("**")/* ** */, OP_AT("@")/* @ */, OP_RARROW("->")/* -> */, OP_LARROW("<-")/* <- */, LPAREN("(")/* ( */, RPAREN(")")/* ) */, 
		LSQUARE("[")/* [ */, RSQUARE("]")/* ] */, SEMI(";")/* ; */, COMMA(",")/* , */, EOF("");
		String name;
		
		Kind(String name){
			this.name = name;
		}
		
		String getName(){
			return name;
		}
	}
	/** enum of states in DFA
	 *  This is defined to represent states in a DFA
	 *  @author jay
	 */
	public static enum State {
		START("start"), COMMENT("comment"), GOTEQUAL("="), GOTEX("!"), GOTMINUS("-"), GOTSTAR("*"), GOTLT("<"),
		GOTGT(">"), DIGITS("digits"), IDENTSTART("startidentifier"), IDENTPART("partidentifier"), STRINGS("string"), DIV("/"); 
		
		String name;
		
		State(String name){
			this.name = name;
		}
		
		String getName(){
			return name;
		}
	}
	/** Class to represent Tokens. 
	 * 
	 * This is defined as a (non-static) inner class
	 * which means that each Token instance is associated with a specific 
	 * Scanner instance.  We use this when some token methods access the
	 * chars array in the associated Scanner.
	 * 
	 * 
	 * @author Beverly Sanders
	 *
	 */
	public class Token {
		public final Kind kind;
		public final int pos;
		public final int length;
		public final int line;
		public final int pos_in_line;

		public Token(Kind kind, int pos, int length, int line, int pos_in_line) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
			this.line = line;
			this.pos_in_line = pos_in_line;
		}

		public String getText() {
			if (kind == Kind.STRING_LITERAL) {
				return chars2String(chars, pos, length);
			}
			else return String.copyValueOf(chars, pos, length);
		}

		/**
		 * To get the text of a StringLiteral, we need to remove the
		 * enclosing " characters and convert escaped characters to
		 * the represented character.  For example the two characters \ t
		 * in the char array should be converted to a single tab character in
		 * the returned String
		 * 
		 * @param chars
		 * @param pos
		 * @param length
		 * @return
		 */
		private String chars2String(char[] chars, int pos, int length) {
			StringBuilder sb = new StringBuilder();
			for (int i = pos + 1; i < pos + length - 1; ++i) {// omit initial and final "
				char ch = chars[i];
				if (ch == '\\') { // handle escape
					i++;
					ch = chars[i];
					switch (ch) {
					case 'b':
						sb.append('\b');
						break;
					case 't':
						sb.append('\t');
						break;
					case 'f':
						sb.append('\f');
						break;
					case 'r':
						sb.append('\r'); //for completeness, line termination chars not allowed in String literals
						break;
					case 'n':
						sb.append('\n'); //for completeness, line termination chars not allowed in String literals
						break;
					case '\"':
						sb.append('\"');
						break;
					case '\'':
						sb.append('\'');
						break;
					case '\\':
						sb.append('\\');
						break;
					default:
						assert false;
						break;
					}
				} else {
					sb.append(ch);
				}
			}
			return sb.toString();
		}

		/**
		 * precondition:  This Token is an INTEGER_LITERAL
		 * 
		 * @returns the integer value represented by the token
		 */
		public int intVal() {
			assert kind == Kind.INTEGER_LITERAL;
			return Integer.valueOf(String.copyValueOf(chars, pos, length));
		}

		public String toString() {
			return "[" + kind + "," + String.copyValueOf(chars, pos, length)  + "," + pos + "," + length + "," + line + ","
					+ pos_in_line + "]";
		}
		
		public boolean isKinds(Kind[] matchKinds){
			for(Kind k: matchKinds){
				if(kind == k){
					return true;
				}
			}
			return false;
		}
		public boolean isKind(Kind k){
			return kind==k;
		}
		/** 
		 * Since we overrode equals, we need to override hashCode.
		 * https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#equals-java.lang.Object-
		 * 
		 * Both the equals and hashCode method were generated by eclipse
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + line;
			result = prime * result + pos;
			result = prime * result + pos_in_line;
			return result;
		}

		/**
		 * Override equals method to return true if other object
		 * is the same class and all fields are equal.
		 * 
		 * Overriding this creates an obligation to override hashCode.
		 * 
		 * Both hashCode and equals were generated by eclipse.
		 * 
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (kind != other.kind)
				return false;
			if (length != other.length)
				return false;
			if (line != other.line)
				return false;
			if (pos != other.pos)
				return false;
			if (pos_in_line != other.pos_in_line)
				return false;
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is 
		 * associated with.
		 * @return
		 */
		private Scanner getOuterType() {
			return Scanner.this;
		}

	}

	/** 
	 * Extra character added to the end of the input characters to simplify the
	 * Scanner.  
	 */
	static final char EOFchar = 0;
	
	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;
    final HashMap<String, Kind> map_keywords;

	/**
	 * An array of characters representing the input.  These are the characters
	 * from the input string plus and additional EOFchar at the end.
	 */
	final char[] chars;  



	
	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;

	Scanner(String inputString) {
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input string terminated with null char
		chars[numChars] = EOFchar;
		tokens = new ArrayList<Token>();
		this.map_keywords = new HashMap<>();
		 for (Kind keyword : Kind.values()) {
			 map_keywords.put(keyword.getName(), keyword);
	        }
	}
	
    
	/**
	 * Method to scan the input and create a list of Tokens.
	 * 
	 * If an error is encountered during scanning, throw a LexicalException.
	 * 
	 * @return
	 * @throws LexicalException
	 */
	public Scanner scan() throws LexicalException {
		/* TODO  Replace this with a correct and complete implementation!!! */
		int pos = 0;
		int line = 1;
		int posInLine = 1;
		State curState = State.START;
		int startPos = pos;
		int length = chars.length;
//		System.out.println("Scanning:" + pos + " " +  posInLine + " " + curState + " " + length) ;
		while( pos < length){
			char ch = chars[pos];
//			System.out.println("[ "+  curState + " ] -> '" + ch +  "' @("  + pos + "," +  line + "," +  posInLine +  ")");
			switch(curState){
				case START:
					if(Character.isWhitespace(ch)){
						pos++;
						posInLine = posInLine + (pos - startPos);
						startPos = pos; 
						if(ch == '\n' || (ch == '\r' && chars[pos]!='\n')){
							line += 1;		
							posInLine = 1; //Reset position in line
						}
//						else if(ch == '\r'){
//							if(chars[pos]=='\n'){
//								System.out.println("Special \\r\\n case -- Skip this \r");
//							}else{
//								line += 1;		
//								posInLine = 1; //Reset position in line
//							}
//						}
					} 
					else if(Character.isDigit(ch) && ch!='0'){
						//Special case of '0' at START state is handled in the switch
						curState = State.DIGITS;
						posInLine = posInLine + (pos-startPos); //Old Position in line + length of last token
						startPos = pos;
						pos++;
					}
					else if(ch=='"'){
						curState = State.STRINGS;
						posInLine = posInLine + (pos-startPos); 
						startPos = pos;
						pos++;
					}
					else if(Character.isAlphabetic(ch)||ch=='$'||ch=='_'){
						curState = State.IDENTSTART;
						posInLine = posInLine + (pos-startPos); 
						startPos = pos;
						pos++;
						
					}
					else if(ch=='/'){
						curState = State.DIV;
						posInLine = posInLine + (pos-startPos); 
						startPos = pos;
						pos++;
					}
					else if(ch=='='){
						curState = State.GOTEQUAL;
						posInLine = posInLine + (pos-startPos); 
						startPos = pos;
						pos++;
					}
					else if(ch=='>'){
						curState = State.GOTGT;
						posInLine = posInLine + (pos-startPos); 
						startPos = pos;
						pos++;
					}
					else if(ch=='<'){
						curState = State.GOTLT;
						posInLine = posInLine + (pos-startPos); 
						startPos = pos;
						pos++;
					}
					else if(ch=='!'){
						curState = State.GOTEX;
						posInLine = posInLine + (pos-startPos); 
						startPos = pos;
						pos++;
					}
					else if(ch=='*'){
						curState = State.GOTSTAR;
						posInLine = posInLine + (pos-startPos); 
						startPos = pos;
						pos++;
					}
					else if(ch=='-'){
						curState = State.GOTMINUS;
						posInLine = posInLine + (pos-startPos); 
						startPos = pos;
						pos++;
					}
					else{
						//Special Chars : Separators, Operators
						posInLine = posInLine + (pos-startPos);
						startPos = pos;
						switch(ch){
						//Separators
							case ',':
								tokens.add(new Token(Kind.COMMA, startPos, 1, line, posInLine));
							break;
							case ';':
								tokens.add(new Token(Kind.SEMI, startPos, 1, line, posInLine));
							break;
							case ')':
								tokens.add(new Token(Kind.RPAREN, startPos, 1, line, posInLine));
							break;
							case '(':
								tokens.add(new Token(Kind.LPAREN, startPos, 1, line, posInLine));
							break;
							case '[':
								tokens.add(new Token(Kind.LSQUARE, startPos, 1, line, posInLine));
							break;
							case ']':
								tokens.add(new Token(Kind.RSQUARE, startPos, 1, line, posInLine));
							break;
						//Operators
							case ':':
								tokens.add(new Token(Kind.OP_COLON, startPos, 1, line, posInLine));
							break;
							case '%':
								tokens.add(new Token(Kind.OP_MOD, startPos, 1, line, posInLine));
							break;
							case '@':
								tokens.add(new Token(Kind.OP_AT, startPos, 1, line, posInLine));
							break;
							case '+':
								tokens.add(new Token(Kind.OP_PLUS, startPos, 1, line, posInLine));
							break;
							case '?':
								tokens.add(new Token(Kind.OP_Q, startPos, 1, line, posInLine));								
							break;
							case '|':
								tokens.add(new Token(Kind.OP_OR, startPos, 1, line, posInLine));								
							break;
							case '&':
								tokens.add(new Token(Kind.OP_AND, startPos, 1, line, posInLine));
							break;
							case '0':
								tokens.add(new Token(Kind.INTEGER_LITERAL, startPos, 1, line, posInLine));
							case EOFchar:
								break;
							default:
								throw new LexicalException("Invalid character Input at line:" + line + ", position:"+ pos, pos);
							//break;
						}
						pos++;
					}
					
				break;
				case DIGITS:
					if(Character.isDigit(ch)){
						pos++;						
					}
					else{
						try{
							Token intToken = new Token(Kind.INTEGER_LITERAL, startPos, pos-startPos, line, posInLine);
//							System.out.println("Integer Token:"+ intToken.toString());
							Integer integerVal = intToken.intVal();
							if(integerVal<=Integer.MAX_VALUE){
								tokens.add(intToken);
								curState = State.START;
							}							
						}catch(NumberFormatException e){
							//NumberFormatException
//							System.out.println("Exception:"+e.toString());
							throw new LexicalException("Integer out of range at line:" + line + ",  position:"+ pos, startPos);
						}
					}
					break;
				case STRINGS:
					if(ch != '"'){
						if(ch==EOFchar){
							throw new LexicalException("String ended abruptly at line:" + line + ",  position:"+ pos, pos);
						}
						else if(ch=='\n'||ch=='\r'){//Character.isWhitespace(ch) && ch!=' '){
							//Escape chars not escaped properly
							throw new LexicalException("String Escape Sequence error at line:" + line + ",  position:"+ pos, pos);
						}
						else if(ch=='\\'){
							try{
								ch=chars[pos+1];
//								System.out.println("Special Char ->>" + ch + "<<-");
								if(ch=='n'||ch=='t'||ch=='f'||ch=='r'||ch=='b'||ch=='"'||ch=='\''||ch=='\\'){
									pos++;
								}else if(ch==EOFchar){
									throw new LexicalException("String ended abruptly at line:" + line + ",  position:"+ pos, pos);
								}else{
									throw new LexicalException("Unknown character " + ch + " at line:" + line + ",  position:"+ pos, pos);
								}
							}catch(Exception e){
								throw new LexicalException(e.getMessage(),pos+1);
							}							
						}
						pos++;
					}
					else{
						pos++;
						tokens.add(new Token(Kind.STRING_LITERAL, startPos, pos-startPos, line, posInLine));
						curState = State.START;
					}
				break;
				case DIV:
					if(ch == '/'){
						curState = State.COMMENT;
						pos++;
					}else{
						tokens.add(new Token(Kind.OP_DIV, startPos, 1, line, posInLine));
						curState = State.START;
					}
				break;
				case COMMENT:
					if(ch == '\n' || ch == '\r'){
						curState = State.START;
//						pos++;
//						line += 1; 		// Update lines
//						posInLine = 1; 	// Reset Line Pointer
					} else{
						curState = State.COMMENT;
						pos++;
//						posInLine++;
					}
				break;
				case IDENTSTART:
					if(Character.isAlphabetic(ch)||ch=='$'||ch=='_'||Character.isDigit(ch)){//Character.isJavaIdentifierPart(ch)){
						pos++;
					}else{
						Kind inputKind;
//						System.out.println("Identifier formed:" + makeString(startPos, pos-startPos) + "(" + startPos + "," + (pos-startPos) + ")");
						if(isBooleanLiteral(startPos, pos-startPos)){
							tokens.add(new Token(Kind.BOOLEAN_LITERAL, startPos, pos-startPos, line, posInLine ));
						}
						else if((inputKind = checkKeyword(startPos, pos-startPos))!=null){
							tokens.add(new Token(inputKind, startPos, pos-startPos, line, posInLine));
						}
						else{
							tokens.add(new Token(Kind.IDENTIFIER, startPos, pos-startPos, line, posInLine ));
						}
						curState = State.START;						
					}
				break;
				case GOTEQUAL:
					if(ch=='='){
						pos++; // Add one more =
						tokens.add(new Token(Kind.OP_EQ, startPos, pos-startPos, line, posInLine));
						curState = State.START;
					}
					else{
						tokens.add(new Token(Kind.OP_ASSIGN, startPos, pos-startPos, line, posInLine));
						curState = State.START;
					}
				break;
				case GOTGT:
					if(ch=='='){
						pos++;
						tokens.add(new Token(Kind.OP_GE, startPos, pos-startPos, line, posInLine));
						curState = State.START;
					}else{
						tokens.add(new Token(Kind.OP_GT, startPos, pos-startPos, line, posInLine));
						curState = State.START;
					}
				break;
				case GOTLT:
					if(ch=='='){
						pos++;
						tokens.add(new Token(Kind.OP_LE, startPos, pos-startPos, line, posInLine));
						curState = State.START;
					}else if(ch=='-'){
						pos++;
						tokens.add(new Token(Kind.OP_LARROW, startPos, pos-startPos, line, posInLine));
						curState = State.START;
					}else{
						tokens.add(new Token(Kind.OP_LT, startPos, pos-startPos, line, posInLine));
						curState = State.START;
					}
				break;
				case GOTEX:
					if(ch=='='){
						pos++;
						tokens.add(new Token(Kind.OP_NEQ, startPos, pos-startPos, line, posInLine));
						curState = State.START;
					}else{
						tokens.add(new Token(Kind.OP_EXCL, startPos, pos-startPos, line, posInLine));
						curState = State.START;
					}
				break;
				case GOTMINUS:
					if(ch=='>'){
						pos++;
						tokens.add(new Token(Kind.OP_RARROW, startPos, pos-startPos, line, posInLine));
						curState = State.START;
					}else{
						tokens.add(new Token(Kind.OP_MINUS, startPos, pos-startPos, line, posInLine));
						curState = State.START;
					}
				break;
				case GOTSTAR:
					if(ch=='*'){
						pos++;
						tokens.add(new Token(Kind.OP_POWER, startPos, pos-startPos, line, posInLine));
						curState = State.START;	
					}else{
						tokens.add(new Token(Kind.OP_TIMES, startPos, pos-startPos, line, posInLine));
						curState = State.START;
					}
				break;
				default:
					System.out.println("Unknown State!!");		
			}
			if(ch == EOFchar) break;
		}
		tokens.add(new Token(Kind.EOF, pos, 0, line, posInLine));
		return this;

	}

	private String makeString(int startPos, int len){
		StringBuilder sb = new StringBuilder();
		for (int i = startPos; i < startPos + len; i++){
			sb.append(chars[i]);
		}
		return sb.toString();
	}
	
	/**
	 * Keyword Map checking
	 * @param startPos
	 * @param len
	 * @return
	 */
	private Kind checkKeyword(int startPos, int len) {
		// TODO Auto-generated method stub
		String input = makeString(startPos, len);
		if(map_keywords.containsKey(input)){			
			return map_keywords.get(input);
		}
		return null;
	}

	/**
	 * Checking if its a boolean literal
	 * @param startPos
	 * @param len
	 * @return
	 */
	private boolean isBooleanLiteral(int startPos, int len) {
		// TODO Auto-generated method stub
		String input = makeString(startPos, len);
		return (input.equals("true") || input.equals("false"))?true:false;
	}


	/**
	 * Returns true if the internal interator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}

	/**
	 * Returns the next Token and updates the internal iterator so that
	 * the next call to nextToken will return the next token in the list.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}
	
	/**
	 * Returns the next Token, but does not update the internal iterator.
	 * This means that the next call to nextToken or peek will return the
	 * same Token as returned by this methods.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * 
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}
	
	
	/**
	 * Resets the internal iterator so that the next call to peek or nextToken
	 * will return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}

	/**
	 * Returns a String representation of the list of Tokens 
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		return sb.toString();
	}

}
