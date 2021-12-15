package other.packages;
import cs132.IR.visitor.*;
import cs132.IR.syntaxtree.*;
import java.util.*;


public class AssignValVisitor extends GJVoidDepthFirst<CurFuncLine> {
	
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
	
	
	public void addParams(String funcName, String id, int lineNum) {
			
	
		// Starting line number is already defined on id.
		if (usedParams.get(funcName).containsKey(id)) {
			ArrayList<String> structParam = usedParams.get(funcName).get(id);
			int minLine = Math.min(lineNum, Integer.parseInt(structParam.get(1)));
			int maxLine = Math.max(lineNum, Integer.parseInt(structParam.get(2)));
			structParam.set(1, String.valueOf(minLine));
			structParam.set(2, String.valueOf(maxLine));	
		}
		else {
			
			addVars(funcName, id, lineNum);
			
//			String reg = usedParams.get(funcName).get(id).get(0);
//			String lineNumStr = String.valueOf(lineNum);
//			usedParams.get(funcName).put(id, new ArrayList<>(Arrays.asList(reg, lineNumStr, lineNumStr)));
		}
	}
	
	public void addVars(String funcName, String id, int lineNum) {
		
		
		if (usedParams.get(funcName).containsKey(id)) {
			this.addParams(funcName, id, lineNum);
			return;
		}
		
		if (!usedVars.containsKey(funcName))
			usedVars.put(funcName, new HashMap<String, ArrayList<Integer>>());
		if (!usedVars.get(funcName).containsKey(id))
			usedVars.get(funcName).put(id, new ArrayList<Integer>(Arrays.asList(lineNum, lineNum)));
		else {
//			System.err.println("AssignValVisitor funcName minLine: ");
			int minLine = Math.min(lineNum, usedVars.get(funcName).get(id).get(0));
			int maxLine = Math.max(lineNum, usedVars.get(funcName).get(id).get(1));
			usedVars.get(funcName).get(id).set(0, minLine);
			usedVars.get(funcName).get(id).set(1, maxLine);
//			System.err.println("AssignValVisitor funcName minLine222222: ");
			
		}
	}
	
