package sketchit.yuml;

import static sketchit.domain.klazz.Stereotypes.decryptStereotypeDelimiters;
import static sketchit.domain.klazz.Stereotypes.encryptStereotypeDelimiters;

import sketchit.domain.Styles;
import sketchit.domain.klazz.ClassElement;
import sketchit.domain.klazz.Element;
import sketchit.domain.Id;
import sketchit.domain.klazz.NoteElement;
import sketchit.domain.klazz.Relationship;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class YumlParser {

    public interface Handler {
        Id emit(Element<?> element);
        Id emit(Relationship relationship);
        void emit(Map<String, String> meta);
    }

    public static final Pattern EXPR_META = Pattern.compile("\\s*@\\{([^\\}]+)\\}\\s*");
    public static final Pattern EXPR_LEFTSIDE = Pattern.compile("\\[([^\\]]+)\\]" // left side element
            + "([^\\[]+)?" // relationship
    );

    public void parseExpressions(String expressions, Handler handler) {
        for(String expression : expressions.split("[\n\r]+")) {
            parseExpression(expression, handler);
        }
    }

    public void parseExpression(CharSequence expression, Handler handler) {
        Matcher matcherMeta = EXPR_META.matcher(expression);
        if(matcherMeta.find()) {
            Map<String,String> meta = new HashMap<String, String>();
            String inlined = matcherMeta.group(1);
            parseStyles(meta, inlined);
            handler.emit(meta);
            return;
        }

        Id leftId = null;
        String relationExpr = null;

        Matcher matcher = EXPR_LEFTSIDE.matcher(expression);
        while(matcher.find()) {
            Element element = parseElement(matcher.group(1));
            Id rightId = handler.emit(element);

            if(relationExpr!=null) {
                handler.emit(parseRelation(leftId, rightId, relationExpr));
            }
            relationExpr = matcher.group(2);
            leftId = rightId;
        }
    }

    public static final Pattern META = Pattern.compile("(\\{([^\\}]+)\\})");
    public static final Pattern PARTS = Pattern.compile("\\|");
    public static final Pattern VALUES = Pattern.compile(";");
    public static final Pattern NOTE = Pattern.compile("^[\\s]*note:(.*)");

    public Element parseElement(CharSequence content) {
        Map<String,String> styles = new HashMap<String, String>();
        Matcher matcher = META.matcher(content);
        if(matcher.find()) {
            parseStyles(styles, matcher.group(2));
            content = matcher.replaceFirst("");
        }

        Matcher noteMatcher = NOTE.matcher(content);
        if(noteMatcher.matches()) {
            return new NoteElement(noteMatcher.group(1).trim()).usingStyles(styles);
        }
        else {
            String[] parts = PARTS.split(content);
            List<String> attributes = splitValuesOrEmpty(parts, 1);
            List<String> methods = splitValuesOrEmpty(parts, 2);
            return new ClassElement(parts[0], attributes, methods).usingStyles(styles);
        }
    }

    public Relationship parseRelation(Id leftId, Id rightId, String relationExpr) {
        Relationship relationship = new Relationship(leftId, rightId);

        String[] leftAndRight;
        if(relationExpr.contains("-.-")) {
            relationship.usingLineStyle(Styles.LineStyle.Dashed);
            leftAndRight = relationExpr.split("[\\-]+(\\.[\\-]+)+");
        }
        else if(relationExpr.contains("...")) {
            relationship.usingLineStyle(Styles.LineStyle.Dotted);
            leftAndRight = relationExpr.split("[\\.]{3,}");
        }
        else if(relationExpr.contains("===")) {
            relationship.usingLineStyle(Styles.LineStyle.Bold);
            leftAndRight = relationExpr.split("[=]{3,}");
        }
        else {
            relationship.usingLineStyle(Styles.LineStyle.Solid);
            leftAndRight = relationExpr.split("[\\-]+");
        }

        if(leftAndRight.length>1) {
            parseRelationEndPoint(relationship.leftEndPoint(), leftAndRight[0]);
            parseRelationEndPoint(relationship.rightEndPoint(), leftAndRight[1]);
        }
        else if(leftAndRight.length>0) {
            parseRelationEndPoint(relationship.leftEndPoint(), leftAndRight[0]);
        }

        return relationship;
    }

    private static void parseRelationEndPoint(Relationship.EndPoint endPoint, String expr) {

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

    private static  void parseStyles(Map<String, String> styles, String inlinedStyles) {
        String[] keyValues = inlinedStyles.split("[,;]");
        for(String keyValue : keyValues) {
            String[] kv = keyValue.split(":");
            if(kv.length>1)
               styles.put(kv[0].trim(), kv[1].trim());
            else
                styles.put(keyValue.trim(), "");
        }
    }

    private static List<String> splitValuesOrEmpty(String[] strings, int index) {
        if(index<strings.length) {
            String[] values = VALUES.split(strings[index]);
            return Arrays.asList(values);
        }
        return Collections.emptyList();
    }
}
