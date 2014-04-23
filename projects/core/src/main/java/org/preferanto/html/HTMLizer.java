package org.preferanto.html;
import java.io.*;

public class HTMLizer {
	private static JavaCharStream stream = null;
	
    private final Reader input;

    public String output;

    public HTMLizer(Reader input) {
    	this.input = input;
    }
    
    public void go() {    	
        StringBuilder out = new StringBuilder();
        
        if(stream == null) {
            stream = new JavaCharStream(input);
        } else {
        	stream.ReInit(input);
        }
		JavaParserTokenManager.ReInit(stream);
//        new JavaParserTokenManager(new JavaCharStream(input));
        Token t;
        while ((t = JavaParserTokenManager.getNextToken()).kind != JavaParserConstants.EOF) {
            out.append(t.image);
        }
        output = out.toString();        
    }

    public void writeToFile(String filename) throws IOException {
        FileWriter fw = new FileWriter(filename);
        fw.write(output);
        fw.close();
    }
}
