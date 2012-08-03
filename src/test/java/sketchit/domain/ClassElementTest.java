package sketchit.domain;

import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;

/**
 *
 *
 */
public class ClassElementTest {

    @Test
    public void completeWith_sameClass_sameAttributes () {
        ClassElement element = new ClassElement("Customer", asList("FirstName"), null);
        ClassElement other = new ClassElement("Customer", asList("FirstName"), null);

        assertThat(element.isSameElementAs(other)).isTrue();

        element.completeWith(other);
        assertThat(element.getClassName()).isEqualTo("Customer");
        assertThat(element.getAttributes()).containsOnly("FirstName");
        assertThat(element.getAttributes()).hasSize(1); // no duplicate
        assertThat(element.getMethods()).isEmpty();
    }

    @Test
    public void completeWith_sameClass_differentAttributes () {
        ClassElement element = new ClassElement("Customer", asList("FirstName"), null);
        ClassElement other = new ClassElement("Customer", asList("LastName"), null);

        assertThat(element.isSameElementAs(other)).isTrue();

        element.completeWith(other);
        assertThat(element.getClassName()).isEqualTo("Customer");
        assertThat(element.getAttributes()).containsOnly("FirstName", "LastName");
        assertThat(element.getAttributes()).hasSize(2); // no duplicate
        assertThat(element.getMethods()).isEmpty();
    }

}
