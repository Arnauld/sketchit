package sketchit.domain.klazz;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class Stereotypes {
    /**
     * Remove any stereotypes that may be present in the text.
     * <table border="1px">
     * <tr>
     * <td>&lt;&lt;Entity&gt;&gt; Customer</td>
     * <td>Customer</td>
     * </tr>
     * <tr>
     * <td>&lt;&lt;Entity&gt;&gt;;Customer</td>
     * <td>Customer</td>
     * </tr>
     * <tr>
     * <td>Customer</td>
     * <td>Customer</td>
     * </tr>
     * </table>
     *
     * @param text to cleanup
     * @return text wihout any stereotypes
     */
    public static String discardStereotypes(CharSequence text) {
        return Pattern.compile("<<.*>>;?").matcher(text).replaceAll("").trim();
    }

    /**
     * Retrieve all stereotypes that may be present in the text.
     * <table>
     * <tr>
     * <td>&lt;&lt;Entity&gt;&gt; Customer</td>
     * <td>Customer</td>
     * </tr>
     * <tr>
     * <td>Entity</td>
     * <td></td>
     * </tr>
     * </table>
     *
     * @param text to analyze
     * @return list of stereotype
     */
    public static List<String> extractStereotypes(CharSequence text) {
        List<String> stereotypes = new ArrayList<String>();
        Matcher matcher = Pattern.compile("(<<[^>]+>>)").matcher(text);
        while(matcher.find()) {
            stereotypes.add(matcher.group(1));
        }
        return stereotypes;
    }

    //FD3E PARENTHESE GAUCHE ORNEE: ﴾
    //FD3F PARENTHESE DROITE ORNEE: ﴿
    public static String encryptStereotypeDelimiters(String text) {
        return Pattern.compile("(<<([^>]+)>>)").matcher(text).replaceAll("\ufd3e$2\ufd3f");
    }

    public static String decryptStereotypeDelimiters(String text) {
        return Pattern.compile("(\ufd3e([^\ufd3f]+)\ufd3f)").matcher(text).replaceAll("<<$2>>");
    }

    public static String toSignature(List<String> stereotypes) {
        StringBuilder builder = new StringBuilder();
        for(String stereotype: stereotypes) {
            builder.append("<<").append(stereotype).append(">>");
        }
        return builder.toString();
    }
}
