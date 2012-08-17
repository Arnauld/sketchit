package sketchit.yuml;

import sketchit.domain.klazz.Element;
import sketchit.domain.Id;
import sketchit.domain.klazz.Relationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class YumlParserListenerCollector implements YumlParser.Handler {

    private Map<String, String> meta = new HashMap<String, String>();
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

    @Override
    public void emit(Map<String, String> meta) {
        this.meta.putAll(meta);
    }

    public List<Element> getElements() {
        return elements;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }

    public Map<String, String> getMeta() {
        return meta;
    }
}
