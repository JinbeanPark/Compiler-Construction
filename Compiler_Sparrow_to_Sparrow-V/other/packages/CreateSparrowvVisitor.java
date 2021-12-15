package other.packages;
import cs132.IR.visitor.*;
import cs132.IR.syntaxtree.*;
import java.util.*;


public class CreateSparrowvVisitor extends GJDepthFirst<SparrowVShape, CurFuncLine> {
	
	// Count lines
	int cntLine = 1;
	
	// usedVars -> [funcName, [localVar, {starting line number, ending line number}]]>>
	public HashMap<String, HashMap<String, ArrayList<Integer>>> usedVars = new HashMap<>();
	// usedParams -> [funcName, [parameters, {register, starting line number, ending line number}]]
	public HashMap<String, HashMap<String, ArrayList<String>>> usedParams = new HashMap<>();
	// usedLabels -> [labelName, [var, line number]]
	public HashMap<String, HashMap<String, Integer>> usedLabels = new HashMap<>();
	// loops -> {{starting line number, ending line number}}
	public ArrayList<ArrayList<Integer>> loops = new ArrayList<>();
	// rangeVarsFunc
	public HashMap<String, HashMap<String, ArrayList<Integer>>> rangeVarsFunc = new HashMap<>();
	// rangeParamsFunc
	public HashMap<String, HashMap<String, ArrayList<String>>> rangeParamsFunc = new HashMap<>();
	// livenessEachFunc
	public HashMap<String, HashMap<String, String>> livessEachFunc = new HashMap<>();
	
	
	public SparrowVShape visit(NodeList n, CurFuncLine funcLine) {
        int _count = 0;

        for(Enumeration e = n.elements(); e.hasMoreElements(); ++_count) {
            ((Node)e.nextElement()).accept(this, funcLine);
        }
        return new SparrowVShape();
    }
	
    public SparrowVShape visit(NodeListOptional n, CurFuncLine funcLine) {
    	SparrowVShape result = new SparrowVShape();
    	
        if (n.present()) {
            int _count = 0;

            for(Enumeration <Node> e = n.elements(); e.hasMoreElements(); ++_count) {
                SparrowVShape subRes = e.nextElement().accept(this, funcLine);
                result.resultOptList.add(subRes.sparrVsrc);
                result.resultOpt.add(subRes.s);
            }
        }
        return result;
    }

    public SparrowVShape visit(NodeOptional n, CurFuncLine funcLine) {
        if (n.present()) {
            n.node.accept(this, funcLine);
        }
        return new SparrowVShape();
    }

    public SparrowVShape visit(NodeSequence n, CurFuncLine funcLine) {
        int _count = 0;

        for(Enumeration e = n.elements(); e.hasMoreElements(); ++_count) {
            ((Node)e.nextElement()).accept(this, funcLine);
        }
        return new SparrowVShape();
    }

    public SparrowVShape visit(NodeToken n, CurFuncLine funcLine) {return new SparrowVShape();}
    
    /*
     *  f0 -> ( FunctionDeclaration() )* 
     *  f1 ->
     */
 
    public SparrowVShape visit(Program n, CurFuncLine funcLine) {
    	SparrowVShape result = new SparrowVShape();
    	SparrowVShape f0Result = n.f0.accept(this, funcLine);
    	
    	for (ArrayList<String> subSparrVsrc: f0Result.resultOptList)
    		result.sparrVsrc.addAll(subSparrVsrc);
    	return result;
    }
    
    
    /*
     *   f0 -> "func" 
     *   f1 -> FunctionName() 
     *   f2 -> "(" 
     *   f3 -> ( Identifier() )* 
     *   f4 -> ")" 
     *   f5 -> Block()
     */

