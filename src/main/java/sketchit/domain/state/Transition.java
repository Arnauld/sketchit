package sketchit.domain.state;

import sketchit.domain.Id;
import sketchit.domain.Styles;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class Transition {

    private final EndPoint leftEndPoint;
    private final EndPoint rightEndPoint;
    private Id id;
    private Styles.LineStyle lineStyle = Styles.LineStyle.Solid;

    public Transition(Id leftId, Id rightId) {
        this.leftEndPoint = new EndPoint(leftId);
        this.rightEndPoint = new EndPoint(rightId);
    }

    @SuppressWarnings("unchecked")
    public Transition usingId(Id id) {
        if (this.id != null) {
            throw new IllegalStateException("Id already assigned!");
        }
        this.id = id;
        return this;
    }


    public Transition usingLineStyle(Styles.LineStyle lineStyle) {
        this.lineStyle = lineStyle;
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
