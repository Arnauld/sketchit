package sketchit.domain.klazz;

import static org.fest.assertions.api.Assertions.assertThat;
import static sketchit.domain.klazz.Stereotypes.discardStereotypes;
import static sketchit.domain.klazz.Stereotypes.extractStereotypes;

import org.junit.Test;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class StereotypesTest {

    @Test
    public void discardStereotype_none() {
        assertThat(discardStereotypes("Customer")).isEqualTo("Customer");
    }

    @Test
    public void discardStereotype_single() {
        assertThat(discardStereotypes("<<Entity>>Customer")).isEqualTo("Customer");
        assertThat(discardStereotypes("<<Entity>> Customer")).isEqualTo("Customer");
        assertThat(discardStereotypes("<<Entity>>\nCustomer")).isEqualTo("Customer");
    }

    @Test
    public void discardStereotype_single_withSeparator() {
        assertThat(discardStereotypes("<<Entity>>;Customer")).isEqualTo("Customer");
        assertThat(discardStereotypes("<<Entity>>; Customer")).isEqualTo("Customer");
    }

    @Test
    public void discardStereotypes_multipleSepratedWithCommas() {
        assertThat(discardStereotypes("<<IDisposable>><<Writable>><<LocaleAware>>;Session")).isEqualTo("Session");
        assertThat(discardStereotypes("<<IDisposable>>;<<Writable>>;<<LocaleAware>>;Session")).isEqualTo("Session");
    }

    @Test
    public void extractStereotypes_single() {
        assertThat(extractStereotypes("<<Entity>>Customer")).containsOnly("<<Entity>>");
        assertThat(extractStereotypes("<<Entity>> Customer")).containsOnly("<<Entity>>");
        assertThat(extractStereotypes("<<Entity>>\nCustomer")).containsOnly("<<Entity>>");
    }

    @Test
    public void extractStereotypes_multipleSepratedWithCommas() {
        assertThat(extractStereotypes("<<IDisposable>><<Writable>><<LocaleAware>>;Session"))
                .containsOnly("<<IDisposable>>", "<<Writable>>", "<<LocaleAware>>");
        assertThat(extractStereotypes("<<IDisposable>>;<<Writable>>;<<LocaleAware>>;Session"))
                .containsOnly("<<IDisposable>>", "<<Writable>>", "<<LocaleAware>>");
    }

}
