package sketchit.parser;

import sketchit.domain.Id;
import sketchit.domain.state.Repository;
import sketchit.domain.state.State;
import sketchit.domain.state.Transition;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class RepositoryStateDiagramParserHandlerAdapter implements StateDiagramParser.Handler {

    private final Repository repository;

    public RepositoryStateDiagramParserHandlerAdapter(Repository repository) {
        this.repository = repository;
    }

    @Override
    public Id emit(State state) {
        return repository.addOrComplete(state);
    }

    @Override
    public Id emit(Transition transition) {
        return repository.add(transition);
    }
}
