package sketchit.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 *
 */
public class ProcessPipeline {

    /**
     *
     * @param commands
     *          The complete dot invocation and format specifier, such as ["dot","-Tsvg"].
     * @param streamWriter
     *          Callback that will write its content to the process
     * @param output
     *          Stream on which the process will write its output on.
     */
    public void invoke(List<String> commands, StreamWriter streamWriter, OutputStream output)
            throws IOException
    {
        ProcessBuilder pb = new ProcessBuilder(commands);
        Process proc = pb.start();

        ByteArrayOutputStream procError = new ByteArrayOutputStream();

        try {
            Thread t1 = new StreamCopyRunnable(proc.getErrorStream(), procError).asThread();
            Thread t2 = new StreamCopyRunnable(proc.getInputStream(), output).asThread();

            t1.start();
            t2.start();

            // pipe the graph generation output directly
            streamWriter.writeTo(proc.getOutputStream());
            proc.getOutputStream().close();

            int r = proc.waitFor();
            t1.join();
            t2.join();

            System.err.println("E> " + procError.toString());

            if(r!=0)
                throw new IllegalArgumentException("Something got wrong: " + procError.toString());
        }
        catch (IOException e) {
            throw new RuntimeException(procError.toString(), e);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(procError.toString(), e);
        }
    }
}
