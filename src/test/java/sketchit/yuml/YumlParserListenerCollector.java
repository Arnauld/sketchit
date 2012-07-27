package sketchit.yuml;

import sketchit.domain.Element;
import sketchit.domain.Id;
import sketchit.domain.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 */
public class YumlParserListenerCollector implements YumlParser.Handler {

    private List<Element> elements = new ArrayList<Element>();
    private List<Relationship> relationships = new ArrayList<Relationship>();
    private int i;

    @Override
    public Id emit(Element<?> element) {
        if(element==null)
            throw new IllegalArgumentException();
        elements.add(element);
        return new Id(i++);
    }

    @Override
    public Id emit(Relationship relationship) {
        if(relationship==null)
            throw new IllegalArgumentException();
        relationships.add(relationship);
        return new Id(i++);
    }

    public List<Element> getElements() {
        return elements;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }
}
