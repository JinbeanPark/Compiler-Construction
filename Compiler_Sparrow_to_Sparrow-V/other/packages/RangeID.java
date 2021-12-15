package other.packages;
import java.util.*;
import java.lang.*;
import java.io.*;

public class RangeID {
	
	int startLine, endLine;
	String id;
	
	public RangeID (String startLine, String endLine, String id) {
		this.startLine = Integer.parseInt(startLine);
		this.endLine = Integer.parseInt(endLine);
		this.id = id;
	}
	
	public static Comparator<RangeID> idComparator = new Comparator<RangeID>() {
		
		public int compare(RangeID a, RangeID b) {
			
			if ((a.startLine == -1 && b.endLine == -1) || (a.endLine == -1 && b.startLine == -1)) {
				
				if (a.endLine == -1 && b.startLine == -1) {
					if (a.startLine != b.endLine)
						return a.startLine - b.endLine;
					else {
						
//						System.out.println("a.start: " + a.startLine + ", a.endLine: " + a.endLine);
//						System.out.println("b.start: " + b.startLine + ", b.endLine: " + b.endLine);
						
						
						return 1;
					}
				
				}
				else if (a.startLine == -1 && b.endLine == -1) {
					if (a.endLine != b.startLine)
						return a.endLine - b.startLine;
					else {
						
						
//						System.out.println("a.start: " + a.startLine + ", a.endLine: " + a.endLine);
//						System.out.println("b.start: " + b.startLine + ", b.endLine: " + b.endLine);
						
						return -1;
					}
				}
				return 1;
			}
			else {
				if (a.endLine == -1)
					return a.startLine - b.startLine;
				else
					return a.endLine - b.endLine;
			}
		}
		
	};
	
}

