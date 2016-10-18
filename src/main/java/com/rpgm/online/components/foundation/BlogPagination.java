package com.rpgm.online.components.foundation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rpgm.online.services.BlogService;

/**
 * Sightly component for blog list/digest view pagination.
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class BlogPagination {

    /**
     * The querystring parameter for pagination.
     */
    private static final String PAGINATION_PARAMETER = "page";

    /**
     * The component property name which sets the page size.
     */
    private static final String PAGE_SIZE_PROPERTY = "pageSize";

    /**
     * The default number of blog posts on the list/digest page.
     */
    private static final int DEFAULT_PAGE_SIZE = 5;

    /**
     * The URL segment for the pagination querystring.
     */
    private static final String PAGE_QUERYSTRING = "?" + PAGINATION_PARAMETER + "=";

    @OSGiService
    private BlogService blogService = null;

    /**
     * The current HTTP request.
     */
    private SlingHttpServletRequest request;
    /**
     * The number of blog posts per page. Set in the component.
     */
    private int pageSize = DEFAULT_PAGE_SIZE;

    /**
     * The current page index.
     */
    private int currentPage = 0;

    /**
     * The total number of pages.
     */
    private long totalPages = 0;

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BlogPagination.class);

    public BlogPagination(SlingHttpServletRequest request) {
        this.request = request;
        ValueMap properties = request.getResource().adaptTo(ValueMap.class);
        pageSize = properties.get(PAGE_SIZE_PROPERTY, Integer.class);
    }

    @PostConstruct()
    private void post() {
        currentPage = getCurrentIndex();
        totalPages = getTotalPageCount();
    }

    /**
     * Get the total page count taking posts per page into account.
     *
     * @return The total page count.
     */
    private long getTotalPageCount() {
        long pages = 0;

        if (blogService != null) {
            pages = blogService.getNumberOfPages(request, pageSize);
        }

        return pages;
    }

    /**
     * Get the current page index, defaults to 1.
     *
     * @return The current page index.
     */
    private int getCurrentIndex() {
        int offset = 1;

        String param = request.getParameter(PAGINATION_PARAMETER);

        if (param != null) {
            try {
                offset = Integer.valueOf(param);
            } catch (NumberFormatException e) {
                LOGGER.error("Could not get offset", e);
            }
        }

       return offset;
    }

    /**
     * Get the current page number.
     *
     * @return The current page number.
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Get the total page count.
     *
     * @return The total page count.
     */
    public long getTotalPages(){
        return totalPages;
    }

    /**
     * Whether the current page is the first page.
     *
     * @return True if the current page is the first page.
     */
    public boolean getFirstPage() {
        return currentPage == 1;
    }

    /**
     * Whether the current page is the last page.
     *
     * @return True if the current page is the last page.
     */
    public boolean getLastPage() {
        return currentPage == totalPages;
    }

    /**
     * Get the path to the previous page.
     *
     * @return The path to the previous page.
     */
    public String getPreviousPath() {
        return request.getRequestURI() + PAGE_QUERYSTRING + (currentPage - 1);
    }

    /**
     * Get the path to the next page.
     *
     * @return The path to the next page.
     */
    public String getNextPath() {
        return request.getRequestURI() + PAGE_QUERYSTRING + (currentPage + 1);
    }

    /**
     * Get the list of pages and their paths.
     *
     * The list includes the index, whether the page is the current page
     * and the path of the page.
     *
     * @return The list of pages and their paths.
     */
    public List<HashMap<String, Object>> getPages() {
        List<HashMap<String, Object>> pages = new ArrayList<>();
        String path = request.getRequestURI();

        for (int x = 1; x <= totalPages; x++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("page", x);
            map.put("path", path + PAGE_QUERYSTRING + x);
            map.put("current", x == currentPage);
            pages.add(map);
        }

        return pages;
    }
}