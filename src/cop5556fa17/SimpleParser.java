package cop5556fa17;

/*
 * @author: Jayachandra
 */

import java.util.Arrays;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.SimpleParser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class SimpleParser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}

	Scanner scanner;
	Token t;

	SimpleParser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * Main method called by compiler to parser input. Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public void parse() throws SyntaxException {
		program();
		matchEOF();
	}

	/*
	 * Just Consume a token
	 */
	public void consume() throws SyntaxException {
//		System.out.println("Consuming -  ==> " + t.kind.toString() + "\t|" + t.toString());
		t = scanner.nextToken();
	}
	
	private String lineStamp(String tok){
		return "Expected "+ tok +" at " + t.line + ":" + t.pos_in_line;
	}

	/**
	 * match a token
	 * 
	 * @param Kind
	 */
	public boolean matchAndConsume(Kind matchKind) throws SyntaxException {
//		System.out.println("Matching:" + matchKind + " == " + t.kind.toString() + "\t|" + t.toString());
		if (matchKind == t.kind) {
			t = scanner.nextToken();
			return true;
		} else {
			System.out.print("Failed to match" );
			throw new SyntaxException(t, lineStamp(matchKind.toString() + " but found "+ t.getText() + " of type " + t.kind));
			// return false;
		}
	}

	public boolean matchAndConsume(Kind[] matchKinds) throws SyntaxException {
		boolean allBool = true;
		for (Kind k : matchKinds) {
			allBool = allBool && matchAndConsume(k); // All Bool should be true for everything for it to be true
		}
		return allBool;
	}

	/**
	 * Program ::= IDENTIFIER ( Declaration SEMI | Statement SEMI )*
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
	void program() throws SyntaxException {
		/*
		 * Program ::= IDENTIFIER ( Declaration SEMI | Statement SEMI )*
		 */

		if (t.isKind(EOF)) {
			throw new SyntaxException(t, "Empty Program");
		}
		
		matchAndConsume(IDENTIFIER);
		while (t.isKinds(new Kind[] { KW_int, KW_boolean, KW_image, KW_url, KW_file, IDENTIFIER })) {
			if (t.isKinds(new Kind[] { KW_int, KW_boolean, KW_image, KW_url, KW_file })) {
				declare();
			} else if (t.isKind(IDENTIFIER)) {
				statement();
			} else {
				throw new SyntaxException(t, lineStamp("program"));
			}
			matchAndConsume(SEMI);
		}

