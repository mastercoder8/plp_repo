package cop5556fa17;

import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
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
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.Scanner.Kind;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.ImageFrame;
import cop5556fa17.ImageSupport;


import static cop5556fa17.Scanner.Kind.*;
public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */


	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		fillmap();
	}
	public void fillmap(){
		kind2Op.put(OP_GE, IFLT);
		kind2Op.put(OP_GT, IFLE);
		kind2Op.put(OP_LE, IFGT);
		kind2Op.put(OP_LT, IFGE);
		kind2Op.put(OP_AND,IAND);
		kind2Op.put(OP_OR, IOR);
		kind2Op.put(OP_DIV,IDIV);
		kind2Op.put(OP_TIMES, Opcodes.IMUL);
		kind2Op.put(OP_PLUS, Opcodes.IADD);
		kind2Op.put(OP_MINUS, Opcodes.ISUB);
		kind2Op.put(OP_MOD, Opcodes.IREM);
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	FieldVisitor fv;

	int DEF_X = 256, DEF_Y = 256, Z = 16777215; 
	HashMap<Kind, Integer> kind2Op = new HashMap<>();
	
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;  
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();		
		//add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);		
		// if GRADE, generates code to add string to log
//		CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		//  and instructions to main method, respectivley
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
//		CodeGenUtils.genLog(GRADE, mv, "leaving main");
		
		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);
		
		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		
		//handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("x", "I", null, mainStart, mainEnd, 1);
		mv.visitLocalVariable("y", "I", null, mainStart, mainEnd, 2);
		mv.visitLocalVariable("X", "I", null, mainStart, mainEnd, 3);
		mv.visitLocalVariable("Y", "I", null, mainStart, mainEnd, 4);
		mv.visitLocalVariable("r", "I", null, mainStart, mainEnd, 5);
		mv.visitLocalVariable("a", "I", null, mainStart, mainEnd, 6);
		mv.visitLocalVariable("R", "I", null, mainStart, mainEnd, 7);
		mv.visitLocalVariable("A", "I", null, mainStart, mainEnd, 8);
		
		//Sets max stack size and number of local vars.
		//Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		//asm will calculate this itself and the parameters are ignored.
		//If you have trouble with failures in this routine, it may be useful
		//to temporarily set the parameter in the ClassWriter constructor to 0.
		//The generated classfile will not be correct, but you will at least be
		//able to see what is in it.


		mv.visitMaxs(0, 0);
		
		//terminate construction of main method
		mv.visitEnd();
		
		//terminate class construction
		cw.visitEnd();

		//generate classfile as byte array and return
		return cw.toByteArray();
	}

//	public void localVars(){
//	}
	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		String type_var = "";
		if(declaration_Variable.getTypeName() == Type.INTEGER){
			type_var = "I";
			fv = cw.visitField(ACC_STATIC, declaration_Variable.name, "I", null, null);
		}
		else if(declaration_Variable.getTypeName() == Type.BOOLEAN) {
			type_var = "Z";
            fv = cw.visitField(ACC_STATIC, declaration_Variable.name, "Z", null, null);
        }
		fv.visitEnd();
		if(declaration_Variable.e!=null){
			declaration_Variable.e.visit(this, arg);			
			mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.name,  type_var);
		}
		return null;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
        Label next = new Label();
        Expression e0 = expression_Binary.e0;
		e0.visit(this, arg);
		Expression e1 = expression_Binary.e1;
		e1.visit(this, arg);
		switch(expression_Binary.op){
			case OP_PLUS:
				mv.visitInsn(IADD);
				break;
			case OP_MINUS:
				mv.visitInsn(ISUB);
				break;
			case OP_DIV:
				mv.visitInsn(IDIV);
				break;
			case OP_TIMES:
				mv.visitInsn(IMUL);
				break;
			case OP_MOD:
				mv.visitInsn(IREM);
			case OP_POWER:
				// TO BE IMPLEMENTED
				break;
			case OP_OR:
				mv.visitInsn(IOR);
				break;
			case OP_AND:
				mv.visitInsn(IAND);
				break;
			case OP_GT:
				Label gt = new Label();
				mv.visitJumpInsn(IF_ICMPLE, gt);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, next);
				mv.visitLabel(gt);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, next);
				break;
			case OP_GE:
				Label ge = new Label();
				mv.visitJumpInsn(IF_ICMPLT, ge);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, next);
				mv.visitLabel(ge);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, next);
				break;
			case OP_LT:
				Label lt = new Label();
				mv.visitJumpInsn(IF_ICMPGE, lt);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, next);
				mv.visitLabel(lt);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, next);
				break;
			case OP_LE:
				Label le = new Label();
				mv.visitJumpInsn(IF_ICMPGT, le);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, next);
				mv.visitLabel(le);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, next);
				break;
			case OP_EQ:
				Label eq = new Label();
                mv.visitJumpInsn(IF_ICMPNE, eq);
                mv.visitInsn(ICONST_1);
                mv.visitJumpInsn(GOTO, next);
                mv.visitLabel(eq);
                mv.visitInsn(ICONST_0);
                mv.visitJumpInsn(GOTO, next);
                break;
			case OP_NEQ:
				Label ne = new Label();
                mv.visitJumpInsn(IF_ICMPEQ, ne);
                mv.visitInsn(ICONST_1);
                mv.visitJumpInsn(GOTO, next);
                mv.visitLabel(ne);
                mv.visitInsn(ICONST_0);
                mv.visitJumpInsn(GOTO, next);
                break;
			default:
		}
		mv.visitLabel(next);
