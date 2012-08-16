package sketchit.yuml;

import sketchit.domain.klazz.Element;
import sketchit.domain.Id;
import sketchit.domain.klazz.Relationship;
import sketchit.domain.klazz.Repository;

import java.util.Map;

/**
 *
 *
 */
public class RepositoryYumlParserHandlerAdapter implements YumlParser.Handler {

    private final Repository repository;

    public RepositoryYumlParserHandlerAdapter(Repository repository) {
        this.repository = repository;
    }

    @Override
    public Id emit(Element<?> element) {
        return repository.addOrComplete(element);
    }

    @Override
    public Id emit(Relationship relationship) {
        return repository.add(relationship);
    }

    @Override
    public void emit(Map<String, String> meta) {
        repository.defineMeta(meta);
    }

}
