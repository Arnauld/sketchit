package sketchit.dot;

import static java.util.Arrays.asList;

import sketchit.domain.ClassElement;
import sketchit.domain.Repository;
import sketchit.util.ProcessPipeline;
import sketchit.util.StreamCopyRunnable;
import sketchit.util.StreamWriter;

import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 *
 */
public class ClassDiagramGeneratorTest {

    private ClassDiagramGenerator generator;
    private Repository repository;
    private ClassElement customerClassElement;

    @Before
    public void setUp () {
        repository = new Repository();
        customerClassElement = new ClassElement("Customer", asList("Firstname", "Lastname"), asList("copy()"));
    }

    @Test
    public void generate_oneClass() throws IOException, FileNotFoundException, InterruptedException {
        repository.addOrComplete(customerClassElement);
        generator = new ClassDiagramGenerator(repository);

        ByteArrayOutputStream dotOut = new ByteArrayOutputStream(1024);

        // svg generation
        new ProcessPipeline().invoke(
                Arrays.asList("/usr/local/bin/dot","-Tsvg"),
                generator,
                dotOut
        );

        System.out.println("ClassDiagramGeneratorTest.generate_oneClass " + dotOut.toString("utf-8"));
    }
}