    public SparrowVShape visit(FunctionDeclaration n, CurFuncLine funcLine) {
    	
    	SparrowVShape result = new SparrowVShape();
        
    	String funcName = n.f1.f0.tokenImage;
    	funcLine.curFuncName = funcName;
    	String src = "func " + funcName + "(";
    	
    	// extraParams -> <id, extra reg>
    	HashMap<String, String> extraParams = new HashMap<>();
    	// Regs to use for parameters: a2 - a7
    	int regSeq = 2;
        if (n.f3.present()) {
            int _count = 0;

            for(Enumeration e = n.f3.elements(); e.hasMoreElements(); ++_count) {
            	
            	//String aReg = " a" + Integer.toString(regSeq);
            	String param = ((Identifier)e.nextElement()).f0.toString();
            	if (regSeq++ > 7) {
            		src += " " + param;
            		if (this.livessEachFunc.get(funcName).containsKey(param))
            			extraParams.put(param, livessEachFunc.get(funcName).get(param));
            	}
            }
        }
        src += ")";
        result.sparrVsrc.add(src);
        
        for (String id: extraParams.keySet()) {
        	String extraReg = extraParams.get(id);
        	result.sparrVsrc.add(extraReg + " = " + id);
        }
        
        SparrowVShape f5Result = n.f5.accept(this, funcLine);
        result.sparrVsrc.addAll(f5Result.sparrVsrc);
        cntLine = 1;
        return result;
    }
    
    /*
     *   f0 -> ( Instruction() )*
     *   f1 -> "return" 
     *   f2 -> Identifier()
     */

    public SparrowVShape visit(Block n, CurFuncLine funcLine) {
    	
    	SparrowVShape result = new SparrowVShape();
    	SparrowVShape f0Result = n.f0.accept(this, funcLine);
    	
    	for (ArrayList<String> src: f0Result.resultOptList)
    		result.sparrVsrc.addAll(src);
    	
    	String returnObj = n.f2.f0.tokenImage;
    	
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(returnObj)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(returnObj);
    		result.sparrVsrc.add(returnObj + " = " + reg);
    		result.sparrVsrc.add("return " + returnObj);
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(returnObj)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(returnObj).get(0);
    		result.sparrVsrc.add(returnObj + " = " + reg);
    		result.sparrVsrc.add("return " + returnObj);
    	}
    	else
    		result.sparrVsrc.add("return " + returnObj);
    	
