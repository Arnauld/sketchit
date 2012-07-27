package sketchit.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 *
 */
public interface StreamWriter {
    void writeTo(OutputStream outputStream) throws IOException;
}
