package cop5556fa17;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.Parser.SyntaxException;

public class SimpleParserTest2 {
	

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Simple test case with an empty program.  This test 
	 * expects an SyntaxException because all legal programs must
	 * have at least an identifier
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = "";  //The input is the empty string.  This is not legal
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		Parser parser = new Parser(scanner);  //Create a parser
		thrown.expect(SyntaxException.class);
		try {
		parser.parse();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		}
	}

	
	/** Another example.  This is a legal program and should pass when 
	 * your parser is implemented.
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */

	@Test
	public void testDec1() throws LexicalException, SyntaxException {
		String input = "prog int k;";
		show(input);
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		Parser parser = new Parser(scanner);  //
		parser.parse();
	}
	

	/**
	 * This example invokes the method for expression directly. 
	 * Effectively, we are viewing Expression as the start
	 * symbol of a sub-language.
	 *  
	 * Although a compiler will always call the parse() method,
	 * invoking others is useful to support incremental development.  
	 * We will only invoke expression directly, but 
	 * following this example with others is recommended.  
	 * 
	 * @throws SyntaxException
	 * @throws LexicalException
	 */
	@Test
	public void expression1() throws SyntaxException, LexicalException {
		String input = "2";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);  
		parser.expression();  //Call expression directly.  
	}
	
	@Test
	public void expression2() throws SyntaxException, LexicalException {
		String input = "x != y";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);  
		parser.expression();  //Call expression directly.  
	}
	
	@Test
	public void expressioninfra() throws SyntaxException, LexicalException {
		String input = "a:b:c";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);  
		parser.expression();    
	}
	
	@Test
	public void andExpressionTest() throws SyntaxException, LexicalException {
		String input = "x / y - x * y >= x / y - x * y != x / y - x * y >= x / y - x * y & x / y - x * y >= x / y - x * y != x / y - x * y >= x / y - x * y";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.expression();
	}
	
	@Test
	public void orExpressionTest() throws SyntaxException, LexicalException {
		String input = "x / y - x * y >= x / y - x * y != x / y - x * y >= x / y - x * y & x / y - x * y >= x / y - x * y != x / y - x * y >= x / y - x * y & x / y - x * y >= x / y - x * y != x / y - x * y >= x / y - x * y & x / y - x * y >= x / y - x * y != x / y - x * y >= x / y - x * y";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.expression();
	}
	
	@Test
	public void expressionTest2() throws SyntaxException, LexicalException {
		String input = "a = (c > d) ? e : f ";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.expression();
	}
	
	@Test
	public void imageDeclarationTest() throws SyntaxException, LexicalException {
		String input = "image [ 3 + 4, 5 + 6 ]flag <- \"nikita\"";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.imageDeclare();
	}
	
	@Test
	public void expressionTest3() throws SyntaxException, LexicalException {
		String input = "flag1 + flag2";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.expression();
	}
	
//	@Test
//	public void identOrPixelSelectorExpressionTest() throws SyntaxException, LexicalException {
//		String input = "flag1";
//		show(input);
//		Scanner scanner = new Scanner(input).scan();
//		show(scanner);
//		Parser parser = new Parser(scanner);
//		parser.identOrPixelSelectorExpression();
//	}
	
