package domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * Generic base class for all entities in the system.
 *
 * @param <ID> the type of the entity's identifier
 */
public class Entity<ID> implements Serializable {
    private ID id;

    /**
     * Returns the identifier of the entity.
     *
     * @return the identifier of type {@code ID}
     */
    public ID getId(){
        return id;
    }

    /**
     * Sets the identifier of the entity.
     *
     * @param id the new identifier of the entity
     */
    public void setId(ID id){
        this.id = id;
    }

    /**
     * Checks equality between the current entity and another object based on the identifier.
     *
     * @param o the object to compare with
     * @return {@code true} if the objects have the same identifier, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o){
        if(this==o) return true;
        if(!(o instanceof Entity<?> entity)) return false;
        return Objects.equals(id,entity.id);
    }

    /**
     * Computes the hash code associated with the entity based on its identifier.
     *
     * @return the hash code of the identifier
     */
    @Override
    public int hashCode(){
        return Objects.hash(id);
    }

    /**
     * Returns the string representation of the entity.
     *
     * @return the string representing the entity
     */
    @Override
    public String toString(){
        return "Entity{id=" + id +'}';
    }
}
