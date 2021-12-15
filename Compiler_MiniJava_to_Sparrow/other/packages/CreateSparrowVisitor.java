package other.packages;
import cs132.minijava.syntaxtree.*;
import cs132.minijava.visitor.*;
import cs132.minijava.*;
import java.util.*;

public class CreateSparrowVisitor extends GJDepthFirst<SparrowShape, IDnType> {
	
	private int tmpCount = 0;
	private HashMap<String, String> id2ClassType = new HashMap<>();
	private String curExpClass = "";
	private String curExpMethod = "";
	
	public String getTmpVar() {
		return "w" + Integer.toString(tmpCount++);
	}

	public SparrowShape visit(NodeList n, IDnType idTypes) {

		for (int i = 0; i < n.size(); i++)
			n.elementAt(i).accept(this, idTypes);
		return new SparrowShape();
	}

	public SparrowShape visit(NodeListOptional n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();

		for (int i = 0; i < n.size(); i++) {
			SparrowShape subSparrowRes = n.elementAt(i).accept(this, idTypes);
			sparrowRes.assignTmpParams.add(subSparrowRes.sparrSrc);
			sparrowRes.tmpParams.add(subSparrowRes.tmpVarName);
		}
		return sparrowRes;
	}

	public SparrowShape visit(NodeOptional n, IDnType idTypes) {
		if (n.present())
			return n.node.accept(this, idTypes);
		return new SparrowShape();
	}

	public SparrowShape visit(NodeSequence n, IDnType idTypes) {

		for (int i = 0; i < n.size(); i++)
			n.elementAt(i).accept(this, idTypes);
		return new SparrowShape();
	}

	public SparrowShape visit(NodeToken n, IDnType idTypes) { return new SparrowShape();}


	/**
	 * f0 -> MainClass()
	 * f1 -> ( TypeDeclaration() )* 
	 * f2 -> <EOF>
	 */
	public SparrowShape visit(Goal n, IDnType idTypes) {
		
		//System.out.println("Goal");
		
		SparrowShape sparrowRes = new SparrowShape();
		SparrowShape f0Result = n.f0.accept(this, idTypes);
		sparrowRes.sparrSrc.addAll(f0Result.sparrSrc);
		
		SparrowShape f1Result = n.f1.accept(this, idTypes);

		for (List<String> typeDec: f1Result.assignTmpParams) {
			
			//for (int i = 0; i < typeDec.size(); i++)
			//	System.out.println("Goal sparrSrc: " + typeDec.get(i));
			
			sparrowRes.sparrSrc.addAll(typeDec);
		}
		
		n.f2.accept(this, idTypes);
		
		return sparrowRes;
	}

	/**
	 * f0 -> "class" 
	 * f1 -> Identifier() 
	 * f2 -> "{" 
	 * f3 -> "public" 
	 * f4 -> "static" 
	 * f5 -> "void" 
	 * f6 -> "main" 
	 * f7 -> "(" 
	 * f8 -> "String" 
	 * f9 -> "[" 
	 * f10 -> "]" 
	 * f11 -> Identifier() 
	 * f12 -> ")" 
	 * f13 -> "{" 
	 * f14 -> ( VarDeclaration() )* 
	 * f15 -> (Statement() )* 
	 * f16 -> "}" 
	 * f17 -> "}"
	 */
	
	public SparrowShape visit(MainClass n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		n.f0.accept(this, idTypes);
		
		String f1Result = n.f1.f0.tokenImage;
		String defMainFunc = "func " + f1Result + "main()";
		sparrowRes.sparrSrc.add(defMainFunc);
		idTypes.curClass = f1Result;
		idTypes.curMethod = "main";
		
//		n.f2.accept(this, idTypes);
//		n.f3.accept(this, idTypes);
//		n.f4.accept(this, idTypes);
//		n.f5.accept(this, idTypes);
//		n.f6.accept(this, idTypes);
//		n.f7.accept(this, idTypes);
//		n.f8.accept(this, idTypes);
//		n.f9.accept(this, idTypes);
//		n.f10.accept(this, idTypes);
//		n.f11.accept(this, idTypes);
//		n.f12.accept(this, idTypes);
//		n.f13.accept(this, idTypes);
//		n.f14.accept(this, idTypes);
		
		SparrowShape f15Result = n.f15.accept(this, idTypes);
		for (List<String> statementRes: f15Result.assignTmpParams) {
			
//			for (int i = 0; i < statementRes.size(); i++)
//				System.out.println("MainClass statement: " + statementRes.get(i));
			
			sparrowRes.sparrSrc.addAll(statementRes);
		}
		
		n.f16.accept(this, idTypes);
		n.f17.accept(this, idTypes);
		
		sparrowRes.sparrSrc.add("mainReturn = 0");
		sparrowRes.sparrSrc.add("return mainReturn");
		
		idTypes.curClass = "";
		idTypes.curMethod = "";
		return sparrowRes;
	}

	/**
	 * f0 -> ClassDeclaration() | ClassExtendsDeclaration()
	 */
	public SparrowShape visit(TypeDeclaration n, IDnType idTypes) {
		return n.f0.accept(this, idTypes);
	}

	/**
	 * f0 -> "class" 
	 * f1 -> Identifier() 
	 * f2 -> "{" 
	 * f3 -> ( VarDeclaration() )* 
	 * f4 -> ( MethodDeclaration() )* 
	 * f5 -> "}"
	 */
	
	public SparrowShape visit(ClassDeclaration n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		String className = n.f1.f0.tokenImage;
		if (!idTypes.className.contains(className))
			System.out.println("Class doesn't exist");
		
		idTypes.curClass = className;
		id2ClassType.clear();
		SparrowShape f4Result = n.f4.accept(this, idTypes);
		for (List<String> methodDec: f4Result.assignTmpParams)
			sparrowRes.sparrSrc.addAll(methodDec);
		
		idTypes.curClass = "";
		id2ClassType.clear();
		return sparrowRes;
	}

