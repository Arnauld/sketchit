package sketchit.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public class IO {

    private void copyAndClose(InputStream inputStream, FileOutputStream outputStream) throws IOException {
        byte[] buffer = new byte[2048];
        int read;
        try {
            while((read=inputStream.read(buffer))>0) {
                outputStream.write(buffer, 0, read);
            }
        }
        finally {
            closeQuietly(inputStream);
            closeQuietly(outputStream);
        }
    }

    private void closeQuietly(InputStream inputStream) {
        try {
            inputStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void closeQuietly(OutputStream outputStream) {
        try {
            outputStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
