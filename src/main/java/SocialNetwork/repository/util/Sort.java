package SocialNetwork.repository.util;

/**
 * Specifies sorting criteria for query results, defining the entity field
 * to sort by and the direction of the sort.
 *
 * @param field     the name of the entity attribute or column to sort by
 * @param ascending {@code true} for ascending order, {@code false} for descending order
 */
public record Sort(String field, boolean ascending) {

}