    	return result;
    }
    
    
    /*
     *   f0 -> LabelWithColon() 
     *   | SetInteger() | SetFuncName() | Add() 
     *   | Subtract() | Multiply() | LessThan() 
     *   | Load() | Store() | Move() | Alloc() 
     *   | Print() | ErrorMessage() | Goto() 
     *   | IfGoto() | Call()
     */

    public SparrowVShape visit(Instruction n, CurFuncLine funcLine) {
    	
    	SparrowVShape result = new SparrowVShape();
        funcLine.curLineNum = cntLine++;
        SparrowVShape f0Result =  n.f0.choice.accept(this, funcLine);
        result.sparrVsrc.addAll(f0Result.sparrVsrc);
        return result;
    }

    /*
     *   f0 -> Label() 
     *   f1 -> ":"
     */
    
    public SparrowVShape visit(LabelWithColon n, CurFuncLine funcLine) {
    	
    	SparrowVShape result = new SparrowVShape();
    	String f0Result =  n.f0.f0.tokenImage;
    	result.sparrVsrc.add(f0Result + ":");
    	return result;
    }
    
    /*
     *  f0 -> Identifier() 
     *  f1 -> "=" 
     *  f2 -> IntegerLiteral()
     */

    public SparrowVShape visit(SetInteger n, CurFuncLine funcLine) {
    	
    	SparrowVShape result = new SparrowVShape();
    	String f0Result = n.f0.f0.tokenImage;
    	String f2Result = n.f2.f0.toString();
    	
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f0Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f0Result);
    		result.sparrVsrc.add(reg + " = " + f2Result);
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f0Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f0Result).get(0);
    		result.sparrVsrc.add(reg + " = " + f2Result);
    	}
    	else {
    		result.sparrVsrc.add("t0 = " + f2Result);
    		result.sparrVsrc.add(f0Result + " = t0");
    	}
    	return result;
    }

    
    /*
     *   f0 -> Identifier() 
     *   f1 -> "=" 
     *   f2 -> "@" 
     *   f3 -> FunctionName()
     */
    
    public SparrowVShape visit(SetFuncName n, CurFuncLine funcLine) {
    	
    	SparrowVShape result = new SparrowVShape();
    	String f0Result = n.f0.f0.tokenImage;
    	String f3Result = n.f3.f0.toString();
    	
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f0Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f0Result);
    		result.sparrVsrc.add(reg + " = @" + f3Result);
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f0Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f0Result).get(0);
    		result.sparrVsrc.add(reg + " = @" + f3Result);
    	}
    	else {
    		result.sparrVsrc.add("t0 = @" + f3Result);
    		result.sparrVsrc.add(f0Result + " = t0");
    	}
    	return result;
    }

    /*
     *    f0 -> Identifier() 
     *    f1 -> "=" 
     *    f2 -> Identifier() 
     *    f3 -> "+" 
     *    f4 -> Identifier()
     */
    
    public SparrowVShape visit(Add n, CurFuncLine funcLine) {
    	
    	SparrowVShape result = new SparrowVShape();
    	String f0Result = n.f0.f0.tokenImage;
    	String f2Result = n.f2.f0.tokenImage;
    	String f4Result = n.f4.f0.tokenImage;
    	
    	String f2Reg = "t0";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f2Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f2Result);
    		f2Reg = reg;
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f2Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f2Result).get(0);
    		f2Reg = reg;
    	}
    	else
    		result.sparrVsrc.add("t0 = " + f2Result);
    	
    	String f4Reg = "t1";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f4Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f4Result);
    		f4Reg = reg;
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f4Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f4Result).get(0);
    		f4Reg = reg;
    	}
    	else
    		result.sparrVsrc.add("t1 = " + f4Result);
    	
    	String f0Reg = "t2";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f0Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f0Result);
    		result.sparrVsrc.add(reg + " = " + f2Reg + " + " + f4Reg);
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f0Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f0Result).get(0);
    		result.sparrVsrc.add(reg + " = " + f2Reg + " + " + f4Reg);
    	}
    	else {
    		result.sparrVsrc.add(f0Reg + " = " + f2Reg + " + " + f4Reg);
    		result.sparrVsrc.add(f0Result + " = " + f0Reg);
    	}
    	
    	return result;
    }

    /*
     *    f0 -> Identifier() 
     *    f1 -> "=" 
     *    f2 -> Identifier() 
     *    f3 -> "-" 
     *    f4 -> Identifier()
     */
    public SparrowVShape visit(Subtract n, CurFuncLine funcLine) {
    	
    	SparrowVShape result = new SparrowVShape();
    	String f0Result = n.f0.f0.tokenImage;
    	String f2Result = n.f2.f0.tokenImage;
    	String f4Result = n.f4.f0.tokenImage;
    	
    	String f2Reg = "t0";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f2Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f2Result);
    		f2Reg = reg;
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f2Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f2Result).get(0);
    		f2Reg = reg;
    	}
    	else
    		result.sparrVsrc.add("t0 = " + f2Result);
    	
    	String f4Reg = "t1";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f4Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f4Result);
    		f4Reg = reg;
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f4Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f4Result).get(0);
    		f4Reg = reg;
    	}
    	else
    		result.sparrVsrc.add("t1 = " + f4Result);
    	
    	String f0Reg = "t2";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f0Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f0Result);
    		result.sparrVsrc.add(reg + " = " + f2Reg + " - " + f4Reg);
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f0Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f0Result).get(0);
    		result.sparrVsrc.add(reg + " = " + f2Reg + " - " + f4Reg);
    	}
    	else {
    		result.sparrVsrc.add(f0Reg + " = " + f2Reg + " - " + f4Reg);
    		result.sparrVsrc.add(f0Result + " = " + f0Reg);
    	}
    	
    	return result;
    }

    
    /*
     *    f0 -> Identifier() 
     *    f1 -> "=" 
     *    f2 -> Identifier() 
     *    f3 -> "*" 
     *    f4 -> Identifier()
     */
    public SparrowVShape visit(Multiply n, CurFuncLine funcLine) {
    	
    	SparrowVShape result = new SparrowVShape();
    	String f0Result = n.f0.f0.tokenImage;
    	String f2Result = n.f2.f0.tokenImage;
    	String f4Result = n.f4.f0.tokenImage;
    	
    	String f2Reg = "t0";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f2Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f2Result);
    		f2Reg = reg;
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f2Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f2Result).get(0);
    		f2Reg = reg;
    	}
    	else
    		result.sparrVsrc.add("t0 = " + f2Result);
    	
    	String f4Reg = "t1";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f4Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f4Result);
    		f4Reg = reg;
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f4Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f4Result).get(0);
    		f4Reg = reg;
    	}
    	else
    		result.sparrVsrc.add("t1 = " + f4Result);
    	
    	String f0Reg = "t2";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f0Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f0Result);
    		result.sparrVsrc.add(reg + " = " + f2Reg + " * " + f4Reg);
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f0Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f0Result).get(0);
    		result.sparrVsrc.add(reg + " = " + f2Reg + " * " + f4Reg);
    	}
    	else {
    		result.sparrVsrc.add(f0Reg + " = " + f2Reg + " * " + f4Reg);
    		result.sparrVsrc.add(f0Result + " = " + f0Reg);
    	}
    	
    	return result;
    }

    
    /*
     *    f0 -> Identifier() 
     *    f1 -> "=" 
     *    f2 -> Identifier() 
     *    f3 -> "<" 
     *    f4 -> Identifier()
     */
    public SparrowVShape visit(LessThan n, CurFuncLine funcLine) {
    	
    	SparrowVShape result = new SparrowVShape();
    	String f0Result = n.f0.f0.tokenImage;
    	String f2Result = n.f2.f0.tokenImage;
    	String f4Result = n.f4.f0.tokenImage;
    	
    	String f2Reg = "t0";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f2Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f2Result);
    		f2Reg = reg;
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f2Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f2Result).get(0);
    		f2Reg = reg;
    	}
    	else
    		result.sparrVsrc.add("t0 = " + f2Result);
    	
    	String f4Reg = "t1";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f4Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f4Result);
    		f4Reg = reg;
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f4Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f4Result).get(0);
    		f4Reg = reg;
    	}
    	else
    		result.sparrVsrc.add("t1 = " + f4Result);
    	
    	String f0Reg = "t2";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f0Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f0Result);
    		
