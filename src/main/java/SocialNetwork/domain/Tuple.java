package SocialNetwork.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * Generic class for representing a pair of entities/values.
 *
 * @param <E1> the type of the first element
 * @param <E2> the type of the second element
 */
public class Tuple<E1, E2> implements Serializable {
    private E1 e1;
    private E2 e2;

    /**
     * Constructs a pair of elements.
     *
     * @param e1 the first element
     * @param e2 the second element
     */
    public Tuple(E1 e1, E2 e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    /**
     * Returns the first element (left).
     *
     * @return the first element
     */
    public E1 getLeft() {
        return e1;
    }

    /**
     * Sets the first element (left).
     *
     * @param e1 the new first element
     */
    public void setLeft(E1 e1) {
        this.e1 = e1;
    }

    /**
     * Returns the second element (right).
     *
     * @return the second element
     */
    public E2 getRight() {
        return e2;
    }

    /**
     * Sets the second element (right).
     *
     * @param e2 the new second element
     */
    public void setRight(E2 e2) {
        this.e2 = e2;
    }

    /**
     * Alias for {@link #getLeft()}.
     *
     * @return the first element
     */
    public E1 getFirst() {
        return e1;
    }

    /**
     * Alias for {@link #setLeft(Object)}.
     *
     * @param e1 the new first element
     */
    public void setFirst(E1 e1) {
        this.e1 = e1;
    }

    /**
     * Alias for {@link #getRight()}.
     *
     * @return the second element
     */
    public E2 getSecond() {
        return e2;
    }

    /**
     * Alias for {@link #setRight(Object)}.
     *
     * @param e2 the new second element
     */
    public void setSecond(E2 e2) {
        this.e2 = e2;
    }

    /**
     * Returns the string representation of the tuple.
     *
     * @return the string representing the tuple
     */
    @Override
    public String toString() {
        return "(" + e1 + ", " + e2 + ")";
    }

    /**
     * Checks equality between this tuple and another object.
     *
     * @param o the object to compare with
     * @return {@code true} if the objects are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple<?, ?> tuple)) return false;
        return Objects.equals(e1, tuple.e1) && Objects.equals(e2, tuple.e2);
    }

    /**
     * Computes the hash code for this tuple.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(e1, e2);
    }
}
