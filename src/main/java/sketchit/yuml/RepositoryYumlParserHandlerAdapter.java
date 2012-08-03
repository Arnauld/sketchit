package sketchit.yuml;

import sketchit.domain.Element;
import sketchit.domain.Id;
import sketchit.domain.Relationship;
import sketchit.domain.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
}