//    		System.out.println("CreateSpar LessThan reg: " + reg + ", f2Reg: " + f2Reg + ", f4Reg: " + f4Reg);
    		
    		result.sparrVsrc.add(reg + " = " + f2Reg + " < " + f4Reg);
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f0Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f0Result).get(0);
    		
//    		System.out.println("@@CreateSpar LessThan reg: " + reg + ", f2Reg: " + f2Reg + ", f4Reg: " + f4Reg);    		
    		
    		result.sparrVsrc.add(reg + " = " + f2Reg + " < " + f4Reg);
    	}
    	else {
    		result.sparrVsrc.add(f0Reg + " = " + f2Reg + " < " + f4Reg);
    		result.sparrVsrc.add(f0Result + " = " + f0Reg);
    		
//    		System.out.println("@@@CreateSpar LessThan f0Reg: " + f0Reg + ", f2Reg: " + f2Reg + ", f4Reg: " + f4Reg);

    	}
    	
    	return result;
    }

    /*
     *   f0 -> Identifier() 
     *   f1 -> "=" 
     *   f2 -> "[" 
     *   f3 -> Identifier() 
     *   f4 -> "+" 
     *   f5 -> IntegerLiteral() 
     *   f6 -> "]"
     */
    public SparrowVShape visit(Load n, CurFuncLine funcLine) {
    	
    	SparrowVShape result = new SparrowVShape();
    	String f0Result = n.f0.f0.tokenImage;
    	String f3Result = n.f3.f0.tokenImage;
    	String f5Result = n.f5.f0.toString();
    	
    	String f3Reg = "t0";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f3Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f3Result);
    		f3Reg = reg;
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f3Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f3Result).get(0);
    		f3Reg = reg;
    	}
    	else
    		result.sparrVsrc.add("t0 = " + f3Result);
    	
    	String f0Reg = "t1";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f0Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f0Result);
    		result.sparrVsrc.add(reg + " = [" + f3Reg + " + " + f5Result + "]");
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f0Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f0Result).get(0);
    		result.sparrVsrc.add(reg + " = [" + f3Reg + " + " + f5Result + "]");
    	}
    	else {
    		result.sparrVsrc.add(f0Reg + " = [" + f3Reg + " + " + f5Result + "]");
    		result.sparrVsrc.add(f0Result + " = " + f0Reg);
    	}
    	
    	return result;
    }

    
    /*
     *    f0 -> "[" 
     *    f1 -> Identifier() 
     *    f2 -> "+" 
     *    f3 -> IntegerLiteral() 
     *    f4 -> "]" 
     *    f5 -> "=" 
     *    f6 -> Identifier()
     */
    public SparrowVShape visit(Store n, CurFuncLine funcLine) {

    	SparrowVShape result = new SparrowVShape();
    	String f1Result = n.f1.f0.tokenImage;
    	String f3Result = n.f3.f0.tokenImage;
    	String f6Result = n.f6.f0.toString();
    	
    	String f6Reg = "t0";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f6Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f6Result);
    		f6Reg = reg;
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f6Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f6Result).get(0);
    		f6Reg = reg;
    	}
    	else
    		result.sparrVsrc.add("t0 = " + f6Result);
    	
    	String f1Reg = "t1";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f1Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f1Result);
    		result.sparrVsrc.add("[" + reg + " + " + f3Result + "] = " + f6Reg);
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f1Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f1Result).get(0);
    		result.sparrVsrc.add("[" + reg + " + " + f3Result + "] = " + f6Reg);
    	}
    	else {
    		result.sparrVsrc.add(f1Reg + " = " + f1Result);
    		result.sparrVsrc.add("[" + f1Reg + " + " + f3Result + "] = " + f6Reg);
    	}
    	
    	return result;
    }
    
    /*
     *     f0 -> Identifier() 
     *     f1 -> "=" 
     *     f2 -> Identifier()
     */

    public SparrowVShape visit(Move n, CurFuncLine funcLine) {
    	
    	SparrowVShape result = new SparrowVShape();
    	String f0Result = n.f0.f0.tokenImage;
    	String f2Result = n.f2.f0.tokenImage;
    	
    	String f2Reg = f2Result;
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f2Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f2Result);
    		f2Reg = reg;
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f2Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f2Result).get(0);
    		f2Reg = reg;
    	}
    	
    	String f0Reg = "t0";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f0Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f0Result);
    		result.sparrVsrc.add(reg + " = " + f2Reg);
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f0Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f0Result).get(0);
    		result.sparrVsrc.add(reg + " = " + f2Reg);
    	}
    	else {
    		result.sparrVsrc.add(f0Reg + " = " + f2Reg);
    		result.sparrVsrc.add(f0Result + " = " + f0Reg);
    	}
    	
    	return result;
    }

    
    /*
     *    f0 -> Identifier() 
     *    f1 -> "=" 
     *    f2 -> "alloc" 
     *    f3 -> "(" 
     *    f4 -> Identifier() 
     *    f5 -> ")"
     */
    public SparrowVShape visit(Alloc n, CurFuncLine funcLine) {
    	
    	SparrowVShape result = new SparrowVShape();
    	String f0Result = n.f0.f0.tokenImage;
    	String f4Result = n.f4.f0.tokenImage;
    	
    	String f4Reg = "t0";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f4Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f4Result);
    		f4Reg = reg;
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f4Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f4Result).get(0);
    		f4Reg = reg;
    	}
    	else
    		result.sparrVsrc.add("t0 = " + f4Result);
    	
    	String f0Reg = "t1";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f0Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f0Result);
    		result.sparrVsrc.add(reg + " = alloc(" + f4Reg + ")");
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f0Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f0Result).get(0);
    		result.sparrVsrc.add(reg + " = alloc(" + f4Reg + ")");
    	}
    	else {
    		result.sparrVsrc.add(f0Reg + " = alloc(" + f4Reg + ")");
    		result.sparrVsrc.add(f0Result + " = " + f0Reg);
    	}
    	
    	return result;
    }

    
    /*
     *     f0 -> "print" 
     *     f1 -> "(" 
     *     f2 -> Identifier() 
     *     f3 -> ")"
     */
    public SparrowVShape visit(Print n, CurFuncLine funcLine) {
    	
    	SparrowVShape result = new SparrowVShape();
    	String f2Result = n.f2.f0.tokenImage;
    	
    	String f2Reg = "t0";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f2Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f2Result);
    		result.sparrVsrc.add("print( " + reg + " )");
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f2Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f2Result).get(0);
    		result.sparrVsrc.add("print( " + reg + " )");
    	}
    	else {
    		result.sparrVsrc.add(f2Reg + " = " + f2Result);
    		result.sparrVsrc.add("print(" + f2Reg + ")");
    	}
    	return result;
    }
    
    /*
     *     f0 -> "error" 
     *     f1 -> "(" 
     *     f2 -> StringLiteral() 
     *     f3 -> ")"
     */
    public SparrowVShape visit(ErrorMessage n, CurFuncLine funcLine) {
    	
    	SparrowVShape result = new SparrowVShape();
    	String f2Result = n.f2.f0.tokenImage;
    	result.sparrVsrc.add("error(" + f2Result + ")");
    	return result;
    }

    
    /*
     *     f0 -> "goto" 
     *     f1 -> Label()
     */
    public SparrowVShape visit(Goto n, CurFuncLine funcLine) {
    	
    	SparrowVShape result = new SparrowVShape();
    	String f1Result = n.f1.f0.tokenImage;
    	result.sparrVsrc.add("goto " + f1Result);
    	return result;
    }

    /*
     *     f0 -> "if0" 
     *     f1 -> Identifier() 
     *     f2 -> "goto" 
     *     f3 -> Label()
     */
    public SparrowVShape visit(IfGoto n, CurFuncLine funcLine) {
    	
    	SparrowVShape result = new SparrowVShape();
    	String f1Result = n.f1.f0.tokenImage;
    	String f3Result = n.f3.f0.tokenImage;
    	
    	String f1Reg = "t0";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f1Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f1Result);
    		result.sparrVsrc.add("if0 " + reg + " goto " + f3Result);
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f1Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f1Result).get(0);
    		result.sparrVsrc.add("if0 " + reg + " goto " + f3Result);
    	}
    	else {
    		result.sparrVsrc.add(f1Reg + " = " + f1Result);
    		result.sparrVsrc.add("if0 " + f1Reg + " goto " + f3Result);
    	}
    	
    	return result;
    }

    
    /*
     *      f0 -> Identifier() 
     *      f1 -> "=" 
     *      f2 -> "call" 
     *      f3 -> Identifier() 
     *      f4 -> "(" 
     *      f5 -> ( Identifier() )* 
     *      f6 -> ")"
     */
    public SparrowVShape visit(Call n, CurFuncLine funcLine) {
    	
    	SparrowVShape result = new SparrowVShape();
    	String curFunc = funcLine.curFuncName;
    	int curLine = funcLine.curLineNum;
    	String f0Result = n.f0.f0.tokenImage;
    	String f3Result = n.f3.f0.tokenImage;
    	
    	HashMap<String, ArrayList<Integer>> varStruct = this.usedVars.get(curFunc);
    	HashMap<String, String> curUsedVals = new HashMap<>();
    	for (String var: this.livessEachFunc.get(curFunc).keySet()) {
    		int startLine = varStruct.get(var).get(0);
    		int endLine = varStruct.get(var).get(1);
    		if (startLine <= curLine && curLine < endLine) {
    			String reg = this.livessEachFunc.get(curFunc).get(var);
    			result.sparrVsrc.add(var + " = " + reg);
    			curUsedVals.put(reg, var);
    		}
    	}
    	
    	HashMap<String, ArrayList<String>> paramStruct = this.usedParams.get(curFunc);
    	HashMap<String, String> curUsedParams = new HashMap<>();
    	for (String param: paramStruct.keySet()) {
    		int endLine = Integer.parseInt(paramStruct.get(param).get(2));
    		if (curLine <= endLine) {
    			String reg = paramStruct.get(param).get(0);
    			String stackReg = "stacksave_" + reg;
    			result.sparrVsrc.add(stackReg + " = " + reg);
    			
//    			System.out.println("CreateSpar call curUsedParams param: " + param + ", reg: " + reg);
    			
    			curUsedParams.put(param, reg);
    		}
    	}
    	
    	
    	// extraParams -> <id, extra reg>
    	HashMap<String, String> extraParams = new HashMap<>();
    	// Regs to use for parameters: a2 - a7
    	int regSeq = 2;
        if (n.f5.present()) {
            int _count = 0;

            for(Enumeration e = n.f5.elements(); e.hasMoreElements(); ++_count) {
            	
            	//String aReg = " a" + Integer.toString(regSeq);
            	String reg = "a" + Integer.toString(regSeq);
            	String param = ((Identifier)e.nextElement()).f0.toString();
            	
            	
//            	System.out.println("CreateSpar call param: " + param + ", reg: " + reg);
            	
            	
            	if (regSeq > 7) {
            		
                	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(param)) {
                		String assignedReg = this.livessEachFunc.get(funcLine.curFuncName).get(param);
                		result.sparrVsrc.add(reg + " = " + assignedReg);                		
                	}
                	else if (this.usedParams.get(funcLine.curFuncName).containsKey(param)) {
                		String assignedReg = this.usedParams.get(funcLine.curFuncName).get(param).get(0);
                		result.sparrVsrc.add("t0 = " + "stacksave_" + assignedReg);
                		result.sparrVsrc.add(reg + " = t0");
                		
//                		System.out.println("CreateSpa call @@@");
                		
                	}
                	else {
                		result.sparrVsrc.add("t0 = " + param);
                		result.sparrVsrc.add(reg + " = t0");
                	}
            		extraParams.put(reg, param);
            	}
            	else {
            		if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(param)) {
                		String assignedReg = this.livessEachFunc.get(funcLine.curFuncName).get(param);
                		result.sparrVsrc.add(reg + " = " + assignedReg);
                	}
                	else if (this.usedParams.get(funcLine.curFuncName).containsKey(param)) {
                		String assignedReg = this.usedParams.get(funcLine.curFuncName).get(param).get(0);
                		result.sparrVsrc.add(reg + " = stacksave_" + assignedReg);
                		
//                		System.out.println("@@@@ CreateSpa call @@@");
//                		System.out.println("CreateSpa call: " + reg + " = stacksave_" + assignedReg);
                		
                	}
                	else
                		result.sparrVsrc.add(reg + " = " + param);	
            	}
            	regSeq++;
            }
        }
        
    	
    		
    	String f3Reg = "t1";
    	if (this.livessEachFunc.get(funcLine.curFuncName).containsKey(f3Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f3Result);
    		f3Reg = reg;
    	}
    	else if (this.usedParams.get(funcLine.curFuncName).containsKey(f3Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f3Result).get(0);
    		f3Reg = reg;
    	}
    	else
    		result.sparrVsrc.add(f3Reg + " = " + f3Result);
    	
    	
    	//String funcName = f3Result;
    	//funcLine.curFuncName = funcName;
    	String src = "call " + f3Reg + "(";
    	
    	for (String extraParam: extraParams.keySet())
    		src += " " + extraParam;
    	src += ")";
    	
        
        String f0Reg = "t2";
        boolean isOutReg = false;
        
