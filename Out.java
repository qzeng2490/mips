import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import basic_entry.BTB_Entry;
import basic_entry.Register_Entry;

public class Out {

    // force Unicode UTF-8 encoding; otherwise it's system dependent
    private static final String CHARSET_NAME = "UTF-8";

    private PrintWriter out;


   /**
     * Create an Out object using a file specified by the given name.
     */
    public Out(String s) {
        try {
            OutputStream os = new FileOutputStream(s);
            OutputStreamWriter osw = new OutputStreamWriter(os, CHARSET_NAME);
            out = new PrintWriter(osw, true);
        }
        catch (IOException e) { e.printStackTrace(); }
    }

   /**
     * Close the output stream.
     */
    public void close() { out.close(); }

    public void print(String s){
    	out.print(s);
    	out.flush();
    	System.out.print(s);
    }

    public void println(String s){
    	out.println(s);
    	out.flush();
    	System.out.println(s);
    }

    public void println(Command c){
    	String s = c.toString();
    	println(s);
    }
    
    public void println(BTB_Entry e){
    	String s = e.toString();
    	println(s);
    }
    
    public void print(Register_Entry c){
    	String s = c.toString();
    	out.print(s);
    }

}