//		if (!t.isKind(EOF))
//			throw new SyntaxException(t, lineStamp("program"));
	}

	private void declare() throws SyntaxException {
		/*
		 * Declaration :: = VariableDeclaration | ImageDeclaration | SourceSinkDeclaration
		 * @eg: int a=0 | image
		 */
		switch (t.kind) {
		case KW_int:
		case KW_boolean:
			variableDeclare();
			break;
		case KW_image:
			imageDeclare();
			break;
		case KW_url:
		case KW_file:
			ssDeclare();
			break;
		default:
			throw new SyntaxException(t, lineStamp("declaration"));
		}
	}

	private void statement() throws SyntaxException {
		/*
		 * Statement ::= AssignmentStatement | ImageOutStatement | ImageInStatement
		 * AssignmentStatement ::= Lhs OP_ASSIGN Expression
		 * ImageInStatement ::= IDENTIFIER OP_LARROW Source 
		 * ImageOutStatement ::= IDENTIFIER OP_RARROW Sink 
		 * Lhs::= IDENTIFIER ( LSQUARE LhsSelector RSQUARE | EPS )
		 */
		matchAndConsume(Kind.IDENTIFIER); // Common FIRST(Statement)
		switch (t.kind) {
		case OP_LARROW:
			matchAndConsume(OP_LARROW);
			source();
			break;
		case OP_RARROW:
			matchAndConsume(OP_RARROW);
			sink();
			break;
		case LSQUARE: // lhs
			matchAndConsume(LSQUARE);
			lhsSelector();
			matchAndConsume(RSQUARE);
		case OP_ASSIGN: // lhs is EPS
			matchAndConsume(OP_ASSIGN);
			expression();
			break;
		default:
			throw new SyntaxException(t, lineStamp("statement"));
		}

	}

	private void variableDeclare() throws SyntaxException {
		/*
		 * VariableDeclaration ::= VarType IDENTIFIER ( OP_ASSIGN Expression | EPS ) 
		 * VarType ::= KW_int | KW_boolean
		 * @eg: int a = 0
		 */
		consume(); // VarType
		matchAndConsume(Kind.IDENTIFIER);
		if (t.kind == OP_ASSIGN) {
			matchAndConsume(Kind.OP_ASSIGN);
			expression();
		}
		// else EPS
	}

	public void imageDeclare() throws SyntaxException {
		/*
		 * ImageDeclaration ::= KW_image (LSQUARE Expression COMMA Expression RSQUARE | EPS) IDENTIFIER ( OP_LARROW Source | EPS )
		 * @eg image [sin(x), cos(x)] equals <- source
		 */
		matchAndConsume(Kind.KW_image);
		if (t.kind == Kind.LSQUARE) {
			matchAndConsume(Kind.LSQUARE);
			expression();
			matchAndConsume(Kind.COMMA);
			expression();
			matchAndConsume(Kind.RSQUARE);
		}

		matchAndConsume(Kind.IDENTIFIER);
		if (t.kind == Kind.OP_LARROW) {
			matchAndConsume(Kind.OP_LARROW);
			source();
		}
	}

	public void ssDeclare() throws SyntaxException {
		/*
		 * SourceSinkDeclaration ::= SourceSinkType IDENTIFIER OP_ASSIGN Source
		 * SourceSinkType := KW_url | KW_file
		 */
		consume(); // SourceSinkType
		matchAndConsume(new Kind[] { IDENTIFIER, OP_ASSIGN });
		source();
	}

	public void source() throws SyntaxException {
		/*
		 * Source ::= STRING_LITERAL 
		 * Source ::= OP_AT Expression 
		 * Source ::= IDENTIFIER
		 * eg: 
		 * "hello world"
		 * @exp
		 * variable
		 * 
		 */
		switch (t.kind) {
		case STRING_LITERAL:
			matchAndConsume(Kind.STRING_LITERAL);
			break;
		case OP_AT:
			matchAndConsume(Kind.OP_AT);
			expression();
			break;
		case IDENTIFIER:
			matchAndConsume(Kind.IDENTIFIER);
			break;
		default:
			throw new SyntaxException(t, lineStamp("source"));
		}
	}

	private void sink() throws SyntaxException {
		/*
		 * Sink ::= IDENTIFIER | KW_SCREEN
		 */
		if (t.kind == IDENTIFIER) {
			matchAndConsume(IDENTIFIER);
		} else if (t.kind == KW_SCREEN) {
			matchAndConsume(KW_SCREEN);
		} else {
			throw new SyntaxException(t, lineStamp("sink"));
		}
	}

	public void lhsSelector() throws SyntaxException {
		/*
		 * LhsSelector ::= LSQUARE ( XySelector | RaSelector ) RSQUARE
		 * XySelector ::= KW_x COMMA KW_y RaSelector ::= KW_r COMMA KW_A
		 */
		matchAndConsume(LSQUARE);
		if (t.isKind(KW_x)) { // XySelector
			matchAndConsume(new Kind[] { KW_x, COMMA, KW_y });
		} else if (t.isKind(KW_r)) { // RaSelector
			matchAndConsume(new Kind[] { KW_r, COMMA, KW_A });
		} // else EPS
		matchAndConsume(RSQUARE);
	}

	/**
	 * Expression ::= OrExpression OP_Q Expression OP_COLON Expression |
	 * OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental
	 * development.
	 * 
	 * @throws SyntaxException
	 */
	void expression() throws SyntaxException {
		/*
		 * Expression ::= OrExpression OP_Q Expression OP_COLON Expression 
		 * 				| OrExpression
		 */
		orExpression();
		if (t.isKind(OP_Q)) {
			matchAndConsume(OP_Q);
			expression();
			matchAndConsume(OP_COLON);
			expression();
		}
		// else EPS
	}

	private void orExpression() throws SyntaxException {
		/*
		 * OrExpression ::= AndExpression ( OP_OR AndExpression)*
		 */
		andExpression();
		while (t.isKind(OP_OR)) {
			matchAndConsume(OP_OR);
			andExpression();
		}
	}

	private void andExpression() throws SyntaxException {
		/*
		 * AndExpression ::= EqExpression ( OP_AND EqExpression )*
		 */
		eqExpression();
		while (t.isKind(OP_AND)) {
			matchAndConsume(OP_AND);
			eqExpression();
		}
	}

	private void eqExpression() throws SyntaxException {
		/*
		 * EqExpression ::= RelExpression ( (OP_EQ | OP_NEQ ) RelExpression )*
		 */
		relExpression();
		while (t.isKind(OP_EQ) || t.isKind(OP_NEQ)) {
			consume(); // (OP_EQ | OP_NEQ )
			relExpression();
		}
	}

	private void relExpression() throws SyntaxException {
		/*
		 * RelExpression ::= AddExpression ( ( OP_LT | OP_GT | OP_LE | OP_GE )  AddExpression)*
		 */
		addExpression();
		while (t.isKinds(new Kind[] { OP_LT, OP_GT, OP_LE, OP_GE })) {
			consume(); // OP_LT | OP_GT | OP_LE | OP_GE
			addExpression();
		}
	}

	private void addExpression() throws SyntaxException {
		/*
		 * AddExpression ::= MultExpression ( (OP_PLUS | OP_MINUS ) MultExpression )*
		 */
		multExpression();
		while (t.isKind(OP_PLUS) || t.isKind(OP_MINUS)) {
			consume();
			multExpression();
		}
	}

	public void multExpression() throws SyntaxException {
		/*
		 * MultExpression := UnaryExpression ( ( OP_TIMES | OP_DIV | OP_MOD ) UnaryExpression )*
		 */
		unaryExpression();
		while (t.isKinds(new Kind[] { OP_TIMES, OP_DIV, OP_MOD })) {
			consume();
			unaryExpression();
		}
	}

	public void unaryExpression() throws SyntaxException {
		/*
		 * UnaryExpression ::= OP_PLUS UnaryExpression 
		 * 					| OP_MINUS UnaryExpression
		 *  				| UnaryExpressionNotPlusMinus
		 */
		if (t.isKind(OP_PLUS) || t.isKind(OP_MINUS)) {
			consume();
			unaryExpression();
		} else {
			ueNotPlusMinus();
		}
	}

	public void ueNotPlusMinus() throws SyntaxException {
		/*
		 * UnaryExpressionNotPlusMinus ::= OP_EXCL UnaryExpression | Primary | IdentOrPixelSelectorExpression 
		 * 								| KW_x | KW_y | KW_r | KW_a | KW_X | KW_Y | KW_Z | KW_A | KW_R | KW_DEF_X | KW_DEF_Y
		 * @eg: ! 
		 */
		if (t.isKinds(new Kind[] { KW_x, KW_y, KW_r, KW_a, KW_X, KW_Y, KW_Z, KW_A, KW_R, KW_DEF_X, KW_DEF_Y })) {
			consume();
		} else if (t.isKind(OP_EXCL)) {
			matchAndConsume(OP_EXCL);
			unaryExpression();
		} else if (t.isKind(IDENTIFIER)) {
			identOrPixelSelectorExpression();
		} else if (t.isKinds(new Kind[] { INTEGER_LITERAL, BOOLEAN_LITERAL, LPAREN, KW_sin, KW_cos, KW_atan, KW_abs,
				KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r })) {
			primary();
		} else {
			throw new SyntaxException(t, lineStamp("UnaryExpressionNotPlusMinus"));
		}
	}

	public void primary() throws SyntaxException {
		/*
		 * Primary ::= INTEGER_LITERAL 
		 * 			 | LPAREN Expression RPAREN 
		 * 			 | FunctionApplication
		 *  		 | BOOLEAN_LITERAL
		 */
		if (t.isKind(INTEGER_LITERAL) || t.isKind(BOOLEAN_LITERAL)) {
			consume();
		} else if (t.isKind(LPAREN)) {
			matchAndConsume(LPAREN);
			expression();
			matchAndConsume(RPAREN);
		} else if (t.isKinds(
				new Kind[] { KW_sin, KW_cos, KW_atan, KW_abs, KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r })) {
			functionApp();
		} else {
			throw new SyntaxException(t, lineStamp("primary"));
		}
	}

	public void identOrPixelSelectorExpression() throws SyntaxException {
		/*
		 * IdentOrPixelSelectorExpression::= IDENTIFIER LSQUARE Selector RSQUARE
		 * 								   | IDENTIFIER
		 * @eg: test[a,b], notest
		 */
		matchAndConsume(IDENTIFIER);
		if (t.isKind(LSQUARE)) {
			matchAndConsume(LSQUARE);
			selector();
			matchAndConsume(RSQUARE);
		}
	}

	public void selector() throws SyntaxException {
		/*
		 * Selector ::= Expression COMMA Expression
		 */
		try {
			expression();
			matchAndConsume(COMMA);
			expression();
		} catch (Exception e) {
			throw new SyntaxException(t, lineStamp("selector"));
		}
	}

	public void functionApp() throws SyntaxException {
		/*
		 * FunctionApplication ::= FunctionName LPAREN Expression RPAREN |
		 * FunctionName LSQUARE Selector RSQUARE
		 * @eg: sin(a+b) | cart_x[x+y]
		 */
		functionName();
		if (t.isKind(LPAREN)) {
			matchAndConsume(LPAREN);
			expression();
			matchAndConsume(RPAREN);
		} else if (t.isKind(LSQUARE)) {
			matchAndConsume(LSQUARE);
			selector();
			matchAndConsume(RSQUARE);
		} else {
			throw new SyntaxException(t, lineStamp("function application"));
		}
	}

	public void functionName() throws SyntaxException {
		/*
		 * FunctionName ::= KW_sin | KW_cos | KW_atan | KW_abs | KW_cart_x |
		 * KW_cart_y | KW_polar_a | KW_polar_r
		 */
		if (t.isKinds(new Kind[] { KW_sin, KW_cos, KW_atan, KW_abs, KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r })) {
			consume();
		} else {
			throw new SyntaxException(t, lineStamp("function name"));
		}
	}

	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to
	 * get nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message = "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}
}
