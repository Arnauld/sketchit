package sketchit.dot;

import sketchit.domain.ClassElement;
import sketchit.domain.Element;
import sketchit.domain.NoteElement;
import sketchit.domain.Repository;
import sketchit.util.StreamWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 *
 */
public class ClassDiagramGenerator implements StreamWriter {

    private final Repository repository;
    private String rankDir = "LR";
    public ClassDiagramGenerator(Repository repository) {
        this.repository = repository;
    }

    /**
     * @param rankDir such as "TD", "LR", ...
     * @return this
     */
    public ClassDiagramGenerator usingRankDir(String rankDir) {
        this.rankDir = rankDir;
        return this;
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        PrintStream printStream = new PrintStream(outputStream, true, "utf-8");
        generate(printStream);
    }

    public void generate(PrintStream out) {
        generateHeader(out);
        generateElements(out);
        generateFooter(out);
    }

    private void generateElements(PrintStream out) {
        for(Element element : repository.elements()) {
            if(element instanceof ClassElement) {
                generateClassElement(out, (ClassElement) element);
            }
            else if(element instanceof NoteElement) {
                generateNoteElement(out, (NoteElement) element);
            }
        }
    }

    private void generateClassElement(PrintStream out, ClassElement element) {
        out.println("    node [");
        out.println("        shape = \"record\"");
        out.println("        height = 0.50");
        out.println("        fontsize = 10");
        out.println("        margin = 0.20,0.05");
        out.println("    ]");
        out.println("    C" + element.getId().asInt() + " [");
        out.println("        label = \"" + formatClassElementLabel(element) + "\"");
        out.println("        style = \"filled\"");
        out.println("        fillcolor = \"yellow\"");
        out.println("    ]");
    }

    private String formatClassElementLabel(ClassElement element) {
        return element.getClassName();
    }

    private void generateNoteElement(PrintStream out, NoteElement element) {
        //To change body of created methods use File | Settings | File Templates.
    }

    private void generateHeader(PrintStream out) {
        out.println("digraph G {");
        out.println("    ranksep = 1");
        out.println("    rankdir = " + rankDir);
    }

    private void generateFooter(PrintStream out) {
        out.println("}");
    }
}