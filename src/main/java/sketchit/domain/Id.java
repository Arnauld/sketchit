package sketchit.domain;

/**
 */
public class Id {
    private final int id;

    public Id(int id) {
        this.id = id;
    }

    public int asInt() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Id other = (Id) o;

        if (id != other.id) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
