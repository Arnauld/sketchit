package sketchit.domain.klazz;

import sketchit.domain.Id;
import sketchit.domain.Styles;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Relationship {

    private final EndPoint leftEndPoint;
    private final EndPoint rightEndPoint;
    private Map<String, String> styles;
    private Id id;
    private Styles.LineStyle lineStyle = Styles.LineStyle.Solid;

    public Relationship(Id leftId, Id rightId) {
        this.leftEndPoint = new EndPoint(leftId);
        this.rightEndPoint = new EndPoint(rightId);
    }

    @SuppressWarnings("unchecked")
    public Relationship usingId(Id id) {
        if (this.id != null) {
            throw new IllegalStateException("Id already assigned!");
        }
        this.id = id;
        return this;
    }

    public Relationship usingLineStyle(Styles.LineStyle lineStyle) {
        this.lineStyle = lineStyle;
        return this;
    }

    @SuppressWarnings("unchecked")
    public Relationship usingStyles(Map<String, String> styles) {
        getStyles().putAll(styles);
        return this;
    }

    public EndPoint leftEndPoint() {
        return leftEndPoint;
    }

    public EndPoint rightEndPoint() {
        return rightEndPoint;
    }

    public Styles.LineStyle getLineStyle() {
        return lineStyle;
    }

    public Map<String, String> getStyles() {
        if (styles == null) {
            styles = new HashMap<String, String>();
        }
        return styles;
    }

    public String getStyle(String key) {
        if(styles!=null)
            return styles.get(key);
        return null;
    }

    public boolean hasStyle(String key) {
        return styles!=null && styles.containsKey(key);
    }

    public static class EndPoint {
        private final Id elementId;
        private String label;
        private Styles.Decoration decoration = Styles.Decoration.None;

        public EndPoint(Id elementId) {
            this.elementId = elementId;
        }

        public Id getElementId() {
            return elementId;
        }

        public String getLabel() {
            return label;
        }

        public Styles.Decoration getDecoration() {
            return decoration;
        }

        public EndPoint usingLabel(String label) {
            this.label = label;
            return this;
        }

        public EndPoint usingDecoration(Styles.Decoration decoration) {
            this.decoration = decoration;
            return this;
        }
    }
}
