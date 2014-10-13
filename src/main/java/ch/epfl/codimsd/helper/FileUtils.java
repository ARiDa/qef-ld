package ch.epfl.codimsd.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class FileUtils {
	
    public static String readFile(String fileName) throws IOException {
        FileInputStream stream = new FileInputStream(new File(fileName));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return Charset.defaultCharset().decode(bb).toString();
        }
        finally {
            stream.close();
        }
    }

    public static void writeFile(String fileName, String content) throws IOException {
    	writeFile(fileName, content.getBytes());
    }    
    
    public static void writeFile(String fileName, byte[] bytes) throws IOException {
        OutputStream out = new FileOutputStream(new File(fileName));
        out.write(bytes);
        out.close();
    }
    
}
