package cop5556fa17;

import java.net.MalformedURLException;
import java.net.URL;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.TypeUtils;
import cop5556fa17.TypeUtils.Type;
public class TypeCheckVisitor implements ASTVisitor {
		SymbolTable symbTable = new SymbolTable();

		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}

		}		

	
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		String name = declaration_Variable.name;
		declaration_Variable.setTypeName(TypeUtils.getType(declaration_Variable.type));



		if(declaration_Variable.e!=null){
			declaration_Variable.e.visit(this, arg);
			if(declaration_Variable.getTypeName() != declaration_Variable.e.getTypeName()){
				throw new SemanticException(declaration_Variable.firstToken, "Type mismatch - " + name); 
			}
			declaration_Variable.setTypeName(declaration_Variable.e.getTypeName());
		}
		
		if(!symbTable.insert(name, declaration_Variable)){
			throw new SemanticException(declaration_Variable.firstToken, "Variable already declared - " + name); 
		}	
		
		
		return declaration_Variable;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		expression_Binary.e0.visit(this, arg);
		expression_Binary.e1.visit(this, arg);
		Type e0_type = expression_Binary.e0.getTypeName();
		Type e1_type = expression_Binary.e1.getTypeName();
//		System.out.println("Expression Binary : Before - " + expression_Binary.getTypeName() + "=" +  e0_type + expression_Binary.op.getName() + e1_type );
		if(e0_type ==  e1_type && expression_Binary.getTypeName() == null){
			if(expression_Binary.op.matches(new String[]{"!=","=="})){
				expression_Binary.setTypeName(Type.BOOLEAN);
			}
			else if(expression_Binary.op.matches(new String[]{">=", ">", "<=", "<"}) &&  e0_type == Type.INTEGER){
				expression_Binary.setTypeName(Type.BOOLEAN);
			}
			else if(expression_Binary.op.matches(new String[]{"&","|"}) && (e0_type == Type.INTEGER || e0_type == Type.BOOLEAN)){
				expression_Binary.setTypeName(e0_type);
			}
			else if(expression_Binary.op.matches(new String[]{"/", "-","+","^","*","%"}) && e0_type == Type.INTEGER){
				expression_Binary.setTypeName(Type.INTEGER);				
			}			
//			System.out.println("Expression Binary : After - " + expression_Binary.getTypeName() + "=" +  e0_type + expression_Binary.op.getName() + e1_type );
			return expression_Binary;
		}else{
			throw new SemanticException(expression_Binary.firstToken, "Binary Expression Type issue");
		}
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		expression_Unary.e.visit(this, arg);
		Type expr_type  = expression_Unary.e.getTypeName();
		if((expression_Unary.op == Kind.OP_EXCL) && (expr_type == Type.BOOLEAN || expr_type == Type.INTEGER)){
			expression_Unary.setTypeName(expr_type);
		}
		else if((expression_Unary.op == Kind.OP_PLUS || expression_Unary.op == Kind.OP_MINUS) && expr_type == Type.INTEGER){
			expression_Unary.setTypeName(Type.INTEGER);
		}
		if(expression_Unary.getTypeName() == null){
			throw new SemanticException(expression_Unary.firstToken, "Expression Unary not set");
		}
		return expression_Unary;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		index.e0.visit(this, arg);
		index.e1.visit(this, arg);
		if(! (index.e0.getTypeName() == Type.INTEGER && index.e1.getTypeName() == Type.INTEGER) ){
			throw new SemanticException(index.firstToken, "Expression Types should be integers");
		}
		index.setCartesian(!(index.e0.firstToken.isKind(Kind.KW_r) && index.e1.firstToken.isKind(Kind.KW_a)));
		System.out.println("index->>" + index.isCartesian());
		return index;
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		if(symbTable.lookUp(expression_PixelSelector.name) == null){
			throw new SemanticException(expression_PixelSelector.firstToken, "Variable not declared  - " + expression_PixelSelector.name);
		}
		
		Type nameType = symbTable.lookUp(expression_PixelSelector.name).getTypeName();
		if(nameType == Type.IMAGE){
			expression_PixelSelector.setTypeName(Type.INTEGER);	
		}
		else if(expression_PixelSelector.index == null){
			expression_PixelSelector.setTypeName(nameType);
		}
		if(expression_PixelSelector.index != null)
			expression_PixelSelector.index.visit(this, arg);
		
		if(expression_PixelSelector.getTypeName() == null){
			throw new SemanticException(expression_PixelSelector.firstToken, "Type issue Pixel Selector");
		}
		return expression_PixelSelector;
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		expression_Conditional.condition.visit(this, arg);
		expression_Conditional.trueExpression.visit(this, arg);
		expression_Conditional.falseExpression.visit(this, arg);
		if(expression_Conditional.condition.getTypeName() == Type.BOOLEAN && expression_Conditional.trueExpression.getTypeName().equals(expression_Conditional.falseExpression.getTypeName())){
			expression_Conditional.setTypeName(expression_Conditional.trueExpression.getTypeName());
			return expression_Conditional.getTypeName();
		}
		else{
			if(expression_Conditional.trueExpression.getTypeName().equals(expression_Conditional.falseExpression.getTypeName())){
				throw new SemanticException(expression_Conditional.firstToken, "Condition Expression - Condition should return boolean");
			}else{
				throw new SemanticException(expression_Conditional.firstToken, "Conditional Expression type mismatch");
			}
		}
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		if(!symbTable.insert(declaration_Image.name, declaration_Image)){
			throw new SemanticException(declaration_Image.firstToken, "Variable Already declared");
		}
		declaration_Image.setTypeName(Type.IMAGE);
		
		if(declaration_Image.xSize!=null && declaration_Image.ySize!= null){
			declaration_Image.xSize.visit(this, arg);
			declaration_Image.ySize.visit(this, arg);
			if(!(declaration_Image.xSize.getTypeName() == Type.INTEGER && 
			     declaration_Image.ySize.getTypeName() == Type.INTEGER))
				throw new SemanticException(declaration_Image.firstToken, "XSize, YSize should be Integers");
		}
		
		if(declaration_Image.source!=null)
			declaration_Image.source.visit(this, arg);
		
		return declaration_Image;
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		source_StringLiteral.setTypeName(isValidURL(source_StringLiteral.fileOrUrl)?Type.URL:Type.FILE);
//		System.out.println("Source type:" + source_StringLiteral.getTypeName());
		return source_StringLiteral;
	}

	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		source_CommandLineParam.setTypeName(null);
		source_CommandLineParam.paramNum.visit(this, arg);
