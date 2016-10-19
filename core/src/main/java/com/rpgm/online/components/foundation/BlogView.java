package com.rpgm.online.components.foundation;

import java.net.URI;
import java.net.URISyntaxException;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.scripting.SlingScriptHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rpgm.online.services.LinkRewriterService;
import com.rpgm.online.sightly.WCMUse;

/**
 * Sightly component to display a single blog post.
 */
public class BlogView extends WCMUse {

    /**
     * Logger instance to log and debug errors.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BlogView.class);

    /**
     * Link Rewriter to create proper display paths for meta
     * tags and social shares.
     */
    private LinkRewriterService linkRewriter;

    /**
     * Selector to request view for displaying blog post in
     * list/digest view.
     */
    private static final String LIST_VIEW_SELECTOR = "list";

    private static final String PUBLISHED_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DISPLAY_DATE_FORMAT = "MMMM dd, yyyy";

    /**
     * The resource resolver to map paths.
     */
    private ResourceResolver resolver;

    private Resource resource;
    private SlingHttpServletRequest request;
    private String title;
    private Long month;
    private Long year;
    private String url;
    private boolean visible;
    private String[] keywords;
    private String image;
    private String content;
    private String description;
    private boolean listView;

    /**
     * Blog post date in ISO-8601 format (e.g. 2015-07-29 or yyyy-MM-dd)
     * per Open Graph specifications.
     */
    private String publishedDate;

    /**
     * Display date in MMMM dd, yyyy format.
     */
    private String displayDate;

    /**
     * The blog post image's relative path
     */
    private String imageRelativePath;

    /**
     * The blog post image's absolute path taking extensionless
     * URLs into account.
     */
    private String imageAbsolutePath;

    /**
     * Sightly component initialization.
     */
    @Override
    public void activate() {
        resource = getResource();
        request = getRequest();
        resolver = getResourceResolver();
        listView = Arrays.asList(request.getRequestPathInfo().getSelectors()).contains(LIST_VIEW_SELECTOR);

        SlingScriptHelper scriptHelper = getSlingScriptHelper();
        linkRewriter = scriptHelper.getService(LinkRewriterService.class);

        getBlog(resource);
    }

    /**
     * Get the blog post properties from the resource.
     *
     * @param blog The blog post resource.
     */
    private void getBlog(Resource blog) {
        if (blog != null) {
            ValueMap properties = blog.adaptTo(ValueMap.class);
            title = properties.get("title", String.class);
            month = properties.get("month", Long.class);
            year = properties.get("year", Long.class);
            url = properties.get("url", String.class);
            visible = Boolean.valueOf(properties.get("visible", false));
            keywords = properties.get("keywords", String[].class);
            content = properties.get("content", String.class);
            description = properties.get("description", String.class);
            image = properties.get("image", String.class);

            if (image != null) {
                image = resolver.map(image);
            }

            Date date = properties.get(JcrConstants.JCR_CREATED, Date.class);

            publishedDate = getDate(date, PUBLISHED_DATE_FORMAT);
            displayDate = getDate(date, DISPLAY_DATE_FORMAT);

            imageRelativePath = image;
            imageAbsolutePath = getAbsolutePath(image);
        }
    }

    /**
     * Format date in selected format.
     *
     * @param date The date.
     * @param format The format.
     * @return The formatted date.
     */
    private String getDate(final Date date, final String format) {
        String formattedDate = null;

        if (date != null) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(format);
            formattedDate = dateFormatter.format(date);
        }

        return formattedDate;
    }

    /**
     * Get the blog post title.
     *
     * @return The blog post title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the blog post month selected by the author.
     *
     * @return The blog post month.
     */
    public Long getMonth() {
        return month;
    }

    /**
     * Get the blog post year selected by the author.
     *
     * @return The blog post year.
     */
    public Long getYear() {
        return year;
    }

    /**
     * Get the friendly URL set by the author.
     *
     * This is the node name.
     *
     * @return return the blog post node name.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the blog post visibility set by the author.
     *
     * @return The blog post visibility.
     */
    public boolean getVisible() {
        return visible;
    }

    /**
     * Get the blog post keywords/tags set by the author.
     *
     * @return The blog post keywords/tags.
     */
    public String[] getKeywords() {
        return keywords;
    }

    /**
     * Get the blog post main content written by the author.
     *
     * @return The blog post main content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Get the blog post description written by the author.
     *
     * @return The blog post description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get whether the blog post is being requested in list view.
     *
     * @return Whether the blog post is being requested in list view.
     */
    public boolean getListView() {
        return listView;
    }

    /**
     * Get the blog post published date in format "yyyy-MM-dd".
     *
     * @return The blog post published date.
     */
    public String getPublishedDate() {
        return publishedDate;
    }

    /**
     * Get the blog post display date in formation "MMMM dd, yyyy".
     *
     * @return The blog post display date.
     */
    public String getDisplayDate() {
        return displayDate;
    }

    /**
     * Get the blog post image's relative path with "/content" removed.
     *
     * @return The blog post image's relative path.
     */
    public String getImageRelativePath() {
        return imageRelativePath;
    }

    /**
     * Get the blog post image's absolute path with "/content" removed.
     *
     * @return The blog post image's absolute path.
     */
    public String getImageAbsolutePath() {
        return imageAbsolutePath;
    }
}