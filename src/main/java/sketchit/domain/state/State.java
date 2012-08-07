package sketchit.domain.state;

import sketchit.domain.Id;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class State {

    private final String name;
    private Id id;
    private Map<String, String> styles;
    private String description;
    private Boolean doubled;

    public State(String name, String description) {
        this.name = StringUtils.trim(name);
        this.description = StringUtils.trim(description);
    }

    public String getName() {
        return name;
    }

    public State usingId(Id id) {
        if (this.id != null) {
            throw new IllegalStateException("Id already assigned!");
        }
        this.id = id;
        return this;
    }

    public boolean isSameElementAs(State other) {
        return StringUtils.equals(name, other.name);
    }

    public State completeWith(State other) {
        completeStylesWith(other);
        if (other != null) {
            description = StringUtils.defaultString(description, other.description);
            if(other.isDoubled())
                markDoubled();
        }
        return this;
    }

    public State markDoubled() {
        this.doubled = true;
        return this;
    }

    public Id getId() {
        return id;
    }

    public boolean isStart() {
        return isDoubled() && "".equals(name);
    }
    public boolean isEnd() {
        return isDoubled() && "X".equals(name);
    }

    public State usingStyles(Map<String, String> styles) {
        resetStyles();
        getStyles().putAll(styles);
        return this;
    }

    private void resetStyles() {
        this.styles = null;
    }

    public State completeStylesWith(State other) {
        Map<String, String> elementStyles = other.getStyles();
        getStyles().putAll(elementStyles);
        return this;
    }

    public Map<String, String> getStyles() {
        if (styles == null) {
            styles = new HashMap<String, String>();
        }
        return styles;
    }

    public String getDescription() {
        return description;
    }

    public String getBackground() {
        if (styles == null) {
            return null;
        }
        else {
            return styles.get("bg");
        }
    }

    public boolean isDoubled() {
        return BooleanUtils.isTrue(doubled);
    }
}
