import cs132.IR.visitor.GJVoidDepthFirst;
import cs132.IR.syntaxtree.*;
import cs132.IR.ParseException;
import cs132.IR.SparrowParser;
import cs132.IR.registers.Registers;
import other.packages.*;

public class SV2V {
	public static void main(String [] args) {
    	try {
    		
    		Registers.SetRiscVregs();
    		Node root = new SparrowParser(System.in).Program();
    		
//    		System.err.println("@@@ Entered @@@");
    		
    		CurFuncLine cfl = new CurFuncLine();
    		AssignValVisitor av = new AssignValVisitor();
    		root.accept(av, cfl);
    		
//    		System.err.println(" ========= Entered ==========");
    		
    		CreateRiscvVisitor crv = new CreateRiscvVisitor();
    		crv.funIDs = av.funIDs;
    		crv.usedLabels = av.usedLabels;
    		crv.funArgs = av.funArgs;
    		crv.stackMgr = av.stackMgr;
    		cfl = new CurFuncLine();
    		RiscvShape rvs = root.accept(crv, cfl);
    		
    		for (String src: rvs.riscVsrc)
    			System.out.println(src);
    		
    	} catch (ParseException e) { e.printStackTrace(); }   
    }
}
