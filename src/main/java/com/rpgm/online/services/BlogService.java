package com.rpgm.online.services;

import java.util.Iterator;
import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

/**
 * API to search and retrieve blog posts.
 */
public interface BlogService {

    /**
     * Get all blog posts in oder of newest first.
     * @param request the {@link SlingHttpServletRequest} used to identify the user requesting this information
     * @return All blog posts in order of newest first
     */
    Iterator<Resource> getPosts(SlingHttpServletRequest request);

    /**
     * Get all published blog posts in order of newest first.
     *
     * @param request the {@link SlingHttpServletRequest} used to identify the user requesting this information
     * @return All published blog posts in order of newest first.
     */
    List<Resource> getPublishedPosts(SlingHttpServletRequest request);

    /**
     * Get paginated published blog posts in order of newest first.
     *
     * @param request the {@link SlingHttpServletRequest} used to identify the user requesting this information
     * @param offset The starting point of blog posts to get.
     * @param limit The number of blog posts to get.
     * @return The published blog posts according to the starting point and length.
     */
    List<Resource> getPublishedPosts(SlingHttpServletRequest request, long offset, long limit);

    /**
     * Get the number of blog posts in the system.
     *
     * @param request the {@link SlingHttpServletRequest} used to identify the user requesting this information
     * @return The number of blog posts.
     */
    long getNumberOfPosts(SlingHttpServletRequest request);

    /**
     * Get the number of pagination pages determined by the total
     * number of blog posts and specified number of blog posts
     * per page.
     *
     * @param request the {@link SlingHttpServletRequest} used to identify the user requesting this information
     * @param pageSize The number of blog pages per size.
     * @return The number of pagination pages required to display all
     *            blog posts.
     */
    long getNumberOfPages(SlingHttpServletRequest request, int pageSize);
}