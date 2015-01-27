import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;



public final class BinaryIn {
    private BufferedInputStream in;      // the input stream
    private static final int EOF = -1;   // end of file
    private int buffer;                  // one character buffer

   /**
     * Create a binary input stream from a filename.
     */
    public BinaryIn(String s) {

        try {
            // first try to read file from local file system
            File file = new File(s);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                in = new BufferedInputStream(fis);
                fillBuffer();
            }
        }
        catch (IOException ioe) {
            System.err.println("Could not open " + s);
        }
    }
    
    private void fillBuffer() {
        try { buffer = in.read();  }
        catch (IOException e) { System.err.println("EOF"); buffer = EOF; }
    }
    
    /**
     * Read the next 8 bits from the binary input stream and return as an 8-bit char.
     * @return the next 8 bits of data from the binary input stream as a <tt>char</tt>
     * @throws RuntimeException if there are fewer than 8 bits available
     */
    
    public char readChar() {
        if (isEmpty()) throw new RuntimeException("Reading from empty input stream");
        int x = buffer;
        fillBuffer();
        return (char) (x & 0xff);
    }
    
   /**
     * Read the next 32 bits from the binary input stream and return as a 32-bit int.
     * @return the next 32 bits of data from the binary input stream as a <tt>int</tt>
 * @throws IOException 
     * @throws RuntimeException if there are fewer than 32 bits available
     */
    public int readInt() {
    	int x = 0;
        for (int i = 0; i < 4; i++) {
            char c = readChar();
            x <<= 8;
            x |= c;
        }
        return x;
    }
    
    public boolean isEmpty(){
    	return buffer == EOF;
    }
}
