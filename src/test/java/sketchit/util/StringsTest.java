package sketchit.util;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;

/**
 *
 *
 */
public class StringsTest {

    @Test
    public void flip_empty() {
        assertThat(Strings.flipText("")).isEqualTo("");
    }

    @Test
    public void flip_oneChar() {
        assertThat(Strings.flipText("a")).isEqualTo("a");
    }

    @Test
    public void flip_twoChars() {
        assertThat(Strings.flipText("ab")).isEqualTo("ba");
    }

    @Test
    public void flip_threeChars() {
        assertThat(Strings.flipText("aob")).isEqualTo("boa");
    }

    @Test
    public void flip_fourChars() {
        assertThat(Strings.flipText("aokb")).isEqualTo("bkoa");
    }
}