	/**
	 * f0 -> "class" 
	 * f1 -> Identifier() 
	 * f2 -> "extends" 
	 * f3 -> Identifier() 
	 * f4 -> "{"
	 * f5 -> ( VarDeclaration() )* 
	 * f6 -> ( MethodDeclaration() )* 
	 * f7 -> "}"
	 */
	
	public SparrowShape visit(ClassExtendsDeclaration n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		String className = n.f1.f0.tokenImage;
		if (!idTypes.className.contains(className))
			System.out.println("Class doesn't exist");
		
		idTypes.curClass = className;
		id2ClassType.clear();
		SparrowShape f6Result = n.f6.accept(this, idTypes);
		for (List<String> methodDec: f6Result.assignTmpParams)
			sparrowRes.sparrSrc.addAll(methodDec);
		
		idTypes.curClass = "";
		id2ClassType.clear();
		return sparrowRes;
	}

	/**
	 * f0 -> Type() 
	 * f1 -> Identifier() 
	 * f2 -> ";"
	 */
	
//	public R visit(VarDeclaration n, A argu) {
//		R _ret = null;
//		n.f0.accept(this, argu);
//		n.f1.accept(this, argu);
//		n.f2.accept(this, argu);
//		return _ret;
//	}

	/**
	 * f0 -> "public" 
	 * f1 -> Type() 
	 * f2 -> Identifier() 
	 * f3 -> "(" 
	 * f4 -> (FormalParameterList() )? 
	 * f5 -> ")" 
	 * f6 -> "{" 
	 * f7 -> ( VarDeclaration() )* 
	 * f8 -> ( Statement() )* 
	 * f9 -> "return" 
	 * f10 -> Expression() 
	 * f11 -> ";" 
	 * f12 -> "}"
	 */
	
	public SparrowShape visit(MethodDeclaration n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		String currentClass = idTypes.curClass;
		
		String methodName = n.f2.f0.tokenImage;
		if (!idTypes.classMethods.get(currentClass).containsKey(methodName))
			System.err.println("The current class does not have the method");
		
		String methodDec = "func " + currentClass + methodName + "(this";
//		String methodReturnType = idTypes.classMethods.get(currentClass).get(methodName);
//		if (!idTypes.className.contains(methodReturnType))
//			System.err.println("The return type of method is wrong");
		
		ArrayList<String> methodParamIds = idTypes.methodParamName.get(currentClass).get(methodName);
		for (String paramId: methodParamIds)
			methodDec = methodDec + " " + paramId;
		methodDec += ")";
		
		//System.out.println("Create MethodDeclareation methodDec: " + methodDec);
		
		sparrowRes.sparrSrc.add(methodDec);
		
		idTypes.curMethod = methodName;
		SparrowShape f8Result = n.f8.accept(this, idTypes);
		//System.out.println("MethodDeclaraiton f8Result.src.size(): " + f8Result.sparrSrc.size());
		
	
		
		for (List<String> statement: f8Result.assignTmpParams) {
			
			//System.out.println("@@@@ Create MethodDeclareation statement: " + statement);
			
			sparrowRes.sparrSrc.addAll(statement);
		}
		
		//System.out.println("Go out from @@@@ Create MEthodDeclaration");
		
		SparrowShape f10Result = n.f10.accept(this, idTypes);
		sparrowRes.sparrSrc.addAll(f10Result.sparrSrc);
		sparrowRes.sparrSrc.add("return " + f10Result.tmpVarName);
		sparrowRes.tmpVarName = f10Result.tmpVarName;
		sparrowRes.className = f10Result.className;
		idTypes.curMethod = "";
		
//		for (int i = 0; i < sparrowRes.sparrSrc.size(); i++)
//			System.out.println("MethodDeclaration sparrSrc: " + sparrowRes.sparrSrc.get(i));
		
		return sparrowRes;
	}

	/**
	 * f0 -> FormalParameter() 
	 * f1 -> ( FormalParameterRest() )*
	 */
	
//	public R visit(FormalParameterList n, A argu) {
//		R _ret = null;
//		n.f0.accept(this, argu);
//		n.f1.accept(this, argu);
//		return _ret;
//	}

	/**
	 * f0 -> Type() 
	 * f1 -> Identifier()
	 */
	
//	public R visit(FormalParameter n, A argu) {
//		R _ret = null;
//		n.f0.accept(this, argu);
//		n.f1.accept(this, argu);
//		return _ret;
//	}

	/**
	 * f0 -> "," 
	 * f1 -> FormalParameter()
	 */
	
//	public R visit(FormalParameterRest n, A argu) {
//		R _ret = null;
//		n.f0.accept(this, argu);
//		n.f1.accept(this, argu);
//		return _ret;
//	}

	/**
	 * f0 -> ArrayType() | BooleanType() | IntegerType() | Identifier()
	 */
	
//	public R visit(Type n, A argu) {
//		R _ret = null;
//		n.f0.accept(this, argu);
//		return _ret;
//	}

	/**
	 * f0 -> "int" 
	 * f1 -> "[" 
	 * f2 -> "]"
	 */
	
//	public R visit(ArrayType n, A argu) {
//		R _ret = null;
//		n.f0.accept(this, argu);
//		n.f1.accept(this, argu);
//		n.f2.accept(this, argu);
//		return _ret;
//	}

	/**
	 * f0 -> "boolean"
	 */
	
//	public R visit(BooleanType n, A argu) {
//		R _ret = null;
//		n.f0.accept(this, argu);
//		return _ret;
//	}

