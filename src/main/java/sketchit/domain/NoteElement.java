package sketchit.domain;

/**
 *
 *
 */
public class NoteElement extends Element<NoteElement> {

    private final CharSequence text;

    public NoteElement(CharSequence text) {
        this.text = text;
    }

    public CharSequence getText() {
        return text;
    }

    @Override
    public boolean isSameElementAs(Element<?> element) {
        NoteElement other = element.adaptTo(NoteElement.class);
        if(other==null) {
            return false;
        }
        return other.getText().equals(other);
    }

    @Override
    public NoteElement completeWith(Element<?> element) {
        completeStylesWith(element);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T adaptTo(Class<T> type) {
        if(type.equals(NoteElement.class)) {
            return (T) this;
        }
        return null;
    }
}
