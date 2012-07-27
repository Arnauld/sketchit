package sketchit.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 *
 */
public class OutputStreamTee extends OutputStream {

    private OutputStream leftStream;
    private OutputStream rightStream;

    @Override
    public void write(int b) throws IOException {
        leftStream.write(b);
        rightStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        leftStream.write(b);
        rightStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        leftStream.write(b, off, len);
        rightStream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        leftStream.flush();
        rightStream.flush();
    }

    @Override
    public void close() throws IOException {
        leftStream.close();
        rightStream.close();
    }
}
