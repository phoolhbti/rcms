package com.rpgm.online.impl.servlets;

import com.rpgm.online.RPGMConstants;
import com.rpgm.online.services.RecaptchaService;
import com.rpgm.online.services.UserService;

import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import javax.servlet.ServletException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Post servlet to save reCAPTCHA config updates.
 */
@SlingServlet(paths = RPGMConstants.SERVLET_PATH_ADMIN + "/recaptchaconfig")
public class RecaptchaConfigServlet extends AdminServlet {

    /** Service to get and set and set reCAPTCHA settings. */
    @Reference
    private RecaptchaService recaptchaService;

    /** Service to determine if the current user has write permissions. */
    @Reference
    private UserService userService;

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(RecaptchaConfigServlet.class);

    /** The site key request parameter */
    private static final String SITE_KEY_PROPERTY = "siteKey";

    /** The secret key request parameter */
    private static final String SECRET_KEY_PROPERTY = "secretKey";

    /** The enabled request parameter */
    private static final String ENABLED_PROPERTY = "enabled";

    /**
     * Save reCAPTCHA properties on POST.
     *
     * @param request The Sling HTTP servlet request.
     * @param response The Sling HTTP servlet response.
     */
    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        final PrintWriter writer = response.getWriter();
        final boolean allowWrite = userService.isAuthorable(request.getResourceResolver());

        response.setCharacterEncoding(CharEncoding.UTF_8);
        response.setContentType("application/json");

        if (allowWrite) {
            final String siteKey = request.getParameter(SITE_KEY_PROPERTY);
            final String secretKey = request.getParameter(SECRET_KEY_PROPERTY);
            final boolean enabled = Boolean.parseBoolean(request.getParameter(ENABLED_PROPERTY));

            final Map<String, Object> properties = new HashMap<String, Object>();

            properties.put(RecaptchaService.RECAPTCHA_SITE_KEY, siteKey);
            properties.put(RecaptchaService.RECAPTCHA_ENABLED, enabled);

            /* Don't save the password if it's all stars. Don't save the password
             * if the user just added text to the end of the stars. This shouldn't
             * happen as the JavaScript should remove the value on focus. Save the
             * password if it's null or blank in order to clear it out. */
            if (StringUtils.isBlank(secretKey) || !secretKey.contains(RPGMConstants.PASSWORD_REPLACEMENT)) {
                properties.put(RecaptchaService.RECAPTCHA_SECRET_KEY, secretKey);
            }

            final boolean result = recaptchaService.setProperties(properties);

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