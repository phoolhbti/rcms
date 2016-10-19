package com.rpgm.online.services.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.jcr.resource.JcrResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rpgm.online.RPGMConstants;
import com.rpgm.online.services.AkismetService;
import com.rpgm.online.services.CommentService;

/**
 * Service to get, save, and update comments as well as mark them as
 * spam or ham (valid).
 */
@Service(value = CommentService.class)
@Component(immediate = true,
           name = "rpgm comments service",
           description = "Service to get, delete, update and add comments.")
public class CommentServiceImpl implements CommentService {

    /** Akismet service to mark comments as spam or ham. */
    @Reference
    private AkismetService akismetService;

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentServiceImpl.class);

    /**
     * JCR_SQL2 query to get all comments that are not hidden in order of newest first.
     */
    private static final String BLOG_QUERY = String.format("SELECT * FROM [%s] AS s WHERE "
            + "ISDESCENDANTNODE([%s]) AND s.[%s] = '%s' ORDER BY [%s] desc",
            RPGMConstants.NODE_TYPE_COMMENT,
            RPGMConstants.COMMENTS_PATH,
            RPGMConstants.COMMENT_PROPERTY_DISPLAY,
            true,
            JcrConstants.JCR_CREATED);

    /**
     * Get all Comments in the order of newest to oldest.
     *
     * @param request The current request
     * @return List of all comments
     */
    public List<Resource> getComments(final SlingHttpServletRequest request) {
        final List<Resource> comments = new ArrayList<>();
        final Iterator<Resource> queryResults = request.getResourceResolver().findResources(BLOG_QUERY, Query.JCR_SQL2);

        while (queryResults.hasNext()) {
            comments.add(queryResults.next());
        }

        return comments;
    }

    /**
     * Delete comment by setting it's display property to false.
     *
     * @param request The current request to get session and Resource Resolver
     * @param id The comment UUID
     * @return true if the operation was successful
     */
    public boolean deleteComment(final SlingHttpServletRequest request, final String id) {
        boolean result = false;

        try {
            Session session = request.getResourceResolver().adaptTo(Session.class); 
            Node node = session.getNodeByIdentifier(id);

            if (node != null) {
                JcrResourceUtil.setProperty(node, RPGMConstants.COMMENT_PROPERTY_DISPLAY, false);
                session.save();
                result = true;
            }
        } catch (RepositoryException e) {
            LOGGER.error("Could not delete comment from JCR", e);
        }

        return result;
    }

    /**
     * Edit comment and mark it as author edited.
     *
     * @param request The current request to get session and Resource Resolver
     * @param id The comment UUID
     * @param text The comment text
     * @return true if the operation was successful
     */
    public boolean editComment(final SlingHttpServletRequest request, final String id, String text) {
        boolean result = false;

        try {
            Session session = request.getResourceResolver().adaptTo(Session.class);
            Node node = session.getNodeByIdentifier(id);

            if (node != null) {
                JcrResourceUtil.setProperty(node, RPGMConstants.COMMENT_PROPERTY_COMMENT, text);
                JcrResourceUtil.setProperty(node, RPGMConstants.COMMENT_PROPERTY_EDITED, true);
                session.save();
                result = true;
            }
        } catch (RepositoryException e) {
            LOGGER.error("Could not update comment from JCR", e);
        }

        return result;
    }

    /**
     * Mark comment as spam, submit it to Akismet and delete it by setting
     * it's display property to false.
     *
     * @param request The current request to get session and Resource Resolver
     * @param id The comment UUID
     * @return true if the operation was successful
     */
    public boolean markAsSpam(final SlingHttpServletRequest request, final String id) {
        boolean result = false;

        try {
            final ResourceResolver resolver = request.getResourceResolver();
            final Session session = resolver.adaptTo(Session.class);
            final Node node = session.getNodeByIdentifier(id);

            if (node != null) {
                final Resource resource = resolver.getResource(node.getPath());
                result = akismetService.submitSpam(resource);
            }
        } catch (RepositoryException e) {
            LOGGER.error("Could not submit spam.", e);
        }

        return result;
    }

    /**
     * Mark comment as ham, submit it to Akismet and mark it valid it by setting
     * it's display property to true.
     *
     * @param request The current request to get session and Resource Resolver
     * @param id The comment UUID
     * @return true if the operation was successful
     */
    public boolean markAsHam(final SlingHttpServletRequest request, final String id) {
        boolean result = false;

        try {
            final ResourceResolver resolver = request.getResourceResolver();
            final Session session = resolver.adaptTo(Session.class);
            final Node node = session.getNodeByIdentifier(id);

            if (node != null) {
                final Resource resource = resolver.getResource(node.getPath());
                result = akismetService.submitHam(resource);
            }
        } catch (RepositoryException e) {
            LOGGER.error("Could not submit ham.", e);
        }

        return result;
    }

    /**
     * Get the number of replies for a given comment.
     *
     * @param comment The current comment
     * @return The number of replies for the comment
     */
    public int numberOfReplies(final Resource comment) {
        final Iterator<Resource> children = comment.listChildren();
        int size = 0;

        while (children.hasNext()) {
            children.next();
            size++;
        }

        return size;
    }

    /**
     * Get the blog post associated with the given comment.
     *
     * There are only two levels of comments. You can reply to a post
     * and you can reply to a top level comment.
     *
     * @param comment The current comment
     * @return the number of replies to the given comment
     */
    public Resource getParentPost(final Resource comment) {
        final ResourceResolver resolver = comment.getResourceResolver();
        Resource parent = comment.getParent();

        // Try one level up
        Resource post = resolver.getResource(parent.getPath().replace("/comments/", "/blog/"));

        if (post == null) {
            //try two levels up
            parent = parent.getParent();
            post = resolver.getResource(parent.getPath().replace("/comments/", "/blog/"));
        }

        return post;
    }
}