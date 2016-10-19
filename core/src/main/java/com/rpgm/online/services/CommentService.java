package com.rpgm.online.services;

import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

/**
 * API's to get, save, and update comments as well as mark them as
 * spam or ham (valid).
 */
public interface CommentService {

    /**
     * Get all Comments in the order of newest to oldest.
     *
     * @param request The current request
     * @return List of all comments
     */
    List<Resource> getComments(final SlingHttpServletRequest request);

    /**
     * Delete comment by setting it's display property to false.
     *
     * @param request The current request to get session and Resource Resolver
     * @param id The comment UUID
     * @return true if the operation was successful
     */
    boolean deleteComment(final SlingHttpServletRequest request, final String id);

    /**
     * Edit comment and mark it as author edited.
     *
     * @param request The current request to get session and Resource Resolver
     * @param id The comment UUID
     * @param text The comment text
     * @return true if the operation was successful
     */
    boolean editComment(final SlingHttpServletRequest request, final String id, String text);

    /**
     * Mark comment as spam, submit it to Akismet and delete it by setting
     * it's display property to false.
     *
     * @param request The current request to get session and Resource Resolver
     * @param id The comment UUID
     * @return true if the operation was successful
     */
    boolean markAsSpam(final SlingHttpServletRequest request, final String id);

    /**
     * Mark comment as ham, submit it to Akismet and mark it valid it by setting
     * it's display property to true.
     *
     * @param request The current request to get session and Resource Resolver
     * @param id The comment UUID
     * @return true if the operation was successful
     */
    boolean markAsHam(final SlingHttpServletRequest request, final String id);

    /**
     * Get the number of replies for a given comment.
     *
     * @param comment The current comment
     * @return The number of replies for the comment
     */
    int numberOfReplies(final Resource comment);

    /**
     * Get the blog post associated with the given comment.
     *
     * @param comment The current comment
     * @return the number of replies to the given comment
     */
    Resource getParentPost(final Resource comment);
}