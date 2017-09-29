/**
 * /**
 * JUunit tests for the Scanner for the class project in COP5556 Programming Language Principles 
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

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.Scanner.Token;

import static cop5556fa17.Scanner.Kind.*;

public class ScannerTest {

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
	 *Retrieves the next token and checks that it is an EOF token. 
	 *Also checks that this was the last token.
	 *
	 * @param scanner
	 * @return the Token that was retrieved
	 */
	
	Token checkNextIsEOF(Scanner scanner) {
		Scanner.Token token = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF, token.kind);
		assertFalse(scanner.hasTokens());
		return token;
	}


	/**
	 * Retrieves the next token and checks that its kind, position, length, line, and position in line
	 * match the given parameters.
	 * 
	 * @param scanner
	 * @param kind
	 * @param pos
	 * @param length
	 * @param line
	 * @param pos_in_line
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length, int line, int pos_in_line) {
		Token t = scanner.nextToken();
//		System.out.println("Token:" +  t.toString());
		assertEquals(scanner.new Token(kind, pos, length, line, pos_in_line), t);
		return t;
	}

	/**
	 * Retrieves the next token and checks that its kind and length match the given
	 * parameters.  The position, line, and position in line are ignored.
	 * 
	 * @param scanner
	 * @param kind
	 * @param length
	 * @return  the Token that was retrieved
	 */
	Token check(Scanner scanner, Scanner.Kind kind, int length) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(length, t.length);
		return t;
	}

	/**
	 * Simple test case with a (legal) empty program
	 *   
	 * @throws LexicalException
	 */
	@Test
	public void testEmpty() throws LexicalException {
		String input = "";  //The input is the empty string.  This is legal
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
	}
	
	/**
	 * Test illustrating how to put a new line in the input program and how to
	 * check content of tokens.
	 * 
	 * Because we are using a Java String literal for input, we use \n for the
	 * end of line character. (We should also be able to handle \n, \r, and \r\n
	 * properly.)
	 * 
	 * Note that if we were reading the input from a file, as we will want to do 
	 * later, the end of line character would be inserted by the text editor.
	 * Showing the input will let you check your input is what you think it is.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testSemi() throws LexicalException {
		String input = ";;\n;;";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, SEMI, 3, 1, 2, 1);
		checkNext(scanner, SEMI, 4, 1, 2, 2);
		checkNextIsEOF(scanner);
	}
	
	/**
	 * This example shows how to test that your scanner is behaving when the
	 * input is illegal.  In this case, we are giving it a String literal
	 * that is missing the closing ".  
	 * 
	 * Note that the outer pair of quotation marks delineate the String literal
	 * in this test program that provides the input to our Scanner.  The quotation
	 * mark that is actually included in the input must be escaped, \".
	 * 
	 * The example shows catching the exception that is thrown by the scanner,
	 * looking at it, and checking its contents before rethrowing it.  If caught
	 * but not rethrown, then JUnit won't get the exception and the test will fail.  
	 * 
	 * The test will work without putting the try-catch block around 
	 * new Scanner(input).scan(); but then you won't be able to check 
	 * or display the thrown exception.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void failUnclosedStringLiteral() throws LexicalException {
		String input = "\" greetings  ";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(13,e.getPos());
			throw e;
		}
	}
	
	@Test
	public void testDigit() throws LexicalException {
		String input = "123 ";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 3, 1, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testFailDigit() throws LexicalException{
		String input = " 12388888888888";
		show(input);
		thrown.expect(LexicalException.class);
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(1,e.getPos());
			throw e;
		}
	}
	
	@Test
	public void testFailString() throws LexicalException{
		String input = "\"Marco123\\l\"//Comment";
		show(input);
		thrown.expect(LexicalException.class);
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(10,e.getPos());
			throw e;
		}
	}
	
	@Test
	public void testFailStringEscape() throws LexicalException{
		String input =  "\"a\nb\"";
		show(input);
		thrown.expect(LexicalException.class);
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(2,e.getPos());
			throw e;
		}
	}
	
	@Test
	public void testComment() throws LexicalException {
		String input = "\n//Comment";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testAssign() throws LexicalException {
		String input = "A = DEF_Y(Y)";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, KW_A, 0,1,1,1);
		checkNext(scanner, OP_ASSIGN,2,1,1,3);
		checkNext(scanner, KW_DEF_Y, 4,5,1,5);
		checkNext(scanner, LPAREN, 9,1,1,10);
		checkNext(scanner, KW_Y, 10,1,1,11);
		checkNext(scanner,RPAREN, 11,1,1,12);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testComparision() throws LexicalException {
		String input = "X<=Y";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, KW_X, 0,1,1,1);
		checkNext(scanner, OP_LE, 1,2,1,2);
		checkNext(scanner, KW_Y, 3,1,1,4);

		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testStringLit() throws LexicalException {
		String input = "\"PLP\"";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, STRING_LITERAL, 0,5,1,1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testSinFunction() throws LexicalException {
		String input = "123 & sin(A,B)//Comment";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL,0,3,1,1);
		checkNext(scanner, OP_AND , 4,1,1,5);
		checkNext(scanner, KW_sin, 6,3,1,7);
		checkNext(scanner, LPAREN, 9,1,1,10);
		checkNext(scanner, KW_A, 10,1,1,11);
		checkNext(scanner, COMMA, 11,1,1,12);
		checkNext(scanner, IDENTIFIER, 12, 1, 1, 13);
		checkNext(scanner, RPAREN, 13, 1, 1, 14);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testZeroDigit() throws LexicalException {
		String input = "01BRAVO123//Comment";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner,INTEGER_LITERAL,0,1,1,1);
		checkNext(scanner, INTEGER_LITERAL,1,1,1,2);
		checkNext(scanner, IDENTIFIER, 2, 8, 1, 3);
//		checkNext(scanner, C, 0, 5, 1, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testStringEscape() throws LexicalException {
		String input = "9\"Marco123\\n\"//Comment";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner,INTEGER_LITERAL,0,1,1,1);
		checkNext(scanner, STRING_LITERAL,1,12,1,2);
//		checkNext(scanner, C, 0, 5, 1, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testSpecial1() throws LexicalException{
		String input =  " \n\"a\\nb\";";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);	
		checkNext(scanner, STRING_LITERAL,2,6,2,1);
		checkNext(scanner, SEMI, 8,1,2,7);
		checkNextIsEOF(scanner);

	}
	@Test
	public void testSpecial2() throws LexicalException{
		String input ="\" \\\\ \"";// "\"a\tb\"";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);	
		checkNext(scanner, STRING_LITERAL,0,6,1,1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testGeneric() throws LexicalException {
		String input = "02Fire_$67";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner,INTEGER_LITERAL,0,1,1,1);
		checkNext(scanner, INTEGER_LITERAL,1,1,1,2);
		checkNext(scanner, IDENTIFIER, 2, 8, 1, 3);
//		checkNext(scanner, C, 0, 5, 1, 1);
		checkNextIsEOF(scanner);
	}

	@Test
	public void testCommentCode() throws LexicalException {
		String input = "Hello123//Comment\nA=abs( y)\n";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 8, 1, 1);
		checkNext(scanner, KW_A,18,1,2,1);
		checkNext(scanner, OP_ASSIGN,19,1,2,2);
		checkNext(scanner, KW_abs,20,3,2,3);
		checkNext(scanner, LPAREN,23,1,2,6);
		checkNext(scanner, KW_y, 25,1,2,8);
		checkNext(scanner, RPAREN, 26,1,2,9);
		checkNextIsEOF(scanner);
	}

}
