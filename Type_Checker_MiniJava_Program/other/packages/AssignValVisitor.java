package other.packages;
import cs132.minijava.syntaxtree.*;
import cs132.minijava.visitor.*;
import cs132.minijava.*;
import java.util.*;

public class AssignValVisitor implements GJVisitor<String, IDnType> {

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
			return "Type error";
		else {
			idTypes.curClass = f1Result;
			idTypes.className.add(f1Result);
			idTypes.childToParent.put(f1Result, "");
			idTypes.types.add(f1Result);
			idTypes.classFields.put(f1Result, new HashMap<String, String>());
			idTypes.classMethods.put(f1Result, new HashMap<String, String>());
			idTypes.methodParamName.put(f1Result, new HashMap<String, ArrayList<String>>());
			idTypes.methodParamType.put(f1Result, new HashMap<String, ArrayList<String>>());
		}
		


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
		String f14Result = n.f14.accept(this, idTypes);
		if (f14Result.equals("Type error"))
			return "Type error";
		
		n.f15.accept(this, idTypes);
		

		//System.out.println("MainClass f15");
		

		n.f16.accept(this, idTypes);
		n.f17.accept(this, idTypes);
		
		return "Type correct";
	}

	/**
	 * f0 -> ClassDeclaration() | ClassExtendsDeclaration()
	 */
	public String visit(TypeDeclaration n, IDnType idTypes) {
		
		//System.out.println("TypeDeclaration Entered");
		
		String f0Result = n.f0.accept(this, idTypes);
		
		//System.out.println("TypeDeclaration idTypes.isInMethod:" + idTypes.isInMethod);
		
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
			return "Type error";
		else {
			idTypes.curClass = f1Result;
			idTypes.className.add(f1Result);
			idTypes.childToParent.put(f1Result, "");
			idTypes.types.add(f1Result);
			idTypes.classFields.put(f1Result, new HashMap<String, String>());
			idTypes.classMethods.put(f1Result, new HashMap<String, String>());
			idTypes.methodParamName.put(f1Result, new HashMap<String, ArrayList<String>>());
			idTypes.methodParamType.put(f1Result, new HashMap<String, ArrayList<String>>());
		}
		
		n.f2.accept(this, idTypes);
		String f3Result = (n.f3.accept(this, idTypes));
		String f4Result = (n.f4.accept(this, idTypes)); 
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
		String f3Result = n.f3.accept(this, idTypes);
		
		// Check whether the name of class already exists in a class name.
		if (idTypes.className.contains(f1Result) || !idTypes.className.contains(f3Result) || f1Result.equals(f3Result))
			return "Type error";
		else {
			idTypes.curClass = f1Result;
			idTypes.className.add(f1Result);
			idTypes.childToParent.put(f1Result, f3Result);
			idTypes.types.add(f1Result);
			idTypes.classFields.put(f1Result, new HashMap<String, String>());
			idTypes.classMethods.put(f1Result, new HashMap<String, String>());
			idTypes.methodParamName.put(f1Result, new HashMap<String, ArrayList<String>>());
			idTypes.methodParamType.put(f1Result, new HashMap<String, ArrayList<String>>());
		}
		
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
		//System.out.println("Vardeclaration idTypes.isInMethod: " + idTypes.isInMethod);
		
		
		String f0Result = n.f0.accept(this, idTypes);
		//System.out.println("VarDeclaration f0Result: " + f0Result);
		
		String f1Result = n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		
		// Case 1. Put id and types into "current method". 
		if (idTypes.isInMethod) {
			
			
			//System.out.println("varDeclaration idTypes.isInMethod == True");
			
			
			String curSearchMethod = idTypes.curMethod;
			
			if ((idTypes.methodParamName.get(idTypes.curClass).containsKey(curSearchMethod)) && 
				(idTypes.methodParamType.get(idTypes.curClass).containsKey(curSearchMethod))) {

				ArrayList<String> varList = idTypes.methodParamName.get(idTypes.curClass).get(idTypes.curMethod);
				// Check out whether the method has same name of parameter with f1Result.
				if (varList.contains(f1Result))
					return "Type error";
				
				// Check out whether the method has same name of variable with f1Result.
				if (idTypes.methodVar.get(curSearchMethod).containsKey(f1Result))
					return "Type error";
				// Put id and type into methodVar
				else
					idTypes.methodVar.get(curSearchMethod).put(f1Result, f0Result);
			}
			else
				return "Type error";
		}
		// Case 2. Put id and types into "current class".
		else {
			
			String curSearchClass = idTypes.curClass;
			
			// Check out wheteher there is current class in classFields and whether current class already has same name of variable with f1Result.
			if (idTypes.classFields.containsKey(curSearchClass) && !idTypes.classFields.get(curSearchClass).containsKey(f1Result)) {
				HashMap<String, String> idTypeHM = idTypes.classFields.get(curSearchClass);
				idTypeHM.put(f1Result, f0Result);
			}
			else {
				
				//System.out.println("varDeclaration f2");
				
				return "Type error";
				
			}
		}
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
		// check out whethere the type of method exists or not.
		if (!idTypes.types.contains(f1Result) && !idTypes.className.contains(f1Result))
			return "Type error";
		
		String f2Result = n.f2.accept(this, idTypes);
		String curClass = idTypes.curClass;
		idTypes.curMethod = f2Result;
		idTypes.isInMethod = true;
		// check out whether the class already has the same name of method.
		if (idTypes.classMethods.get(curClass).containsKey(f2Result))
			return "Type error";
		// put the type and id of method
		else {
			HashMap<String, String> classMethod = idTypes.classMethods.get(curClass);
			classMethod.put(f2Result, f1Result);
		}
		if (idTypes.methodParamName.get(curClass).containsKey(f2Result))
			return "Type error";
		else {
			HashMap<String, ArrayList<String>> methodParamName = idTypes.methodParamName.get(curClass);
			methodParamName.put(f2Result, new ArrayList<String>());
		}
		if (idTypes.methodParamType.get(curClass).containsKey(f2Result))
			return "Type error";
		else {
			HashMap<String, ArrayList<String>> methodParamType = idTypes.methodParamType.get(curClass);
			methodParamType.put(f2Result, new ArrayList<String>());
		}
		idTypes.methodVar.put(f2Result, new HashMap<String, String>());
		
		n.f3.accept(this, idTypes);
		
		if (n.f4.accept(this, idTypes).equals("Type error"))
			return "Type error";
		
		if (!idTypes.curClass.equals("")) {
			// Check out if the method comes from a parent class
			if (!idTypes.childToParent.get(curClass).equals("")) {
				String parentClass = idTypes.childToParent.get(curClass);
				for (String method : idTypes.classMethods.get(parentClass).keySet()) {
					if (method.equals(f2Result)) {
						// Check out if the return type of method in parent equal to the return type of
						// method in child.
						if (!f1Result.equals(idTypes.classMethods.get(parentClass).get(method)))
							return "Type error";
						// Compare parameters of method in parent with parameters of method in child.
						ArrayList<String> parentParam = idTypes.methodParamType.get(parentClass).get(method);
						ArrayList<String> childParam = idTypes.methodParamType.get(curClass).get(method);
						if (parentParam.size() != childParam.size())
							return "Type error";
						else {
							for (int i = 0; i < parentParam.size(); i++) {
								if (!parentParam.get(i).equals(childParam.get(i)))
									return "Type error";
							}
						}
					}
				}
			}
		}
		
		n.f5.accept(this, idTypes);
		n.f6.accept(this, idTypes);
		
		if (n.f7.accept(this, idTypes).equals("Type error"))
			return "Type error";
		
		n.f8.accept(this, idTypes);
		n.f9.accept(this, idTypes);
		n.f10.accept(this, idTypes);
		n.f11.accept(this, idTypes);
		n.f12.accept(this, idTypes);
		
		idTypes.curMethod = "";
		idTypes.isInMethod = false;
		
		return "Type correct";
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
		
		String f0Result = n.f0.accept(this, idTypes);
		String f1Result = n.f1.accept(this, idTypes);
		String currentClass = idTypes.curClass;
		String currentMethod = idTypes.curMethod;
		ArrayList<String> paramIdList = idTypes.methodParamName.get(currentClass).get(currentMethod);
		// Check out whether the method has the same name of parameter with f1Result.
		if (paramIdList.contains(f1Result))
			return "Type error";
		else {
			idTypes.methodParamName.get(currentClass).get(currentMethod).add(f1Result);
			idTypes.methodParamType.get(currentClass).get(currentMethod).add(f0Result);
		}
		
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
		
		return n.f0.accept(this, idTypes);
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
		
		n.f0.accept(this, idTypes);
		return "";
	}

	/**
	 * f0 -> "{" 
	 * f1 -> ( Statement() ) 
	 * f2 -> "}"
	 */
	public String visit(Block n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		return "";
	}

	/**
	 * f0 -> Identifier() 
	 * f1 -> "=" 
	 * f2 -> Expression() 
	 * f3 -> ";"
	 */
	public String visit(AssignmentStatement n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		n.f3.accept(this, idTypes);
		return "";
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
		
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		n.f3.accept(this, idTypes);
		n.f4.accept(this, idTypes);
		n.f5.accept(this, idTypes);
		n.f6.accept(this, idTypes);
		return "";
		
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
		n.f2.accept(this, idTypes);
		n.f3.accept(this, idTypes);
		n.f4.accept(this, idTypes);
		n.f5.accept(this, idTypes);
		n.f6.accept(this, idTypes);
		return "";
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
		n.f2.accept(this, idTypes);
		n.f3.accept(this, idTypes);
		n.f4.accept(this, idTypes);
		return "";
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
		n.f2.accept(this, idTypes);
		n.f3.accept(this, idTypes);
		n.f4.accept(this, idTypes);
		return "";
	}

	/**
	 * f0 -> AndExpression() | CompareExpression() | PlusExpression() |
	 * MinusExpression() | TimesExpression() | ArrayLookup() | ArrayLength() |
	 * MessageSend() | PrimaryExpression()
	 */
	public String visit(Expression n, IDnType idTypes) {
		
		//System.out.println("Expression Entered");
		
		n.f0.accept(this, idTypes);
		return "Type correct";
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "&&" 
	 * f2 -> PrimaryExpression()
	 */
	public String visit(AndExpression n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		return "";
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "<" 
	 * f2 -> PrimaryExpression()
	 */
	public String visit(CompareExpression n, IDnType idTypes) {

		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		return "";
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "+" 
	 * f2 -> PrimaryExpression()
	 */
	public String visit(PlusExpression n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		return "";
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "-" 
	 * f2 -> PrimaryExpression()
	 */
	public String visit(MinusExpression n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		return "";
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "*" 
	 * f2 -> PrimaryExpression()
	 */
	public String visit(TimesExpression n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		return "";
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "[" 
	 * f2 -> PrimaryExpression() 
	 * f3 -> "]"
	 */
	public String visit(ArrayLookup n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		n.f3.accept(this, idTypes);
		return "";
	}

	/**
	 * f0 -> PrimaryExpression() 
	 * f1 -> "." 
	 * f2 -> "length"
	 */
	public String visit(ArrayLength n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		return "";		
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
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		n.f3.accept(this, idTypes);
		n.f4.accept(this, idTypes);
		n.f5.accept(this, idTypes);
		return "";
	}

	/**
	 * f0 -> Expression() 
	 * f1 -> ( ExpressionRest() )* <- NodeListOptional!!!!
	 */
	public String visit(ExpressionList n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		return "";
		
	}

	/**
	 * f0 -> "," 
	 * f1 -> Expression()
	 */
	public String visit(ExpressionRest n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		return "";
	}

	/**
	 * f0 -> IntegerLiteral() | TrueLiteral() | FalseLiteral() | Identifier() |
	 * ThisExpression() | ArrayAllocationExpression() | AllocationExpression() |
	 * NotExpression() | BracketExpression()
	 */
	public String visit(PrimaryExpression n, IDnType idTypes) {
		
		//System.out.println("PrimaryExpression Entered");
		
		return n.f0.accept(this, idTypes);
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
		return "";
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
		n.f3.accept(this, idTypes);
		n.f4.accept(this, idTypes);
		return "";
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
		n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		n.f3.accept(this, idTypes);
		return "";
	}

	/**
	 * f0 -> "!" 
	 * f1 -> Expression()
	 */
	public String visit(NotExpression n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		return "";
	}

	/**
	 * f0 -> "(" 
	 * f1 -> Expression() 
	 * f2 -> ")"
	 */
	public String visit(BracketExpression n, IDnType idTypes) {
		
		n.f0.accept(this, idTypes);
		n.f1.accept(this, idTypes);
		n.f2.accept(this, idTypes);
		return "";
	}
}