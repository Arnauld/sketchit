package sketchit.domain.state;

import sketchit.domain.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class Repository {

    private AtomicInteger idGen = new AtomicInteger();
    private final List<Transition> transitions = new ArrayList<Transition>();
    private final List<State> states = new ArrayList<State>();

    private Id nextId() {
        return new Id(idGen.incrementAndGet());
    }

    public Id addOrComplete(State element) {
        for (State actual : states) {
            if (actual.isSameElementAs(element)) {
                actual.completeWith(element);
                return actual.getId();
            }
        }

        // still there? one doesn't find it, so let's create it
        Id id = nextId();
        states.add(element.usingId(id));
        return id;
    }

    public Id add(Transition transition) {
        Id id = nextId();
        transitions.add(transition.usingId(id));
        return id;
    }

    public Iterable<State> states() {
        return states;
    }

    public Iterable<Transition> transitions() {
        return transitions;
    }
}
