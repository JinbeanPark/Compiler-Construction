import java.util.*;
import java.util.regex.*;
import other.packages.Token;

public class Parse {

    public static HashMap<String, String> terSyms; // <Symbol type, Symbol pattern>
    public static ArrayList<Token> tokens;
    public static Token curToken = new Token("", "");

    public static void putTerSyms() {
        terSyms = new HashMap<>();

        // Put terminal symbols into HashMap.
        terSyms.put("OpenBracket", "\\{");
        terSyms.put("CloseBracket", "\\}");
        terSyms.put("Sysout", "System.out.println");
        terSyms.put("OpenParenths", "\\(");
        terSyms.put("CloseParenths", "\\)");
        terSyms.put("Semicolon", "\\;");
        terSyms.put("If", "if");
        terSyms.put("Else", "else");
        terSyms.put("While", "while");
        terSyms.put("True", "true");
        terSyms.put("False", "false");
        terSyms.put("Not", "\\!");
        terSyms.put("Whitespace", "[\t]+");
        terSyms.put("Newline", "\n");
        terSyms.put("Num", "0|[1-9][0-9]*");
        terSyms.put("Identifier", "[a-zA-Z][a-zA-Z0-9]*");
    }

    public static boolean eatTok(String curTokenType) {
    	
    	/*
    	if (!tokens.isEmpty())
    		System.out.println("EatTok => " + " tokenType: "+ tokens.get(0).tokenType + ", curTokenType: " + curTokenType);
    	*/
    	
        if (!tokens.isEmpty() && tokens.get(0).tokenType.equals(curTokenType)) {
            tokens.remove(0);
            return true;
        }
        else
            return false;
    }

    public static boolean parseS() {

        if (tokens.isEmpty()) return false;
        
        //System.out.println("ParseS => " + "tokenType: " + tokens.get(0).tokenType + ", tokenVal: " + tokens.get(0).tokenVal);

        // Case 1: { L }
        if (tokens.get(0).tokenType.equals("OpenBracket")) {

            if (!eatTok("OpenBracket")) return false;

            return (parseL() && parseTerminal("CloseBracket"));
        }
        // Case 2: System.out.println ( E ) ;
        else if (tokens.get(0).tokenType.equals("Sysout")) {

            if (!eatTok("Sysout")) return false;

            return (parseTerminal("OpenParenths") && parseE() && parseTerminal("CloseParenths") && parseTerminal("Semicolon"));
        }
        // Case 3: if (E) S else S
        else if (tokens.get(0).tokenType.equals("If")) {

            if (!eatTok("If")) return false;
            
            //System.out.println("Eat 'If' successfully");
            
            return (parseTerminal("OpenParenths") && parseE() && parseTerminal("CloseParenths") && parseS() && parseTerminal("Else") && parseS());
        }
        // Case 4: while (E) S
        else if (tokens.get(0).tokenType.equals("While")) {

            if (!eatTok("While")) return false;

            return (parseTerminal("OpenParenths") && parseE() && parseTerminal("CloseParenths") && parseS());
        }
        else
            return false;
    }

    public static boolean parseL() {
    	
        // Case 1: Epsilon
    	if (!tokens.isEmpty() && tokens.get(0).tokenType.equals("CloseBracket")) return true;
        
    	//System.out.println("ParseL => " + "tokenType: " + tokens.get(0).tokenType + ", tokenVal: " + tokens.get(0).tokenVal);
        
        // Case 2: SL
        return (parseS() && parseL());
    }

    public static boolean parseE() {

        if (tokens.isEmpty()) return false;
        
    	//System.out.println("ParseE => " + "tokenType: " + tokens.get(0).tokenType + ", tokenVal: " + tokens.get(0).tokenVal);

        // Case 1: True or False
        if (tokens.get(0).tokenType.equals("True") || tokens.get(0).tokenType.equals("False")) {
            if (!eatTok(tokens.get(0).tokenType)) {
                return false;
            }
            return true;
        }
        // Case 2: NOT
        else if (tokens.get(0).tokenType.equals("Not")) {
            if (!eatTok(tokens.get(0).tokenType)) {
                return false;
            }
            return parseE();
        }
        return false;
    }

    public static boolean parseTerminal(String curTokenType) {

        if (tokens.isEmpty()) return false;
                
    	//System.out.println("ParseTerminal => " + "tokenType: " + tokens.get(0).tokenType + ", tokenVal: " + tokens.get(0).tokenVal);

        if (!eatTok(curTokenType))
            return false;

        return true;
    }

    public static void main(String[] args) {
    	
    	//System.out.println("@@@@@@@@@@@@ Start!! @@@@@@@@@@@@");
    	
    	// Define scanner to get the input.
        Scanner sc = new Scanner(System.in);
        
    	// In order to change the string dynamically, I used StringBuffer instead of String.
        StringBuffer sb = new StringBuffer();
        
        while (sc.hasNextLine()) {
            sb.append(sc.nextLine());
            sb.append("\n");
        }
        
        // Convert StringBuffer into String.
        String inputStr = sb.toString();
        //String inputStr = "{ } { }\n";
        
        // Put terminal Symbols into HashMap.
        putTerSyms();

        // Clear the buffer
        sb.delete(0, sb.length());
        
        // Creating a regex for terminal symbols.
        /* Reference: https://www.javatpoint.com/post/java-matcher-group-method */
        for (String typeSym: terSyms.keySet()) {
            String patternSym = terSyms.get(typeSym);
            sb.append("|(?<" + typeSym + ">" + patternSym + ")");
        }
        String regexTerSyms = sb.substring(1, sb.length()).toString();
        
        //System.out.println("regexTerSyms: " + regexTerSyms);

        // Create a pattern from regex for terminal symbols
        Pattern pattern = Pattern.compile(regexTerSyms);

        // Create a matcher for the input String
        Matcher matcher = pattern.matcher(inputStr);

        tokens = new ArrayList<>();
        while (matcher.find()) {
            // Get the token matched with a regex
            String curTerSym = matcher.group();
            
            //System.out.println("curTerSym: " + curTerSym);

            if (matcher.group("Whitespace") != null || matcher.group("Newline") != null)
                continue;
            else if (curTerSym.equals("if"))
            	tokens.add(new Token("If", curTerSym));
            else if (curTerSym.equals("else"))
            	tokens.add(new Token("Else", curTerSym));
            else if (curTerSym.equals("while"))
            	tokens.add(new Token("While", curTerSym));
            else if (curTerSym.equals("true"))
            	tokens.add(new Token("True", curTerSym));
            else if (curTerSym.equals("false"))
            	tokens.add(new Token("False", curTerSym));
            
            if (curTerSym.equals("if") || curTerSym.equals("else") || curTerSym.equals("while") || curTerSym.equals("true") || curTerSym.equals("false"))
            	continue;

            // Insert tokens into arraylist.
            for (String typeSym: terSyms.keySet()) {
                // Find the token type for the curTerSym.
                if (matcher.group(typeSym) != null) {
                	
                	//System.out.println("typeSym: " + typeSym + ", curTerSym: " + curTerSym);
                	
                    tokens.add(new Token(typeSym, curTerSym));
                }
            }
        }
        
        /*
        for (int i = 0; i < tokens.size(); i++) {
        	System.out.println("indx: " + i + " tokenVal: " + tokens.get(i).tokenVal);
        }
        */
        
        boolean resultParse = parseS();

        if (!resultParse || tokens.size() != 0)
            System.out.println("Parse error");
        else
            System.out.println("Program parsed successfully");
    }
}