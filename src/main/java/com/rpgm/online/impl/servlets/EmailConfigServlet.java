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

import com.rpgm.online.RPGMConstants;
import com.rpgm.online.services.EmailService;
import com.rpgm.online.services.UserService;

/**
 * Post servlet to save email config updates.
 */
@SlingServlet(paths = RPGMConstants.SERVLET_PATH_ADMIN + "/emailconfig")
public class EmailConfigServlet extends AdminServlet {

    /** Service to get and set email configurations. */
    @Reference
    private EmailService emailService;

    /** Service to determine if the current user has write permissions. */
    @Reference
    private UserService userService;

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailConfigServlet.class);

    /** The SMTP username parameter. */
    private static final String SMTP_USERNAME_PROPERTY = "smtpUsername";

    /** The SMTP password parameter. */
    private static final String SMTP_PASSWORD_PROPERTY = "smtpPassword";

    /** The sender parameter. */
    private static final String SENDER_PROPERTY = "sender";

    /** The recipient parameter. */
    private static final String RECIPIENT_PROPERTY = "recipient";

    /** The host parameter. */
    private static final String HOST_PROPERTY = "host";

    /** The port parameter. */
    private static final String PORT_PROPERTY = "port";

    /**
     * Save email configuration on POST.
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
            final String smtpUsername = request.getParameter(SMTP_USERNAME_PROPERTY);
            final String smtpPassword = request.getParameter(SMTP_PASSWORD_PROPERTY);
            final String sender = request.getParameter(SENDER_PROPERTY);
            final String recipient = request.getParameter(RECIPIENT_PROPERTY);
            final String host = request.getParameter(HOST_PROPERTY);
            final Long port = getPortNumber(request.getParameter(PORT_PROPERTY));

            final Map<String, Object> properties = new HashMap<String, Object>();

            properties.put(EmailService.EMAIL_SMTP_USERNAME, smtpUsername);
            properties.put(EmailService.EMAIL_SENDER, sender);
            properties.put(EmailService.EMAIL_RECIPIENT, recipient);
            properties.put(EmailService.EMAIL_SMTP_HOST, host);
            properties.put(EmailService.EMAIL_SMTP_PORT, port);

            /* Don't save the password if it's all stars. Don't save the password
             * if the user just added text to the end of the stars. This shouldn't
             * happen as the JavaScript should remove the value on focus. Save the
             * password if it's null or blank in order to clear it out. */
            if (smtpPassword == null || !smtpPassword.contains(RPGMConstants.PASSWORD_REPLACEMENT)) {
                properties.put(EmailService.EMAIL_SMTP_PASSWORD, smtpPassword);
            }

            final boolean result = emailService.setProperties(properties);

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

    /**
     * Convert the String request port parameter to a Long.
     *
     * @param port The port request parameter
     * @return The port number
     */
    private Long getPortNumber(final String port) {
        Long ret = null;

        if (port != null) {
            try {
                ret = Long.valueOf(port);
            } catch (NumberFormatException e) {
                ret = null;
            }
        }

        return ret;
    }
}