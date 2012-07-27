package sketchit.util;

/**
 *
 *
 */
public class Strings {

    public static String flipText(String expr) {
        char[] chars = expr.toCharArray();
        for(int i=0;i<chars.length/2;i++) {
            char c = chars[i];
            chars[i] = chars[chars.length-1-i];
            chars[chars.length-1-i] = c;
        }
        return new String(chars);
    }

}
