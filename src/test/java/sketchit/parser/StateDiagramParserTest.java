package sketchit.parser;

import static org.fest.assertions.api.Assertions.assertThat;
import static sketchit.domain.Styles.Decoration.Arrow;
import static sketchit.domain.Styles.Decoration.None;
import static sketchit.domain.Styles.LineStyle.Dashed;
import static sketchit.domain.Styles.LineStyle.Dotted;
import static sketchit.domain.Styles.LineStyle.Solid;

import sketchit.domain.Id;
import sketchit.domain.state.State;
import sketchit.domain.state.Transition;

import org.junit.Before;
import org.junit.Test;
import java.util.List;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class StateDiagramParserTest {

    private static final Id ID_1 = new Id(17);
    private static final Id ID_2 = new Id(11);

    private StateDiagramParser parser;

    @Before
    public void setUp() {
        parser = new StateDiagramParser();
    }

    @Test
    public void parse_start() {
        State state = parser.parseState("()");
        assertThat(state).isNotNull();
        assertThat(state.isStart()).isTrue();
        assertThat(state.isEnd()).isFalse();
    }

    @Test
    public void parse_end() {
        State state = parser.parseState("(X)");
        assertThat(state).isNotNull();
        assertThat(state.isStart()).isFalse();
        assertThat(state.isEnd()).isTrue();
    }

    @Test
    public void parse_state_nameOnly() {
        State state = parser.parseState("Open");
        assertThat(state).isNotNull();
        assertThat(state.isStart()).isFalse();
        assertThat(state.isEnd()).isFalse();
        assertThat(state.getName()).isEqualTo("Open");
        assertThat(state.getDescription()).isNullOrEmpty();
        assertThat(state.isDoubled()).isFalse();
    }

    @Test
    public void parse_state_nameOnly_doubled() {
        State state = parser.parseState("(Open)");
        assertThat(state).isNotNull();
        assertThat(state.isStart()).isFalse();
        assertThat(state.isEnd()).isFalse();
        assertThat(state.getName()).isEqualTo("Open");
        assertThat(state.getDescription()).isNullOrEmpty();
        assertThat(state.isDoubled()).isTrue();
    }

    @Test
    public void parse_state_nameAndDescriptionOnly() {
        State state = parser.parseState("Open|The door is open");
        assertThat(state).isNotNull();
        assertThat(state.isStart()).isFalse();
        assertThat(state.isEnd()).isFalse();
        assertThat(state.getName()).isEqualTo("Open");
        assertThat(state.getDescription()).isEqualTo("The door is open");
    }

    @Test
    public void parse_state_nameOnly_with_background() {
        State state = parser.parseState("Open{bg:green}");
        assertThat(state).isNotNull();
        assertThat(state.isStart()).isFalse();
        assertThat(state.isEnd()).isFalse();
        assertThat(state.getName()).isEqualTo("Open");
        assertThat(state.getBackground()).isEqualTo("green");
    }

    @Test
    public void parseTransition_basic() {
        Transition relationship = parser.parseTransition(ID_1, ID_2, "-");
        assertThat(relationship).isNotNull();

        Transition.EndPoint leftEndPoint = relationship.leftEndPoint();
        Transition.EndPoint righttEndPoint = relationship.rightEndPoint();

        assertThat(leftEndPoint.getLabel()).isNull();
        assertThat(righttEndPoint.getLabel()).isNull();
        assertThat(relationship.getLineStyle()).isEqualTo(Solid);
        assertThat(leftEndPoint.getDecoration()).isEqualTo(None);
        assertThat(righttEndPoint.getDecoration()).isEqualTo(None);
    }

    @Test
    public void parseTransition_basicDashed() {
        Transition relationship = parser.parseTransition(ID_1, ID_2, "-.-");
        assertThat(relationship).isNotNull();

        Transition.EndPoint leftEndPoint = relationship.leftEndPoint();
        Transition.EndPoint rightEndPoint = relationship.rightEndPoint();

        assertThat(leftEndPoint.getLabel()).isNullOrEmpty();
        assertThat(rightEndPoint.getLabel()).isNullOrEmpty();
        assertThat(relationship.getLineStyle()).isEqualTo(Dashed);
        assertThat(leftEndPoint.getDecoration()).isEqualTo(None);
        assertThat(rightEndPoint.getDecoration()).isEqualTo(None);
    }

    @Test
    public void parseTransition_basicDotted() {
        Transition relationship = parser.parseTransition(ID_1, ID_2, "...");
        assertThat(relationship).isNotNull();

        Transition.EndPoint leftEndPoint = relationship.leftEndPoint();
        Transition.EndPoint rightEndPoint = relationship.rightEndPoint();

        assertThat(leftEndPoint.getLabel()).isNullOrEmpty();
        assertThat(rightEndPoint.getLabel()).isNullOrEmpty();
        assertThat(relationship.getLineStyle()).isEqualTo(Dotted);
        assertThat(leftEndPoint.getDecoration()).isEqualTo(None);
        assertThat(rightEndPoint.getDecoration()).isEqualTo(None);
    }

    @Test
    public void parseTransition_arrow() {
        Transition relationship = parser.parseTransition(ID_1, ID_2, "...>");
        assertThat(relationship).isNotNull();

        Transition.EndPoint leftEndPoint = relationship.leftEndPoint();
        Transition.EndPoint rightEndPoint = relationship.rightEndPoint();

        assertThat(leftEndPoint.getLabel()).isNullOrEmpty();
        assertThat(rightEndPoint.getLabel()).isNullOrEmpty();
        assertThat(relationship.getLineStyle()).isEqualTo(Dotted);
        assertThat(leftEndPoint.getDecoration()).isEqualTo(None);
        assertThat(rightEndPoint.getDecoration()).isEqualTo(Arrow);
    }

    @Test
    public void parseExpression_() {
        StateDiagramHandlerCollector handler = new StateDiagramHandlerCollector();
        parser.parseExpression("(())-->(Open)-->(Close)-->(Open)", handler);

        Id id0 = new Id(0);
        Id id1 = new Id(1);
        Id id2 = new Id(2);

        List<State> states = handler.getStates();
        assertThat(states.size()).isGreaterThan(0);
        assertThat(states.get(0).getName()).isEqualTo("");
        assertThat(states.get(0).isStart()).isTrue();
        assertThat(states.get(0).getId()).isEqualTo(id0);
        assertThat(states.size()).isGreaterThan(1);
        assertThat(states.get(1).getName()).isEqualTo("Open");
        assertThat(states.get(1).getId()).isEqualTo(id1);
        assertThat(states.size()).isGreaterThan(2);
        assertThat(states.get(2).getName()).isEqualTo("Close");
        assertThat(states.get(2).getId()).isEqualTo(id2);

        List<Transition> transitions = handler.getTransitions();
        assertThat(transitions).hasSize(3);
        assertThat(transitions.get(0).leftEndPoint().getElementId()).isEqualTo(id0);
        assertThat(transitions.get(0).rightEndPoint().getElementId()).isEqualTo(id1);
        assertThat(transitions.get(1).leftEndPoint().getElementId()).isEqualTo(id1);
        assertThat(transitions.get(1).rightEndPoint().getElementId()).isEqualTo(id2);
        assertThat(transitions.get(2).leftEndPoint().getElementId()).isEqualTo(id2);
        assertThat(transitions.get(2).rightEndPoint().getElementId()).isEqualTo(id1);

    }
}
