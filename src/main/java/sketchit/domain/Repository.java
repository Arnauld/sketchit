package sketchit.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 *
 */
public class Repository {

    private AtomicInteger idGen = new AtomicInteger();
    private final List<Relationship> relationships = new ArrayList<Relationship>();
    private final List<Element> elements = new ArrayList<Element>();

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

    public Iterable<Element> elements() {
        return elements;
    }
}
