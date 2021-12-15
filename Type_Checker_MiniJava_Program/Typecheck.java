import cs132.minijava.*;
import cs132.minijava.syntaxtree.*;
import cs132.minijava.visitor.*;
import other.packages.IDnType;
import other.packages.AssignValVisitor;
import other.packages.TypeChkVisitor;


public class Typecheck {
    public static void main(String [] args) {
    	
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
    	
    	if (root.accept(new AssignValVisitor(), idnt).equals("Type error")) {
    		
    		//System.out.println("Error AssignValVisitor");
    		
    		System.out.println("Type error");
    	}
    	else if (root.accept(new TypeChkVisitor(), idnt).equals("Type error")) {
    		
    		//System.out.println("Error TypeChkVisitor");
    		
    		System.out.println("Type error");
    	}
    	else
    		System.out.println("Program type checked successfully");
    }
}


