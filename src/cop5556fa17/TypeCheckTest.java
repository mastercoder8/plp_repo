package cop5556fa17;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.*;

import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.Parser.SyntaxException;
import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeCheckVisitor.SemanticException;

import static cop5556fa17.Scanner.Kind.*;

public class TypeCheckTest {

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
	 * Scans, parses, and type checks given input String.
	 * 
	 * Catches, prints, and then rethrows any exceptions that occur.
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		try {
			Scanner scanner = new Scanner(input).scan();
			ASTNode ast = new Parser(scanner).parse();
			show(ast);
			ASTVisitor v = new TypeCheckVisitor();
			ast.visit(v, null);
		} catch (Exception e) {
			show(e);
			throw e;
		}
	}

	/**
	 * Simple test case with an almost empty program.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSmallest() throws Exception {
		String input = "n"; //Smallest legal program, only has a name
		show(input); // Display the input
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and
														// initialize it
		show(scanner); // Display the Scanner
		Parser parser = new Parser(scanner); // Create a parser
		ASTNode ast = parser.parse(); // Parse the program
		TypeCheckVisitor v = new TypeCheckVisitor();
		String name = (String) ast.visit(v, null);
		show("AST for program " + name);
		show(ast);
	}



	
	/**
	 * This test should pass with a fully implemented assignment
	 * @throws Exception
	 */
	 @Test
	 public void testDec1() throws Exception {
	 String input = "prog int k = 42;";
	 typeCheck(input);
	 }
	 
	 /**
	  * This program does not declare k. The TypeCheckVisitor should
	  * throw a SemanticException in a fully implemented assignment.
	  * @throws Exception
	  */
	 @Test
	 public void testUndec() throws Exception {
	 String input = "prog k = 42;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 @Test
	 public void testfail1() throws Exception{
		String input = "prog image _abc;_abc <- \"SCREEN\"; _pqr [[x,y]] = a;";
		thrown.expect(SemanticException.class);
		typeCheck(input);
	 }
	 
	 @Test
	 public void testfail2() throws Exception{
		String input = "prog image _abc;_abc <- \"SCREEN\"; _pqr [[x,y]] = a;";
		thrown.expect(SemanticException.class);
		typeCheck(input);
	 }
	 
	 @Test
	 public void testfail3() throws Exception{
		String input = "prog image _abc; int k = sin(a+b);";
		thrown.expect(SemanticException.class);
		typeCheck(input);
	 }
	 @Test
	 public void testfail4() throws Exception{
		String input = "prog image _jc;_jc = 4; int _a = 5;";
		thrown.expect(SemanticException.class);
		typeCheck(input);
	 }
	 
	 @Test
	 public void testfail5() throws Exception{
		String input = "prog image _abc;_abc <- \"C:\\\\jc\\\\image.png\"; int _a = 5;";
		thrown.expect(SemanticException.class);
		typeCheck(input);
	 }
	 
	 @Test
	 public void test1working() throws Exception{
		String input = "prog image _abc;_abc <- \"C:\\\\jc\\\\image.png\"; int _a = 5;";
		thrown.expect(SemanticException.class);

		typeCheck(input);
	 }
	 
	 @Test
	 public void test10() throws Exception{
		 //Statement out check
		String input = "prog image _abc;_abc -> SCREEN; int _a = 5;";
		typeCheck(input);
	 }
	 
	 @Test
	 public void test9() throws Exception{
		 String input ="prog int _j = 10; int _c = 20; int max = (_c > _j)?_c:_j;";
		 typeCheck(input);
	 }
	 
	 @Test
	 public void testTAFailA4() throws Exception{
		 String input = "prog file f = \"file_name\"; f <- \"file_name2\" ;";
		 typeCheck(input);
	 }
	 @Test
	 public void test8extra() throws Exception{
		 String input ="prog int _x = 10; int _y = 20; int max = ((_x > _y) & max>0)?_x:_y;";
		 thrown.expect(SemanticException.class);
		 typeCheck(input);
	 }
	 @Test
	 public void test8() throws Exception{
		 String input ="prog int _x = 10; int _y = 20; int max = 10; max = ((_x > _y) & max>0)?_x:_y;";
		 typeCheck(input);
	 }
	 
	 @Test
	 public void test5() throws Exception{
		String input = "prog int k = 1; int k2 = k+1;";
		typeCheck(input);
	 }
	 
	 @Test
	 public void test3() throws Exception{
		String input = "prog image _abc; int k = sin(10);";
		typeCheck(input);
	 }
	 
	 
	 @Test
	 public void test4() throws Exception{
		String input = "prog image _abc; int _a= 4; int b=3; int k = sin(_a+b);";
		typeCheck(input);
	 }

}
