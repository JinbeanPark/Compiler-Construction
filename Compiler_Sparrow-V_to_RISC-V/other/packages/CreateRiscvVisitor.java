package other.packages;
import cs132.IR.visitor.*;
import cs132.IR.syntaxtree.*;
import java.util.*;


public class CreateRiscvVisitor extends GJDepthFirst<RiscvShape, CurFuncLine> {
	
	// Check whether the function is main function or not.
	boolean chkJumMain = false;
	// idIndx => Index of id in stack.
	int idIndx = 0, labelIndx = 1;
	// funIDs -> [funcName, number of IDs]
	public HashMap<String, Integer> funIDs = new HashMap<>();
	// usedLabels -> [funcName, [label name, goToLabel]]
	public HashMap<String, HashMap<String, String>> usedLabels = new HashMap<>();
	// funArgs -> [funcName, number of args]
	public HashMap<String, Integer> funArgs = new HashMap<>();
	// stackMgr -> [funcName, [ID, stack address]]
	public HashMap<String, HashMap<String, String>> stackMgr = new HashMap<>();
	// Registers
	ArrayList<String> regs = new ArrayList<>(Arrays.asList("a2", "a3", "a4", "a5", "a6", "a7", "t0", "t1", "t2", "t3",
			"t4", "t5", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11"));
	
	public String getLabel() {
		return "k_" + String.valueOf(this.labelIndx++);
	}
	
	public RiscvShape visit(NodeList n, CurFuncLine funcLine) {
        int _count = 0;

        for(Enumeration e = n.elements(); e.hasMoreElements(); ++_count) {
            ((Node)e.nextElement()).accept(this, funcLine);
        }
        return new RiscvShape();
    }
	
    public RiscvShape visit(NodeListOptional n, CurFuncLine funcLine) {
    	RiscvShape result = new RiscvShape();
    	
        if (n.present()) {
            int _count = 0;

            for(Enumeration <Node> e = n.elements(); e.hasMoreElements(); ++_count) {
            	RiscvShape subRes = e.nextElement().accept(this, funcLine);
                result.resultOptList.add(subRes.riscVsrc);
                result.resultOpt.add(subRes.s);
            }
        }
        return result;
    }

    public RiscvShape visit(NodeOptional n, CurFuncLine funcLine) {
        if (n.present()) {
            n.node.accept(this, funcLine);
        }
        return new RiscvShape();
    }

    public RiscvShape visit(NodeSequence n, CurFuncLine funcLine) {
        int _count = 0;

        for(Enumeration e = n.elements(); e.hasMoreElements(); ++_count) {
            ((Node)e.nextElement()).accept(this, funcLine);
        }
        return new RiscvShape();
    }

    public RiscvShape visit(NodeToken n, CurFuncLine funcLine) {return new RiscvShape();}
    
    /*
     *  f0 -> ( FunctionDeclaration() )* 
     *  f1 ->
     */
 
    public RiscvShape visit(Program n, CurFuncLine funcLine) {
    	RiscvShape result = new RiscvShape();
    	
    	result.riscVsrc.add("  .equiv @sbrk, 9");
    	result.riscVsrc.add("  .equiv @print_string, 4");
    	result.riscVsrc.add("  .equiv @print_char, 11");
    	result.riscVsrc.add("  .equiv @print_int, 1");
    	result.riscVsrc.add("  .equiv @exit 10");
    	result.riscVsrc.add("  .equiv @exit2, 17");
    	
    	RiscvShape f0Result = n.f0.accept(this, funcLine);
    	
    	for (ArrayList<String> subriscVsrc: f0Result.resultOptList)
    		result.riscVsrc.addAll(subriscVsrc);
    	
    	
//    	System.err.println("CreateRiscvVisitor Program Begin");
    	
    	
    	result.riscVsrc.add("# Print the error message at a0 and ends the program");
    	result.riscVsrc.add(".globl error");
    	result.riscVsrc.add("error:");
    	result.riscVsrc.add("  mv a1, a0                                # Move msg address to a1");
    	result.riscVsrc.add("  li a0, @print_string                     # Code for print_string ecall");
    	result.riscVsrc.add("  ecall                                    # Print error message in a1");
    	result.riscVsrc.add("  li a1, 10                                # Load newline character");
    	result.riscVsrc.add("  li a0, @print_char                       # Code for print_char ecall");
    	result.riscVsrc.add("  ecall                                    # Print newline");
    	result.riscVsrc.add("  li a0, @exit                             # Code for exit ecall");
    	result.riscVsrc.add("  ecall                                    # Exit with code");
    	result.riscVsrc.add("abort_17:                                  # Infinite loop");
    	result.riscVsrc.add("  j abort_17                               # Prevent fallthrough");
    	
    	result.riscVsrc.add("# Allocate a0 bytes on the heap, returns pointer to start in a0");
    	result.riscVsrc.add(".globl alloc");
    	result.riscVsrc.add("alloc:");
    	result.riscVsrc.add("  mv a1, a0                                # Move requested size to a1");
    	result.riscVsrc.add("  li a0, @sbrk                             # Code for ecall: sbrk");
    	result.riscVsrc.add("  ecall                                    # Request a1 bytes");
    	result.riscVsrc.add("  jr ra                                    # Return to caller");
    	
    	result.riscVsrc.add(".data");
    	result.riscVsrc.add(".globl msg_0");
    	result.riscVsrc.add("msg_0:");
    	result.riscVsrc.add("  .asciiz \"null pointer\"");
    	result.riscVsrc.add("  .align 2");
    	result.riscVsrc.add("");
    	
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

    public RiscvShape visit(FunctionDeclaration n, CurFuncLine funcLine) {
    	
    	RiscvShape result = new RiscvShape();
    	String curFunName = n.f1.f0.tokenImage;
    	funcLine.curFuncName = curFunName;
    	
    	if (!this.chkJumMain) {
    		result.riscVsrc.add(".text");
        	result.riscVsrc.add("  jal " + curFunName + "                                 # Jump to main");
        	result.riscVsrc.add("  li a0, @exit                             # Code for ecall: exit");
        	result.riscVsrc.add("  ecall");
        	chkJumMain = !chkJumMain;
    	}
    	
    	result.riscVsrc.add(".globl " + curFunName);
    	result.riscVsrc.add(curFunName + ":");
    	result.riscVsrc.add("  sw fp, -8(sp)                        # Store old frame pointer");
    	result.riscVsrc.add("  mv fp, sp                            # Set new fp");
    	// s1 - s11 + ids in curFunName + 20 extra ids.
    	int stackSize = (11 + this.funIDs.get(curFunName)) * 4 + 80;
    	result.riscVsrc.add("  li t6, " + String.valueOf(stackSize));
    	result.riscVsrc.add("  sub sp, sp, t6                       # Allocate a new frame of size");
    	result.riscVsrc.add("  sw ra, -4(fp)                        # Store return address");
    	
    	// Store s1 - s11 to the new stack frame.
    	// -4(fp) return-address, -8(fp) previous frame pointer, -12(fp) local-var1, -16(fp) local - var2 ...
    	// s1 - s11 => -12(fp) to -52(fp)
    	for (int i = 1; i < 12; i++) {
    		String fp = String.valueOf((i + 2) * 4) + "(fp)";
    		result.riscVsrc.add("sw s" + String.valueOf(i) + ", -" + fp);
    	}
    	this.idIndx = -56;
    	
    	// 0(fp) in[0], 4(fp) in[1] ... 20(fp) in[5]
    	int extraParamIndx = 24;
    	
    	if (n.f3.present()) {
            int _count = 0;

            for(Enumeration e = n.f3.elements(); e.hasMoreElements(); ++_count) {
            	
            	String param = ((Identifier)e.nextElement()).f0.toString();
            	String fp = String.valueOf(extraParamIndx) + "(fp)";
            	this.stackMgr.get(curFunName).put(param, String.valueOf(fp));
            	extraParamIndx += 4;
            }
        }
    	
    	RiscvShape f5Result = n.f5.accept(this, funcLine);
        result.riscVsrc.addAll(f5Result.riscVsrc);
        
        return result;
    }
    
    /*
     *   f0 -> ( Instruction() )*
     *   f1 -> "return" 
     *   f2 -> Identifier()
     */

    public RiscvShape visit(Block n, CurFuncLine funcLine) {
    	
    	RiscvShape result = new RiscvShape();
    	RiscvShape f0Result = n.f0.accept(this, funcLine);
    	
    	for (ArrayList<String> src: f0Result.resultOptList)
    		result.riscVsrc.addAll(src);
    	
    	String returnObj = n.f2.f0.tokenImage;
    	
    	// Load the value of -12(fp) ... -52(fp) to s1 ... s11  
    	for (int i = 1; i < 12; i++) {
    		String fp = String.valueOf((i + 2) * 4) + "(fp)";
    		result.riscVsrc.add("lw s" + String.valueOf(i) + ", -" + fp);
    	}
    	
    	// Load the address of the return value to a0
    	String returnObjAddr = this.stackMgr.get(funcLine.curFuncName).get(returnObj);
    	result.riscVsrc.add("  lw a0, " + returnObjAddr + "			# Load return value to a0");
    	result.riscVsrc.add("  lw ra, -4(fp)                         # Restore ra register");
    	result.riscVsrc.add("  lw fp, -8(fp)                         # Restore old fp");
    	
    	// The first addi gives back the stack space allocated at the start of a frame.
    	int stackSize = (11 + this.funIDs.get(funcLine.curFuncName)) * 4 + 80;
    	result.riscVsrc.add("  addi sp, sp, " + String.valueOf(stackSize));
    	// The second addi deals with the stack space allocated during the function operation.
    	int extraStackSize = this.funArgs.get(funcLine.curFuncName) * 4;
    	result.riscVsrc.add("  addi sp, sp, " + String.valueOf(extraStackSize));
    	// Return
    	result.riscVsrc.add("  jr ra                                 # Return");
    	
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

    public RiscvShape visit(Instruction n, CurFuncLine funcLine) {
    	
        RiscvShape f0Result =  n.f0.choice.accept(this, funcLine);
        return f0Result;
    }

    /*
     *   f0 -> Label()
     *   f1 -> ":"
     */
    
    public RiscvShape visit(LabelWithColon n, CurFuncLine funcLine) {
    	
    	RiscvShape result = new RiscvShape();
    	String f0Result =  n.f0.f0.tokenImage;
    	String goToLabel = this.usedLabels.get(funcLine.curFuncName).get(f0Result);
    	result.riscVsrc.add(goToLabel + ":");
    	return result;
    }
    
    /*
     *  f0 -> Identifier() 
     *  f1 -> "=" 
     *  f2 -> IntegerLiteral()
     */

    public RiscvShape visit(SetInteger n, CurFuncLine funcLine) {
    	
    	RiscvShape result = new RiscvShape();
    	String f0Result = n.f0.f0.tokenImage;
    	String f2Result = n.f2.f0.toString();
    	
    	if (!this.regs.contains(f0Result)) {
    		if (!this.stackMgr.get(funcLine.curFuncName).containsKey(f0Result)) {
    			String newAllocStackAddr = String.valueOf(this.idIndx) + "(fp)";
    			this.idIndx -= 4;
    			this.stackMgr.get(funcLine.curFuncName).put(f0Result, newAllocStackAddr);
    		}
    		String idAddr = stackMgr.get(funcLine.curFuncName).get(f0Result);
    		result.riscVsrc.add("  li t6, " + f2Result + " 				# Dynamically allocate out array on the stack upon calling");
    		result.riscVsrc.add("  sw t6, " + idAddr);
    	}
    	else
    		result.riscVsrc.add("  li " + f0Result + ", " + f2Result + " # Dynamically allocate out array on the stack upon calling");
    	
    	return result;
    }

    
    /*
     *   f0 -> Identifier() 
     *   f1 -> "=" 
     *   f2 -> "@" 
     *   f3 -> FunctionName()
     */
    
    public RiscvShape visit(SetFuncName n, CurFuncLine funcLine) {
    	
    	RiscvShape result = new RiscvShape();
    	String f0Result = n.f0.f0.tokenImage;
    	String f3Result = n.f3.f0.toString();
    	
    	if (!this.regs.contains(f0Result)) {
    		if (!this.stackMgr.get(funcLine.curFuncName).containsKey(f0Result)) {
    			String newAllocStackAddr = String.valueOf(this.idIndx) + "(fp)";
    			this.idIndx -= 4;
    			this.stackMgr.get(funcLine.curFuncName).put(f0Result, newAllocStackAddr);
    		}
    		String idAddr = stackMgr.get(funcLine.curFuncName).get(f0Result);
    		result.riscVsrc.add("  la t6, " + f3Result + " 				# Dynamically allocate out array on the stack upon calling");
    		result.riscVsrc.add("  sw t6, " + idAddr);
    	}
    	else
    		result.riscVsrc.add("  la " + f0Result + ", " + f3Result + " # Dynamically allocate out array on the stack upon calling");
    	
    	return result;
    }

    /*
     *    f0 -> Identifier() 
     *    f1 -> "=" 
     *    f2 -> Identifier() 
     *    f3 -> "+" 
     *    f4 -> Identifier()
     */
    
    public RiscvShape visit(Add n, CurFuncLine funcLine) {
    	
    	RiscvShape result = new RiscvShape();
    	String f0Result = n.f0.f0.tokenImage;
    	String f2Result = n.f2.f0.tokenImage;
    	String f4Result = n.f4.f0.tokenImage;
    	result.riscVsrc.add("add " + f0Result + ", " + f2Result + ", " + f4Result);
    	
    	return result;
    }

    /*
     *    f0 -> Identifier() 
     *    f1 -> "=" 
     *    f2 -> Identifier() 
     *    f3 -> "-" 
     *    f4 -> Identifier()
     */
    public RiscvShape visit(Subtract n, CurFuncLine funcLine) {
    	
    	RiscvShape result = new RiscvShape();
    	String f0Result = n.f0.f0.tokenImage;
    	String f2Result = n.f2.f0.tokenImage;
    	String f4Result = n.f4.f0.tokenImage;
    	result.riscVsrc.add("sub " + f0Result + ", " + f2Result + ", " + f4Result);
    	
    	return result;
    }

    
    /*
     *    f0 -> Identifier() 
     *    f1 -> "=" 
     *    f2 -> Identifier() 
     *    f3 -> "*" 
     *    f4 -> Identifier()
     */
    public RiscvShape visit(Multiply n, CurFuncLine funcLine) {
    	
    	RiscvShape result = new RiscvShape();
    	String f0Result = n.f0.f0.tokenImage;
    	String f2Result = n.f2.f0.tokenImage;
    	String f4Result = n.f4.f0.tokenImage;
    	result.riscVsrc.add("mul " + f0Result + ", " + f2Result + ", " + f4Result);
    	
    	return result;
    }

    
    /*
     *    f0 -> Identifier() 
     *    f1 -> "=" 
     *    f2 -> Identifier() 
     *    f3 -> "<" 
     *    f4 -> Identifier()
     */
    public RiscvShape visit(LessThan n, CurFuncLine funcLine) {
    	
    	RiscvShape result = new RiscvShape();
    	String f0Result = n.f0.f0.tokenImage;
    	String f2Result = n.f2.f0.tokenImage;
    	String f4Result = n.f4.f0.tokenImage;
    	result.riscVsrc.add("slt " + f0Result + ", " + f2Result + ", " + f4Result);
    	
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
    public RiscvShape visit(Load n, CurFuncLine funcLine) {
    	
    	RiscvShape result = new RiscvShape();
    	String f0Result = n.f0.f0.tokenImage;
    	String f3Result = n.f3.f0.tokenImage;
    	String f5Result = n.f5.f0.toString();
    	result.riscVsrc.add("lw " + f0Result + ", " + f5Result + "(" + f3Result + ")");
    	
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
    public RiscvShape visit(Store n, CurFuncLine funcLine) {

    	RiscvShape result = new RiscvShape();
    	String f1Result = n.f1.f0.tokenImage;
    	String f3Result = n.f3.f0.tokenImage;
    	String f6Result = n.f6.f0.toString();
    	result.riscVsrc.add("sw " + f6Result + ", " + f3Result + "(" + f1Result + ")");
    	
    	return result;
    }
    
    /*
     *     f0 -> Identifier() 
     *     f1 -> "=" 
     *     f2 -> Identifier()
     */

    public RiscvShape visit(Move n, CurFuncLine funcLine) {
    	
    	RiscvShape result = new RiscvShape();
    	String f0Result = n.f0.f0.tokenImage;
    	String f2Result = n.f2.f0.tokenImage;
    	
		if (!this.regs.contains(f0Result) || !this.regs.contains(f2Result)) {
			if (!this.regs.contains(f0Result)) {
				if (!this.stackMgr.get(funcLine.curFuncName).containsKey(f0Result)) {
					String newAllocStackAddr = String.valueOf(this.idIndx) + "(fp)";
					this.idIndx -= 4;
					this.stackMgr.get(funcLine.curFuncName).put(f0Result, newAllocStackAddr);
				}
				String idAddr = stackMgr.get(funcLine.curFuncName).get(f0Result);
				
//				System.out.println("CreateRiscvVisitor Move sw  f2Result: " + f2Result + ", idAddr" + idAddr);
				
				result.riscVsrc.add("  sw " + f2Result+ ", " + idAddr);
			}
			if (!this.regs.contains(f2Result)) {
				if (!this.stackMgr.get(funcLine.curFuncName).containsKey(f2Result)) {
					String newAllocStackAddr = String.valueOf(this.idIndx) + "(fp)";
					this.idIndx -= 4;
					this.stackMgr.get(funcLine.curFuncName).put(f2Result, newAllocStackAddr);
				}
				String idAddr = stackMgr.get(funcLine.curFuncName).get(f2Result);
				result.riscVsrc.add("  lw " + f0Result + ", " + idAddr);
			}
		}
		else
			result.riscVsrc.add("  mv " + f0Result + ", " + f2Result);

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
    public RiscvShape visit(Alloc n, CurFuncLine funcLine) {
    	
    	RiscvShape result = new RiscvShape();
    	String f0Result = n.f0.f0.tokenImage;
    	String f4Result = n.f4.f0.tokenImage;
    	
    	result.riscVsrc.add("  mv a0, " + f4Result + "# Move requested size to a0");
    	result.riscVsrc.add("  jal alloc 				# Call alloc subroutine to request heap memory");
    	result.riscVsrc.add("  mv " + f0Result + ", a0 				# Move the returned pointer to f0Result");
    	
    	return result;
    }

    
    /*
     *     f0 -> "print" 
     *     f1 -> "(" 
     *     f2 -> Identifier() 
     *     f3 -> ")"
     */
    public RiscvShape visit(Print n, CurFuncLine funcLine) {
    	
    	RiscvShape result = new RiscvShape();
    	String f2Result = n.f2.f0.tokenImage;
    	
    	result.riscVsrc.add("  mv a1, " + f2Result + " 				# Mov the content to be printed to a1");
    	result.riscVsrc.add("li a0, @print_int");
    	result.riscVsrc.add("ecall");
    	result.riscVsrc.add("  li a1, 10                                # Load newline character");
    	result.riscVsrc.add("  li a0, @print_char                       # Code for print_char ecall");
    	result.riscVsrc.add("  ecall                                    # Print newline");
    	return result;
    }
    
    /*
     *     f0 -> "error" 
     *     f1 -> "(" 
     *     f2 -> StringLiteral() 
     *     f3 -> ")"
     */
    public RiscvShape visit(ErrorMessage n, CurFuncLine funcLine) {
    	
    	RiscvShape result = new RiscvShape();	
    	result.riscVsrc.add("la a0, msg_0 				# Load the address of the error message to a0");
    	result.riscVsrc.add("j error");
    	return result;
    }

    
    /*
     *     f0 -> "goto" 
     *     f1 -> Label()
     */
    public RiscvShape visit(Goto n, CurFuncLine funcLine) {
    	
    	RiscvShape result = new RiscvShape();
    	String f1Result = n.f1.f0.tokenImage;
    	result.riscVsrc.add("li t6, 0");
    	result.riscVsrc.add("jal t6, " + this.usedLabels.get(funcLine.curFuncName).get(f1Result));
    	return result;
    }

    /*
     *     f0 -> "if0" 
     *     f1 -> Identifier() 
     *     f2 -> "goto" 
     *     f3 -> Label()
     */
    public RiscvShape visit(IfGoto n, CurFuncLine funcLine) {
    	
    	RiscvShape result = new RiscvShape();
    	String f1Result = n.f1.f0.tokenImage;
    	String f3Result = n.f3.f0.tokenImage;
    	
    	String tmpLabel = this.getLabel();
    	result.riscVsrc.add("li t6, 0");
    	result.riscVsrc.add("bne " + f1Result + ", t6, " + tmpLabel);
    	result.riscVsrc.add("jal t6, " + this.usedLabels.get(funcLine.curFuncName).get(f3Result));
    	result.riscVsrc.add(tmpLabel + ":");
    	
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
    public RiscvShape visit(Call n, CurFuncLine funcLine) {
    	
    	RiscvShape result = new RiscvShape();
    	String f0Result = n.f0.f0.tokenImage;
    	String f3Result = n.f3.f0.tokenImage;
    	
    	int totArgs = 6;
        if (n.f5.present()) {
            int _count = 0;
            for(Enumeration e = n.f5.elements(); e.hasMoreElements(); ++_count) {
            	((Identifier)e.nextElement()).f0.toString();
            	totArgs++;
            }
        }
        
        int stackSize = totArgs * 4;
    	result.riscVsrc.add("  li t6, " + String.valueOf(stackSize));
    	result.riscVsrc.add("  sub sp, sp, t6                       # Allocate a new frame of size");
    	
    	// Store a2 - a7 to 0(sp) - 20(sp)
    	for (int i = 2; i < 8; i++) {
    		String stackPnr = String.valueOf(4 * (i - 2)) + "(sp)";
    		result.riscVsrc.add("sw " + ("a" + String.valueOf(i)) + ", " + stackPnr);
    	}
    	

    	int extraParamIndx = 24;    	
    	
    	if (n.f5.present()) {
            int _count = 0;

            for(Enumeration e = n.f5.elements(); e.hasMoreElements(); ++_count) {
            	
            	String param = ((Identifier)e.nextElement()).f0.toString();
            	String extraStackPnr = String.valueOf(extraParamIndx) + "(sp)";
            	extraParamIndx += 4;
            	String idAddr = this.stackMgr.get(funcLine.curFuncName).get(param);
            	result.riscVsrc.add("lw t6, " + idAddr);
            	result.riscVsrc.add("sw t6, " + extraStackPnr);
            }
        }
    	
    	result.riscVsrc.add("jalr ra, " + f3Result + ", 0");
    	result.riscVsrc.add("mv " + f0Result + ", a0"); // Store the ret-value in a0.
    	
        return result;
    }

    public RiscvShape visit(FunctionName n, CurFuncLine funcLine) {return new RiscvShape();}

    
    /*
     *      f0 ->
     */
    public RiscvShape visit(Label n, CurFuncLine funcLine) {
    	RiscvShape result = new RiscvShape();
    	String label = n.f0.tokenImage;
    	result.riscVsrc.add(label);
    	return result;
    }

    /*
     *      f0 ->
     */
    public RiscvShape visit(Identifier n, CurFuncLine funcLine) {
    	RiscvShape result = new RiscvShape();
    	String id = n.f0.tokenImage;
    	result.riscVsrc.add(id);
    	return result;
    	
    }

    public RiscvShape visit(IntegerLiteral n, CurFuncLine funcLine) {
    	
    	RiscvShape result = new RiscvShape();
    	String id = n.f0.tokenImage;
    	result.riscVsrc.add(id);
    	return result;
    }

    public RiscvShape visit(StringLiteral n, CurFuncLine funcLine) {
    	
    	RiscvShape result = new RiscvShape();
    	String id = n.f0.tokenImage;
    	result.riscVsrc.add(id);
    	return result;
    }

    public RiscvShape visit(If n, CurFuncLine funcLine) {return new RiscvShape();}

    public RiscvShape visit(LabeledInstruction n, CurFuncLine funcLine) {return new RiscvShape();}
}
