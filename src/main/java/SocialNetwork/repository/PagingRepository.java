package SocialNetwork.repository;


import SocialNetwork.domain.Entity;
import SocialNetwork.repository.util.Page;
import SocialNetwork.repository.util.Sort;

import SocialNetwork.repository.util.Pageable;

public interface PagingRepository<ID , E extends Entity<ID>> extends Repository<ID,E> {
    Page<E> findAllOnPage(Pageable pageable);
}
