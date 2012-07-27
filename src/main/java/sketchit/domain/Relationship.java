package sketchit.domain;

/**
 *
 */
public class Relationship {

    public enum Decoration {
        None,
        Composition,
        Aggregation,
        Arrow,
        Inheritance
    }
    
    public enum LineStyle {
        Solid,
        Dashed
    }



    private final EndPoint leftEndPoint;
    private final EndPoint rightEndPoint;
    private Id id;
    private LineStyle lineStyle = LineStyle.Solid;

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


    public Relationship usingLineStyle(LineStyle lineStyle) {
        this.lineStyle = lineStyle;
        return this;
    }

    public EndPoint leftEndPoint() {
        return leftEndPoint;
    }

    public EndPoint rightEndPoint() {
        return rightEndPoint;
    }

    public LineStyle getLineStyle() {
        return lineStyle;
    }

    public static class EndPoint {
        private final Id elementId;
        private String label;
        private Decoration decoration = Decoration.None;

        public EndPoint(Id elementId) {
            this.elementId = elementId;
        }

        public Id getElementId() {
            return elementId;
        }

        public String getLabel() {
            return label;
        }

        public Decoration getDecoration() {
            return decoration;
        }

        public EndPoint usingLabel(String label) {
            this.label = label;
            return this;
        }

        public EndPoint usingDecoration(Decoration decoration) {
            this.decoration = decoration;
            return this;
        }
    }
}
