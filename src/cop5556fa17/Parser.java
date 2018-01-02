package cop5556fa17;


/*
 * @author: Jayachandra
 */

import java.util.*;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.AST.*;
import cop5556fa17.Parser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class Parser {

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

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * Main method called by compiler to parser input. Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}

	/*
	 * Just Consume a token
	 */
	public void consume() throws SyntaxException {
//		System.out.println("Consuming -  ==> " + t.kind.toString() + "\t|" + t.toString());
		t = scanner.nextToken();
	}
	
	private String lineStamp(String tok){
		return "Expected "+ tok +" at " + t.line + ":" + t.pos_in_line + ", found " + t.getText();
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
	Program program() throws SyntaxException {
		/*
		 * Program ::= IDENTIFIER ( Declaration SEMI | Statement SEMI )*
		 */
		Token firstToken = t;
		ArrayList<ASTNode> decAndStmts = new ArrayList<ASTNode>();
		if (t.isKind(EOF)) {
			throw new SyntaxException(t, "Empty Program");
		}
		
		matchAndConsume(IDENTIFIER);
		while (t.isKinds(new Kind[] { KW_int, KW_boolean, KW_image, KW_url, KW_file, IDENTIFIER })) {
			if (t.isKinds(new Kind[] { KW_int, KW_boolean, KW_image, KW_url, KW_file })) {
				decAndStmts.add(declare());
			} else if (t.isKind(IDENTIFIER)) {
				decAndStmts.add(statement());
			} else {
				throw new SyntaxException(t, lineStamp("program"));
			}
			matchAndConsume(SEMI);
		}
		return new Program(firstToken, firstToken, decAndStmts);
	}

	private Declaration declare() throws SyntaxException {
		/*
		 * Declaration :: = VariableDeclaration | ImageDeclaration | SourceSinkDeclaration
		 * @eg: int a=0 | image
		 */
		switch (t.kind) {
		case KW_int:
		case KW_boolean:
			return variableDeclare();
		case KW_image:
			return imageDeclare();
		case KW_url:
		case KW_file:
			return ssDeclare();
		default:
			throw new SyntaxException(t, lineStamp("declaration"));
		}
	}

	public Statement statement() throws SyntaxException {
		/*
		 * Statement ::= AssignmentStatement | ImageOutStatement | ImageInStatement
		 * AssignmentStatement ::= Lhs OP_ASSIGN Expression
		 * ImageInStatement ::= IDENTIFIER OP_LARROW Source 
		 * ImageOutStatement ::= IDENTIFIER OP_RARROW Sink 
		 * Lhs::= IDENTIFIER ( LSQUARE LhsSelector RSQUARE | EPS )
		 */
		Token firstToken = t;
		Index lhsindex = null;
		matchAndConsume(Kind.IDENTIFIER); // Common FIRST(Statement)
		switch (t.kind) {
		case OP_LARROW:
			matchAndConsume(OP_LARROW);
			Source src = source();
			return new Statement_In(firstToken, firstToken, src);
		case OP_RARROW:
			matchAndConsume(OP_RARROW);
			Sink sink = sink();
			return new Statement_Out(firstToken, firstToken, sink);
		case LSQUARE: // lhs
			matchAndConsume(LSQUARE);
			lhsindex = lhsSelector();
			matchAndConsume(RSQUARE);
//			matchAndConsume(OP_ASSIGN);
//			Expression e = expression();
//			return new Statement_Assign(firstToken, new LHS(firstToken, firstToken, lhsindex), e);
		case OP_ASSIGN: // lhs is EPS i.e Identifier only
			matchAndConsume(OP_ASSIGN);
			Expression expr = expression();
			return new Statement_Assign(firstToken, new LHS(firstToken, firstToken, lhsindex), expr);
		default:
			throw new SyntaxException(t, lineStamp("statement"));
		}

	}

	public Declaration_Variable variableDeclare() throws SyntaxException {
		/*
		 * VariableDeclaration ::= VarType IDENTIFIER ( OP_ASSIGN Expression | EPS ) 
		 * VarType ::= KW_int | KW_boolean
		 * @eg: int a = 0
		 */
		Token firstToken = t;
		Expression expr = null;
		consume(); // VarType
		Token name = t;
		matchAndConsume(Kind.IDENTIFIER);
		if (t.kind == OP_ASSIGN) {
			matchAndConsume(Kind.OP_ASSIGN);
			expr = expression();
		}
		// else EPS
		return new Declaration_Variable(firstToken, firstToken, name, expr);
	}

	public Declaration_Image imageDeclare() throws SyntaxException {
		/*
		 * ImageDeclaration ::= KW_image (LSQUARE Expression COMMA Expression RSQUARE | EPS) IDENTIFIER ( OP_LARROW Source | EPS )
		 * @eg image [sin(x), cos(x)] equals <- source
		 */
		Token firstToken = t;
		Expression xSize = null, ySize = null;
		Source src = null;
		matchAndConsume(Kind.KW_image);
		if (t.kind == Kind.LSQUARE) {
			matchAndConsume(Kind.LSQUARE);
			xSize = expression();
			matchAndConsume(Kind.COMMA);
			ySize = expression();
			matchAndConsume(Kind.RSQUARE);
		}
		Token name = t;
		matchAndConsume(Kind.IDENTIFIER);
		if (t.kind == Kind.OP_LARROW) {
			matchAndConsume(Kind.OP_LARROW);
			src = source();
		}
		
		return new Declaration_Image(firstToken, xSize, ySize, name, src);
	}

	public Declaration_SourceSink ssDeclare() throws SyntaxException {
		/*
		 * SourceSinkDeclaration ::= SourceSinkType IDENTIFIER OP_ASSIGN Source
		 * SourceSinkType := KW_url | KW_file
		 */
		Token firstToken = t;
		consume(); // SourceSinkType
		Token name = t;
		matchAndConsume(new Kind[] { IDENTIFIER, OP_ASSIGN });
		Source source = source();
		return new Declaration_SourceSink(firstToken, firstToken, name, source);
	}

	public Source source() throws SyntaxException {
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
		Token firstToken = t;
		switch (t.kind) {
		case STRING_LITERAL:
			matchAndConsume(Kind.STRING_LITERAL);
			return new Source_StringLiteral(firstToken, firstToken.getText());
		case OP_AT:
			matchAndConsume(Kind.OP_AT);
			Expression paramNum = expression();
			return new Source_CommandLineParam(firstToken, paramNum);
		case IDENTIFIER:
			matchAndConsume(Kind.IDENTIFIER);
			return new Source_Ident(firstToken, firstToken);
		default:
			throw new SyntaxException(t, lineStamp("source"));
		}
	}

	private Sink sink() throws SyntaxException {
		/*
		 * Sink ::= IDENTIFIER | KW_SCREEN
		 */
		Token firstToken = t;
		if (t.kind == IDENTIFIER) {
			matchAndConsume(IDENTIFIER);
			return new Sink_Ident(firstToken, firstToken);
		} else if (t.kind == KW_SCREEN) {
			matchAndConsume(KW_SCREEN);
			return new Sink_SCREEN(firstToken);
		} else {
			throw new SyntaxException(t, lineStamp("sink"));
		}
	}

	public Index lhsSelector() throws SyntaxException {
		/*
		 * LhsSelector ::= LSQUARE ( XySelector | RaSelector ) RSQUARE
		 * XySelector ::= KW_x COMMA KW_y RaSelector ::= KW_r COMMA KW_A
		 */
		Index ind = null;
		matchAndConsume(LSQUARE);
		if (t.isKind(KW_x)) { // XySelector
			ind = xySelector();
		} else if (t.isKind(KW_r)) { // RaSelector
			ind = raSelector();	
		} // else EPS
		matchAndConsume(RSQUARE);
		return ind;
	}
	
	public Index xySelector() throws SyntaxException{
		Token firstToken = t;
		matchAndConsume(new Kind[] { KW_x, COMMA });
		Token secondToken = t;
		matchAndConsume(KW_y);
		return new Index(firstToken, new Expression_PredefinedName(firstToken, firstToken.kind), new Expression_PredefinedName(secondToken, secondToken.kind));
	}
	
	public Index raSelector() throws SyntaxException{
		Token firstToken = t;
		matchAndConsume(new Kind[] { KW_r, COMMA });
		Token secondToken = t;
		matchAndConsume(KW_a);
		return new Index(firstToken, new Expression_PredefinedName(firstToken, firstToken.kind), new Expression_PredefinedName(secondToken, secondToken.kind));
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
	Expression expression() throws SyntaxException {
		/*
		 * Expression ::= OrExpression OP_Q Expression OP_COLON Expression 
		 * 				| OrExpression
		 */
		Token firstToken = t;
		Expression condition = orExpression();
		if (t.isKind(OP_Q)) {
			matchAndConsume(OP_Q);
			Expression trueExpression = expression();
			matchAndConsume(OP_COLON);
			Expression falseExpression = expression();
			return new Expression_Conditional(firstToken, condition, trueExpression, falseExpression);
		}
		// else EPS
		return condition;
	}

	private Expression orExpression() throws SyntaxException {
		/*
		 * OrExpression ::= AndExpression ( OP_OR AndExpression)*
		 */
		Token firstToken = t;
		Expression e0 = andExpression();
		while (t.isKind(OP_OR)) {
			Token op = t;
			matchAndConsume(OP_OR);
			e0 = new Expression_Binary(firstToken, e0, op, andExpression());
		}
		return e0;
	}

	private Expression andExpression() throws SyntaxException {
		/*
		 * AndExpression ::= EqExpression ( OP_AND EqExpression )*
		 */
		Token firstToken = t;
		Expression e0 = eqExpression();
		while (t.isKind(OP_AND)) {
			Token op = t;
			matchAndConsume(OP_AND);
			e0 = new Expression_Binary(firstToken, e0, op, eqExpression());
		}
		return e0;
	}

	private Expression eqExpression() throws SyntaxException {
		/*
		 * EqExpression ::= RelExpression ( (OP_EQ | OP_NEQ ) RelExpression )*
		 */
		Token firstToken = t;
		Expression e0 = relExpression();//, e1 = null;
		while (t.isKind(OP_EQ) || t.isKind(OP_NEQ)) {
			Token op = t;
			consume(); // (OP_EQ | OP_NEQ )
			e0 = new Expression_Binary(firstToken, e0, op, relExpression());
		}
		return e0;
	}

	private Expression relExpression() throws SyntaxException {
		/*
		 * RelExpression ::= AddExpression ( ( OP_LT | OP_GT | OP_LE | OP_GE )  AddExpression)*
		 */
		Token firstToken = t;
		Expression e0 = addExpression();
		while (t.isKinds(new Kind[] { OP_LT, OP_GT, OP_LE, OP_GE })) {
			Token op = t;
			consume(); // OP_LT | OP_GT | OP_LE | OP_GE
			e0 = new Expression_Binary(firstToken, e0, op, addExpression());
		}
		return e0;
	}

	private Expression addExpression() throws SyntaxException {
		/*
		 * AddExpression ::= MultExpression ( (OP_PLUS | OP_MINUS ) MultExpression )*
		 */
		Token firstToken = t;
		Expression e0 = multExpression();
		while (t.isKind(OP_PLUS) || t.isKind(OP_MINUS)) {
			Token op = t;
			consume();
			e0 = new Expression_Binary(firstToken, e0, op, multExpression());
		}
		return e0;
	}

	public Expression multExpression() throws SyntaxException {
		/*
		 * MultExpression := UnaryExpression ( ( OP_TIMES | OP_DIV | OP_MOD ) UnaryExpression )*
		 */
		Token firstToken = t;
		Expression e0 = unaryExpression();
		while (t.isKinds(new Kind[] { OP_TIMES, OP_DIV, OP_MOD })) {
			Token op = t;
			consume();
			e0 = new Expression_Binary(firstToken, e0, op, unaryExpression());
		}
		return e0;
	}

	public Expression unaryExpression() throws SyntaxException {
		/*
		 * UnaryExpression ::= OP_PLUS UnaryExpression 
		 * 					| OP_MINUS UnaryExpression
		 *  				| UnaryExpressionNotPlusMinus
		 */
		Token firstToken = t;
		if (t.isKind(OP_PLUS) || t.isKind(OP_MINUS)) {
			consume();
			return new Expression_Unary(firstToken, firstToken, unaryExpression());
		} else {
			return ueNotPlusMinus();
		}
	}

	public Expression ueNotPlusMinus() throws SyntaxException {
		/*
		 * UnaryExpressionNotPlusMinus ::= OP_EXCL UnaryExpression | Primary | IdentOrPixelSelectorExpression 
		 * 								| KW_x | KW_y | KW_r | KW_a | KW_X | KW_Y | KW_Z | KW_A | KW_R | KW_DEF_X | KW_DEF_Y
		 * @eg: ! 
		 */
		Token firstToken = t;
		if (t.isKinds(new Kind[] { KW_x, KW_y, KW_r, KW_a, KW_X, KW_Y, KW_Z, KW_A, KW_R, KW_DEF_X, KW_DEF_Y })) {
			consume();
			return new Expression_PredefinedName(firstToken, firstToken.kind);
		} else if (t.isKind(OP_EXCL)) {
			matchAndConsume(OP_EXCL);
			return new Expression_Unary(firstToken, firstToken, unaryExpression());
		} else if (t.isKind(IDENTIFIER)) {
			return identOrPixelSelectorExpression();
		} else if (t.isKinds(new Kind[] { INTEGER_LITERAL, BOOLEAN_LITERAL, LPAREN, KW_sin, KW_cos, KW_atan, KW_abs,
				KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r })) {
			return primary();
		} else {
			throw new SyntaxException(t, lineStamp("UnaryExpressionNotPlusMinus"));
		}
	}

	public Expression primary() throws SyntaxException {
		/*
		 * Primary ::= INTEGER_LITERAL 
		 * 			 | LPAREN Expression RPAREN 
		 * 			 | FunctionApplication
		 *  		 | BOOLEAN_LITERAL
		 */
		Token firstToken = t;
		if (t.isKind(INTEGER_LITERAL)){
			consume();
			return new Expression_IntLit(firstToken, Integer.parseInt(firstToken.getText()));
		}else if(t.isKind(BOOLEAN_LITERAL)) {
			consume();
			return new Expression_BooleanLit(firstToken, firstToken.getText().equals("true"));
		} else if (t.isKind(LPAREN)) {
			matchAndConsume(LPAREN);
			Expression e = expression();
			matchAndConsume(RPAREN);
			return e;
		} else if (t.isKinds(
				new Kind[] { KW_sin, KW_cos, KW_atan, KW_abs, KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r })) {
			return functionApp();
		} else {
			throw new SyntaxException(t, lineStamp("primary"));
		}
	}

	public Expression identOrPixelSelectorExpression() throws SyntaxException {
		/*
		 * IdentOrPixelSelectorExpression::= IDENTIFIER LSQUARE Selector RSQUARE
		 * 								   | IDENTIFIER
		 * @eg: test[a,b], notest
		 */
		Token firstToken = t;
		matchAndConsume(IDENTIFIER);
		if (t.isKind(LSQUARE)) {
			matchAndConsume(LSQUARE);
			Index index = selector();
			matchAndConsume(RSQUARE);
			return new Expression_PixelSelector(firstToken, firstToken, index);
		}
		return new Expression_Ident(firstToken, firstToken);
	}

	public Index selector() throws SyntaxException {
		/*
		 * Selector ::= Expression COMMA Expression
		 */
		Token firstToken = t;
		try {
			Expression e0 = expression();
			matchAndConsume(COMMA);
			Expression e1 = expression();
			return new Index(firstToken, e0, e1);
		} catch (Exception e) {
			throw new SyntaxException(t, lineStamp("selector"));
		}
	}

	public Expression functionApp() throws SyntaxException {
		/*
		 * FunctionApplication ::= FunctionName LPAREN Expression RPAREN |
		 * FunctionName LSQUARE Selector RSQUARE
		 * @eg: sin(a+b) | cart_x[x+y]
		 */
		Token firstToken = t;
		Index arg = null;
		functionName();
		if (t.isKind(LPAREN)) {
			matchAndConsume(LPAREN);
			Expression e0 = expression();
			matchAndConsume(RPAREN);
			return new Expression_FunctionAppWithExprArg(firstToken, firstToken.kind, e0);
		} else if (t.isKind(LSQUARE)) {
			matchAndConsume(LSQUARE);
			arg = selector();
			matchAndConsume(RSQUARE);
			return new Expression_FunctionAppWithIndexArg(firstToken, firstToken.kind, arg);
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
