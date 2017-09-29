package cop5556fa17;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.Scanner.Token;
import cop5556fa17.Parser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class SimpleParserTest_becky {

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
		//show(scanner);   //Display the Scanner
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
		String input = "prog "
		        + " int k; "
		        + " image[56/34,67/34] cgh ; "
		        + " image svf <- @(x+y+2/4*5*r*Z);"
		        + " image vkdj ; "
		        + " boolean k = true;"
		        + " int k = a + 76/9 * 67; "
		        + " url vgjvbhvj = \"vhvjhm\"; "
		        + " url fbdjob = vjsdlv ;"
		        + " file file_name = @(x+y+a*X) ; "
		        + " //this is a commment  ; ";
		show(input);
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		//show(scanner);   //Display the Scanner
		Parser parser = new Parser(scanner);  //
		parser.parse();
	}
	//positive test cases
	@Test
    public void statement() throws LexicalException, SyntaxException {
        String input = "prog sai [[x,y]] = (67/x*y);"
                + "sgf[[r,A]] = 534657+x ;"
                + "nath -> screen ;"
                + "gsf ->vfgh ;"
                + "reddy <- @x+y ;"
                + "dtgbg = u[534/x,67+y];";
        show(input);
        Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
        show(scanner);   //Display the Scanner
        Parser parser = new Parser(scanner);  //
        parser.parse();
    }
	@Test
    public void expressionabs() throws LexicalException, SyntaxException {
        String input = "abs(x/y);";
        show(input);
        Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
        //show(scanner);   //Display the Scanner
        Parser parser = new Parser(scanner);  //
        parser.expression();
    }
	@Test
    public void expressionsin() throws LexicalException, SyntaxException {
        String input = "sin[x/y,a/A];";
        show(input);
        Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
        //show(scanner);   //Display the Scanner
        Parser parser = new Parser(scanner);  //
        parser.expression();
    }
	@Test
    public void expressionsin1() throws LexicalException, SyntaxException {
        String input = "sin(DEF_X);";
        show(input);
        Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
        //show(scanner);   //Display the Scanner
        Parser parser = new Parser(scanner);  //
        parser.expression();
    }
	@Test
    public void expressioncos() throws LexicalException, SyntaxException {
        String input = "cos(DEF_Y);";
        show(input);
        Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
        //show(scanner);   //Display the Scanner
        Parser parser = new Parser(scanner);  //
        parser.expression();
    }
	@Test
    public void expressionatan() throws LexicalException, SyntaxException {
        String input = "atan[R,r] / abs(34+23+A-R/Y);";
        show(input);
        Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
        //show(scanner);   //Display the Scanner
        Parser parser = new Parser(scanner);  //
        parser.expression();
    }
	@Test
    public void expressioncartx() throws LexicalException, SyntaxException {
        String input = "cart_x[(45*(X)/Z)>54*Y,a+!A+R+r+y*Y-DEF_X /DEF_Y > (x/X)]; ";
        show(input);
        Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
        //show(scanner);   //Display the Scanner
        Parser parser = new Parser(scanner);  //
        parser.expression();
    }
	@Test
    public void expressioncarty() throws LexicalException, SyntaxException {
        String input = "cart_y(y*x);";
        show(input);
        Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
        //show(scanner);   //Display the Scanner
        Parser parser = new Parser(scanner);  //
        parser.expression();
    }
	@Test
    public void expressionpolar() throws LexicalException, SyntaxException {
        String input = "polar_a(54*76);";
        show(input);
        Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
        //show(scanner);   //Display the Scanner
        Parser parser = new Parser(scanner);  //
        parser.expression();
    }
	@Test
    public void expressionident() throws LexicalException, SyntaxException {
        String input = "gdhv[54,67]";
        show(input);
        Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
        //show(scanner);   //Display the Scanner
        Parser parser = new Parser(scanner);  //
        parser.expression();
    }
	@Test
    public void expressionunary() throws LexicalException, SyntaxException {
        String input = "x ? 323:y";
        show(input);
        Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
        //show(scanner);   //Display the Scanner
        Parser parser = new Parser(scanner);  //
        parser.expression();
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
		String input = "2+3 /4";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		//show(scanner);   
		Parser parser = new Parser(scanner);  
		parser.expression();  //Call expression directly.  
	}


	
	}

