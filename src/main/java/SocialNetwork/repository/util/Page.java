package SocialNetwork.repository.util;

/**
 * Represents a single page of items retrieved from a repository,
 * including the elements for that page and the total number of items available across all pages.
 *
 * @param <E> the type of entity elements contained in the page
 */
public class Page<E> {
    private Iterable<E> elementsOnPage;
    private int totalNumberOfElements;

    /**
     * Constructs a new {@code Page} instance.
     *
     * @param elementsOnPage        an iterable containing the elements present on this page
     * @param totalNumberOfElements the total count of elements across all pages
     */
    public Page(Iterable<E> elementsOnPage, int totalNumberOfElements) {
        this.elementsOnPage = elementsOnPage;
        this.totalNumberOfElements = totalNumberOfElements;
    }

    /**
     * Returns the elements contained on the current page.
     *
     * @return an {@link Iterable} over the elements of this page
     */
    public Iterable<E> getElementsOnPage() {
        return elementsOnPage;
    }

    /**
     * Returns the total number of elements available across all pages.
     *
     * @return the total element count
     */
    public int getTotalNumberOfElements() {
        return totalNumberOfElements;
    }
}
