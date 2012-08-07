package sketchit.dot;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.text.WordUtils.wrap;

import sketchit.domain.Styles;
import sketchit.domain.klazz.ClassElement;
import sketchit.domain.klazz.Element;
import sketchit.domain.klazz.NoteElement;
import sketchit.domain.klazz.Relationship;
import sketchit.domain.klazz.Repository;
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

    // ---

    private String direction = "TD";
    private String fontName = "jd";
    private int edgeLabelDistance = 2;
    private int edgeFontSize = 8;
    private int nodeFontSize = 12;
    private int nodeStereotypeFontSize = 10;
    private int noteFontSize = 10;
    private int noteWrapLength = 30;

    public ClassDiagramGenerator(Repository repository) {
        this.repository = repository;
    }

    /**
     * @param rankDir such as "TD", "LR", ...
     * @return this
     */
    public ClassDiagramGenerator usingRankDir(String rankDir) {
        this.direction = rankDir;
        return this;
    }

    public ClassDiagramGenerator usingFontName(String fontName) {
        this.fontName = fontName;
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
        out.println("    rankdir = " + direction);
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
        out.println("       labeldistance = " + edgeLabelDistance);
        out.println("            fontsize = " + edgeFontSize);
        out.println("            fontname = \""+ fontName + "\"");
        out.println("    ]");
        out.println("    N" + id2Int(relationship.leftEndPoint()) + " -> N" + id2Int(relationship.rightEndPoint()));
    }

    private static String formatLabel(String label) {
        return (label == null ) ? "" : label;
    }

    private static int id2Int(Relationship.EndPoint endPoint) {
        return endPoint.getElementId().asInt();
    }

    private String decoration2Dot(Styles.Decoration decoration) {
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

    private String lineStyle2Dot(Styles.LineStyle lineStyle) {
        switch (lineStyle) {
            case Dashed:
                return "dashed";
            case Dotted:
                return "dotted";
            case Bold:
                return "bold";
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
        out.println("        fontsize = " + nodeFontSize);
        out.println("          margin = 0.20,0.05");
        out.println("    ]");
    }

    private void generateNodeDefForNote(PrintStream out) {
        out.println("    node [");
        out.println("           shape = \"note\"");
        out.println("          height = 0.50");
        out.println("        fontsize = " + noteFontSize);
        out.println("          margin = 0.20,0.05");
        out.println("      constraint = false");
        out.println("    ]");
    }

    //-------------------------------------------------------------------------

    private void generateClassElement(PrintStream out, ClassElement element) {
        out.println("    N" + element.getId().asInt() + " [");
        out.println("            label = \"" + formatClassElementLabel(element) + "\"");
        out.println("            style = \"filled\"");
        out.println("        fillcolor = \"" + defaultString(element.getBackground(), "lightgrey") + "\"");
        out.println("         fontname = \"" + fontName + "\"");
        out.println("    ]");
    }

    private String formatClassElementLabel(ClassElement element) {
        StringBuilder formatted = new StringBuilder();
        if(direction.equals("TD") || direction.equals("DT")) {
            formatted.append('{');
        }

        List<String> stereotypes = element.getStereotypes();
        if(!stereotypes.isEmpty()) {
            // TODO investigate HTML label...
            //formatted.append("<FONT POINT-SIZE=\"").append(nodeStereotypeFontSize).append("\">");
            //formatted.append("<I>");
            for(String stereotype : stereotypes) {
                formatted.append(ensureTextIsValid(stereotype)).append("\\n");
            }
            //formatted.append("</I>");
            //formatted.append("</FONT>");
        }
        formatted.append(ensureTextIsValid(element.getClassName()));

        formatClassFeatures(formatted, element.getAttributes());
        formatClassFeatures(formatted, element.getMethods());

        if(direction.equals("TD") || direction.equals("DT")) {
            formatted.append('}');
        }

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
                formatted.append(ensureTextIsValid(feature));
            }
        }
    }

    private void generateNoteElement(PrintStream out, NoteElement element) {
        out.println("    N" + element.getId().asInt() + " [");
        out.println("              label = \"" + formatNoteElementLabel(element) + "\"");
        out.println("              style = \"filled\"");
        out.println("          fillcolor = \"" + defaultString(element.getBackground(), "yellow") + "\"");
        out.println("           fontname = \"" + fontName + "\"");
        out.println("    ]");
    }

    private String formatNoteElementLabel(NoteElement element) {
        String noteText = ensureTextIsValid(element.getText().toString());
        return wrap(noteText, noteWrapLength, "\\n", false);
    }

    private static String ensureTextIsValid(String text) {
        return text.replaceAll("([<>])", "\\\\$1");
    }

}