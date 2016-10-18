package com.rpgm.online.components.admin;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.auth.core.spi.AuthenticationHandler;
import org.apache.sling.models.annotations.Model;

import com.rpgm.online.sightly.WCMUse;

/**
 * Sightly login component to control what displays when the user
 * attempts to log in. Currently the component simply displays the
 * JAAS provided error message, but eventually will control the
 * display of the reCAPTCHA by calling the reCAPTCHA service.
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class Login {

    /**
     * The request.
     */
    private SlingHttpServletRequest request;

    public Login(SlingHttpServletRequest request) {
        this.request = request;
    }

    public String getLoginRedirect() {
        String loginRedirect = request.getParameter("resource");
        if (StringUtils.isEmpty(loginRedirect)) {
            loginRedirect = "/content/rpgm/admin.html";
        }
        return loginRedirect;
    }

    /**
     * Get the reason that authentication failed.
     *
     * @return The reason that authentication failed.
     */
    public String getReason() {
        return request.getParameter(AuthenticationHandler.FAILURE_REASON);
    }
}