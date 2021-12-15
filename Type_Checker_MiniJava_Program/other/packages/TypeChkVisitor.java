package other.packages;
import cs132.minijava.syntaxtree.*;
import cs132.minijava.visitor.*;
import cs132.minijava.*;
import other.packages.IDnType;
import java.util.*;

public class TypeChkVisitor implements GJVisitor<String, IDnType> {

	public String visit(NodeList n, IDnType idTypes) {
		
		for (int i = 0; i < n.size(); i++) {
			if (n.elementAt(i).accept(this, idTypes).equals("Type error"))
				return "Type error";
		}
		return "Type correct";
	}

	public String visit(NodeListOptional n, IDnType idTypes) {

		
		for (int i = 0; i < n.size(); i++) {
			if (n.elementAt(i).accept(this, idTypes).equals("Type error"))
				return "Type error";
		}
		return "Type correct";
	}

	public String visit(NodeOptional n, IDnType idTypes) {
		
		if (n.present())
			return n.node.accept(this, idTypes);
		else
			return "";
	}

	public String visit(NodeSequence n, IDnType idTypes) {
		
		for (int i = 0; i < n.nodes.size(); i++) {
			n.nodes.get(i).accept(this, idTypes);
		}
		return "";
	}

	public String visit(NodeToken n, IDnType idTypes) { return ""; }


