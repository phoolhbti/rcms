package com.rpgm.online.impl.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rpgm.online.RPGMConstants;
import com.rpgm.online.services.BlogService;

@Service
@Component
public class BlogServiceImpl implements BlogService {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BlogServiceImpl.class);

    /**
     * JCR_SQL2 query to get all blog posts in order of newest first.
     */
    private static final String PUBLISHED_BLOGS_QUERY = String.format("SELECT * FROM [%s] AS s WHERE "
            + "ISDESCENDANTNODE([%s]) AND s.[%s] = '%s' AND s.[%s] = '%s' ORDER BY [%s] desc",
        JcrConstants.NT_UNSTRUCTURED,
        RPGMConstants.BLOG_PATH,
        JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY,
        RPGMConstants.PAGE_TYPE_BLOG,
        RPGMConstants.PROP_BLOG_VISIBLE,
        "true",
        JcrConstants.JCR_CREATED);

    /**
     * JCR_SQL2 query to get all blog posts in order of newest first.
     */
    private static final String ALL_BLOGS_QUERY = String.format("SELECT * FROM [%s] AS s WHERE "
            + "ISDESCENDANTNODE([%s]) AND s.[%s] = '%s' ORDER BY [%s] desc",
        JcrConstants.NT_UNSTRUCTURED,
        RPGMConstants.BLOG_PATH,
        JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY,
        RPGMConstants.PAGE_TYPE_BLOG,
        JcrConstants.JCR_CREATED);

    @Override
    public Iterator<Resource> getPosts(SlingHttpServletRequest request) {
        return request.getResourceResolver().findResources(ALL_BLOGS_QUERY, "JCR-SQL2");
    }

    public List<Resource> getPublishedPosts(SlingHttpServletRequest request) {
        return getPublishedPosts(request, 0, 0);
    }


    public List<Resource> getPublishedPosts(SlingHttpServletRequest request, long offset, long limit) {
        ArrayList<Resource> posts = new ArrayList<>();
        Iterator<Resource> blogPosts = request.getResourceResolver().findResources(PUBLISHED_BLOGS_QUERY, "JCR-SQL2");
        long count = 0;
        while (blogPosts.hasNext()) {
            count++;
            if (limit != 0 && posts.size() >= limit) {
                break;
            }
            if (offset > 0 && count <= offset) {
                // consume
                blogPosts.next();
                continue;
            }
            posts.add(blogPosts.next());
        }
        return posts;
    }

    public long getNumberOfPages(SlingHttpServletRequest request, int pageSize) {
        long posts = getNumberOfPosts(request);

        return (long)Math.ceil((double)posts / pageSize);
    }


    public long getNumberOfPosts(SlingHttpServletRequest request) {
        return getPublishedPosts(request).size();
    }



}