//		source_CommandLineParam.setTypeName(source_CommandLineParam.paramNum.getTypeName());
//		source_CommandLineParam.paramNum.setTypeName(Type.INTEGER);
		if(!source_CommandLineParam.paramNum.getTypeName().equals(TypeUtils.Type.INTEGER)){
			throw new SemanticException(source_CommandLineParam.firstToken, "Source CommandLine Param should be of Type Int");
		}
		return source_CommandLineParam;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		if(symbTable.lookUp(source_Ident.name) == null)
			throw new SemanticException(source_Ident.firstToken, "Variable not declared - " + source_Ident.name);
		
		source_Ident.setTypeName(symbTable.lookUp(source_Ident.name).getTypeName());
//		System.out.println("Source Ident: " + source_Ident.getTypeName());
		if(!(source_Ident.getTypeName() == Type.FILE || source_Ident.getTypeName() ==  Type.URL)){
			throw new SemanticException(source_Ident.firstToken, "Source Ident should be of Type File/URL");
		}
		return source_Ident;
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		if(!symbTable.insert(declaration_SourceSink.name, declaration_SourceSink)){
			throw new SemanticException(declaration_SourceSink.firstToken, "Variable Already declared - " + declaration_SourceSink.name);
		}
		declaration_SourceSink.setTypeName(TypeUtils.getType(declaration_SourceSink.type));
		declaration_SourceSink.source.visit(this, arg);
