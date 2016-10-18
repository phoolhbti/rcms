package com.rpgm.online.components.admin;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.models.annotations.Model;

/**
 * Sightly component to edit blog posts in the admin section. The
 * component determines whether to create a new blog post or edit
 * and existing blog post. To edit an existing blog post, pass
 * the resource path in the URL as the suffix.
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class BlogEdit {

    private SlingHttpServletRequest request;
    private String url;
    private String title;
    private Long month;
    private Long year;
    private boolean visible;
    private String[] keywords;
    private String image;
    private String content;
    private String description;

    public BlogEdit(SlingHttpServletRequest request) throws Exception {
        this.request = request;
        String blogPath = request.getRequestPathInfo().getSuffix();
        Resource blog = request.getResourceResolver().getResource(blogPath);
        if (blog != null) {
            ValueMap properties = blog.adaptTo(ValueMap.class);
            title = properties.get("title", String.class);
            url = properties.get("url", blog.getName());
            month = properties.get("month", Long.class);
            year = properties.get("year", Long.class);
            visible = properties.get("visible", false);
            keywords = properties.get("keywords", String[].class);
            image = properties.get("image", String.class);
            content = properties.get("content", String.class);
            description = properties.get("description", String.class);
        } else {
            Calendar calendar = Calendar.getInstance();
            month = ((long) calendar.get(Calendar.MONTH)) + 1;
            year = (long) calendar.get(Calendar.YEAR);
        }
    }

    /**
     * Get the title property.
     *
     * @return The title property.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the month property.
     *
     * @return The month property.
     */
    public Long getMonth() {
        return month;
    }

    /**
     * Get the year property.
     *
     * @return The year property.
     */
    public Long getYear() {
        return year;
    }

    /**
     * Get the resource name of the blog post URL.
     *
     * @return The resource name of hte blog post URL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the visible property.
     *
     * @return The visible property.
     */
    public boolean getVisible() {
        return visible;
    }

    /**
     * Get the multi-value keyword property.
     *
     * @return The multi-value keyword property.
     */
    public String[] getKeywords() {
        return keywords;
    }

    /**
     * Get the multi-value keywords property as a JSON string.
     *
     * @return The multi-value keyword property as a JSON string.
     */
    public String getKeywordsJSON() {
        JSONArray jsonArray;

        if (keywords != null) {
            jsonArray = new JSONArray(Arrays.asList(keywords));
        } else {
            jsonArray = new JSONArray();
        }

        return jsonArray.toString();
    }

    /**
     * Get the image path property.
     *
     * @return The image path property.
     */
    public String getImage() {
        return image;
    }

    /**
     * Get the blog post content.
     *
     * @return The blog post content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Get the description property.
     *
     * @return The description property.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the month property.
     *
     * @return The month property.
     */
    public List<Long> getMonths() {
        List<Long> months = new ArrayList<>();
        int length = 12;

        for (long x = 0; x < length; x++) {
            months.add(x + 1);
        }

        return months;
    }

    /**
     * Get the year property.
     *
     * @return the year property.
     */
    public List<Long> getYears() {
        List<Long> years = new ArrayList<>();
        int preYears = 2;
        int postYears = 2;
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        for (long x = currentYear - preYears; x <= currentYear + postYears; x++) {
            years.add(x);
        }

        return years;
    }
}