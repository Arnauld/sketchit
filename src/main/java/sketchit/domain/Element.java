package sketchit.domain;

import sketchit.util.Adaptable;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public abstract class Element<T extends Element<T>> implements Adaptable {
    private Map<String, String> styles;
    private Id id;

    public String getBackground() {
        if (styles == null) {
            return null;
        }
        else {
            return styles.get("bg");
        }
    }

    @SuppressWarnings("unchecked")
    public T usingId(Id id) {
        if (this.id != null) {
            throw new IllegalStateException("Id already assigned!");
        }
        this.id = id;
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T usingStyles(Map<String, String> styles) {
        resetStyles();
        getStyles().putAll(styles);
        return (T) this;
    }

    private void resetStyles() {
        this.styles = null;
    }

    public T completeStylesWith(Element<?> element) {
        Map<String, String> elementStyles = element.getStyles();
        getStyles().putAll(elementStyles);
        return (T)this;
    }

    public abstract boolean isSameElementAs(Element<?> element);

    public abstract T completeWith(Element<?> element);

    public Id getId() {
        return id;
    }

    public Map<String, String> getStyles() {
        if (styles == null) {
            styles = new HashMap<String, String>();
        }
        return styles;
    }
}