//		System.out.println("Declaration SS: " + declaration_SourceSink.getTypeName() + " <- " + declaration_SourceSink.source.getTypeName());
		if(!(declaration_SourceSink.source.getTypeName() == declaration_SourceSink.getTypeName() || declaration_SourceSink.source !=null)){
			throw new SemanticException(declaration_SourceSink.firstToken, "Type Mismatch declaration source sink "+ declaration_SourceSink.source.getTypeName() + ", " +  declaration_SourceSink.getTypeName());
		}
		return declaration_SourceSink;
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		expression_IntLit.setTypeName(TypeUtils.Type.INTEGER);
//		System.out.println("Expression IntLit :" + expression_IntLit.getTypeName());
		return expression_IntLit.getTypeName();
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		expression_FunctionAppWithExprArg.arg.visit(this, arg);
		if(expression_FunctionAppWithExprArg.arg.getTypeName() == TypeUtils.Type.INTEGER) 
			expression_FunctionAppWithExprArg.setTypeName(TypeUtils.Type.INTEGER);
		return expression_FunctionAppWithExprArg;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		expression_FunctionAppWithIndexArg.arg.visit(this, arg);
		expression_FunctionAppWithIndexArg.setTypeName(TypeUtils.Type.INTEGER);
		return expression_FunctionAppWithIndexArg;
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		expression_PredefinedName.setTypeName(TypeUtils.Type.INTEGER);
		return expression_PredefinedName.getTypeName();
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		statement_Out.setDec(symbTable.lookUp(statement_Out.name));
		if(statement_Out.getDec()!=null){
			statement_Out.sink.visit(this, arg);
//			System.out.println("Statement Out: " + statement_Out.getDec().getTypeName() + " -> " + statement_Out.sink.getTypeName());
			Type nameType = statement_Out.getDec().getTypeName();
			Type sinkType = statement_Out.sink.getTypeName();
			
			if( ((nameType == Type.INTEGER || nameType == Type.BOOLEAN) && sinkType == Type.SCREEN) || 
				(nameType == Type.IMAGE && ( sinkType == Type.FILE || sinkType == Type.SCREEN)))
				return statement_Out.getTypeName();
			else{
				throw new SemanticException(statement_Out.firstToken, "Type Mismatch - statement_Out"); 
			}
		}
		else{
			throw new SemanticException(statement_Out.firstToken, "Declaration Not found");
		}
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		statement_In.setDec(symbTable.lookUp(statement_In.name));
		statement_In.source.visit(this, arg);
//		System.out.println("Statement In: " + statement_In.getDec().getTypeName() + " <- " + statement_In.source.getTypeName());
		return statement_In.getTypeName();

//		if(statement_In.getDec()!=null && statement_In.getDec().getTypeName().equals(statement_In.source.getTypeName())){
//			return statement_In.getTypeName();
//		}else{
//			throw new SemanticException(statement_In.firstToken, "Type mismatch statement_In - " + statement_In.getDec().getTypeName() + " <- " + statement_In.source.getTypeName());
//		}
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		statement_Assign.lhs.visit(this, arg);
		statement_Assign.e.visit(this, arg);
//		System.out.println("Statement Assign: " + statement_Assign.lhs.getTypeName() + " = " + statement_Assign.e.getTypeName());
		if(statement_Assign.lhs.getTypeName() == statement_Assign.e.getTypeName() || (statement_Assign.lhs.getTypeName().isType(Type.IMAGE) && statement_Assign.e.getTypeName().isType(Type.INTEGER))){
			statement_Assign.setCartesian(statement_Assign.lhs.isCartesian);
			return statement_Assign.getTypeName();
		}else{
			throw new SemanticException(statement_Assign.firstToken, "Type mismatch statement_assign - " + statement_Assign.lhs.getTypeName() + " = " + statement_Assign.e.getTypeName());
		}
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {

		if(symbTable.lookUp(lhs.name) == null){
			throw new SemanticException(lhs.firstToken, "Variable not declared - " + lhs.name);
		}
		lhs.declaration = symbTable.lookUp(lhs.name);
		lhs.setTypeName(lhs.declaration.getTypeName());
		if(lhs.index!=null){
			lhs.index.visit(this, arg);
			lhs.isCartesian = lhs.index.isCartesian();
		}
		return lhs;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		sink_SCREEN.setTypeName(Type.SCREEN);
		return sink_SCREEN.getTypeName();
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		if(symbTable.lookUp(sink_Ident.name) == null){
			throw new SemanticException(sink_Ident.firstToken, "Variable not declared - " + sink_Ident.name);
		}
		sink_Ident.setTypeName(symbTable.lookUp(sink_Ident.name).getTypeName());
		if(!sink_Ident.getTypeName().equals(TypeUtils.Type.FILE)){
			throw new SemanticException(sink_Ident.firstToken, "Sink Ident type not file");
		}
		return sink_Ident.getTypeName();
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		expression_BooleanLit.setTypeName(TypeUtils.Type.BOOLEAN);
		return expression_BooleanLit.getTypeName();
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
//		System.out.println("Expression Ident : " + expression_Ident.name + " Before - "  + expression_Ident.getTypeName());
		if(symbTable.lookUp(expression_Ident.name) == null){
			throw new SemanticException(expression_Ident.firstToken, "Variable Not Declared - " + expression_Ident.name);
		}
		expression_Ident.setTypeName(symbTable.lookUp(expression_Ident.name).getTypeName());
//		System.out.println("Expression Ident : " + expression_Ident.name + " After - "  + expression_Ident.getTypeName());

		return expression_Ident;
	}
	
	public boolean isValidURL(String urlStr) {
	    try {
	      new URL(urlStr);
	      return true;
	    }
	    catch (MalformedURLException e) {
	        return false;
	    }
	}
}
