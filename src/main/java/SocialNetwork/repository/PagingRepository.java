package SocialNetwork.repository;

import SocialNetwork.domain.Entity;
import SocialNetwork.repository.util.Page;
import SocialNetwork.repository.util.Pageable;

/**
 * Repository interface that extends {@link Repository} to support pagination of entities.
 *
 * @param <ID> the type of the entity identifier
 * @param <E>  the type of the entity managed, extending {@link Entity}
 */
public interface PagingRepository<ID, E extends Entity<ID>> extends Repository<ID, E> {
    /**
     * Retrieves a page of entities based on the given pagination information.
     *
     * @param pageable pagination specification defining page number and page size
     * @return a {@link Page} containing the elements of the requested page and total element count
     */
    Page<E> findAllOnPage(Pageable pageable);
}
