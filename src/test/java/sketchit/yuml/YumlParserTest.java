package sketchit.yuml;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import sketchit.domain.ClassElement;
import sketchit.domain.Element;
import sketchit.domain.Id;
import sketchit.domain.NoteElement;
import sketchit.domain.Relationship;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import java.util.List;

/**
 *
 *
 */
public class YumlParserTest {

    private static final Id ID_1 = new Id(17);
    private static final Id ID_2 = new Id(11);


    private YumlParser parser;

    @Before
    public void setUp() {
        parser = new YumlParser();
    }

    @Test
    public void parse_class_nameOnly() {
        Element element = parser.parseElement("Customer");
        assertThat(element).isInstanceOf(ClassElement.class);
        ClassElement classElement = (ClassElement) element;
        assertThat(classElement.getNameSignature()).isEqualTo("Customer");
    }

    @Test
    public void parse_class_nameAndAttribute_noMethod() {
        Element element = parser.parseElement("Customer|Forename");
        assertThat(element).isInstanceOf(ClassElement.class);
        ClassElement classElement = (ClassElement) element;
        assertThat(classElement.getNameSignature()).isEqualTo("Customer");
        assertThat(classElement.getAttributes()).containsOnly("Forename");
        assertThat(classElement.getMethods()).isEmpty();
    }

    @Test
    public void parse_class_nameAndAttribute_emptyMethod() {
        Element element = parser.parseElement("Customer|Forename;|");
        assertThat(element).isInstanceOf(ClassElement.class);
        ClassElement classElement = (ClassElement) element;
        assertThat(classElement.getNameSignature()).isEqualTo("Customer");
        assertThat(classElement.getAttributes()).containsOnly("Forename");
        assertThat(classElement.getMethods()).isEmpty();
    }

    @Test
    public void parse_class_nameAttributesAndMethods() {
        Element element = parser.parseElement("Customer|Forename;Surname;Email|Save()");
        assertThat(element).isInstanceOf(ClassElement.class);
        ClassElement classElement = (ClassElement) element;
        assertThat(classElement.getNameSignature()).isEqualTo("Customer");
        assertThat(classElement.getAttributes()).containsOnly("Forename", "Surname", "Email");
        assertThat(classElement.getMethods()).containsOnly("Save()");
    }

    @Test
    public void parse_class_nameOnly_with_background() {
        Element element = parser.parseElement("Customer{bg:green}");
        assertThat(element).isInstanceOf(ClassElement.class);
        ClassElement classElement = (ClassElement) element;
        assertThat(classElement.getNameSignature()).isEqualTo("Customer");
        assertThat(classElement.getBackground()).isEqualTo("green");
    }

    @Test
    public void parse_note_with_background() {
        Element element = parser.parseElement("note: Aggregate Root{bg:cornsilk}");
        assertThat(element).isInstanceOf(NoteElement.class);
        NoteElement noteElement = (NoteElement) element;
        assertThat(noteElement.getText()).isEqualTo("Aggregate Root");
        assertThat(noteElement.getBackground()).isEqualTo("cornsilk");
    }

    @Test
    public void parseExpression_classWithNameOnly() {
        YumlParser.Handler handler = Mockito.mock(YumlParser.Handler.class);
        parser.parseExpression("[Customer]", handler);

        ArgumentCaptor<Element> captor = ArgumentCaptor.forClass(Element.class);
        verify(handler).emit(captor.capture());
        verifyNoMoreInteractions(handler);

        Element element = captor.getValue();
        assertThat(element).isInstanceOf(ClassElement.class);
        ClassElement classElement = (ClassElement) element;
        assertThat(classElement.getNameSignature()).isEqualTo("Customer");
    }

    @Test
    public void parseExpression_oneClass_linkedTo_aNote() {
        YumlParserListenerCollector handler = new YumlParserListenerCollector();
        parser.parseExpression("[Customer]-[note: Aggregate Root{bg:cornsilk}]", handler);

        List<Element> emitted = handler.getElements();
        assertThat(emitted).hasSize(2);

        Element elementOne = emitted.get(0);
        assertThat(elementOne).isInstanceOf(ClassElement.class);
        ClassElement classElement = (ClassElement) elementOne;
        assertThat(classElement.getNameSignature()).isEqualTo("Customer");

        Element elementTwo = emitted.get(1);
        assertThat(elementTwo).isInstanceOf(NoteElement.class);
        NoteElement noteElement = (NoteElement) elementTwo;
        assertThat(noteElement.getText()).isEqualTo("Aggregate Root");
        assertThat(noteElement.getBackground()).isEqualTo("cornsilk");
    }

