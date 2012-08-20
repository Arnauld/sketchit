package sketchit.domain.klazz;

import static sketchit.domain.klazz.Stereotypes.discardStereotypes;
import static sketchit.domain.klazz.Stereotypes.extractStereotypes;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 */
public class ClassElement extends Element<ClassElement> {
    private final String className;
    private final List<String> stereotypes;
    private final List<String> attributes;
    private final List<String> methods;

    public ClassElement(CharSequence nameSignature, List<String> attributes, List<String> methods) {
        this.className = discardStereotypes(nameSignature);
        this.stereotypes = extractStereotypes(nameSignature);
        this.attributes = newArrayList(attributes);
        this.methods = newArrayList(methods);
    }

    private static <T> ArrayList<T> newArrayList(List<T> values) {
        if (values != null) {
            return new ArrayList<T>(values) ;
        }
        else {
            return new ArrayList<T>();
        }
    }

    public CharSequence getNameSignature() {
        String stereotypes = Stereotypes.toSignature(this.stereotypes);
        if(stereotypes!=null && !stereotypes.isEmpty())
            return stereotypes + " " + className;
        return className;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public List<String> getMethods() {
        return methods;
    }

    public String getClassName() {
        return className;
    }

    public List<String> getStereotypes() {
        return stereotypes;
    }

    @Override
    public boolean isSameElementAs(Element<?> element) {
        ClassElement other = element.adaptTo(ClassElement.class);
        if (other == null) {
            return false;
        }
        return getClassName().equals(other.getClassName());
    }

    @Override
    public ClassElement completeWith(Element<?> element) {
        completeStylesWith(element);
        ClassElement other = element.adaptTo(ClassElement.class);
        if (other != null) {
            addMissings(other.getStereotypes(), stereotypes);
            addMissings(other.getAttributes(), attributes);
            addMissings(other.getMethods(), methods);
        }
        return this;
    }

    private static <T> void addMissings(List<T> src, List<T> dst) {
        for (T other : src) {
            if (!dst.contains(other)) {
                dst.add(other);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T adaptTo(Class<T> type) {
        if (type.equals(ClassElement.class)) {
            return (T) this;
        }
        return null;
    }

}
