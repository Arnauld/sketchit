package sketchit.domain.klazz;

import sketchit.domain.Id;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 *
 */
public class Repository {

    private final AtomicInteger idGen = new AtomicInteger();
    private final List<Relationship> relationships = new ArrayList<Relationship>();
    private final List<Element> elements = new ArrayList<Element>();
    private final Map<String, String> meta = new HashMap<String, String>();

    private Id nextId() {
        return new Id(idGen.incrementAndGet());
    }

    public Id addOrComplete(Element<?> element) {
        for (Element actual : elements) {
            if (actual.isSameElementAs(element)) {
                actual.completeWith(element);
                return actual.getId();
            }
        }

        // still there? one doesn't find it, so let's create it
        Id id = nextId();
        elements.add(element.usingId(id));
        return id;
    }

    public Id add(Relationship relationship) {
        Id id = nextId();
        relationships.add(relationship.usingId(id));
        return id;
    }

    public void defineMeta(Map<String, String> meta) {
        this.meta.putAll(meta);
    }

    public boolean hasMeta(String key) {
        return meta.containsKey(key);
    }
    public String meta(String key) {
        return meta.get(key);
    }

    public String meta(String key, String defaultValue) {
        String value = meta(key);
        if(value==null)
            return defaultValue;
        return value;
    }

    public Iterable<Element> elements() {
        return elements;
    }

    public Iterable<Relationship> relations() {
        return relationships;
    }

}
