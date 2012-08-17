package sketchit.util;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class NumberFormatTest {

    @Test
    public void format2Decimal() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("#####.##", symbols);
        assertThat(df.format(1234.453f)).isEqualTo("1234.45");

        NumberFormat nf = DecimalFormat.getNumberInstance(Locale.US);
        assertThat(nf.format(1234.453f)).isEqualTo("1,234.453");
    }
}