//		CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.getTypeName());
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		expression_Unary.e.visit(this, arg);
		if(expression_Unary.op == Kind.OP_PLUS){
		}else if(expression_Unary.op == Kind.OP_MINUS){
			mv.visitInsn(INEG);
		}
		else if(expression_Unary.op == Kind.OP_EXCL){
			if(expression_Unary.getTypeName() == Type.INTEGER){
				mv.visitLdcInsn(Integer.MAX_VALUE);
				mv.visitInsn(IXOR);
			}else{
				Label true_label = new Label();
				Label false_label = new Label();
				mv.visitJumpInsn(IFNE, false_label);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, true_label);
				mv.visitLabel(false_label);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(true_label);
			}
		}
//		CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.getTypeName());
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
		index.e0.visit(this, arg);
		index.e1.visit(this, arg);
		System.out.println("index:" + index.isCartesian());
		if(!index.isCartesian()){
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
			mv.visitInsn(DUP_X2);
			mv.visitInsn(POP);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
		}
		return null;
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, expression_PixelSelector.name, ImageSupport.ImageDesc);
		expression_PixelSelector.index.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getPixel", ImageSupport.getPixelSig, false);
		return null;
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		Expression cond = expression_Conditional.condition;
		Expression te = expression_Conditional.trueExpression;
		Expression fe = expression_Conditional.falseExpression;
		Label true_label = new Label();
		cond.visit(this, arg);
		Label false_label = new Label();
		mv.visitJumpInsn(IFNE, false_label);
		fe.visit(this, arg);
		mv.visitJumpInsn(GOTO, true_label);
		mv.visitLabel(false_label);
		te.visit(this, arg);
		mv.visitLabel(true_label);		
		return null;
	}


	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		// TODO HW6
		fv = cw.visitField(ACC_STATIC, declaration_Image.name, ImageSupport.ImageDesc, null, null);
		fv.visitEnd();
		if(declaration_Image.source!=null){
			declaration_Image.source.visit(this, arg);
			if(declaration_Image.xSize == null && declaration_Image.ySize == null){
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}else{
				declaration_Image.xSize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)" + ImageSupport.IntegerDesc, false);
				declaration_Image.ySize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)" + ImageSupport.IntegerDesc, false);
			}
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
		}else{
			if(declaration_Image.xSize == null && declaration_Image.ySize == null){
				mv.visitLdcInsn(DEF_X);
				mv.visitLdcInsn(DEF_Y);
			}else{
				declaration_Image.xSize.visit(this, arg);
				declaration_Image.ySize.visit(this, arg);
			}
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage", ImageSupport.makeImageSig, false);

		}		
		mv.visitFieldInsn(PUTSTATIC, className, declaration_Image.name,  ImageSupport.ImageDesc);
		return null;
	}
	
  
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
//		String typevar = source_StringLiteral.getTypeName() == Type.FILE? "Ljava/io/File" : "Ljava/net/URL";
//		if(source_StringLiteral.getTypeName().isType(Type.FILE) || source_StringLiteral.getTypeName().isType(Type.URL))
//			mv.visitFieldInsn(GETSTATIC, className, source_StringLiteral.fileOrUrl, "Ljava/lang/String;");
		mv.visitLdcInsn(source_StringLiteral.fileOrUrl);
		return null;
	}

	

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		mv.visitIntInsn(ALOAD, 0);
		source_CommandLineParam.paramNum.visit(this, arg);
		mv.visitInsn(AALOAD);
		return null;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		mv.visitFieldInsn(GETSTATIC, className, source_Ident.name, "Ljava/lang/String;");
		return null;
	}


	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
