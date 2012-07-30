package sketchit.dot;

import sketchit.domain.ClassElement;
import sketchit.domain.Element;
import sketchit.domain.NoteElement;
import sketchit.domain.Relationship;
import sketchit.domain.Repository;
import sketchit.util.StreamWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

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

    private void generateHeader(PrintStream out) {
        out.println("digraph G {");
        out.println("    ranksep = 1");
        out.println("    rankdir = " + rankDir);
    }

    private void generateFooter(PrintStream out) {
        out.println("}");
    }

    private void generateElements(PrintStream out) {
        for (Element element : repository.elements()) {
            if (element instanceof ClassElement) {
                generateNodeDefForClass(out);
                generateClassElement(out, (ClassElement) element);
            }
            else if (element instanceof NoteElement) {
                generateNodeDefForNote(out);
                generateNoteElement(out, (NoteElement) element);
            }
        }

        for (Relationship relationship : repository.relations()) {
            generateEdge(out, relationship);
        }
    }

    //-------------------------------------------------------------------------

    private void generateEdge(PrintStream out, Relationship relationship) {
        out.println("    edge [");
        out.println("               shape = \"edge\"");
        out.println("                 dir = \"both\"");
        out.println("               style = \"" + lineStyle2Dot(relationship.getLineStyle()) + "\"");
        out.println("           arrowtail = \"" + decoration2Dot(relationship.leftEndPoint().getDecoration()) + "\"");
        out.println("           arrowhead = \"" + decoration2Dot(relationship.rightEndPoint().getDecoration()) + "\"");
        out.println("           taillabel = \"" + formatLabel(relationship.leftEndPoint().getLabel()) + "\"");
        out.println("           headlabel = \"" + formatLabel(relationship.rightEndPoint().getLabel()) + "\"");
        out.println("       labeldistance = 2");
        out.println("            fontsize = 10");
        out.println("            fontname = \"j.d.\"");
        out.println("    ]");
        out.println("    N" + id2Int(relationship.leftEndPoint()) + " -> N" + id2Int(relationship.rightEndPoint()));
    }

    private static String formatLabel(String label) {
        return (label == null ) ? "" : label;
    }

    private static int id2Int(Relationship.EndPoint endPoint) {
        return endPoint.getElementId().asInt();
    }

    private String decoration2Dot(Relationship.Decoration decoration) {
        switch (decoration) {
            case Arrow:
                return "vee";
            case Aggregation:
                return "odiamond";
            case Composition:
                return "diamond";
            case Inheritance:
                return "onormal";
            case None:
            default:
                return "none";
        }
    }

    private String lineStyle2Dot(Relationship.LineStyle lineStyle) {
        switch (lineStyle) {
            case Dashed:
                return "dashed";
            case Solid:
            default:
                return "solid";
        }
    }

    //-------------------------------------------------------------------------

    private void generateNodeDefForClass(PrintStream out) {
        out.println("    node [");
        out.println("           shape = \"record\"");
        out.println("          height = 0.50");
        out.println("        fontsize = 10");
        out.println("          margin = 0.20,0.05");
        out.println("    ]");
    }

    private void generateNodeDefForNote(PrintStream out) {
        out.println("    node [");
        out.println("           shape = \"note\"");
        out.println("          height = 0.50");
        out.println("        fontsize = 10");
        out.println("          margin = 0.20,0.05");
        out.println("    ]");
    }

    //-------------------------------------------------------------------------

    private void generateClassElement(PrintStream out, ClassElement element) {
        out.println("    N" + element.getId().asInt() + " [");
        out.println("            label = \"" + formatClassElementLabel(element) + "\"");
        out.println("            style = \"filled\"");
        out.println("        fillcolor = \"yellow\"");
        out.println("         fontname = \"j.d.\"");
        out.println("    ]");
    }

    private String formatClassElementLabel(ClassElement element) {
        StringBuilder formatted = new StringBuilder();
        // TODO
        // element.getStereotypes()
        formatted.append(element.getClassName());

        formatClassFeatures(formatted, element.getAttributes());
        formatClassFeatures(formatted, element.getMethods());

        return formatted.toString();
    }

    private void formatClassFeatures(StringBuilder formatted, List<String> features) {
        if (!features.isEmpty()) {
            formatted.append("\\n|");
            boolean first = true;
            for (String feature : features) {
                if (first) {
                    first = false;
                }
                else {
                    formatted.append("\\n");
                }
                formatted.append(feature);
            }
        }
    }

    private void generateNoteElement(PrintStream out, NoteElement element) {
        out.println("    N" + element.getId().asInt() + " [");
        out.println("              label = \"" + formatNoteElementLabel(element) + "\"");
        out.println("              style = \"filled\"");
        out.println("          fillcolor = \"yellow\"");
        out.println("           fontname = \"j.d.\"");
        out.println("    ]");
    }

    private String formatNoteElementLabel(NoteElement element) {
        return element.getText().toString();
    }

}