	/**
	 * f0 -> "int"
	 */
	
//	public R visit(IntegerType n, A argu) {
//		R _ret = null;
//		n.f0.accept(this, argu);
//		return _ret;
//	}

	/**
	 * f0 -> Block() | AssignmentStatement() | ArrayAssignmentStatement() |
	 * IfStatement() | WhileStatement() | PrintStatement()
	 */
	
	public SparrowShape visit(Statement n, IDnType idTypes) {
		
		//System.out.println("@@@ Entered Statement @@@");
		
		SparrowShape sparrowRes = n.f0.choice.accept(this, idTypes);
//		for (int i = 0; i < sparrowRes.sparrSrc.size(); i++)
//			System.out.println("Statement sparrsrc: " + sparrowRes.sparrSrc.get(i));
		return sparrowRes;
	}

	/**
	 * f0 -> "{" 
	 * f1 -> ( Statement() )* 
	 * f2 -> "}"
	 */
	
	public SparrowShape visit(Block n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		SparrowShape statementRes = n.f1.accept(this, idTypes);
		for (List<String> stmt: statementRes.assignTmpParams)
			sparrowRes.sparrSrc.addAll(stmt);
		return sparrowRes;
	}

	/**
	 * f0 -> Identifier() 
	 * f1 -> "=" 
	 * f2 -> Expression() 
	 * f3 -> ";"
	 */
	
	public SparrowShape visit(AssignmentStatement n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		SparrowShape f0Result = n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		SparrowShape f2Result = n.f2.accept(this, idTypes);
		
		sparrowRes.sparrSrc = f2Result.sparrSrc;
		
		//System.out.println("AssignmentStatement sparrowRes.sparrSrc: " + sparrowRes.sparrSrc);
		
		
		String curClass = idTypes.curClass;
		
//		System.out.println("AssignmentStatement !!curClass: " + curClass);
		
//		if (idTypes.curMethod.equals(""))
//			System.out.println("AssignmentStatement curMethod is empty");
//		if (f0Result.chkField)
//			System.out.println("AssignmentStatement f0Result.chkField is false");
//		System.out.println("AssignmentStatement f0Result: " + f0Result);
		
		if (!idTypes.curMethod.equals("") && f0Result.chkField) {
			int pos = idTypes.classFieldsIndx.get(curClass).get(n.f0.f0.tokenImage);
			
//			System.out.println("AssignmentStatement @@curClass: " + curClass);
//			System.out.println("AssignmentStatement pos: " + pos);
			
			sparrowRes.sparrSrc.add("[this + " + Integer.toString(4 * pos) + "] = " + f2Result.tmpVarName);
		}
		else
			sparrowRes.sparrSrc.add(f0Result.tmpVarName + " = " + f2Result.tmpVarName);
		
		if (!f2Result.className.equals(""))
			this.id2ClassType.put(n.f0.f0.tokenImage, f2Result.className);
		
		return sparrowRes;
	}

	/**
	 * f0 -> Identifier() 
	 * f1 -> "[" 
	 * f2 -> Expression() 
	 * f3 -> "]" 
	 * f4 -> "=" 
	 * f5 -> Expression() 
	 * f6 -> ";"
	 */
	
	public SparrowShape visit(ArrayAssignmentStatement n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		SparrowShape f0Result = n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		SparrowShape f2Result = n.f2.accept(this, idTypes);
		n.f3.accept(this, idTypes);
		n.f4.accept(this, idTypes);
		SparrowShape f5Result = n.f5.accept(this, idTypes);
		n.f6.accept(this, idTypes);
		
		sparrowRes.sparrSrc.addAll(f5Result.sparrSrc);
		sparrowRes.sparrSrc.addAll(f2Result.sparrSrc);
		sparrowRes.sparrSrc.addAll(f0Result.sparrSrc);
		
		// Check the array boundary
		String id = f0Result.tmpVarName;
		String arySize = f2Result.tmpVarName;
		
		String one = getTmpVar();
		sparrowRes.sparrSrc.add(one + " = 1");
		String two = getTmpVar();
		sparrowRes.sparrSrc.add(two + " = 2");
		String negOne = getTmpVar();
		sparrowRes.sparrSrc.add(negOne + " = " + one + " - " + two);
		
		//sparrowRes.sparrSrc.add(lowBound + " = -1");
		String chkLowBound = getTmpVar();
		sparrowRes.sparrSrc.add(chkLowBound + " = " + negOne + " < " + arySize);
		
		String aryLen = getTmpVar();
		sparrowRes.sparrSrc.add(aryLen + " = [" + id + " + 0]");
		String chkHighBound = getTmpVar();
		sparrowRes.sparrSrc.add(chkHighBound + " = " + arySize + " < " + aryLen);
		
		String chkBoundExcept = getTmpVar();
		sparrowRes.sparrSrc.add(chkBoundExcept + " = " + chkLowBound + " * " + chkHighBound);
		sparrowRes.sparrSrc.add("if0 " + chkBoundExcept + " goto " + chkLowBound + "_error");
		sparrowRes.sparrSrc.add("goto " + chkLowBound + "_end");
		sparrowRes.sparrSrc.add(chkLowBound + "_error:");
		sparrowRes.sparrSrc.add("error(\"array index out of bounds\")");
		sparrowRes.sparrSrc.add(chkLowBound + "_end:");
		
		String four = getTmpVar();
		sparrowRes.sparrSrc.add(four + " = " + 4);
		String arySizePlusOne = getTmpVar();
		sparrowRes.sparrSrc.add(arySizePlusOne + " = " + arySize + " + " + one);
		String offset = getTmpVar();
		sparrowRes.sparrSrc.add(offset + " = " + arySizePlusOne + " * " + four);
				
		String posToLoad = getTmpVar();
		sparrowRes.sparrSrc.add(posToLoad + " = " + id + " + " + offset);
		sparrowRes.sparrSrc.add("[" + posToLoad + " + 0] = " + f5Result.tmpVarName);
		sparrowRes.tmpVarName = posToLoad;
		
		return sparrowRes;
	}

