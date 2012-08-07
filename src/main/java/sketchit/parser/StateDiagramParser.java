package sketchit.parser;

import static sketchit.domain.klazz.Stereotypes.decryptStereotypeDelimiters;
import static sketchit.domain.klazz.Stereotypes.encryptStereotypeDelimiters;

import sketchit.domain.Id;
import sketchit.domain.Styles;
import sketchit.domain.state.State;
import sketchit.domain.state.Transition;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class StateDiagramParser {
    public interface Handler {
        Id emit(State state);

        Id emit(Transition transition);
    }

    public static final Pattern EXPR_LEFTSIDE = Pattern.compile(
            "\\((?:(\\([^\\)]*\\)|[^\\)]*))\\)" // left side element
            + "([^\\(]+)?" // relationship
    );

    public void parseExpressions(String expressions, Handler handler) {
        for(String expression : expressions.split("[\n\r,]+")) {
            parseExpression(expression, handler);
        }
    }

    public void parseExpression(CharSequence expression, Handler handler) {
        Id leftId = null;
        String relationExpr = null;

        Matcher matcher = EXPR_LEFTSIDE.matcher(expression);
        while(matcher.find()) {
            State state = parseState(matcher.group(1));
            Id rightId = handler.emit(state);

            if(relationExpr!=null) {
                handler.emit(parseTransition(leftId, rightId, relationExpr));
            }
            relationExpr = matcher.group(2);
            leftId = rightId;
        }
    }

    public static final Pattern DOUBLED = Pattern.compile("^\\((.*)\\)$");
    public static final Pattern META = Pattern.compile("(\\{([^\\}]+)\\})");
    public static final Pattern PARTS = Pattern.compile("\\|");


    public State parseState(CharSequence content) {
        boolean isDoubled = false;
        Matcher doubledMatcher = DOUBLED.matcher(content);
        if(doubledMatcher.matches()) {
            isDoubled = true;
            content = doubledMatcher.group(1);
        }

        Map<String, String> styles = new HashMap<String, String>();
        Matcher matcher = META.matcher(content);
        if (matcher.find()) {
            parseStyles(styles, matcher.group(2));
            content = matcher.replaceFirst("");
        }

        String[] parts = PARTS.split(content);
        State state = new State(parts[0], getOrNull(parts, 1));
        if(isDoubled) {
            state.markDoubled();
        }
        return state.usingStyles(styles);
    }

    private static String getOrNull(String[] parts, int index) {
        if (index < parts.length) {
            return parts[index];
        }
        return null;
    }

    private static void parseStyles(Map<String, String> styles, String inlinedStyles) {
        String[] keyValues = inlinedStyles.split(",");
        for (String keyValue : keyValues) {
            String[] kv = keyValue.split(":");
            if (kv.length > 1) {
                styles.put(kv[0].trim(), kv[1].trim());
            }
            else {
                styles.put(keyValue.trim(), "");
            }
        }
    }

    public Transition parseTransition(Id leftId, Id rightId, String transitionExpr) {
        Transition transition = new Transition(leftId, rightId);

        String[] leftAndRight;
        if(transitionExpr.contains("-.-")) {
            transition.usingLineStyle(Styles.LineStyle.Dashed);
            leftAndRight = transitionExpr.split(Pattern.quote("-.-"));
        }
        else if(transitionExpr.contains("...")) {
            transition.usingLineStyle(Styles.LineStyle.Dotted);
            leftAndRight = transitionExpr.split(Pattern.quote("..."));
        }
        else if(transitionExpr.contains("===")) {
            transition.usingLineStyle(Styles.LineStyle.Bold);
            leftAndRight = transitionExpr.split(Pattern.quote("==="));
        }
        else {
            transition.usingLineStyle(Styles.LineStyle.Solid);
            leftAndRight = transitionExpr.split("\\-");
        }

        if(leftAndRight.length>1) {
            parseTransitionEndPoint(transition.leftEndPoint(), leftAndRight[0]);
            parseTransitionEndPoint(transition.rightEndPoint(), leftAndRight[1]);
        }
        else if(leftAndRight.length>0) {
            parseTransitionEndPoint(transition.leftEndPoint(), leftAndRight[0]);
        }

        return transition;
    }

    private static void parseTransitionEndPoint(Transition.EndPoint endPoint, String expr) {

        String label = encryptStereotypeDelimiters(expr);
        Styles.Decoration decoration = Styles.Decoration.None;

        if(expr.contains("<>")) {
            decoration = Styles.Decoration.Aggregation;
            label = label.replace("<>", "");
        }
        else if(expr.contains("++")) {
            decoration = Styles.Decoration.Composition;
            label = label.replace("++", "");
        }
        else if(expr.contains("+")) {
            decoration = Styles.Decoration.Aggregation;
            label = label.replace("+", "");
        }
        else if(expr.contains("<") || expr.contains(">")) {
            decoration = Styles.Decoration.Arrow;
            label = label.replace("<", "").replace(">", "");
        }
        else if(expr.contains("^")) {
            decoration = Styles.Decoration.Inheritance;
            label = label.replace("^", "");
        }

        label = decryptStereotypeDelimiters(label);

        endPoint.usingDecoration(decoration).usingLabel(label.trim());
    }


}
