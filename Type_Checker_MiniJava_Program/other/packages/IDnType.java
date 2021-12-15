package other.packages;
import java.util.*;
import cs132.minijava.*;
import cs132.minijava.syntaxtree.*;
import cs132.minijava.visitor.*;

public class IDnType {
	
	// The current class we are searching
	String curClass = "";
	// The current method we are searching
	String curMethod = "";
	boolean isInMethod = false;
	// To compare formal expression list
	ArrayList<String> expressionList = new ArrayList<>();
	
	// className: class name
	public List<String> className = new ArrayList<>();
	// childToParent: child name -> parent name
	public HashMap<String, String> childToParent = new HashMap<>();
	// types: list of types
	public List<String> types = new ArrayList<>(Arrays.asList("intArray", "boolean", "int"));
	// classFields: class name -> name of class variable -> type of class variable
	public HashMap<String, HashMap<String, String>> classFields = new HashMap<>();
	// classMethods: class name -> method name -> return type of the method
	public HashMap<String, HashMap<String, String>> classMethods = new HashMap<>();
	// methodName: class -> method name -> parameter name
	public HashMap<String, HashMap<String, ArrayList<String>>> methodParamName = new HashMap<>();
	// methodType: class -> method name -> parameter type
	public HashMap<String, HashMap<String, ArrayList<String>>> methodParamType = new HashMap<>();
	// methodVar: method name -> varaible name in method-> variable type in method
	public HashMap<String, HashMap<String, String>> methodVar = new HashMap<>();
	
	
	// Find the type of ID.
	public String findType(String id) {
		
		String curSearchClass = curClass;
		String type = "";
		
		// Stop exploring if there is no more parent class
		while (!curSearchClass.equals("")) {
			// 1. Check out every id of variable in class.
			for (String classVar: classFields.get(curSearchClass).keySet()) {
				if (classVar.equals(id))
					return classFields.get(curSearchClass).get(id);
			}
			/*
			// 2. Check out variables and parameters of every method in current class
			for (String method: classMethods.get(curSearchClass).keySet()) {
				// (1) Check out variable ID
				for (String varId: methodVar.get(method).keySet()) {
					if (varId.equals(id)) {
						//type = methodParam.get(method).get(id);
						return methodVar.get(method).get(id);
					}
				}
				// 3. Check out parameter ID
				ArrayList<String> paramNameList = methodParamName.get(curSearchClass).get(method);
				for (int i = 0; i < paramNameList.size(); i++) {
					if (paramNameList.get(i).equals(id)) {
						return methodParamType.get(curSearchClass).get(method).get(i);
					}
				}
			}
			*/
			if (!curMethod.equals("")) {
				// 2. Check out variables of current method in current class
				if (methodVar.get(curMethod).containsKey(id))
					return methodVar.get(curMethod).get(id);
				// 3. Check out parameters of current method in current class
				else if (methodParamName.get(curSearchClass).containsKey(curMethod)) {
					ArrayList<String> idsInCurMethod = methodParamName.get(curSearchClass).get(curMethod);
					for (int i = 0; i < idsInCurMethod.size(); i++) {
						if (idsInCurMethod.get(i).equals(id))
							return methodParamType.get(curSearchClass).get(curMethod).get(i);
					}
				}
			}
			// 4. Move to parent class
			if (childToParent.containsKey(curSearchClass))
				curSearchClass = childToParent.get(curSearchClass);
		}
		return type;
	}
}