	/**
	 * f0 -> "if" 
	 * f1 -> "(" 
	 * f2 -> Expression() 
	 * f3 -> ")" 
	 * f4 -> Statement() 
	 * f5 -> "else" 
	 * f6 -> Statement()
	 */
	
	public SparrowShape visit(IfStatement n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		
		//System.out.println("@@@ Entered IfStatement @@@");
		
		SparrowShape f2Result = n.f2.accept(this, idTypes);
		sparrowRes.sparrSrc.addAll(f2Result.sparrSrc);
		sparrowRes.sparrSrc.add("if0 " + f2Result.tmpVarName + " goto " + f2Result.tmpVarName + "_else");
		SparrowShape f4Result = n.f4.accept(this, idTypes);
		ArrayList<String> statements = new ArrayList<>(f4Result.sparrSrc);
		for (int i = 0; i < statements.size(); i++)
			sparrowRes.sparrSrc.add(statements.get(i));
		sparrowRes.sparrSrc.add("goto " + f2Result.tmpVarName + "_end");
		sparrowRes.sparrSrc.add(f2Result.tmpVarName + "_else:");
		
		SparrowShape f6Result = n.f6.accept(this, idTypes);
		statements.clear();
		statements = new ArrayList<>(f6Result.sparrSrc);
		for (int i = 0; i < statements.size(); i++)
			sparrowRes.sparrSrc.add(statements.get(i));
		sparrowRes.sparrSrc.add(f2Result.tmpVarName + "_end:");
		
//		for (String s: sparrowRes.sparrSrc) {
//			
//			System.out.println("IfStatement sparrSrc: " + s);
//		}
		
		return sparrowRes;
	}

	/**
	 * f0 -> "while" 
	 * f1 -> "(" 
	 * f2 -> Expression() 
	 * f3 -> ")" 
	 * f4 -> Statement()
	 */
	
	public SparrowShape visit(WhileStatement n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		String loopVar = getTmpVar();
		sparrowRes.sparrSrc.add("loop" + loopVar +":");
		
		SparrowShape f2Result = n.f2.accept(this, idTypes);
		sparrowRes.sparrSrc.addAll(f2Result.sparrSrc);
		sparrowRes.sparrSrc.add("if0 " + f2Result.tmpVarName + " goto " + loopVar + "_end");
		SparrowShape f4Result = n.f4.accept(this, idTypes);
		sparrowRes.sparrSrc.addAll(f4Result.sparrSrc);
		sparrowRes.sparrSrc.add("goto loop" + loopVar);
		sparrowRes.sparrSrc.add(loopVar + "_end:");
		return sparrowRes;
	}

	/**
	 * f0 -> "System.out.println" 
	 * f1 -> "(" 
	 * f2 -> Expression() 
	 * f3 -> ")" 
	 * f4 -> ";"
	 */
	
	public SparrowShape visit(PrintStatement n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		SparrowShape f2Result = n.f2.accept(this, idTypes);
		
		sparrowRes.sparrSrc.addAll(f2Result.sparrSrc);
		sparrowRes.sparrSrc.add("print(" + f2Result.tmpVarName + ")");
		sparrowRes.tmpVarName = "";
		return sparrowRes;
	}

	/**
	 * f0 -> AndExpression() | CompareExpression() | PlusExpression() |
	 * MinusExpression() | TimesExpression() | ArrayLookup() | ArrayLength() |
	 * MessageSend() | PrimaryExpression()
	 */
	