//	@Test
//	public void identOrPixelSelectorExpressionTes2() throws SyntaxException, LexicalException {
//		String input = "flag1[1 + 2, 3 + 4]";
//		show(input);
//		Scanner scanner = new Scanner(input).scan();
//		show(scanner);
//		Parser parser = new Parser(scanner);
//		parser.identOrPixelSelectorExpression();
//	}
	
	@Test
	public void selectorTest() throws SyntaxException, LexicalException {
		String input = "1 + 2, 3 + 4";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.selector();
	}
	
	@Test
	public void unaryExpressionNotPlusMinusTest() throws SyntaxException, LexicalException {
		String input = "flag1[1 + 2, 3 + 4]";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.ueNotPlusMinus();
	}
	
	@Test
	public void unaryExpressionTest() throws SyntaxException, LexicalException {
		String input = "flag1[1 + 2, 3 + 4]";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.unaryExpression();
	}
	
	@Test
	public void sourceSinkDeclarationTest() throws SyntaxException, LexicalException {
		String input = "url flag = \"nikita\"";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.ssDeclare();
	}
	
	@Test
	public void functionApplicationTest1() throws SyntaxException, LexicalException {
		String input = "polar_r (num1 + num2)";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.functionApp();
	}
	
	@Test
	public void functionApplicationTest2() throws SyntaxException, LexicalException {
		String input = "cos [num1 + num2, num3 - num4]";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.functionApp();
	}
	
	@Test
	public void functionApplicationTest3() throws SyntaxException, LexicalException {
		String input = "sin [&, num3 - num4]";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			parser.functionApp();// Parse the program
		} catch (SyntaxException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void primaryTest1() throws SyntaxException, LexicalException {
		String input = "1234";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.primary();
	}
	
	@Test
	public void primaryTest2() throws SyntaxException, LexicalException {
		String input = "(num1 + num2)";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.primary();
	}
	
	@Test
	public void primaryTest3() throws SyntaxException, LexicalException {
		String input = "(num1 + num2";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			parser.primary();// Parse the program
		} catch (SyntaxException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void primaryTest4() throws SyntaxException, LexicalException {
		String input = "(&)";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			parser.primary();// Parse the program
		} catch (SyntaxException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void primaryTest5() throws SyntaxException, LexicalException {
		String input = "sin (num1 + num2)";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.primary();
	}
	
	@Test
	public void primaryTest6() throws SyntaxException, LexicalException {
		String input = "nikita (num1 + num2)";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			parser.primary();
		} catch (SyntaxException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void unaryExpressionNotPlusMinusTest1() throws SyntaxException, LexicalException {
		String input = "DEF_Y";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.ueNotPlusMinus();
	}
	
	@Test
	public void unaryExpressionNotPlusMinusTest2() throws SyntaxException, LexicalException {
		String input = "sin [";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			parser.ueNotPlusMinus();
		} catch (SyntaxException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void identOrPixelSelectorExpressionTest1() throws SyntaxException, LexicalException {
		String input = "nikita";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.identOrPixelSelectorExpression();
	}
	
	@Test
	public void identOrPixelSelectorExpressionTest2() throws SyntaxException, LexicalException {
		String input = "nikita[num1 + num2, num3 - num4]";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.identOrPixelSelectorExpression();
	}
	
	@Test
	public void identOrPixelSelectorExpressionTest3() throws SyntaxException, LexicalException {
		String input = "nikita[num1 + num2 num3 - num4]";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			parser.identOrPixelSelectorExpression();
		} catch (SyntaxException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void unaryExpressionNotPlusMinusTest3() throws SyntaxException, LexicalException {
		String input = "!-x";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.ueNotPlusMinus();
	}
	
	@Test
	public void unaryExpressionTest1() throws SyntaxException, LexicalException {
		String input = "+!x";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.unaryExpression();
	}
	
	@Test
	public void multExpressionTest1() throws SyntaxException, LexicalException {
		String input = "+!x%-3";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.multExpression();
	}
	
	@Test
	public void wholeTest1() throws SyntaxException, LexicalException {
		String input = "nikitaIdentifier";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.parse();
	}
	
	@Test
	public void wholeTest2() throws SyntaxException, LexicalException {
		String input = "nikita int flag = num1 + num2;";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.parse();
	}
	
	@Test
	public void wholeTest3() throws SyntaxException, LexicalException {
		String input = "nikita boolean flag = num1 + num2;";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.parse();
	}
	
	@Test
	public void wholeTest4() throws SyntaxException, LexicalException {
		String input = "nikita boolean flag;";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.parse();
	}
	
	@Test
	public void wholeTest5() throws SyntaxException, LexicalException {
		String input = "nikita boolean flag";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void wholeTest6() throws SyntaxException, LexicalException {
		String input = "nikita float flag";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void selectorTestDashboard1() throws SyntaxException, LexicalException {
		String input = "a*b,c*d";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.selector();
	}
	
	@Test
	public void selectorTestDashboard2() throws SyntaxException, LexicalException {
		String input = "(a*b,c*d";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void selectorTestDashboard3() throws SyntaxException, LexicalException {
		String input = "(a*b),c*d";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.selector();
	}
	
	@Test
	public void expressionDashboardRandom() throws SyntaxException, LexicalException {
		String input = "++++x";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.expression();
	}
	
	
	
	
	
	
}

