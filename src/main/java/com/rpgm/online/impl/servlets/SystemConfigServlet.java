package com.rpgm.online.impl.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;

import org.apache.commons.lang.CharEncoding;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rpgm.online.services.SystemSettingsService;
import com.rpgm.online.services.UserService;

/**
 * Post servlet to save system config updates.
 */
@SlingServlet(
    resourceTypes = "rpgm/components/pages/adminPage",
    selectors = "config",
    extensions = "html",
    methods = "POST"
)
public class SystemConfigServlet extends AdminServlet {

    /** Service to get and set and set system settings. */
    @Reference
    SystemSettingsService systemSettingsService;

    /** Service to determine if the current user has write permissions. */
    @Reference
    UserService userService;

    /** The logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemConfigServlet.class);

    /** The blog name request parameter */
    private static final String BLOG_NAME_PROPERTY = "blogName";

    /** The extensionless URLs request parameter */
    private static final String EXTENSIONLESS_URLS_PROPERTY = "extensionlessUrls";

    /**
     * Save system properties on POST.
     *
     * @param request The Sling HTTP servlet request.
     * @param response The Sling HTTP servlet response.
     */
    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        final PrintWriter writer = response.getWriter();
        final boolean allowWrite = userService.isAuthorable(request.getResourceResolver());

        response.setCharacterEncoding(CharEncoding.UTF_8);
        response.setContentType("application/json");

        if (allowWrite) {
            final String blogName = request.getParameter(BLOG_NAME_PROPERTY);
            final boolean extensionlessUrls = Boolean.parseBoolean(request.getParameter(EXTENSIONLESS_URLS_PROPERTY));

            final Map<String, Object> properties = new HashMap<String, Object>();

            properties.put(SystemSettingsService.SYSTEM_BLOG_NAME, blogName);
            properties.put(SystemSettingsService.SYSTEM_EXTENSIONLESS_URLS, extensionlessUrls);

            boolean result = systemSettingsService.setProperties(properties);

            if (result) {
                response.setStatus(SlingHttpServletResponse.SC_OK);
                sendResponse(writer, "OK", "Settings successfully updated.");
            } else {
                response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                sendResponse(writer, "Error", "Settings failed to update.");
            }
        } else {
            response.setStatus(SlingHttpServletResponse.SC_FORBIDDEN);
            sendResponse(writer, "Error", "Current user not authorized.");
        }
    }
}