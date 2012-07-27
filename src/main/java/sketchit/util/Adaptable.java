package sketchit.util;

/**
 *
 *
 */
public interface Adaptable {
    <T> T adaptTo(Class<T> type);
}
