package com.rpgm.online.components.foundation;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Via;

/**
 * Sightly component to display a single blog post.
 */
@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class BlogView {

    /**
     * Selector to request view for displaying blog post in
     * list/digest view.
     */
    private static final String LIST_VIEW_SELECTOR = "list";

    private static final String PUBLISHED_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DISPLAY_DATE_FORMAT = "MMMM dd, yyyy";

    private SlingHttpServletRequest request;

    @Inject @Via("resource")
    private String title = null;

    @Inject @Via("resource")
    private Long month = null;

    @Inject @Via("resource")
    private Long year = null;

    @Inject @Via("resource")
    private String url = null;

    @Inject @Via("resource")
    private boolean visible = false;

    @Inject @Via("resource")
    private String[] keywords = null;

    @Inject @Via("resource")
    private String image = null;

    @Inject @Via("resource")
    private String content = null;

    @Inject @Via("resource")
    private String description = null;

    @Inject @Via("resource") @Named("jcr:created")
    private Date createdDate = null;

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

    public BlogView(SlingHttpServletRequest request) {
        this.request = request;
    }

    @PostConstruct
    private void post() {
        listView = Arrays.asList(request.getRequestPathInfo().getSelectors()).contains(LIST_VIEW_SELECTOR);
        publishedDate = getDate(createdDate, PUBLISHED_DATE_FORMAT);
        displayDate = getDate(createdDate, DISPLAY_DATE_FORMAT);
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

    public String getImage() {
        return image;
    }
}