package com.rpgm.online.components.admin;

import java.util.Iterator;
import javax.inject.Inject;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;

import com.rpgm.online.services.BlogService;

/**
 * Sightly component to list blog posts in the admin section.
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class BlogList {

    private SlingHttpServletRequest request;

    @Inject
    @OSGiService
    private BlogService blogService = null;

    public BlogList(SlingHttpServletRequest request) {
        this.request = request;
    }

    /**
     * Get all blog posts without pagination.
     *
     * @return The blog posts ordered from newest to oldest.
     */
    public Iterator<Resource> getBlogs() {
        return blogService.getPosts(request);
    }
}