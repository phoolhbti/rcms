package com.rpgm.online.components.admin;

import org.apache.sling.api.scripting.SlingScriptHelper;

import com.rpgm.online.RPGMConstants;
import com.rpgm.online.services.AkismetService;
import com.rpgm.online.sightly.WCMUse;


public class AkismetConfig extends WCMUse {

    /**
     * The Akismet service to save the config properties.
     */
    private AkismetService akismetService;

    /**
     * The Sling Script Helper to get services.
     */
    private SlingScriptHelper scriptHelper;

    /**
     * The Akismet API key.
     */
    private String apiKey;

    /**
     * The Akismet domain name.
     */
    private String domainName;

    /**
     * Determines if the Akismet should be enabled even if the
     * API key and domain name are provided.
     */
    private boolean enabled;

    /**
     * Initialize the Sightly component.
     *
     * @param bindings The current execution context.
     */
    @Override
    public void activate() {
        scriptHelper = getSlingScriptHelper();

        akismetService = scriptHelper.getService(AkismetService.class);

        if (akismetService != null) {
            apiKey = isAuthorable() ? akismetService.getApiKey() : RPGMConstants.PASSWORD_REPLACEMENT;
            domainName = akismetService.getDomainName();
            enabled = akismetService.getEnabled();
            akismetService.verifyKey();
        }
    }

    /**
     * Get the Akismet API key.
     *
     * @return The Akismet API key.
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Get the Akismet domain name.
     *
     * @return The Akismet domain name.
     */
    public String getDomainName() {
        return domainName;
    }

    /**
     * Is Akismet enabled.
     *
     * @return True if Akismet is enabled.
     */
    public boolean getEnabled() {
        return enabled;
    }
}