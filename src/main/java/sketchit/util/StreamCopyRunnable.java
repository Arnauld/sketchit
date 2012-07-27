package sketchit.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 *
 */
public class StreamCopyRunnable implements Runnable {

    private static final int EOF = -1;
    //
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public StreamCopyRunnable(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        int read;
        try {
            while((read=inputStream.read(buffer)) != EOF) {
                outputStream.write(buffer, 0, read);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Thread asThread() {
        return new Thread(this);
    }
}
