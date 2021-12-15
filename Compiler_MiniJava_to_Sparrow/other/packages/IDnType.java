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
	// Count temporary variable
	int cntTmp = 0;
	// classFieldIndx
	//int curClassFieldIndx = 1;
	// classMethodIndx
	//int curClassMethodIndx = 0;
		
	
	// className: class name
	public List<String> className = new ArrayList<>();
	// childToParent: child name -> parent name
	public HashMap<String, String> childToParent = new HashMap<>();
	// types: list of types
	public List<String> types = new ArrayList<>(Arrays.asList("intArray", "boolean", "int"));
	// classFields: class name -> name of class variable -> type of class variable
	public HashMap<String, HashMap<String, String>> classFields = new HashMap<>();
	// classFieldsIndx: class name -> Name of class variable -> index of class variable
	public HashMap<String, HashMap<String, Integer>> classFieldsIndx = new HashMap<>();
	// classFieldsCount: class name -> size of fields in class.
	public HashMap<String, Integer> classFieldsCnt = new HashMap<>();
	// classMethods: class name -> method name -> return type of the method
	public HashMap<String, HashMap<String, String>> classMethods = new HashMap<>();
	// classMethodIndx: class name -> method name of class  -> index of method in class.
	public HashMap<String, HashMap<String, Integer>> classMethodsIndx = new HashMap<>();
	// classFieldsCount: class name -> size of methods in class.
	public HashMap<String, Integer> classMethodsCnt = new HashMap<>();	
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
	
	
	// Extend the parents class to child class.
	public void extendParToChild() {
		
		for (String childClass: this.className) {
			
			// Assgin fields and methods of parent class to child class
			if (!this.childToParent.get(childClass).equals("")) {
				String parentClass = childToParent.get(childClass);
				HashMap<String, String> fieldsPar = this.classFields.get(parentClass);
				HashMap<String, String> fieldsChil = this.classFields.get(childClass);
				for (String fieldP: fieldsPar.keySet()) {
					// If child doesn't have the field of the parent.
					if (!fieldsChil.containsKey(fieldP)) {
						fieldsChil.put(fieldP, fieldsPar.get(fieldP));
						int currentFieldIndx = this.classFieldsCnt.get(childClass);
						this.classFieldsIndx.get(childClass).put(fieldP, currentFieldIndx);
						this.classFieldsCnt.put(childClass, currentFieldIndx + 1);
					}
				}
				HashMap<String, String> methodsPar = this.classMethods.get(parentClass);
				HashMap<String, String> methodsChil = this.classMethods.get(childClass);
				for (String methodP: methodsPar.keySet()) {
					// If child doesn't have the method of the parent.
					if (!methodsChil.containsKey(methodP)) {
						methodsChil.put(methodP, methodsPar.get(methodP));
						int currentMethodsIndx = this.classMethodsCnt.get(childClass);
						this.classMethodsIndx.get(childClass).put(methodP, currentMethodsIndx);
						this.classFieldsCnt.put(childClass, currentMethodsIndx + 1);
					}
				}
			}
		}
	}
}













