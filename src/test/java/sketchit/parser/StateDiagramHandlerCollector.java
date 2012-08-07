package sketchit.parser;

import sketchit.domain.Id;
import sketchit.domain.klazz.Element;
import sketchit.domain.klazz.Relationship;
import sketchit.domain.state.State;
import sketchit.domain.state.Transition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class StateDiagramHandlerCollector implements StateDiagramParser.Handler {

    private List<State> states = new ArrayList<State>();
    private List<Transition> transitions = new ArrayList<Transition>();
    private int iState;
    private int iTransition;

    @Override
    public Id emit(State state) {
        if(state==null)
            throw new IllegalArgumentException();
        for(State st : states) {
            if(st.isSameElementAs(state))
                return st.getId();
        }
        states.add(state);
        Id id = new Id(iState++);
        state.usingId(id);
        return id;
    }

    @Override
    public Id emit(Transition transition) {
        if(transition==null)
            throw new IllegalArgumentException();
        transitions.add(transition);
        Id id = new Id(iTransition++);
        transition.usingId(id);
        return id;
    }

    public List<State> getStates() {
        return states;
    }

    public List<Transition> getTransitions() {
        return transitions;
    }
}