	/**
	 * f0 -> MainClass() 
	 * f1 -> ( TypeDeclaration() )* <- NodeListOptional
	 * f2 -> <EOF>
	 */
	public String visit(Goal n, IDnType idTypes) {
		
		//System.out.println("Entered in Goal");
		
		String f0Result = n.f0.accept(this, idTypes);
		String f1Result = n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		
		/*
		if (f0Result.equals("Type error"))
			System.out.println("Error Goal f0Result");
		if (f1Result.equals("Type error"))
			System.out.println("Error Goal f1Result");
		*/
		
		
		if (f0Result.equals("Type error") || f1Result.equals("Type error"))
			return "Type error";
		else
			return "Type correct";
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
	 * f14 -> ( VarDeclaration() ) * <- NodeListOptional
	 * f15 -> (Statement() ) * <- NodeListOptional
	 * f16 -> "}" 
	 * f17 -> "}"
	 */
	
	public String visit(MainClass n, IDnType idTypes) {
		
		
		//System.out.println("MainClass idTypes.isInMethod:" + idTypes.isInMethod);
		
		
		n.f0.accept(this, idTypes);
		
		String f1Result = n.f1.accept(this, idTypes);
		
		// Put the id of mainclass into the className
		if (idTypes.className.contains(f1Result))
			idTypes.curClass = f1Result;


		//System.out.println("MainClass f1");

		n.f2.accept(this, idTypes);
		n.f3.accept(this, idTypes);
		n.f4.accept(this, idTypes);
		n.f5.accept(this, idTypes);
		n.f6.accept(this, idTypes);
		n.f7.accept(this, idTypes);
		n.f8.accept(this, idTypes);
		n.f9.accept(this, idTypes);
		n.f10.accept(this, idTypes);
		n.f11.accept(this, idTypes);
		n.f12.accept(this, idTypes);
		n.f13.accept(this, idTypes);
		
		
		//System.out.println("@MainClass idTypes.isInMethod:" + idTypes.isInMethod);
		//System.out.println("MainClass f13");


		// Put all id and types.
		n.f14.accept(this, idTypes);
		if (n.f15.accept(this, idTypes).equals("Type error"))
			return "Type error";

		//System.out.println("MainClass f15");

		n.f16.accept(this, idTypes);
		n.f17.accept(this, idTypes);
		
		idTypes.curClass = "";
		
		return "Type correct";
	}

	/**
	 * f0 -> ClassDeclaration() | ClassExtendsDeclaration()
	 */
	public String visit(TypeDeclaration n, IDnType idTypes) {
		
		//System.out.println("TypeDeclaration Entered");
		
		String f0Result = n.f0.accept(this, idTypes);
		
		//System.out.println("TypeDeclaration f0Result: " + f0Result);
		
		return f0Result;
	}

	/**
	 * f0 -> "class" 
	 * f1 -> Identifier() 
	 * f2 -> "{" 
	 * f3 -> ( VarDeclaration() )* <- NodeListOptional
	 * f4 -> ( MethodDeclaration() )* <- NodeListOptional
	 * f5 -> "}"
	 */
	public String visit(ClassDeclaration n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		String f1Result = n.f1.accept(this, idTypes);
		// Check whether the name of class already exists in a class name.
		if (idTypes.className.contains(f1Result))
			idTypes.curClass = f1Result;
		
		//System.out.println("ClassDeclaration f1Result: " + f1Result);
		
		n.f2.accept(this, idTypes);
		String f3Result = (n.f3.accept(this, idTypes));
		String f4Result = (n.f4.accept(this, idTypes)); 
		
		//System.out.println("ClassDeclaration f3Result: " + f3Result);
		//System.out.println("ClassDeclaration f4Result: " + f4Result);
		
		
		if (f3Result.equals("Type error") || f4Result.equals("Type error"))
			return "Type error";
		
		n.f5.accept(this, idTypes);
		idTypes.curClass = "";
		
		return "Type correct";
	}

	/**
	 * f0 -> "class" 
	 * f1 -> Identifier() 
	 * f2 -> "extends" 
	 * f3 -> Identifier() 
	 * f4 -> "{"
	 * f5 -> ( VarDeclaration() )* <- NodeListOptional
	 * f6 -> ( MethodDeclaration() )* <- NodeListOptional
	 * f7 -> "}"
	 */
	public String visit(ClassExtendsDeclaration n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		String f1Result = n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		n.f3.accept(this, idTypes);
		
		// Check whether the name of class already exists in a class name.
		if (idTypes.className.contains(f1Result))
			idTypes.curClass = f1Result;
		
		n.f4.accept(this, idTypes);
		String f5Result = n.f5.accept(this, idTypes);
		String f6Result = n.f6.accept(this, idTypes);
		
		if (f5Result.equals("Type error") || f6Result.equals("Type error"))
			return "Type error";
		
		n.f7.accept(this, idTypes);
		idTypes.curClass = "";
		
		return "Type correct";
	}

	/**
	 * f0 -> Type() 
	 * f1 -> Identifier() 
	 * f2 -> ";"
	 */
	public String visit(VarDeclaration n, IDnType idTypes) {
		
		//System.out.println("Vardeclaration Entered");
		
		String f0Result = n.f0.accept(this, idTypes);
		
		//System.out.println("Vardeclaration f0Result: " + f0Result);
		
		if (!idTypes.types.contains(f0Result)) {
			
			//System.out.println("types doesn't contain the type: " + f0Result);
			
			return "Type error";
		}
		
		n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		
		return "Type correct";
	}

	/**
	 * f0 -> "public" 
	 * f1 -> Type() 
	 * f2 -> Identifier() 
	 * f3 -> "(" 
	 * f4 -> (FormalParameterList() )? <- NodeListOptional
	 * f5 -> ")" 
	 * f6 -> "{" 
	 * f7 -> ( VarDeclaration() )* <- NodeListOptional
	 * f8 -> ( Statement() )* <- NodeListOptional
	 * f9 -> "return" 
	 * f10 -> Expression() 
	 * f11 -> ";" 
	 * f12 -> "}"
	 */
	public String visit(MethodDeclaration n, IDnType idTypes) {
		
		
		//System.out.println("MethodDeclaration Entered");
		
		n.f0.accept(this, idTypes);
		
		String f1Result = n.f1.accept(this, idTypes);
		String f2Result = n.f2.accept(this, idTypes);
		idTypes.curMethod = f2Result;
		idTypes.isInMethod = true;
		
		n.f3.accept(this, idTypes);
		n.f4.accept(this, idTypes);
		n.f5.accept(this, idTypes);
		n.f6.accept(this, idTypes);
		n.f7.accept(this, idTypes);
		
		if (n.f8.accept(this, idTypes).equals("Type error")) {
			
			//System.out.println("Error MethodDeclaration f8Result: Type error");
			
			return "Type error";
		}
		
		n.f9.accept(this, idTypes);
		
		String f10Result = n.f10.accept(this, idTypes);
		if (f10Result.equals("Type error")) {
			
			//System.out.println("Error MethodDeclaration f10Result: Type error");
			
			return "Type error";
		}
		String f10Type = idTypes.findType(f10Result);
		if (!f10Type.equals("") && !idTypes.types.contains(f10Result))
			f10Result = f10Type;
		
		n.f11.accept(this, idTypes);
		n.f12.accept(this, idTypes);
		
		idTypes.curMethod = "";
		idTypes.isInMethod = false;
		
		//System.out.println("MethodDeclaration f1Result: " + f1Result);
		//System.out.println("MethodDeclaration f10Result: " + f10Result);
		
		/*
		if (!f1Result.equals(f10Result))
			System.out.println("Error MethodDeclaration f1Result and f10Result are different");
		*/
		
		return f1Result.equals(f10Result) ? "Type correct" : "Type error";
	}

	/**
	 * f0 -> FormalParameter()
	 * f1 -> ( FormalParameterRest() ) <- NodeListOptional!!
	 */
	public String visit(FormalParameterList n, IDnType idTypes) {
		
		String f0Result = n.f0.accept(this, idTypes);
		if (f0Result.equals("Type error"))
			return "Type error";
		
		String f1Result = n.f1.accept(this, idTypes);
		if (f1Result.equals("Type error"))
			return "Type error";
		
		return "Type correct";
	}
	/**
	 * f0 -> Type() 
	 * f1 -> Identifier()
	 */
	public String visit(FormalParameter n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		return "Type correct";
	}

	/**
	 * f0 -> "," 
	 * f1 -> FormalParameter()
	 */
	public String visit(FormalParameterRest n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		return n.f1.accept(this, idTypes);
	}

	/**
	 * f0 -> ArrayType() | BooleanType() | IntegerType() | Identifier()
	 */
	public String visit(Type n, IDnType idTypes) {
		
		String f0Result =  n.f0.accept(this, idTypes);
		return idTypes.types.contains(f0Result) ? f0Result : "Type error";
	}

	/**
	 * f0 -> "int" 
	 * f1 -> "[" 
	 * f2 -> "]"
	 */
	public String visit(ArrayType n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		return "intArray";
	}

	/**
	 * f0 -> "boolean"
	 */
	public String visit(BooleanType n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		return "boolean";
	}

	/**
	 * f0 -> "int"
	 */
	public String visit(IntegerType n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		return "int";
	}

	/**
	 * f0 -> Block() | AssignmentStatement() | ArrayAssignmentStatement() |
	 * IfStatement() | WhileStatement() | PrintStatement()
	 */
	public String visit(Statement n, IDnType idTypes) {
		
		String f0Result = n.f0.accept(this, idTypes);
		return f0Result;
	}

	/**
	 * f0 -> "{" 
	 * f1 -> ( Statement() ) 
	 * f2 -> "}"
	 */
	public String visit(Block n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		String f1Result = n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		
		return f1Result;
	}

	/**
	 * f0 -> Identifier() 
	 * f1 -> "=" 
	 * f2 -> Expression() 
	 * f3 -> ";"
	 */
	public String visit(AssignmentStatement n, IDnType idTypes) {
		
		String f0Result = n.f0.accept(this, idTypes);
		String f0Type = idTypes.findType(f0Result);
		if (!f0Type.equals(""))
			f0Result = f0Type;
		
		n.f1.accept(this, idTypes);
		
		String f2Result = n.f2.accept(this, idTypes);
		String f2Type = idTypes.findType(f2Result);
		// Check out whether the type of f2Type doesn't belong to both class type and general data tyes.
		if (f2Type.equals("") && !idTypes.types.contains(f2Result))
			return "Type error";
		if (!f2Type.equals(""))
			f2Result = f2Type;
		
		n.f3.accept(this, idTypes);
		
		/*
		System.out.println("AssignmentStatement f0Result: " + f0Result);
		System.out.println("AssignmentStatement f2Result: " + f2Result);
		if (!f0Result.equals(f2Result))
			System.out.println("Error AssignemtStatement");
		*/
		return f0Result.equals(f2Result) ? "Type correct" : "Type error";
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
	public String visit(ArrayAssignmentStatement n, IDnType idTypes) {
		
		String f0Result = n.f0.accept(this, idTypes);
		String f0Type = idTypes.findType(f0Result);
		if (!f0Type.equals(""))
			f0Result = f0Type;
		else
			return "Type error";
		
		n.f1.accept(this, idTypes);
		
		String f2Result = n.f2.accept(this, idTypes);
		String f2Type = idTypes.findType(f2Result);
		// Check out whether the type of f2Type doesn't belong to both class type and general data tyes.
		if (f2Type.equals("") && !idTypes.types.contains(f2Result))
			return "Type error";
		if (!f2Type.equals(""))
			f2Result = f2Type;
		
		n.f3.accept(this, idTypes);
		n.f4.accept(this, idTypes);
		
		String f5Result = n.f5.accept(this, idTypes);
		String f5Type = idTypes.findType(f5Result);
		if (f5Type.equals("") && !idTypes.types.contains(f5Result))
			return "Type error";
		if (!f5Type.equals(""))
			f5Result = f5Type;
		
		n.f6.accept(this, idTypes);
		
		return (f0Result.equals("intArray") && f2Result.equals("int") && f5Result.equals("int")) ? "Type correct" : "Type error";
		
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
	public String visit(IfStatement n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		
		String f2Result = n.f2.accept(this, idTypes);
		String f2Type = idTypes.findType(f2Result);
		if (f2Type.equals("") && !idTypes.types.contains(f2Result))
			return "Type error";
		if (!f2Type.equals(""))
			f2Result = f2Type;
		
		n.f3.accept(this, idTypes);
		
		String f4Result = n.f4.accept(this, idTypes);
		
		n.f5.accept(this, idTypes);
		
		String f6Result = n.f6.accept(this, idTypes);
		
		if (f4Result.equals("Type error") || f6Result.equals("Type error"))
			return "Type error";
		
		return f2Result.equals("boolean") ? "Type correct" : "Type error";
	}

	/**
	 * f0 -> "while" 
	 * f1 -> "(" 
	 * f2 -> Expression() 
	 * f3 -> ")" 
	 * f4 -> Statement()
	 */
	public String visit(WhileStatement n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		
		String f2Result = n.f2.accept(this, idTypes);
		String f2Type = idTypes.findType(f2Result);
		if (f2Type.equals("") && !idTypes.types.contains(f2Result))
			return "Type error";
		if (!f2Type.equals(""))
			f2Result = f2Type;
		
		n.f3.accept(this, idTypes);
		
		String f4Result = n.f4.accept(this, idTypes);
		
		if (f4Result.equals("Type error"))
			return "Type error";
		
		return f2Result.equals("boolean") ? "Type correct" : "Type error";
	}

	/**
	 * f0 -> "System.out.println" 
	 * f1 -> "(" 
	 * f2 -> Expression() 
	 * f3 -> ")" 
	 * f4 -> ";"
	 */
	public String visit(PrintStatement n, IDnType idTypes) {
		
		
		//System.out.println("PrintStatement Entered");
		
		
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		
		String f2Result = n.f2.accept(this, idTypes);
		String f2Type = idTypes.findType(f2Result);
		if (f2Type.equals("") && !idTypes.types.contains(f2Result))
			return "Type error";
		if (!f2Type.equals(""))
			f2Result = f2Type;
		
		n.f3.accept(this, idTypes);
		n.f4.accept(this, idTypes);
		
		//System.out.println("PrintStatement f2Result: " + f2Result);
		
		
		return f2Result.equals("int") ? "Type correct" : "Type error";
	}

	/**
	 * f0 -> AndExpression() | CompareExpression() | PlusExpression() |
	 * MinusExpression() | TimesExpression() | ArrayLookup() | ArrayLength() |
	 * MessageSend() | PrimaryExpression()
	 */
	public String visit(Expression n, IDnType idTypes) {
		
		//System.out.println("Expression Entered");
		
		return n.f0.accept(this, idTypes);
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "&&" 
	 * f2 -> PrimaryExpression()
	 */
	public String visit(AndExpression n, IDnType idTypes) {
		
		String f0Result = n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		String f2Result = n.f2.accept(this, idTypes);
		
		String f0IdType = idTypes.findType(f0Result);
		String f2IdType = idTypes.findType(f2Result);
		if (!f0IdType.equals(""))
			f0Result = f0IdType;
		if (!f2IdType.equals(""))
			f2Result = f2IdType;
		
		/*
		if (!f0Result.equals("boolean"))
			System.out.println("Error AndExpression f0Result should be boolean, but f0Result: " + f0Result);
		*/
		
		return f0Result.equals("boolean") && f2Result.equals("boolean") ? "boolean" : "Type error";
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "<" 
	 * f2 -> PrimaryExpression()
	 */
	public String visit(CompareExpression n, IDnType idTypes) {

		String f0Result = n.f0.accept(this, idTypes);																		
		n.f1.accept(this, idTypes);
		String f2Result = n.f2.accept(this, idTypes);
		
		String f0IdType = idTypes.findType(f0Result);
		String f2IdType = idTypes.findType(f2Result);
		if (!f0IdType.equals(""))
			f0Result = f0IdType;
		if (!f2IdType.equals(""))
			f2Result = f2IdType;
		
		/*
		if (!(f0Result.equals("int") && f2Result.equals("int")))
			System.out.println("Error CompareExpression f0Result: " + f0Result + ", f2Result: " + f2Result);
		*/
		
		
		return f0Result.equals("int") && f2Result.equals("int") ? "boolean" : "Type error";
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "+" 
	 * f2 -> PrimaryExpression()
	 */
	public String visit(PlusExpression n, IDnType idTypes) {
		
		String f0Result = n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		String f2Result = n.f2.accept(this, idTypes);
		
		String f0IdType = idTypes.findType(f0Result);
		String f2IdType = idTypes.findType(f2Result);
		if (!f0IdType.equals(""))
			f0Result = f0IdType;
		if (!f2IdType.equals(""))
			f2Result = f2IdType;
		
		/*
		if (!(f0Result.equals("int") && f2Result.equals("int")))
			System.out.println("Error PlusExpression f0Result: " + f0Result + ", f2Result: " + f2Result);
		*/
		
		return f0Result.equals("int") && f2Result.equals("int") ? "int" : "Type error";
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "-" 
	 * f2 -> PrimaryExpression()
	 */
	public String visit(MinusExpression n, IDnType idTypes) {
		
		String f0Result = n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		String f2Result = n.f2.accept(this, idTypes);
		
		String f0IdType = idTypes.findType(f0Result);
		String f2IdType = idTypes.findType(f2Result);
		if (!f0IdType.equals(""))
			f0Result = f0IdType;
		if (!f2IdType.equals(""))
			f2Result = f2IdType;
		
		/*
		if (!(f0Result.equals("int") && f2Result.equals("int")))
			System.out.println("Error MinusExpression f0Result: " + f0Result + ", f2Result: " + f2Result);
		*/
		
		
		return f0Result.equals("int") && f2Result.equals("int") ? "int" : "Type error";
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "*" 
	 * f2 -> PrimaryExpression()
	 */
	public String visit(TimesExpression n, IDnType idTypes) {
		
		String f0Result = n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		String f2Result = n.f2.accept(this, idTypes);
		
		String f0IdType = idTypes.findType(f0Result);
		String f2IdType = idTypes.findType(f2Result);
		if (!f0IdType.equals(""))
			f0Result = f0IdType;
		if (!f2IdType.equals(""))
			f2Result = f2IdType;
		
		/*
		if (!(f0Result.equals("int") && f2Result.equals("int")))
			System.out.println("Error TimesExpression f0Result: " + f0Result + ", f2Result: " + f2Result);
		*/
		
		
		return f0Result.equals("int") && f2Result.equals("int") ? "int" : "Type error";
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "[" 
	 * f2 -> PrimaryExpression() 
	 * f3 -> "]"
	 */
	public String visit(ArrayLookup n, IDnType idTypes) {
		
		String f0Result = n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		String f2Result = n.f2.accept(this, idTypes);
		n.f3.accept(this, idTypes);
		
		String f0IdType = idTypes.findType(f0Result);
		String f2IdType = idTypes.findType(f2Result);
		if (!f0IdType.equals(""))
			f0Result = f0IdType;
		if (!f2IdType.equals(""))
			f2Result = f2IdType;
		
		/*
		if (!(f0Result.equals("intArray") && f2Result.equals("int")))
			System.out.println("Error ArrayLookup f0Result: " + f0Result + ", f2Result: " + f2Result);
		*/
		
		return f0Result.equals("intArray") && f2Result.equals("int") ? "int" : "Type error";
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "." 
	 * f2 -> "length"
	 */
	public String visit(ArrayLength n, IDnType idTypes) {
		
		String f0Result = n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		
		String f0IdType = idTypes.findType(f0Result);
		if (!f0IdType.equals(""))
			f0Result = f0IdType;
		
		/*
		if (!f0Result.equals("intArray"))
			System.out.println("Error ArrayLength f0Result: " + f0Result);
		*/
		
		if (f0Result.equals("intArray"))
			return "int";
		else
			return "Type error";
		
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "." 
	 * f2 -> Identifier() 
	 * f3 -> "(" 
	 * f4 -> (ExpressionList() )? <- NodeListOptional
	 * f5 -> ")"
	 */
	public String visit(MessageSend n, IDnType idTypes) {
		
		
		//System.out.println("MessageSend entered");
		
		
		String f0Result = n.f0.accept(this, idTypes);
		
		n.f1.accept(this, idTypes);
		
		String f2Result = n.f2.accept(this, idTypes);
		
		n.f3.accept(this, idTypes);
		
		ArrayList<String> expresList = idTypes.expressionList;
		// We should clear expresList before adding new expression elements into list.
		expresList.clear();
		
		String f4Result = n.f4.accept(this, idTypes);
		n.f5.accept(this, idTypes);
		
		if (f4Result.equals("Type error")) {
			
			//System.out.println("Error MessageSend f4Result: Type error");
			
			return "Type error";
		}
		
		// f0Result should be the name of class
		// f2Result should be the method belonging to f0Result class.
		String returnType = "";
		if (idTypes.isInMethod && !idTypes.types.contains(f0Result))
			f0Result = idTypes.findType(f0Result);
		if (!idTypes.className.contains(f0Result) || !idTypes.classMethods.get(f0Result).containsKey(f2Result)) {
			
			
			//System.out.println("Error MessageSend @@@@@@@@@@");
			
			return "Type error";
		}
		// Check out the type of the experssionlist.
		else {
			// Get the type of the variable in method
			ArrayList<String> methodExprList = idTypes.methodParamType.get(f0Result).get(f2Result);
			returnType = idTypes.classMethods.get(f0Result).get(f2Result);
			// 1. Compare the length of expresList with the type of the variable in method.
			if (expresList.size() != methodExprList.size())
				return "Type error";
			// 2. Compare each type of expresList with the each type of the variable in method.
			for (int i = 0; i < expresList.size(); i++) {
				
				if (!expresList.get(i).equals(methodExprList.get(i))) {
					// We should consider whether the type class is child or not.
					boolean isChild = false;
					String childClass = expresList.get(i);
					String parentClass = idTypes.childToParent.get(childClass);
					while (!parentClass.equals("")) {
						if (methodExprList.get(i).equals(parentClass)) {
							isChild = true;
							break;
						}
						parentClass = idTypes.childToParent.get(childClass);
					}
					if (!isChild)
						return "Type error";
				}
			}
		}
		return returnType;
	}

	/**
	 * f0 -> Expression() 
	 * f1 -> ( ExpressionRest() )* <- NodeListOptional!!!!
	 */
	public String visit(ExpressionList n, IDnType idTypes) {
		
		ArrayList<String> expresList = idTypes.expressionList;
		String f0Result = n.f0.accept(this, idTypes);
		
		String f0IdType = idTypes.findType(f0Result);
		if (!f0IdType.equals(""))
			f0Result = f0IdType;
		if (!idTypes.types.contains(f0Result) && !idTypes.className.contains(f0Result))
			return "Type error";
		// Add expression to expresList
		expresList.add(f0Result);
		
		if (n.f1.accept(this, idTypes).equals("Type error"))
			return "Type error";
		return "Type correct";
	}

	/**
	 * f0 -> "," 
	 * f1 -> Expression()
	 */
	public String visit(ExpressionRest n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		
		String f1Result = n.f1.accept(this, idTypes);
		String f1IdType = idTypes.findType(f1Result);
		if (!f1IdType.equals(""))
			f1Result = f1IdType;
		if (f1Result.equals("Type error"))
			return "Type error";
		else {
			idTypes.expressionList.add(f1Result);
			return f1Result;
		}
	}

	/**
	 * f0 -> IntegerLiteral() | TrueLiteral() | FalseLiteral() | Identifier() |
	 * ThisExpression() | ArrayAllocationExpression() | AllocationExpression() |
	 * NotExpression() | BracketExpression()
	 */
	public String visit(PrimaryExpression n, IDnType idTypes) {
		
		//System.out.println("PrimaryExpression Entered");
		String f0Result = n.f0.accept(this, idTypes);
		//System.out.println("PrimaryExpression f0Result: " + f0Result);
		
		return f0Result;
	}

	/**
	 * f0 -> <INTEGER_LITERAL>
	 */
	public String visit(IntegerLiteral n, IDnType idTypes) {
		n.f0.accept(this, idTypes);
		return "int";
	}

	/**
	 * f0 -> "true"
	 */
	public String visit(TrueLiteral n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		return "boolean";
	}

	/**
	 * f0 -> "false"
	 */
	public String visit(FalseLiteral n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		return "boolean";
	}

	/**
	 * f0 -> <IDENTIFIER>
	 */
	public String visit(Identifier n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		return n.f0.tokenImage;
	}

	/**
	 * f0 -> "this"
	 */
	public String visit(ThisExpression n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		
		return idTypes.curClass.equals("") ? "Type error" : idTypes.curClass;
	}

	/**
	 * f0 -> "new" 
	 * f1 -> "int" 
	 * f2 -> "[" 
	 * f3 -> Expression() 
	 * f4 -> "]"
	 */
	public String visit(ArrayAllocationExpression n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		String f3Result = n.f3.accept(this, idTypes);
		n.f4.accept(this, idTypes);
		
		String f3IdType = idTypes.findType(f3Result);
		if (!f3IdType.equals(""))
			f3Result = f3IdType;
		
		return f3Result.equals("int") ? "intArray" : "Type error";
	}

	/**
	 * f0 -> "new" 
	 * f1 -> Identifier() 
	 * f2 -> "(" 
	 * f3 -> ")"
	 */
	public String visit(AllocationExpression n, IDnType idTypes) {
		
		
		//System.out.println("AllocationExpression entered");
		
		
		n.f0.accept(this, idTypes);
		String f1Result = n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		n.f3.accept(this, idTypes);
		
		
		//System.out.println("AllocationExpression f1Result: " + f1Result);
		
		
		if (!idTypes.className.contains(f1Result))
			return "Type error";
		else
			return f1Result;
	}

	/**
	 * f0 -> "!" 
	 * f1 -> Expression()
	 */
	public String visit(NotExpression n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		String f1Result = n.f1.accept(this, idTypes);
		
		String f1IdType = idTypes.findType(f1Result);
		if (!f1IdType.equals(""))
			f1Result = f1IdType;
		
		return f1Result.equals("boolean") ? "boolean" : "Type error";
	}

	/**
	 * f0 -> "(" 
	 * f1 -> Expression() 
	 * f2 -> ")"
	 */
	public String visit(BracketExpression n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		
		String f1Result = n.f1.accept(this, idTypes);
		String f1IdType = idTypes.findType(f1Result);
		if (!f1IdType.equals(""))
			f1Result = f1IdType;
		
		n.f2.accept(this, idTypes);
		
		return f1Result;
	}
}
