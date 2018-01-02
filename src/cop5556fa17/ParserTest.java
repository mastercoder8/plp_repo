package cop5556fa17;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.AST.*;

import cop5556fa17.Parser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class ParserTest {

	// set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	// To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Simple test case with an empty program. This test expects an exception
	 * because all legal programs must have at least an identifier
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = ""; // The input is the empty string. Parsing should fail
		show(input); // Display the input
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and
														// initialize it
		show(scanner); // Display the tokens
		Parser parser = new Parser(scanner); //Create a parser
		thrown.expect(SyntaxException.class);
		try {
			ASTNode ast = parser.parse();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}


	@Test
	public void testNameOnly() throws LexicalException, SyntaxException {
		String input = "prog";  //Legal program with only a name
		show(input);            //display input
		Scanner scanner = new Scanner(input).scan();   //Create scanner and create token list
		show(scanner);    //display the tokens
		Parser parser = new Parser(scanner);   //create parser
		Program ast = parser.parse();          //parse program and get AST
		show(ast);                             //Display the AST
		assertEquals(ast.name, "prog");        //Check the name field in the Program object
		assertTrue(ast.decsAndStatements.isEmpty());   //Check the decsAndStatements list in the Program object.  It should be empty.
	}

	@Test
	public void testUnDec() throws LexicalException, SyntaxException {
		String input = "prog k = 5;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "prog");
	}
	@Test
	public void testDec1() throws LexicalException, SyntaxException {
		String input = "prog int k;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "prog"); 
		//This should have one Declaration_Variable object, which is at position 0 in the decsAndStatements list
		Declaration_Variable dec = (Declaration_Variable) ast.decsAndStatements
				.get(0);  
		assertEquals(KW_int, dec.type.kind);
		assertEquals("k", dec.name);
		assertNull(dec.e);
	}
	@Test
	public void testImage2() throws LexicalException, SyntaxException {
		String input = "prog image _abc;_abc -> SCREEN; _pqr [[x,y]] = a;";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.toString(),
				"Program [name=prog, decsAndStatements=[Declaration_Image [xSize=null, ySize=null, name=_abc, source=null], Statement_Out [name=_abc, sink=Sink_SCREEN [kind=KW_SCREEN]], Statement_Assign [lhs=name [name=_pqr, index=Index [e0=Expression_PredefinedName [name=KW_x], e1=Expression_PredefinedName [name=KW_y]]], e=Expression_PredefinedName [name=KW_a]]]]");
	}
	
	@Test
	public void testExpCompare() throws LexicalException, SyntaxException {
		String input = "a+b < d-c";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		Expression expAst = parser.expression();
		show(expAst);
		assertEquals(expAst.toString(),
				"Expression_Binary [e0=Expression_Binary [e0=Expression_PredefinedName [name=KW_a], op=OP_PLUS, e1=Expression_Ident [name=b]], op=OP_LT, e1=Expression_Binary [e0=Expression_Ident [name=d], op=OP_MINUS, e1=Expression_Ident [name=c]]]");
	}
	
	@Test
	public void testCaseDeclare() throws SyntaxException, LexicalException {
		String input = "class int testVar = x;";
		show(input);
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		Parser parser = new Parser(scanner);   
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "class");
		Declaration_Variable dec = (Declaration_Variable) ast.decsAndStatements.get(0);
		assertEquals(KW_int, dec.type.kind);
		assertEquals("testVar", dec.name);
		assertEquals(dec.e.getClass(), Expression_PredefinedName.class);
		assertEquals(KW_x, ((Expression_PredefinedName)(dec.e)).kind);
	}
	
	@Test
	public void testfunction() throws SyntaxException, LexicalException {
		String input = "sin(a+b)];";
		show(input);
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		Parser parser = new Parser(scanner);
		Expression exp = parser.expression();   //Parse the program
		show(exp);
		assertEquals(exp.getClass(), Expression_FunctionAppWithExprArg.class);
		Expression_FunctionAppWithExprArg exp_fn = (Expression_FunctionAppWithExprArg)exp;
		assertEquals(exp_fn.function, KW_sin);
		assertEquals(exp_fn.arg.getClass(), Expression_Binary.class);
		Expression_Binary exp_fn_arg = (Expression_Binary)exp_fn.arg;
		assertEquals(((Expression_PredefinedName)exp_fn_arg.e0).kind, KW_a);
		assertEquals(exp_fn_arg.op, OP_PLUS);
		assertEquals(((Expression_Ident)exp_fn_arg.e1).name, "b" );
	}
//	@Test
//	public void testimage() throws SyntaxException, LexicalException {
//		String input = "input = prog k [[ x,y ]]"; 
//		show(input);
//		Scanner scanner = new Scanner(input).scan();  
//		show(scanner);   
//		Parser parser = new Parser(scanner);
//		Program ast = parser.program();
//		show(ast);
////		assertEquals(ast.name, "prog");
////		Declaration_Image dec = (Declaration_Image) ast.decsAndStatements.get(0);
////		assertEquals("file1", dec.xSize);
////		assertEquals("file2", dec.ySize);
////		assertEquals("imageName", dec.name);
//
//	}
	@Test
	public void testORexpr() throws SyntaxException, LexicalException {
		String input = "Expr1 | Expr2 & A"; 
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		Expression ast = parser.expression();
		System.out.println(ast.toString());

	}

}
