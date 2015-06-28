package sketchit.dot;

import static org.apache.commons.lang3.StringUtils.defaultString;

import sketchit.domain.Styles;
import sketchit.domain.state.Repository;
import sketchit.domain.state.State;
import sketchit.domain.state.Transition;
import sketchit.util.StreamWriter;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.StringUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class StateDiagramGenerator implements StreamWriter {

    private final Repository repository;

    private String direction = "TD";
    private String fontName = "jd";
    private int edgeLabelDistance = 2;
    private int edgeFontSize = 8;


    public StateDiagramGenerator(Repository repository) {
        this.repository = repository;
    }

    /**
     * @param rankDir such as "TD", "LR", ...
     * @return this
     */
    public StateDiagramGenerator usingRankDir(String rankDir) {
        this.direction = rankDir;
        return this;
    }

    public StateDiagramGenerator usingFontName(String fontName) {
        this.fontName = fontName;
        return this;
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        ByteArrayOutputStream branch = new ByteArrayOutputStream();
        TeeOutputStream tee = new TeeOutputStream(outputStream, branch);
        PrintStream printStream = new PrintStream(tee, true, "utf-8");
        generate(printStream);

        System.out.println(branch.toString("utf-8"));
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
        for (State state : repository.states()) {
            if(state.isStart()) {
                generateStartState(out, state);
            }
            else if(state.isEnd()) {
                generateEndState(out, state);
            }
            else {
                generateState(out, state);
            }
        }

        for (Transition transition : repository.transitions()) {
            generateEdge(out, transition);
        }
    }

    private void generateEdge(PrintStream out, Transition transition) {
        out.println("    edge [");
        out.println("               shape = \"edge\"");
        out.println("                 dir = \"both\"");
        out.println("               style = \"" + lineStyle2Dot(transition.getLineStyle()) + "\"");
        out.println("           arrowtail = \"" + decoration2Dot(transition.leftEndPoint().getDecoration()) + "\"");
        out.println("           arrowhead = \"" + decoration2Dot(transition.rightEndPoint().getDecoration()) + "\"");
        out.println("           taillabel = \"" + formatLabel(transition.leftEndPoint().getLabel()) + "\"");
        out.println("           headlabel = \"" + formatLabel(transition.rightEndPoint().getLabel()) + "\"");
        out.println("       labeldistance = " + edgeLabelDistance);
        out.println("            fontsize = " + edgeFontSize);
        out.println("            fontname = \""+ fontName + "\"");
        out.println("    ];");
        out.println("    S" + id2Int(transition.leftEndPoint()) + " -> S" + id2Int(transition.rightEndPoint()));
    }

    private static String formatLabel(String label) {
        return (label == null ) ? "" : label;
    }

    private void generateState(PrintStream out, State state) {
        out.println("    node [");
        out.println("           shape = \"circle\"");
        out.println("          height = 0.50");
        out.println("          margin = \"0.20,0.05\"");
        out.println("           style = filled");
        out.println("       fillcolor = black");
        out.println("        fontname = \"" + fontName + "\"");
        out.println("    ] " + "S" + id2Int(state));

        out.println("    S" + id2Int(state) + " [");
        out.println("            label = \"" + formatStateLabel(state) + "\"");
        out.println("            style = \"filled\"");
        out.println("        fillcolor = \"" + defaultString(state.getBackground(), "lightgrey") + "\"");
        out.println("         fontname = \"" + fontName + "\"");
        out.println("    ];");
    }

    private void generateStartState(PrintStream out, State state) {
        out.println("    node [");
        out.println("           shape = \"circle\"");
        out.println("          height = 0.50");
        out.println("          margin = \"0.20,0.05\"");
        out.println("           style = filled");
        out.println("       fillcolor = black");
        out.println("    ] " + "S" + id2Int(state));
    }

    private void generateEndState(PrintStream out, State state) {
        out.println("    node [");
        out.println("           shape = \"doublecircle\"");
        out.println("          height = 0.50");
        out.println("          margin = \"0.20,0.05\"");
        out.println("           style = filled");
        out.println("       fillcolor = black");
        out.println("    ] " + "S" + id2Int(state));
    }

    private String formatStateLabel(State state) {
        StringBuilder formatted = new StringBuilder();
        if(direction.equals("TD") || direction.equals("DT")) {
            formatted.append('{');
        }

        formatted.append(ensureTextIsValid(state.getName()));
        String description = state.getDescription();
        if (StringUtils.isNotBlank(description)) {
            formatted.append("\\n|");
            formatted.append(ensureTextIsValid(description));
        }

        if(direction.equals("TD") || direction.equals("DT")) {
            formatted.append('}');
        }
        return formatted.toString();
    }

    private static String decoration2Dot(Styles.Decoration decoration) {
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

    private static String lineStyle2Dot(Styles.LineStyle lineStyle) {
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

    private static int id2Int(Transition.EndPoint endPoint) {
        return endPoint.getElementId().asInt();
    }
    private static int id2Int(State state) {
        return state.getId().asInt();
    }

    private static String ensureTextIsValid(String text) {
        return text.replaceAll("([<>])", "\\\\$1");
    }
}
