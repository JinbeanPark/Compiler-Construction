Made visitors in Java that compile a MiniJava program to Sparrow.

Main file is called J2S.java, and if P.java contains a syntactically correct MiniJava program, then

java J2S < P.java > P.sparrow

creates a Sparrow program P.sparrow with the same behavior as P.java.