	public void addLabels(String funcName, String id, int lineNum) {
		
		if (!usedLabels.containsKey(funcName))
			usedLabels.put(funcName, new HashMap<String, Integer>());
		usedLabels.get(funcName).put(id, lineNum);		
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
        n.f0.accept(this, funcLine);
        String funcName = n.f1.f0.tokenImage;
        funcLine.curFuncName = funcName;
        int curFuncLine = funcLine.curLineNum;
        int regSeq = 2;
        usedParams.put(funcName, new HashMap<String, ArrayList<String>>());
        
        loops.clear();
        if (n.f3.present()) {
            int _count = 0;

            for(Enumeration e = n.f3.elements(); e.hasMoreElements(); ++_count) {
            	
            	String aReg = "a" + Integer.toString(regSeq);
            	String param = ((Identifier)e.nextElement()).f0.toString();
            	if (regSeq++ < 8) {
            		// Initial parameter value of starting and ending line number is 0.
            		usedParams.get(funcName).put(param, new ArrayList<>(Arrays.asList(aReg, String.valueOf(0), String.valueOf(0))));
            		//usedParams.get(funcName).put(param, new ArrayList<>(Arrays.asList(aReg, String.valueOf(curFuncLine), String.valueOf(curFuncLine))));
            	}
            	this.addParams(funcName, param, 0);
            }
        }
        
        n.f5.accept(this, funcLine);
        cntLine = 1;
        ArrayList<RangeID> liveness = new ArrayList<>();
        
        for (String var: usedVars.get(funcName).keySet()) {
        	int startVar = usedVars.get(funcName).get(var).get(0);
        	int endVar = usedVars.get(funcName).get(var).get(1);
        	
        	for (int i = 0; i < loops.size(); i++) {
        		ArrayList<Integer> loop = loops.get(i);
        		int startLoop = loop.get(0);
        		int endLoop = loop.get(1);
        		
        		if ((startLoop <= startVar && startVar <= endLoop) || (startLoop <= endVar && endVar <= endLoop)) {
        			startVar = Math.min(startLoop, startVar);
        			endVar = Math.max(endLoop, endVar);
        			usedVars.get(funcName).get(var).set(0, startVar);
        			usedVars.get(funcName).get(var).set(1, endVar);
        		}
        	}
        	String strStartVar = String.valueOf(startVar);
        	String strEndVar = String.valueOf(endVar);
        	liveness.add(new RangeID(strStartVar, "-1", var));
        	liveness.add(new RangeID("-1", strEndVar, var));
        }
        
        for (String var: usedParams.get(funcName).keySet()) {
        	int startVar = Integer.parseInt(usedParams.get(funcName).get(var).get(1));
        	int endVar = Integer.parseInt(usedParams.get(funcName).get(var).get(2));
        	
        	for (int i = 0; i < loops.size(); i++) {
        		ArrayList<Integer> loop = loops.get(i);
        		int startLoop = loop.get(0);
        		int endLoop = loop.get(1);
        		
        		if ((startLoop <= startVar && startVar <= endLoop) || (startLoop <= endVar && endVar <= endLoop)) {
        			startVar = Math.min(startLoop, startVar);
        			endVar = Math.max(endLoop, endVar);
        			usedParams.get(funcName).get(var).set(1, String.valueOf(startVar));
        			usedParams.get(funcName).get(var).set(2, String.valueOf(endVar));
        		}
        	}
        }
        
        rangeVarsFunc.put(funcName, usedVars.get(funcName));
        rangeParamsFunc.put(funcName, usedParams.get(funcName));
        Collections.sort(liveness, RangeID.idComparator);
        
        
//        for (RangeID id: liveness) {
//        	System.out.println("id: " + id.id + ", startLine: " + id.startLine + ", endLine: " + id.endLine);
//        }
        
        
        HashMap<String, String> usingIDs = new HashMap<>();
        HashMap<String, String> livenessIDs = new HashMap<>();
        Queue<String> availableRegs = new LinkedList<String>(Arrays.asList("t3", "t4", "t5", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11"));
        
        for (RangeID rgID: liveness) {
        	
        	String id = rgID.id;
        	
        	if (rgID.endLine == -1) {
        		if (availableRegs.isEmpty()) {
        			
        			int endLineIDtoAllo = usedVars.get(funcName).get(id).get(1);
        			String idMaxEnd = id;
        			int maxEndUsingIDs = endLineIDtoAllo;
        			
        			for (String usingID: usingIDs.keySet()) {
        				int endLine = usedVars.get(funcName).get(usingID).get(1);
        				if (maxEndUsingIDs < endLine) {
        					idMaxEnd = usingID;
        					maxEndUsingIDs = endLine;
        				}
        			}
        			if (!idMaxEnd.equals(id)) {
        				String regToAllo = usingIDs.get(idMaxEnd);
        				usingIDs.remove(idMaxEnd);
        				usingIDs.put(id, regToAllo);
        			}
        		}
        		else {
        			String allocatReg = availableRegs.poll();
        			usingIDs.put(id, allocatReg);
        		}
        	}
        	else {
        		if (usingIDs.containsKey(id)) {
        			availableRegs.add(usingIDs.get(id));
        			livenessIDs.put(id, usingIDs.get(id));
        			usingIDs.remove(id);
        		}
        	}
        }
        
        for (String id: usingIDs.keySet())
        	livenessIDs.put(id, usingIDs.get(id));
        
//        for (String id: livenessIDs.keySet()) {
//        	System.out.println("id: " + id + ", reg: " + livenessIDs.get(id));
//        }
        
//        System.err.println("AssignValVisitor FuncDeclaration funcName: " + funcName);
        
        this.livessEachFunc.put(funcName, livenessIDs);
        
    }
    
    /*
     *   f0 -> ( Instruction() )*
     *   f1 -> "return" 
     *   f2 -> Identifier()
     */

    public void visit(Block n, CurFuncLine funcLine) {
        n.f0.accept(this, funcLine);
        n.f1.accept(this, funcLine);
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
        n.f0.accept(this, funcLine);
        n.f1.accept(this, funcLine);
    }
    
    /*
     *  f0 -> Identifier() 
     *  f1 -> "=" 
     *  f2 -> IntegerLiteral()
     */

    public void visit(SetInteger n, CurFuncLine funcLine) {
        String id = n.f0.f0.tokenImage;
        this.addVars(funcLine.curFuncName, id, funcLine.curLineNum);
    }

    
    /*
     *   f0 -> Identifier() 
     *   f1 -> "=" 
     *   f2 -> "@" 
     *   f3 -> FunctionName()
     */
    
    public void visit(SetFuncName n, CurFuncLine funcLine) {
    	String id = n.f0.f0.tokenImage;
        this.addVars(funcLine.curFuncName, id, funcLine.curLineNum);
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
        this.addVars(funcLine.curFuncName, intLHS, funcLine.curLineNum);
        
        String intRhsL = n.f2.f0.tokenImage;
        this.addVars(funcLine.curFuncName, intRhsL, funcLine.curLineNum);
        
        String intRhsR = n.f4.f0.tokenImage;
        this.addVars(funcLine.curFuncName, intRhsR, funcLine.curLineNum);
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
        this.addVars(funcLine.curFuncName, intLHS, funcLine.curLineNum);
        
        String intRhsL = n.f2.f0.tokenImage;
        this.addVars(funcLine.curFuncName, intRhsL, funcLine.curLineNum);
        
        String intRhsR = n.f4.f0.tokenImage;
        this.addVars(funcLine.curFuncName, intRhsR, funcLine.curLineNum);
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
        this.addVars(funcLine.curFuncName, intLHS, funcLine.curLineNum);
        
        String intRhsL = n.f2.f0.tokenImage;
        this.addVars(funcLine.curFuncName, intRhsL, funcLine.curLineNum);
        
        String intRhsR = n.f4.f0.tokenImage;
        this.addVars(funcLine.curFuncName, intRhsR, funcLine.curLineNum);
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
        this.addVars(funcLine.curFuncName, intLHS, funcLine.curLineNum);
        
        String intRhsL = n.f2.f0.tokenImage;
        this.addVars(funcLine.curFuncName, intRhsL, funcLine.curLineNum);
        
        String intRhsR = n.f4.f0.tokenImage;
        this.addVars(funcLine.curFuncName, intRhsR, funcLine.curLineNum);
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
        this.addVars(funcLine.curFuncName, loadObj, funcLine.curLineNum);
        
        String obj = n.f3.f0.tokenImage;
        this.addVars(funcLine.curFuncName, obj, funcLine.curLineNum);
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
        this.addVars(funcLine.curFuncName, storeObj, funcLine.curLineNum);
        
        String obj = n.f6.f0.tokenImage;
        this.addVars(funcLine.curFuncName, obj, funcLine.curLineNum);
    }
    
    /*
     *     f0 -> Identifier() 
     *     f1 -> "=" 
     *     f2 -> Identifier()
     */

    public void visit(Move n, CurFuncLine funcLine) {
    	String idLHS = n.f0.f0.tokenImage;
        this.addVars(funcLine.curFuncName, idLHS, funcLine.curLineNum);
        
        String idRHS = n.f2.f0.tokenImage;
        this.addVars(funcLine.curFuncName, idRHS, funcLine.curLineNum);
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
        this.addVars(funcLine.curFuncName, idAlloc, funcLine.curLineNum);
        
        String sizeObj = n.f4.f0.tokenImage;
        this.addVars(funcLine.curFuncName, sizeObj, funcLine.curLineNum);
    }

    
    /*
     *     f0 -> "print" 
     *     f1 -> "(" 
     *     f2 -> Identifier() 
     *     f3 -> ")"
     */
    public void visit(Print n, CurFuncLine funcLine) {
        String printObj = n.f2.f0.tokenImage;
        this.addVars(funcLine.curFuncName, printObj, funcLine.curLineNum);
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
    public void visit(Goto n, CurFuncLine funcLine) {
        String label = n.f1.f0.tokenImage;
        String curFunc = funcLine.curFuncName;
        int curLine = funcLine.curLineNum;
        if (this.usedLabels.containsKey(curFunc) && this.usedLabels.get(curFunc).containsKey(label)) {
        	int lineToMove = this.usedLabels.get(curFunc).get(label);
        	if (lineToMove < curLine)
        		loops.add(new ArrayList<Integer>(Arrays.asList(lineToMove, curLine)));
        }
    }

    /*
     *     f0 -> "if0" 
     *     f1 -> Identifier() 
     *     f2 -> "goto" 
     *     f3 -> Label()
     */
    public void visit(IfGoto n, CurFuncLine funcLine) {
        String idCond = n.f1.f0.tokenImage;
        this.addVars(funcLine.curFuncName, idCond, funcLine.curLineNum);
        
        String label = n.f3.f0.tokenImage;
        String curFunc = funcLine.curFuncName;
        int curLine = funcLine.curLineNum;
        if (this.usedLabels.containsKey(curFunc) && this.usedLabels.get(curFunc).containsKey(label)) {
        	int lineToMove = this.usedLabels.get(curFunc).get(label);
        	if (lineToMove < curLine)
        		loops.add(new ArrayList<Integer>(Arrays.asList(lineToMove, curLine)));
        }
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
    	String resultObj = n.f0.f0.tokenImage;
    	this.addVars(funcLine.curFuncName, resultObj, funcLine.curLineNum);
    	String callObj = n.f3.f0.tokenImage;
    	this.addVars(funcLine.curFuncName, callObj, funcLine.curLineNum);
    	n.f5.accept(this, funcLine);   
    }

    public void visit(FunctionName n, CurFuncLine funcLine) {return;}

    
    /*
     *      f0 ->
     */
    public void visit(Label n, CurFuncLine funcLine) {
    	String label = n.f0.tokenImage;
        this.addLabels(funcLine.curFuncName, label, funcLine.curLineNum);
    }

    /*
     *      f0 ->
     */
    public void visit(Identifier n, CurFuncLine funcLine) {
    	String id = n.f0.tokenImage;
        this.addVars(funcLine.curFuncName, id, funcLine.curLineNum);
    }

    public void visit(IntegerLiteral n, CurFuncLine funcLine) {return;}

    public void visit(StringLiteral n, CurFuncLine funcLine) {return;}

    public void visit(If n, CurFuncLine funcLine) {return;}

    public void visit(LabeledInstruction n, CurFuncLine funcLine) {return;}
}
