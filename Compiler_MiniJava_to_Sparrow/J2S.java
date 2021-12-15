import cs132.minijava.*;
import cs132.minijava.syntaxtree.*;
import cs132.minijava.visitor.*;
import other.packages.*;


public class J2S {
	public static void main(String[] args) {
		
		Goal root = null;
		
    	try {
    		//System.out.println("@@@@@@ Begin @@@@@@");
			
    		root = new MiniJavaParser(System.in).Goal();
    		
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
    	
    	IDnType idnt = new IDnType();
    	
    	root.accept(new AssignValVisitor(), idnt);
    	
    	idnt.extendParToChild();
    	
    	CreateSparrowVisitor sparrowVisitor = new CreateSparrowVisitor();
    	
    	//System.out.println("@@@ Entered @@@");
    	
    	SparrowShape sparrowSrc = root.accept(sparrowVisitor, idnt);
    	
    	//System.out.println("J2S before printing");
    	
    	for (String line: sparrowSrc.sparrSrc) {
    		//System.err.println(line);
    		System.out.println(line);
    	}
	}
}

