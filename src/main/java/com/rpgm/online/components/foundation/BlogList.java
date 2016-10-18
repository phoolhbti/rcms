package com.rpgm.online.components.foundation;

import java.util.List;
import javax.inject.Inject;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rpgm.online.services.BlogService;

/**
 * Sightly component to display a list of blog posts for the public visitor.
 * The component uses the Blog Service to get the posts. Pagination is
 * handled via the pagination component. This component reads the pagination
 * from querystring.
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class BlogList {

    /**
     * The querystring parameter for pagination.
     */
    private static final String PAGINATION_PARAMETER = "page";

    /**
     * Default blog posts per pagination page.
     */
    private static final long DEFAULT_POSTS_PER_PAGE = 5;

    /**
     * The page size property.
     */
    private static final String PAGE_SIZE_PROPERTY = "pageSize";

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BlogList.class);

    @Inject
    private static Resource resource = null;


    @OSGiService
    private BlogService blogService = null;

    private SlingHttpServletRequest request;

    public BlogList(SlingHttpServletRequest request) {
        this.request = request;
    }

    /**
     * Get blog posts from Blog Service using pagination offset
     * and number of posts.
     *
     * @return The blog posts.
     */
    public List<Resource> getBlogs() {
        long postsPerPage = resource.adaptTo(ValueMap.class).get(PAGE_SIZE_PROPERTY, DEFAULT_POSTS_PER_PAGE);
        final Long offset = getOffset(postsPerPage);
        return blogService.getPublishedPosts(request, offset, postsPerPage);
    }

    /**
     * Get the starting point for pagination based on the querystring.
     *
     * If blog post to start at is determined by the page number from
     * the suffix multiplied by the number of posts per page. If the
     * page number is 1 or 0, start from the beginning.
     *
     * @return The blog post number to start at.
     */
    private Long getOffset(long postsPerPage) {
        Long offset = 0L;

        String param = request.getParameter(PAGINATION_PARAMETER);

        if (param != null) {
            try {
                offset = Long.valueOf(param);
            } catch (NumberFormatException e) {
                LOGGER.error("Could not get offset", e);
            }
        }

        if (offset <= 1) {
            return 0L;
        } else {
            return (offset - 1) * postsPerPage;
        }
    }
}