//		String type_var = (declaration_SourceSink.getTypeName() == Type.FILE)?"Ljava/io/File;" : "Ljava/net/URL";
		fv = cw.visitField(ACC_STATIC, declaration_SourceSink.name,  "Ljava/lang/String;", null, null);
		fv.visitEnd();
		if(declaration_SourceSink.source!=null){
			declaration_SourceSink.source.visit(this, arg);
			mv.visitFieldInsn(PUTSTATIC, className, declaration_SourceSink.name, "Ljava/lang/String;");
		}
		return null;
	}
	


	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception { 
		mv.visitLdcInsn(expression_IntLit.value);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		// TODO HW6
		expression_FunctionAppWithExprArg.arg.visit(this, arg);
		switch(expression_FunctionAppWithExprArg.function){
			case KW_abs:
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "abs", RuntimeFunctions.absSig, false);
				break;
			case KW_log:
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "log", RuntimeFunctions.logSig, false);
				break;
			default:
				
		}
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		// TODO HW6
		expression_FunctionAppWithIndexArg.arg.e0.visit(this, arg);
		expression_FunctionAppWithIndexArg.arg.e1.visit(this, arg);
		switch(expression_FunctionAppWithIndexArg.function){
			case KW_cart_x:
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
			break;
			case KW_cart_y:
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
				break;
			case KW_polar_a:
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
				break;
			case KW_polar_r:
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
				break;
			default:
		}
		return null;
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO HW6
		switch(expression_PredefinedName.kind){
			case KW_x:
				mv.visitVarInsn(ILOAD, 1);
				break;
			case KW_y:
				mv.visitVarInsn(ILOAD, 2);
				break;
			case KW_X:
				mv.visitVarInsn(ILOAD, 3);
				break;
			case KW_Y:
				mv.visitVarInsn(ILOAD, 4);
				break;
			case KW_r:
				mv.visitVarInsn(ILOAD, 1);
				mv.visitVarInsn(ILOAD, 2);
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
				mv.visitIntInsn(ISTORE, 5);
				mv.visitIntInsn(ILOAD, 5);
				break;
			case KW_a:
				mv.visitVarInsn(ILOAD, 1);
				mv.visitVarInsn(ILOAD, 2);
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
				mv.visitIntInsn(ISTORE, 6);
				mv.visitIntInsn(ILOAD, 6);
				break;
			case KW_R:
				mv.visitVarInsn(ILOAD, 3);
				mv.visitVarInsn(ILOAD, 4);
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
				mv.visitIntInsn(ISTORE, 7);
				mv.visitIntInsn(ILOAD, 7);
				break;
			case KW_A:
				mv.visitLdcInsn(ICONST_0);
				mv.visitVarInsn(ILOAD, 4);
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
				mv.visitIntInsn(ISTORE, 8);
				mv.visitIntInsn(ILOAD, 8);
				break;
			case KW_DEF_X:
				mv.visitLdcInsn(DEF_X);
				break;
			case KW_DEF_Y:
				mv.visitLdcInsn(DEF_Y);
				break;
			case KW_Z:
				mv.visitLdcInsn(Z);
				break;
			default:
				
		}
		return null;
	}

	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5:  only INTEGER and BOOLEAN
		// TODO HW6 remaining cases
		Type typeName = statement_Out.getDec().getTypeName();
		if(typeName.isType(Type.BOOLEAN) || typeName.isType(Type.INTEGER)){
			String type_var = (typeName == (Type.INTEGER))?"I":"Z";
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, type_var);
			CodeGenUtils.genLogTOS(GRADE, mv, typeName);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",  "(" + type_var + ")V", false);
		}else if(typeName.isType(Type.IMAGE)){
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, ImageSupport.ImageDesc);
			CodeGenUtils.genLogTOS(GRADE, mv, typeName);
			statement_Out.sink.visit(this, arg);
		}
		return null;
	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 * 
	 *  In HW5, you only need to handle INTEGER and BOOLEAN
	 *  Use java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean 
	 *  to convert String to actual type. 
	 *  
	 *  TODO HW6 remaining types
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {		
		Type sin_type = statement_In.getDec().getTypeName();
		statement_In.source.visit(this, arg);
		if(sin_type.isType(Type.INTEGER)){
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "I");
		}else if(sin_type.isType(Type.BOOLEAN)){
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "Z");
		}else if(sin_type.isType(Type.IMAGE)){
			Declaration_Image dec_Image = (Declaration_Image)statement_In.getDec();
			if(dec_Image.xSize == null && dec_Image.ySize == null){
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}else{
				dec_Image.xSize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)" + ImageSupport.IntegerDesc, false);
				dec_Image.ySize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)" + ImageSupport.IntegerDesc, false);
			}
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name,  ImageSupport.ImageDesc);
		}
		return null;
	}


	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		if(lhs.getTypeName() == Type.INTEGER)
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "I");
		else if(lhs.getTypeName() == Type.BOOLEAN)
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "Z");
		else if(lhs.getTypeName().isType(Type.IMAGE)){
			//TO DO	
			mv.visitFieldInsn(GETSTATIC, className, lhs.name, ImageSupport.ImageDesc);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
//			lhs.index.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "setPixel", ImageSupport.setPixelSig, false);			
		}
		return null;
	}
	

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		//TODO HW6
		mv.visitMethodInsn(INVOKESTATIC, ImageFrame.className, "makeFrame", ImageSupport.makeFrameSig, false);
		mv.visitInsn(POP);
		return null;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		//TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, sink_Ident.name,ImageSupport.StringDesc);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "write",  ImageSupport.writeSig, false);
		return null;
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		mv.visitLdcInsn(expression_BooleanLit.value);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		if(expression_Ident.getTypeName() == Type.BOOLEAN)
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "Z");
		else if(expression_Ident.getTypeName() == Type.INTEGER)
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "I");
//		CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.getTypeName());
		return null;
	}
	
	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		if(statement_Assign.lhs.getTypeName().isType(Type.INTEGER) ||statement_Assign.lhs.getTypeName().isType(Type.BOOLEAN)  ){
			statement_Assign.e.visit(this, arg);		
			statement_Assign.lhs.visit(this, arg);
		}
		else if(statement_Assign.lhs.getTypeName().isType(Type.IMAGE)){
			//if(statement_Assign.lhs.isCartesian){
				mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
				mv.visitInsn(DUP);
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
				mv.visitIntInsn(ISTORE, 3);
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
				mv.visitIntInsn(ISTORE, 4);				
			//}
			
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 1);
			Label l1 = new Label();
			mv.visitLabel(l1);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitLineNumber(11, l3);
			mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 2);
			Label l4 = new Label();
			mv.visitLabel(l4);
			Label l5 = new Label();
			mv.visitJumpInsn(GOTO, l5);
			Label l6 = new Label();
			mv.visitLabel(l6);
			mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
			
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
			
			mv.visitIincInsn(2, 1);
			mv.visitLabel(l5);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitIntInsn(ILOAD, 4); // Y MAX VALUE
			mv.visitJumpInsn(IF_ICMPLT, l6);
			Label l7 = new Label();
			mv.visitLabel(l7);
			mv.visitIincInsn(1, 1);
			mv.visitLabel(l2);
			mv.visitFrame(Opcodes.F_CHOP,1, null, 0, null);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitIntInsn(ILOAD, 3); // X MAX VALUE
			mv.visitJumpInsn(IF_ICMPLT, l3);
		}
		return null;
	}

}