//        System.err.println("CreateSparrowvVistor call funcName: " + curFunc);
        
    	if (this.livessEachFunc.get(curFunc).containsKey(f0Result)) {
    		String reg = this.livessEachFunc.get(funcLine.curFuncName).get(f0Result);
    		f0Reg = reg;
    	}
    	else if (this.usedParams.get(curFunc).containsKey(f0Result)) {
    		String reg = this.usedParams.get(funcLine.curFuncName).get(f0Result).get(0);
    		f0Reg = reg;
    	}
    	else
    		isOutReg = true;
        
    	src = f0Reg + " = " + src;
    	result.sparrVsrc.add(src);
    	if (isOutReg)
    		result.sparrVsrc.add(f0Result + " = t2");
    	
    	
    	for (String reg: curUsedVals.keySet()) {
    		if (!reg.equals(f0Reg)) {
    			String var = curUsedVals.get(reg);
    			result.sparrVsrc.add(reg + " = " + var);
    		}
    	}
    	
    	for (String param: curUsedParams.keySet()) {
    		
    		String reg = curUsedParams.get(param);
    		
    		
//    		System.out.println("CreateSpa call curUsedParams reg: " + reg + ", param: " + param);
    		
    		
    		
    		if (!reg.equals(f0Reg) && usedParams.get(curFunc).containsKey(param)) {
    			
    			
//    			System.out.println("@3@3@CreateSpa call curUsedParams reg: " + reg + ", param: " + param);
    			
    			
    			int startLine = Integer.parseInt(usedParams.get(curFunc).get(param).get(1));
    			int endLine = Integer.parseInt(usedParams.get(curFunc).get(param).get(2));
    			
//    			System.out.println("@4@4@CreateSpa call curUsedParams reg: " + reg + ", param: " + param + ", start" + startLine + ", endLine: " + endLine);
    			
    			
    			if (startLine <= curLine && curLine < endLine) {
    				String stackReg = "stacksave_" + reg;
    				
    				
//    				System.out.println("CreateSpa call reg = stackReg: " + reg + " = " + stackReg);
    				
    				
    				result.sparrVsrc.add(reg + " = " + stackReg);
    			}
    		}
    	}
    	
        return result;
    }

    public SparrowVShape visit(FunctionName n, CurFuncLine funcLine) {return new SparrowVShape();}

    
    /*
     *      f0 ->
     */
    public SparrowVShape visit(Label n, CurFuncLine funcLine) {
    	SparrowVShape result = new SparrowVShape();
    	String label = n.f0.tokenImage;
    	result.sparrVsrc.add(label);
    	return result;
    }

    /*
     *      f0 ->
     */
    public SparrowVShape visit(Identifier n, CurFuncLine funcLine) {
    	SparrowVShape result = new SparrowVShape();
    	String id = n.f0.tokenImage;
    	result.sparrVsrc.add(id);
    	return result;
    	
    }

    public SparrowVShape visit(IntegerLiteral n, CurFuncLine funcLine) {
    	
    	SparrowVShape result = new SparrowVShape();
    	String id = n.f0.tokenImage;
    	result.sparrVsrc.add(id);
    	return result;
    }

    public SparrowVShape visit(StringLiteral n, CurFuncLine funcLine) {
    	
    	SparrowVShape result = new SparrowVShape();
    	String id = n.f0.tokenImage;
    	result.sparrVsrc.add(id);
    	return result;
    }

    public SparrowVShape visit(If n, CurFuncLine funcLine) {return new SparrowVShape();}

    public SparrowVShape visit(LabeledInstruction n, CurFuncLine funcLine) {return new SparrowVShape();}
}