	public SparrowShape visit(Expression n, IDnType idTypes) {
		return n.f0.accept(this, idTypes);
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "&&" 
	 * f2 -> PrimaryExpression()
	 */
	
	public SparrowShape visit(AndExpression n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		SparrowShape f0Result = n.f0.accept(this, idTypes);
		sparrowRes.sparrSrc.addAll(f0Result.sparrSrc);
		SparrowShape f2Result = n.f2.accept(this, idTypes);
		sparrowRes.sparrSrc.addAll(f2Result.sparrSrc);
		sparrowRes.tmpVarName = getTmpVar();
		sparrowRes.sparrSrc.add(sparrowRes.tmpVarName + " = " + f0Result.tmpVarName + " * " + f2Result.tmpVarName);
		return sparrowRes;
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "<" 
	 * f2 -> PrimaryExpression()
	 */
	
	public SparrowShape visit(CompareExpression n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		SparrowShape f0Result = n.f0.accept(this, idTypes);
		sparrowRes.sparrSrc.addAll(f0Result.sparrSrc);
		SparrowShape f2Result = n.f2.accept(this, idTypes);
		sparrowRes.sparrSrc.addAll(f2Result.sparrSrc);
		sparrowRes.tmpVarName = getTmpVar();
		sparrowRes.sparrSrc.add(sparrowRes.tmpVarName + " = " + f0Result.tmpVarName + " < " + f2Result.tmpVarName);
		return sparrowRes;
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "+" 
	 * f2 -> PrimaryExpression()
	 */
	
	public SparrowShape visit(PlusExpression n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		SparrowShape f0Result = n.f0.accept(this, idTypes);
		sparrowRes.sparrSrc.addAll(f0Result.sparrSrc);
		SparrowShape f2Result = n.f2.accept(this, idTypes);
		sparrowRes.sparrSrc.addAll(f2Result.sparrSrc);
		sparrowRes.tmpVarName = getTmpVar();
		sparrowRes.sparrSrc.add(sparrowRes.tmpVarName + " = " + f0Result.tmpVarName + " + " + f2Result.tmpVarName);
		return sparrowRes;
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "-" 
	 * f2 -> PrimaryExpression()
	 */
	
	public SparrowShape visit(MinusExpression n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		SparrowShape f0Result = n.f0.accept(this, idTypes);
		sparrowRes.sparrSrc.addAll(f0Result.sparrSrc);
		SparrowShape f2Result = n.f2.accept(this, idTypes);
		sparrowRes.sparrSrc.addAll(f2Result.sparrSrc);
		sparrowRes.tmpVarName = getTmpVar();
		sparrowRes.sparrSrc.add(sparrowRes.tmpVarName + " = " + f0Result.tmpVarName + " - " + f2Result.tmpVarName);
		return sparrowRes;
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "*" 
	 * f2 -> PrimaryExpression()
	 */
	
	public SparrowShape visit(TimesExpression n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		SparrowShape f0Result = n.f0.accept(this, idTypes);
		sparrowRes.sparrSrc.addAll(f0Result.sparrSrc);
		SparrowShape f2Result = n.f2.accept(this, idTypes);
		sparrowRes.sparrSrc.addAll(f2Result.sparrSrc);
		sparrowRes.tmpVarName = getTmpVar();
		sparrowRes.sparrSrc.add(sparrowRes.tmpVarName + " = " + f0Result.tmpVarName + " * " + f2Result.tmpVarName);
		return sparrowRes;
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "[" 
	 * f2 -> PrimaryExpression() 
	 * f3 -> "]"
	 */
	
	public SparrowShape visit(ArrayLookup n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		SparrowShape f0Result = n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		SparrowShape f2Result = n.f2.accept(this, idTypes);
		n.f3.accept(this, idTypes);
		
		sparrowRes.sparrSrc.addAll(f2Result.sparrSrc);
		sparrowRes.sparrSrc.addAll(f0Result.sparrSrc);
		
		// Check the array boundary
		String id = f0Result.tmpVarName;
		String arySize = f2Result.tmpVarName;
//		String lowBound = getTmpVar();
//		sparrowRes.sparrSrc.add(lowBound + " = -1");
//		String chkLowBound = getTmpVar();
//		sparrowRes.sparrSrc.add(chkLowBound + " = " + lowBound + " < " + arySize);
		
		String one = getTmpVar();
		sparrowRes.sparrSrc.add(one + " = 1");
		String two = getTmpVar();
		sparrowRes.sparrSrc.add(two + " = 2");
		String negOne = getTmpVar();
		sparrowRes.sparrSrc.add(negOne + " = " + one + " - " + two);
		String chkLowBound = getTmpVar();
		sparrowRes.sparrSrc.add(chkLowBound + " = " + negOne + " < " + f2Result.tmpVarName);
		
		String aryLen = getTmpVar();
		sparrowRes.sparrSrc.add(aryLen + " = [" + id + " + 0]");
		String chkHighBound = getTmpVar();
		sparrowRes.sparrSrc.add(chkHighBound + " = " + arySize + " < " + aryLen);
		
		String chkBoundExcept = getTmpVar();
		sparrowRes.sparrSrc.add(chkBoundExcept + " = " + chkLowBound + " * " + chkHighBound);
		sparrowRes.sparrSrc.add("if0 " + chkBoundExcept + " goto " + chkLowBound + "_error");
		sparrowRes.sparrSrc.add("goto " + chkLowBound + "_end");
		sparrowRes.sparrSrc.add(chkLowBound + "_error:");
		sparrowRes.sparrSrc.add("error(\"array index out of bounds\")");
		sparrowRes.sparrSrc.add(chkLowBound + "_end:");

		String four = getTmpVar();
		sparrowRes.sparrSrc.add(four + " = " + 4);
		String arySizePlusOne = getTmpVar();
		sparrowRes.sparrSrc.add(arySizePlusOne + " = " + arySize + " + " + one);
		String offset = getTmpVar();
		sparrowRes.sparrSrc.add(offset + " = " + arySizePlusOne + " * " + four);
				
		String posToLoad = getTmpVar();
		sparrowRes.sparrSrc.add(posToLoad + " = " + id + " + " + offset);
		sparrowRes.sparrSrc.add(posToLoad + " = [" + posToLoad + " + 0]");
		sparrowRes.tmpVarName = posToLoad;
		
		return sparrowRes;
		
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "." 
	 * f2 -> "length"
	 */
	
	public SparrowShape visit(ArrayLength n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		SparrowShape f0Result = n.f0.accept(this, idTypes);
		sparrowRes.sparrSrc.addAll(f0Result.sparrSrc);
		
		String lenVar = getTmpVar();
		sparrowRes.sparrSrc.add(lenVar + " = [" + f0Result.tmpVarName + " + 0]");
		sparrowRes.tmpVarName = lenVar;
		return sparrowRes;
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "." 
	 * f2 -> Identifier() 
	 * f3 -> "(" 
	 * f4 -> (ExpressionList() )? 
	 * f5 -> ")"
	 */
	
	public SparrowShape visit(MessageSend n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		SparrowShape f0Result = n.f0.accept(this, idTypes);
		sparrowRes.sparrSrc.addAll(f0Result.sparrSrc);
		
		
		//System.out.println("MessageSend f0Result.className: " + f0Result.className);
//		if (f0Result.className.equals(""))
//			System.out.println("f0Result.className is empty");
//		for (int i = 0; i < f0Result.sparrSrc.size(); i++)
//			System.out.println(f0Result.sparrSrc.get(i));
		
		if (f0Result.className.equals("")) {
			String id = ((Identifier)n.f0.f0.choice).f0.toString(); 
			if (id2ClassType.containsKey(id))
				f0Result.className = id2ClassType.get(id);
			else {
				String errTmp = getTmpVar();
				sparrowRes.sparrSrc.add("goto " + errTmp + "_error");
				sparrowRes.sparrSrc.add(errTmp + "_error:");
				sparrowRes.sparrSrc.add("error(\"null pointer\")");
			}		
		}
		
		// Find the position of function to call in the class (id).
		String className = f0Result.tmpVarName;
		String funVar = getTmpVar();
		sparrowRes.sparrSrc.add(funVar + " = [" + className + " + 0]");
		String funCallName = n.f2.f0.tokenImage;
		
//		System.out.println("MessageSend funCallName: " + funCallName);
//		if (!idTypes.classMethodsIndx.containsKey(f0Result.className))
//			System.out.println("MessageSend classMethodsIndx doesn't contain f0Result.className");
//		for (String method: idTypes.classMethodsIndx.get(f0Result.className).keySet()) {
//			System.out.println("MessageSend classMethodsIndx methods: " + method);
//		}
		
		boolean hasFunCallName = true;
		if (!idTypes.classMethodsIndx.containsKey(f0Result.className))
			hasFunCallName = false;
		else if (!idTypes.classMethodsIndx.get(f0Result.className).containsKey(funCallName))
			hasFunCallName = false;
		if (!hasFunCallName) {
			sparrowRes.sparrSrc.add("goto " + funVar +"_error");
			sparrowRes.sparrSrc.add(funVar + "_error:");
			sparrowRes.sparrSrc.add("error(\"null pointer\")");
		}
		else {	
			int posFunCall = idTypes.classMethodsIndx.get(f0Result.className).get(funCallName);
			sparrowRes.sparrSrc.add(funVar + " = " + "[" + funVar + " + " + Integer.toString(posFunCall * 4) + "]");
		}
		
		// Call the function
		curExpClass = f0Result.className;
		curExpMethod = funCallName;
		String funCallVar = getTmpVar();
		String funCallSrc = funCallVar + " = call " + funVar + "(" + className;
		SparrowShape f4Result = n.f4.accept(this, idTypes);
		
		//System.out.println("MessageSend f4Result.assignTmpParams.size(): " + f4Result.assignTmpParams.size());
		//System.out.println("MessageSend f4Result.tmpParams.size(): " + f4Result.tmpParams.size());
		
		
		for (int i = 0; i < f4Result.tmpParams.size(); i++) {
			// Append the parameters
			funCallSrc = funCallSrc + " " + f4Result.tmpParams.get(i);
			// Append the sparrow code for parameters.
			
			//System.out.println("MessageSend f4Result.assignTmpParams.get(i)" + f4Result.assignTmpParams.get(i));
			
			sparrowRes.sparrSrc.addAll(f4Result.assignTmpParams.get(i));
		}
		funCallSrc += ")";
		sparrowRes.sparrSrc.add(funCallSrc);
		sparrowRes.tmpVarName = funCallVar;
		
		String returnTypeMethod = "";
		if (hasFunCallName)
			returnTypeMethod = idTypes.classMethods.get(f0Result.className).get(funCallName);
		if (idTypes.className.contains(returnTypeMethod))
			sparrowRes.className = returnTypeMethod;
		
		curExpClass = "";
		curExpMethod = "";
		return sparrowRes;
	}

	/**
	 * f0 -> Expression() 
	 * f1 -> ( ExpressionRest() )*
	 */
	
	public SparrowShape visit(ExpressionList n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		
//		System.out.println("CreateSparrowVisitor curExpClass: " + curExpClass);
//		System.out.println("CreateSparrowVisitor curExpMethod: " + curExpMethod);
		
		ArrayList<String> methodParamIDs = idTypes.methodParamName.get(curExpClass).get(curExpMethod);
		
		//System.out.println("Create ExpressionList methodParamIDs.size(): " + methodParamIDs.size());
//		for (String s: methodParamIDs) {
//			System.out.println("Create ExpressionList methodParamIDs: " + s);
//		}
		
		SparrowShape firstExp = n.f0.accept(this, idTypes);
		
		//System.out.println("ExpressionList firstExp.sparrSrc: " + firstExp.sparrSrc);
		//System.out.println("ExpressionList firstExp.tmpVarName: " + firstExp.tmpVarName);
		
		sparrowRes.assignTmpParams.add(firstExp.sparrSrc);
		sparrowRes.tmpParams.add(firstExp.tmpVarName);
		
		for (int i = 1; i < methodParamIDs.size(); i++) {
			SparrowShape expRes = n.f1.elementAt(i - 1).accept(this, idTypes);
			sparrowRes.assignTmpParams.add(expRes.sparrSrc);
			sparrowRes.tmpParams.add(expRes.tmpVarName);
		}
		
//		for (List<String> s: sparrowRes.assignTmpParams) {
//			for (String str: s)
//				System.out.println("ExpressionList assignTmpParams: " + str);
//		}
		
		return sparrowRes;
	}

	/**
	 * f0 -> "," 
	 * f1 -> Expression()
	 */
	
	public SparrowShape visit(ExpressionRest n, IDnType idTypes) {
		return n.f1.accept(this, idTypes);
	}

	/**
	 * f0 -> IntegerLiteral() | TrueLiteral() | FalseLiteral() | Identifier() |
	 * ThisExpression() | ArrayAllocationExpression() | AllocationExpression() |
	 * NotExpression() | BracketExpression()
	 */
	
	public SparrowShape visit(PrimaryExpression n, IDnType idTypes) {
		return n.f0.accept(this, idTypes);
	}

	/**
	 * f0 -> <INTEGER_LITERAL>
	 */
	
	public SparrowShape visit(IntegerLiteral n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		String f0Result = n.f0.toString();
		String intVal = getTmpVar();
		sparrowRes.sparrSrc.add(intVal + " = " + f0Result);
		sparrowRes.tmpVarName = intVal;
		
		//System.out.println("Left IntegralLiteral");
		
		return sparrowRes;
	}

	/**
	 * f0 -> "true"
	 */
	
	public SparrowShape visit(TrueLiteral n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		String f0Result = "1";
		String trueVal = getTmpVar();
		sparrowRes.sparrSrc.add(trueVal + " = " + f0Result);
		sparrowRes.tmpVarName = trueVal;
		
		//System.out.println("Left True");
		
		return sparrowRes;
	}

	/**
	 * f0 -> "false"
	 */
	
	public SparrowShape visit(FalseLiteral n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		String f0Result = "0";
		String trueVal = getTmpVar();
		sparrowRes.sparrSrc.add(trueVal + " = " + f0Result);
		sparrowRes.tmpVarName = trueVal;
		
		//System.out.println("Left False");
		
		return sparrowRes;
	}

	/**
	 * f0 -> <IDENTIFIER>
	 */
	
	public SparrowShape visit(Identifier n, IDnType idTypes) {
		
		
		SparrowShape sparrowRes = new SparrowShape();
		String f0Result = n.f0.tokenImage;
		
//		if (f0Result.equals("MyVisitor"))
//			System.out.println("@@@ MyVisitor Entered @@@");
//		System.out.println("Identifier f0Result: " + n.f0.tokenImage);
		
		String curMethod = idTypes.curMethod;
		
//		if(curMethod.equals(""))
//			System.out.println("Identifier CurrentMethod is empty");
//		else
//			System.out.println("Identifier CurrentMethod: " + curMethod);
//		
//		System.out.println("Identifier Current Class: " + idTypes.curClass);
		
		
		if (!curMethod.equals("")) {
			
//			if (f0Result.equals("head")) {
//				
//				ArrayList<String> tmp = idTypes.methodParamName.get(idTypes.curClass).get(curMethod);
//				for (String s: tmp) {
//					System.out.println("")
//				}
//			}
			
//			System.out.println("Identifier CurrentMethod: " + curMethod);
//			System.out.println("Identifier f0Reuslt: " + f0Result);
			
			boolean idInMethodParam = false;
			boolean idInMethodLocalVar = false;
			if (idTypes.methodParamName.containsKey(idTypes.curClass)
				&& idTypes.methodParamName.get(idTypes.curClass).containsKey(curMethod)
				&& idTypes.methodParamName.get(idTypes.curClass).get(curMethod).contains(f0Result)) {
				idInMethodParam = true;
			}
			if (idTypes.methodVar.containsKey(curMethod) && idTypes.methodVar.get(curMethod).containsKey(f0Result))
				idInMethodLocalVar = true;
			
			if (idInMethodParam || idInMethodLocalVar) {
				
				//System.out.println("@@@@ Entered Identifier: @@@@@");
				
				sparrowRes.tmpVarName = f0Result;
				sparrowRes.chkField = false;
				
				String f0Type = "";
				if (idTypes.methodParamName.get(idTypes.curClass).get(curMethod).contains(f0Result)) {
					int indxOfResult = idTypes.methodParamName.get(idTypes.curClass).get(curMethod).indexOf(f0Result);
					f0Type = idTypes.methodParamType.get(idTypes.curClass).get(curMethod).get(indxOfResult);
				}
				else
					f0Type = idTypes.methodVar.get(curMethod).get(f0Result);
				//System.out.println("@@@@@ Identifier f0Type: " + f0Type);
				
				if (idTypes.className.contains(f0Type)) {
					sparrowRes.className = f0Type;
					
					//System.out.println("@@@@@ Identifier sparrowRes.className: " + f0Type);	
				}
			}
			else {
//				System.out.println("!!!! Entered Identifier: !!!!");
//				System.out.println("Create Identifier TokenImage: " + f0Result);
//				System.out.println("Create Identifier idTypes.curClass: " + idTypes.curClass);
				
//				if (idTypes.curClass.equals(""))
//					System.out.println("Identifier curClass is empty");
//				if (!idTypes.classFields.get(idTypes.curClass).containsKey(f0Result))
//					System.out.println("Identifier " + idTypes.curClass + " does not contain f0Result: " + f0Result);
				
				if (!idTypes.curClass.equals("") && idTypes.classFields.get(idTypes.curClass).containsKey(f0Result)) {
					sparrowRes.chkField = true;
					
//					System.out.println("Create Identifier TokenImage: " + f0Result);
//					System.out.println("Create Identifier idTypes.curClass: " + idTypes.curClass);
//					System.out.println("Create Identifier f0Result: " + f0Result);
					
					int pos = idTypes.classFieldsIndx.get(idTypes.curClass).get(f0Result);
					
					//System.out.println("Identifier pos: " + pos);
					
					String tmpVar = getTmpVar();
					sparrowRes.sparrSrc.add(tmpVar + " = " + "[this + " + Integer.toString(4 * pos) + "]");
					sparrowRes.tmpVarName = tmpVar;
				}
			}
		}
		if (sparrowRes.tmpVarName.equals(""))
			sparrowRes.tmpVarName = f0Result;
		
//		System.out.println("identifier: " + f0Result);
//		System.out.println("Left Identifier");
//		for (int i = 0; i < sparrowRes.sparrSrc.size(); i++)
//			System.out.println(sparrowRes.sparrSrc.get(i));
//		System.out.println("Identifier sparrowRes.className: " + sparrowRes.className);
		
		return sparrowRes;
	}

	/**
	 * f0 -> "this"
	 */
	
	public SparrowShape visit(ThisExpression n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		sparrowRes.className = idTypes.curClass;
		sparrowRes.tmpVarName = "this";
		
		//System.out.println("Left This Expression");
		
		return sparrowRes;
	}

	/**
	 * f0 -> "new" 
	 * f1 -> "int" 
	 * f2 -> "[" 
	 * f3 -> Expression() 
	 * f4 -> "]"
	 */
	
	public SparrowShape visit(ArrayAllocationExpression n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		
		SparrowShape f3Result = n.f3.accept(this, idTypes);
		sparrowRes.sparrSrc.addAll(f3Result.sparrSrc);
		
		n.f4.accept(this, idTypes);
		
		String one = getTmpVar();
		sparrowRes.sparrSrc.add(one + " = " + 1);
		String four = getTmpVar();
		sparrowRes.sparrSrc.add(four + " = " + 4);
		String arySize = f3Result.tmpVarName;
		String arySizePlusOne = getTmpVar();
		sparrowRes.sparrSrc.add(arySizePlusOne + " = " + arySize + " + " + one);
		String offset = getTmpVar();
		sparrowRes.sparrSrc.add(offset + " = " + arySizePlusOne + " * " + four);
		String allocVar = getTmpVar();
		sparrowRes.sparrSrc.add(allocVar + " = alloc(" + offset + ")");
		sparrowRes.tmpVarName = allocVar;
		
		// Check null pointter
		sparrowRes.sparrSrc.add("if0 " + allocVar + " goto " + allocVar + "_error");
		sparrowRes.sparrSrc.add("goto " + allocVar + "_end");
		sparrowRes.sparrSrc.add(allocVar + "_error:");
		sparrowRes.sparrSrc.add("error(\"null pointer\")");
		sparrowRes.sparrSrc.add(allocVar + "_end:");
		
		// First element of array should have the array length
		sparrowRes.sparrSrc.add("[" + allocVar + " + 0] = " + arySize);
		
		
		//System.out.println("Left ArrayAllocationExpres");
		
		
		return sparrowRes;
	}

	/**
	 * f0 -> "new" 
	 * f1 -> Identifier() 
	 * f2 -> "(" 
	 * f3 -> ")"
	 */
	
	public SparrowShape visit(AllocationExpression n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		String className = n.f1.f0.tokenImage;
		
		// Assign the memory of fields
		String idVar = getTmpVar();
		String lenFieldsVar = getTmpVar();
		
//		System.out.println("AllocationExpression className: " + className);
		
		int lenFields = idTypes.classFields.get(className).size() + 1;
		sparrowRes.sparrSrc.add(lenFieldsVar + " = " + Integer.toString(lenFields * 4));
		sparrowRes.sparrSrc.add(idVar + " = alloc(" + lenFieldsVar + ")");
		
		// Assign the memory of the method table.
		int methodTbSize = idTypes.classMethodsCnt.get(className);
		
		//System.out.println("AllocationExpression methodTbSize: " + methodTbSize);
		
		if (methodTbSize != 0) {
			String methodTbSizeVar = getTmpVar();
			sparrowRes.sparrSrc.add(methodTbSizeVar + " = " + Integer.toString(methodTbSize * 4));
			sparrowRes.sparrSrc.add("vmt_" + className + " = alloc(" + methodTbSizeVar + ")");
			for (String method: idTypes.classMethodsIndx.get(className).keySet()) {
				String methodTmp = getTmpVar();
				String parentClass = idTypes.childToParent.get(className);
				sparrowRes.sparrSrc.add(methodTmp + " = @" + className + method);
				
//				if (!parentClass.equals("") && idTypes.classMethods.get(parentClass).containsKey(method)) {
//					
//					sparrowRes.sparrSrc.add(methodTmp + " = @" + parentClass + method);
//					
//					//System.out.println("AllocationExpression @className+method: " + "@" + className + method);
//					
//				}
//				else {
					
					//sparrowRes.sparrSrc.add(methodTmp + " = @" + className + method);
						
					//System.out.println("AllocationExpression @parentClass + method: " + "@" + parentClass + method);
					
				//}
				
				//System.out.println("AllocationExpression method: " + method);
				
				int methodIndx = idTypes.classMethodsIndx.get(className).get(method);
				sparrowRes.sparrSrc.add("[vmt_" + className + " + " + Integer.toString(methodIndx * 4) + " ] = " + methodTmp);
			}
			sparrowRes.sparrSrc.add("[" + idVar + " + 0] = vmt_" + className);
		}
		sparrowRes.tmpVarName = idVar;
		sparrowRes.className = className;
		
		// Check null pointter
		sparrowRes.sparrSrc.add("if0 " + idVar + " goto " + idVar + "_error");
		sparrowRes.sparrSrc.add("goto " + idVar + "_end");
		sparrowRes.sparrSrc.add(idVar + "_error:");
		sparrowRes.sparrSrc.add("error(\"null pointer\")");
		sparrowRes.sparrSrc.add(idVar + "_end:");
		
		//System.out.println("Left AllocationExpression");
		
		return sparrowRes;
	}

	/**
	 * f0 -> "!" 
	 * f1 -> Expression()
	 */
	
	public SparrowShape visit(NotExpression n, IDnType idTypes) {
		
		SparrowShape sparrowRes = new SparrowShape();
		SparrowShape f1Result = n.f1.accept(this, idTypes);
		sparrowRes.sparrSrc.addAll(f1Result.sparrSrc);
		
		String one = getTmpVar();
		sparrowRes.sparrSrc.add(one + " = 1");
		String notExpRes = getTmpVar();
		sparrowRes.sparrSrc.add(notExpRes + " = " + one + " - " + f1Result.tmpVarName);
		sparrowRes.tmpVarName = notExpRes;
		
		//System.out.println("Left NotExpression");
		
		return sparrowRes;
	}

	/**
	 * f0 -> "(" 
	 * f1 -> Expression() 
	 * f2 -> ")"
	 */
	
	public SparrowShape visit(BracketExpression n, IDnType idTypes) {
		
		//System.out.println("Left BracketExpression");
		
		return n.f1.accept(this, idTypes);
	}
}