    @Test
    public void parseExpression_twoClassesAndANoteLinked() {
        YumlParserListenerCollector handler = new YumlParserListenerCollector();
        parser.parseExpression("[Person]-[Address||copy()]-.-[note: Value Object{bg:cornsilk}]", handler);

        List<Element> emitted = handler.getElements();
        assertThat(emitted).hasSize(3);

        Element elementOne = emitted.get(0);
        assertThat(elementOne).isInstanceOf(ClassElement.class);
        ClassElement classElement1 = (ClassElement) elementOne;
        assertThat(classElement1.getNameSignature()).isEqualTo("Person");
        assertThat(classElement1.getBackground()).isNull();

        Element elementTwo = emitted.get(1);
        assertThat(elementTwo).isInstanceOf(ClassElement.class);
        ClassElement classElement2 = (ClassElement) elementTwo;
        assertThat(classElement2.getNameSignature()).isEqualTo("Address");
        assertThat(classElement2.getMethods()).containsOnly("copy()");
        assertThat(classElement2.getBackground()).isNull();

        Element elementThree = emitted.get(2);
        assertThat(elementThree).isInstanceOf(NoteElement.class);
        NoteElement noteElement = (NoteElement) elementThree;
        assertThat(noteElement.getText()).isEqualTo("Value Object");
        assertThat(noteElement.getBackground()).isEqualTo("cornsilk");
    }

    @Test
    public void parseRelation_basic() {
        Relationship relationship = parser.parseRelation(ID_1, ID_2, "-");
        assertThat(relationship).isNotNull();

        Relationship.EndPoint leftEndPoint = relationship.leftEndPoint();
        Relationship.EndPoint righttEndPoint = relationship.rightEndPoint();

        assertThat(leftEndPoint.getLabel()).isNull();
        assertThat(righttEndPoint.getLabel()).isNull();
        assertThat(relationship.getLineStyle()).isEqualTo(Relationship.LineStyle.Solid);
        assertThat(leftEndPoint.getDecoration()).isEqualTo(Relationship.Decoration.None);
        assertThat(righttEndPoint.getDecoration()).isEqualTo(Relationship.Decoration.None);
    }

    @Test
    public void parseRelation_basicDashed() {
        Relationship relationship = parser.parseRelation(ID_1, ID_2, "-.-");
        assertThat(relationship).isNotNull();

        Relationship.EndPoint leftEndPoint = relationship.leftEndPoint();
        Relationship.EndPoint righttEndPoint = relationship.rightEndPoint();

        assertThat(leftEndPoint.getLabel()).isNull();
        assertThat(righttEndPoint.getLabel()).isNull();
        assertThat(relationship.getLineStyle()).isEqualTo(Relationship.LineStyle.Dashed);
        assertThat(leftEndPoint.getDecoration()).isEqualTo(Relationship.Decoration.None);
        assertThat(righttEndPoint.getDecoration()).isEqualTo(Relationship.Decoration.None);
    }

    @Test
    public void parseRelation_case2() {
        Relationship relationship = parser.parseRelation(ID_1, ID_2, "++-0..*>");
        assertThat(relationship).isNotNull();

        Relationship.EndPoint leftEndPoint = relationship.leftEndPoint();
        Relationship.EndPoint rightEndPoint = relationship.rightEndPoint();

        assertThat(relationship.getLineStyle()).isEqualTo(Relationship.LineStyle.Solid);
        assertThat(leftEndPoint.getLabel()).isNullOrEmpty();
        assertThat(leftEndPoint.getDecoration()).isEqualTo(Relationship.Decoration.Composition);
        assertThat(rightEndPoint.getLabel()).isEqualTo("0..*");
        assertThat(rightEndPoint.getDecoration()).isEqualTo(Relationship.Decoration.Arrow);
    }

    @Test
    public void parseExpression_case1() {
        YumlParserListenerCollector handler = new YumlParserListenerCollector();
        parser.parseExpression("[HttpContext]uses -.->[Response]", handler);

        List<Element> elements = handler.getElements();
        assertThat(elements).hasSize(2);

        Element elementOne = elements.get(0);
        assertThat(elementOne).isInstanceOf(ClassElement.class);
        ClassElement classElement1 = (ClassElement) elementOne;
        assertThat(classElement1.getNameSignature()).isEqualTo("HttpContext");
        assertThat(classElement1.getAttributes()).isEmpty();
        assertThat(classElement1.getMethods()).isEmpty();
        assertThat(classElement1.getBackground()).isNull();

        Element elementTwo = elements.get(1);
        assertThat(elementTwo).isInstanceOf(ClassElement.class);
        ClassElement classElement2 = (ClassElement) elementTwo;
        assertThat(classElement2.getNameSignature()).isEqualTo("Response");
        assertThat(classElement2.getAttributes()).isEmpty();
        assertThat(classElement2.getMethods()).isEmpty();
        assertThat(classElement2.getBackground()).isNull();

        List<Relationship> relationships = handler.getRelationships();
        assertThat(relationships).hasSize(1);

        Relationship relationship = relationships.get(0);
        assertThat(relationship.leftEndPoint().getLabel()).isEqualTo("uses ");
        assertThat(relationship.leftEndPoint().getDecoration()).isEqualTo(Relationship.Decoration.None);
        assertThat(relationship.leftEndPoint().getElementId()).isEqualTo(new Id(0));

    }
}
