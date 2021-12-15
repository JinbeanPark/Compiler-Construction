import cs132.IR.visitor.GJVoidDepthFirst;
import cs132.IR.syntaxtree.*;
import cs132.IR.ParseException;
import cs132.IR.SparrowParser;
import other.packages.*;


public class S2SV{
    public static void main(String [] args) {
    	try {
    		cs132.IR.syntaxtree.Node root = new SparrowParser(System.in).Program();
    		CurFuncLine cfl = new CurFuncLine();
    		AssignValVisitor av = new AssignValVisitor();
    		root.accept(av, cfl);
    		
    		//System.err.println("222@!!!!! begin @@");
    		CreateSparrowvVisitor csv = new CreateSparrowvVisitor();
    		csv.usedVars = av.usedVars;
    		csv.usedParams = av.usedParams;
    		csv.usedLabels = av.usedLabels;
    		csv.loops = av.loops;
    		csv.rangeVarsFunc = av.rangeVarsFunc;
    		csv.rangeParamsFunc = av.rangeParamsFunc;
    		csv.livessEachFunc = av.livessEachFunc;
    		cfl = new CurFuncLine();
    		SparrowVShape svs = root.accept(csv, cfl);
    		
    		for (String src: svs.sparrVsrc)
    			System.out.println(src);
    		
    	} catch (ParseException e) { e.printStackTrace(); }   
    }
}
