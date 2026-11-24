package SocialNetwork.repository.util;

/**
 * Encapsulates pagination request information, such as the requested page number (0-indexed)
 * and the number of items per page.
 */
public class Pageable {
    private int PageSize;
    private int PageNumber;

    /**
     * Constructs a new {@code Pageable} instance.
     *
     * @param PageNumber the 0-based page index to retrieve
     * @param PageSize   the maximum number of elements per page
     */
    public Pageable(int PageNumber, int PageSize) {
        this.PageNumber = PageNumber;
        this.PageSize = PageSize;
    }

    /**
     * Returns the size of the page (number of elements to display).
     *
     * @return the page size
     */
    public int getPageSize() {
        return PageSize;
    }

    /**
     * Returns the 0-based index of the page.
     *
     * @return the page number
     */
    public int getPageNumber() {
        return PageNumber;
    }
}
