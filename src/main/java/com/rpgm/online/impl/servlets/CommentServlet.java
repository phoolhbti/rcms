package com.rpgm.online.impl.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rpgm.online.RPGMConstants;
import com.rpgm.online.services.RecaptchaService;

/**
 * <p>
 * Add comment to blog post. Comments are stored under a different parent node as blog posts for ease of access control. The comment node
 * structure mirrors the blog post node structure and this servlet will create it.
 * </p>
 * <p>
 * Blog posts are stored under: /content/blogs/2015/01/title
 * </p>
 * <p>
 * Comments are stored under: /content/comments/2015/01/title/comment_1
 * </p>
 * <p>
 * Comments can be nested two levels deep.
 * </p>
 * <p>
 * A comment can be created by issuing a POST request to a path similar to /content/blogs/2015/01/title.comment.html.
 * </p>
 */
@SlingServlet(
    resourceTypes = RPGMConstants.PAGE_TYPE_BLOG,
    selectors = "comment",
    extensions = "html",
    methods = "POST"
)
public class CommentServlet extends SlingAllMethodsServlet {

    /**
     * reCAPTCHA service to verify user isn't a robot.
     */
    @Reference
    private RecaptchaService recaptchaService = null;

    @Reference
    private ResourceResolverFactory resourceResolverFactory = null;

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentServlet.class);

    /**
     * Request parameter sent from the comment form for Author.
     */
    private static final String AUTHOR_PARAMETER = "author";

    /**
     * Request parameter sent from the comment form for Comment.
     */
    private static final String COMMENT_PARAMETER = "comment";

    /**
     * Save the comment to the JCR.
     * <p>
     * Get or create the comment node structure to mirror the blog post node structure.
     * Create the comment node under the structure and save the author, comment, and date.
     * Currently redirects back to the same page. Verifies against the reCAPTCHA service.
     * The next version will be an asynchronous post.
     *
     * @param request  The Sling HTTP servlet request.
     * @param response The Sling HTTP servlet response.
     */
    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        final String blogPath = request.getResource().getPath();
        final String replyTo = request.getParameter("reply-to-comment");

        if (!recaptchaService.getEnabled() || recaptchaService.validate(request)) {
            final String author = request.getParameter(AUTHOR_PARAMETER);
            final String comment = request.getParameter(COMMENT_PARAMETER);

            ResourceResolver resolver = null;
            try {
                resolver = resourceResolverFactory.getAdministrativeResourceResolver(null);

                String commentParentPath = blogPath.replace(RPGMConstants.BLOG_PATH, RPGMConstants.COMMENTS_PATH);
                if (StringUtils.isNotEmpty(replyTo)) {
                    commentParentPath = commentParentPath + "/" + replyTo;
                }
                Resource commentParentResource = ResourceUtil.getOrCreateResource(resolver, commentParentPath, JcrResourceConstants
                        .NT_SLING_ORDERED_FOLDER, JcrResourceConstants.NT_SLING_ORDERED_FOLDER, true);

                String commentPath = getCommentName(resolver, commentParentPath);
                Map<String, Object> properties = new HashMap<>();
                properties.put(AUTHOR_PARAMETER, author);
                properties.put(COMMENT_PARAMETER, comment);
                properties.put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED);

                Resource commentResource = resolver.create(commentParentResource, commentPath, properties);
                Node commentNode = commentResource.adaptTo(Node.class);
                commentNode.addMixin(NodeType.MIX_CREATED);
                resolver.commit();
            } catch (Exception e) {
                LOGGER.error("Could not create comment node", e);
            } finally {
                if (resolver != null && resolver.isLive()) {
                    resolver.close();
                }
            }
        }

        response.sendRedirect(blogPath + ".html");
    }

    /**
     * Get the first available node name for a comment. Comments are named
     * "comment_1", "comment_2", etc... There is a limit of 1000 comments
     * per parent node.
     *
     * @param parentPath The path of the parent node in the /content/comments
     *                   node structure
     * @return The first available node name.
     */
    private String getCommentName(ResourceResolver resolver, final String parentPath) {
        final int MAX_TRIES = 1000;
        for (int i = 0; i < MAX_TRIES; i++) {
            String commentName = "comment_" + System.currentTimeMillis();
            String commentResourcePath = parentPath + "/" + commentName;
            if (resolver.getResource(commentResourcePath) == null) {
                return commentName;
            }
        }
        throw new UnsupportedOperationException("DOS attempt through blog comments?! :)");
    }
}