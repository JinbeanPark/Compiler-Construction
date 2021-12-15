package other.packages;
import cs132.IR.visitor.*;
import cs132.IR.syntaxtree.*;
import java.util.*;


public class AssignValVisitor extends GJVoidDepthFirst<CurFuncLine> {
	
	// count number of lines.
	int cntLine = 1, indxGoToLabel = 1;
	// funIDs -> [funcName, number of IDs]
	public HashMap<String, Integer> funIDs = new HashMap<>();	
	// usedLabels -> [funcName, [label name, goToLabel]]
	public HashMap<String, HashMap<String, String>> usedLabels = new HashMap<>();
	// funArgs -> [funcName, number of args]
	public HashMap<String, Integer> funArgs = new HashMap<>();
	// stackMgr -> [funcName, [stack address, ID]]
	public HashMap<String, HashMap<String, String>> stackMgr = new HashMap<>();
	// Registers
	ArrayList<String> regs = new ArrayList<>(Arrays.asList("a2", "a3","a4","a5","a6","a7", "t0","t1","t2","t3", "t4", "t5", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11"));
			
	

	public void cntNumIDs(String funcName, String id) {
		
		if (!funIDs.containsKey(funcName))
			funIDs.put(funcName, 1);
		else
			funIDs.put(funcName, funIDs.get(funcName) + 1);
	}
	
	public void addLabels(String funcName, String label, String goToLabel) {
		
		if (!usedLabels.containsKey(funcName))
			usedLabels.put(funcName, new HashMap<String, String>());
		usedLabels.get(funcName).put(label, goToLabel);		
	}
	
	public String getGoToLabel() {
		return "l_" + String.valueOf(this.indxGoToLabel++);
	}
	
	
	public void visit(NodeList n, CurFuncLine funcLine) {
        int _count = 0;

        for(Enumeration e = n.elements(); e.hasMoreElements(); ++_count) {
            ((Node)e.nextElement()).accept(this, funcLine);
        }

    }
	
	
	
    public void visit(NodeListOptional n, CurFuncLine funcLine) {
        if (n.present()) {
            int _count = 0;

            for(Enumeration e = n.elements(); e.hasMoreElements(); ++_count) {
                ((Node)e.nextElement()).accept(this, funcLine);
                
                //System.out.println("AssignValVisitor NodeListOptional _count: " + _count);
            }
        }

    }

    public void visit(NodeOptional n, CurFuncLine funcLine) {
        if (n.present()) {
            n.node.accept(this, funcLine);
        }

    }

    public void visit(NodeSequence n, CurFuncLine funcLine) {
        int _count = 0;

        for(Enumeration e = n.elements(); e.hasMoreElements(); ++_count) {
            ((Node)e.nextElement()).accept(this, funcLine);
        }

    }

    public void visit(NodeToken n, CurFuncLine funcLine) {}
    
    /*
     *  f0 -> ( FunctionDeclaration() )* 
     *  f1 ->
     */
 
    public void visit(Program n, CurFuncLine funcLine) {
    	
//    	System.err.println("Program Entered");
    	
        n.f0.accept(this, funcLine);
    }
    
    
    /*
     *   f0 -> "func" 
     *   f1 -> FunctionName() 
     *   f2 -> "(" 
     *   f3 -> ( Identifier() )* 
     *   f4 -> ")" 
     *   f5 -> Block()
     */

    public void visit(FunctionDeclaration n, CurFuncLine funcLine) {
    	
    	
//    	System.err.println("FunctionDeclaration Entered");
    	
        n.f0.accept(this, funcLine);
        String funcName = n.f1.f0.tokenImage;
        int totArgs = 6;
        stackMgr.put(funcName, new HashMap<String, String>());
        //varMgr.put(funcName, new HashMap<String, String>());
        usedLabels.put(funcName, new HashMap<String, String>());
        funcLine.curFuncName = funcName;
        
        if (n.f3.present()) {
            int _count = 0;
            for(Enumeration e = n.f3.elements(); e.hasMoreElements(); ++_count) {
            	((Identifier)e.nextElement()).f0.toString();
//            	System.err.println("AssignValVisitor FunctionDeclaration _count: " + _count);
            	
            	totArgs++;
            }
        }
        funArgs.put(funcName, totArgs);
        
//        System.err.println("FunctionDeclaration 22Entered22");
        
        n.f5.accept(this, funcLine);
        
//        System.err.println("FunctionDeclaration 333Entered333");
        
        funcLine.curLineNum = 1;
    }
    
    /*
     *   f0 -> ( Instruction() )*
     *   f1 -> "return" 
     *   f2 -> Identifier()
     */

    public void visit(Block n, CurFuncLine funcLine) {
        n.f0.accept(this, funcLine);
        String f2Result = n.f2.f0.tokenImage;
        if (!regs.contains(f2Result))
        	this.cntNumIDs(funcLine.curFuncName, f2Result);
    }
    
    
    /*
     *   f0 -> LabelWithColon() 
     *   | SetInteger() | SetFuncName() | Add() 
     *   | Subtract() | Multiply() | LessThan() 
     *   | Load() | Store() | Move() | Alloc() 
     *   | Print() | ErrorMessage() | Goto() 
     *   | IfGoto() | Call()
     */

    public void visit(Instruction n, CurFuncLine funcLine) {
        funcLine.curLineNum = cntLine++;
        n.f0.choice.accept(this, funcLine);
    }

    /*
     *   f0 -> Label() 
     *   f1 -> ":"
     */
    
    public void visit(LabelWithColon n, CurFuncLine funcLine) {
        String f0Result = n.f0.f0.tokenImage;
        String goToLabel = this.getGoToLabel();
        usedLabels.get(funcLine.curFuncName).put(f0Result, goToLabel);
    }
    
    /*
     *  f0 -> Identifier() 
     *  f1 -> "=" 
     *  f2 -> IntegerLiteral()
     */

    public void visit(SetInteger n, CurFuncLine funcLine) {
        String id = n.f0.f0.tokenImage;
        if (!regs.contains(id))
        	this.cntNumIDs(funcLine.curFuncName, id);
    }

    
    /*
     *   f0 -> Identifier() 
     *   f1 -> "=" 
     *   f2 -> "@" 
     *   f3 -> FunctionName()
     */
    
    public void visit(SetFuncName n, CurFuncLine funcLine) {
    	String id = n.f0.f0.tokenImage;
    	if (!regs.contains(id))
        	this.cntNumIDs(funcLine.curFuncName, id);
    }

    /*
     *    f0 -> Identifier() 
     *    f1 -> "=" 
     *    f2 -> Identifier() 
     *    f3 -> "+" 
     *    f4 -> Identifier()
     */
    
    public void visit(Add n, CurFuncLine funcLine) {
    	String intLHS = n.f0.f0.tokenImage;
    	if (!regs.contains(intLHS))
        	this.cntNumIDs(funcLine.curFuncName, intLHS);
        
        String intRhsL = n.f2.f0.tokenImage;
        if (!regs.contains(intRhsL))
        	this.cntNumIDs(funcLine.curFuncName, intRhsL);
        
        String intRhsR = n.f4.f0.tokenImage;
        if (!regs.contains(intRhsR))
        	this.cntNumIDs(funcLine.curFuncName, intRhsR);
    }

    /*
     *    f0 -> Identifier() 
     *    f1 -> "=" 
     *    f2 -> Identifier() 
     *    f3 -> "-" 
     *    f4 -> Identifier()
     */
    public void visit(Subtract n, CurFuncLine funcLine) {
    	String intLHS = n.f0.f0.tokenImage;
    	if (!regs.contains(intLHS))
        	this.cntNumIDs(funcLine.curFuncName, intLHS);
        
        String intRhsL = n.f2.f0.tokenImage;
        if (!regs.contains(intRhsL))
        	this.cntNumIDs(funcLine.curFuncName, intRhsL);
        
        String intRhsR = n.f4.f0.tokenImage;
        if (!regs.contains(intRhsR))
        	this.cntNumIDs(funcLine.curFuncName, intRhsR);
    }

    
    /*
     *    f0 -> Identifier() 
     *    f1 -> "=" 
     *    f2 -> Identifier() 
     *    f3 -> "*" 
     *    f4 -> Identifier()
     */
    public void visit(Multiply n, CurFuncLine funcLine) {
    	String intLHS = n.f0.f0.tokenImage;
    	if (!regs.contains(intLHS))
        	this.cntNumIDs(funcLine.curFuncName, intLHS);
        
        String intRhsL = n.f2.f0.tokenImage;
        if (!regs.contains(intRhsL))
        	this.cntNumIDs(funcLine.curFuncName, intRhsL);
        
        String intRhsR = n.f4.f0.tokenImage;
        if (!regs.contains(intRhsR))
        	this.cntNumIDs(funcLine.curFuncName, intRhsR);
    }

    
    /*
     *    f0 -> Identifier() 
     *    f1 -> "=" 
     *    f2 -> Identifier() 
     *    f3 -> "<" 
     *    f4 -> Identifier()
     */
    public void visit(LessThan n, CurFuncLine funcLine) {
    	String intLHS = n.f0.f0.tokenImage;
    	if (!regs.contains(intLHS))
        	this.cntNumIDs(funcLine.curFuncName, intLHS);
        
        String intRhsL = n.f2.f0.tokenImage;
        if (!regs.contains(intRhsL))
        	this.cntNumIDs(funcLine.curFuncName, intRhsL);
        
        String intRhsR = n.f4.f0.tokenImage;
        if (!regs.contains(intRhsR))
        	this.cntNumIDs(funcLine.curFuncName, intRhsR);
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
    public void visit(Load n, CurFuncLine funcLine) {
    	
    	String loadObj = n.f0.f0.tokenImage;
    	if (!regs.contains(loadObj))
    		this.cntNumIDs(funcLine.curFuncName, loadObj);
        
        String obj = n.f3.f0.tokenImage;
        if (!regs.contains(obj))
        	this.cntNumIDs(funcLine.curFuncName, obj);
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
    public void visit(Store n, CurFuncLine funcLine) {
    	String storeObj = n.f1.f0.tokenImage;
    	if (!regs.contains(storeObj))
    		this.cntNumIDs(funcLine.curFuncName, storeObj);
        
        String obj = n.f6.f0.tokenImage;
        if (!regs.contains(obj))
    		this.cntNumIDs(funcLine.curFuncName, obj);
    }
    
    /*
     *     f0 -> Identifier() 
     *     f1 -> "=" 
     *     f2 -> Identifier()
     */

    public void visit(Move n, CurFuncLine funcLine) {
    	String idLHS = n.f0.f0.tokenImage;
    	if (!regs.contains(idLHS))
    		this.cntNumIDs(funcLine.curFuncName, idLHS);
        
        String idRHS = n.f2.f0.tokenImage;
        if (!regs.contains(idRHS))
    		this.cntNumIDs(funcLine.curFuncName, idRHS);
    }

    
    /*
     *    f0 -> Identifier() 
     *    f1 -> "=" 
     *    f2 -> "alloc" 
     *    f3 -> "(" 
     *    f4 -> Identifier() 
     *    f5 -> ")"
     */
    public void visit(Alloc n, CurFuncLine funcLine) {
    	
    	String idAlloc = n.f0.f0.tokenImage;
    	if (!regs.contains(idAlloc))
    		this.cntNumIDs(funcLine.curFuncName, idAlloc);
        
        String sizeObj = n.f4.f0.tokenImage;
        if (!regs.contains(sizeObj))
    		this.cntNumIDs(funcLine.curFuncName, sizeObj);
    }

    
    /*
     *     f0 -> "print" 
     *     f1 -> "(" 
     *     f2 -> Identifier() 
     *     f3 -> ")"
     */
    public void visit(Print n, CurFuncLine funcLine) {
        String printObj = n.f2.f0.tokenImage;
        if (!regs.contains(printObj))
    		this.cntNumIDs(funcLine.curFuncName, printObj);
    }
    
    /*
     *     f0 -> "error" 
     *     f1 -> "(" 
     *     f2 -> StringLiteral() 
     *     f3 -> ")"
     */
    public void visit(ErrorMessage n, CurFuncLine funcLine) {}

    
    /*
     *     f0 -> "goto" 
     *     f1 -> Label()
     */
    public void visit(Goto n, CurFuncLine funcLine) {}

    /*
     *     f0 -> "if0" 
     *     f1 -> Identifier() 
     *     f2 -> "goto" 
     *     f3 -> Label()
     */
    public void visit(IfGoto n, CurFuncLine funcLine) {
    	
        String idCond = n.f1.f0.tokenImage;
        if (!regs.contains(idCond))
        	this.cntNumIDs(funcLine.curFuncName, idCond);
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
    public void visit(Call n, CurFuncLine funcLine) {
    	
    	String returnID = n.f0.f0.tokenImage;
        if (!regs.contains(returnID))
        	this.cntNumIDs(funcLine.curFuncName, returnID);
    	
        String callID = n.f3.f0.tokenImage;
        if (!regs.contains(callID))
        	this.cntNumIDs(funcLine.curFuncName, callID);
    	
        n.f5.accept(this, funcLine);   
    }

    public void visit(FunctionName n, CurFuncLine funcLine) {return;}

    
    /*
     *      f0 ->
     */
    public void visit(Label n, CurFuncLine funcLine) { return; }

    /*
     *      f0 ->
     */
    public void visit(Identifier n, CurFuncLine funcLine) {
    	String id = n.f0.tokenImage;
    	if (!regs.contains(id))
    		this.cntNumIDs(funcLine.curFuncName, id);
    }

    public void visit(IntegerLiteral n, CurFuncLine funcLine) {return;}

    public void visit(StringLiteral n, CurFuncLine funcLine) {return;}

    public void visit(If n, CurFuncLine funcLine) {return;}

    public void visit(LabeledInstruction n, CurFuncLine funcLine) {return;}
}
