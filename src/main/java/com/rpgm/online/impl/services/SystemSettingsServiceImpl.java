package com.rpgm.online.impl.services;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rpgm.online.services.SystemSettingsService;

/**
 * System settings configuration to save blog engine settings
 * such as blog name and extensionless URLs.
 */
@Service
@Component(name = "RPGM system settings",
           description = "General blog engine system settings.")
public class SystemSettingsServiceImpl implements SystemSettingsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemSettingsService.class);
    private static final String SETTINGS_PATH = "/content/rpgm/admin/settings";
    private Map<String, Object> settings = new ConcurrentHashMap<>();
    private static final boolean PROP_DEFAULT_RPGM_EXTENSIONLESS_URLS = false;
    private static final String PROP_DEFAULT_RPGM_BLOG_NAME = "RPGM";

    @Reference
    private ResourceResolverFactory resourceResolverFactory = null;


    @Override
    public boolean setProperties(Map<String, Object> properties) {
        Resource settingsResource = getConfigurationResource();
        if (settingsResource != null) {
            ResourceResolver resolver = settingsResource.getResourceResolver();
            try {
                settings.putAll(properties);
                ModifiableValueMap modifiableValueMap = settingsResource.adaptTo(ModifiableValueMap.class);
                modifiableValueMap.putAll(properties);
                resolver.commit();
                return true;
            } catch (PersistenceException e) {
                LOGGER.error("Unable to persist settings changes.", e);
            } finally {
                resolver.close();
            }
        }
        return false;
    }

    @Override
    public String getBlogName() {
        return PropertiesUtil.toString(settings.get(SystemSettingsService.SYSTEM_BLOG_NAME), PROP_DEFAULT_RPGM_BLOG_NAME);
    }

    @Override
    public boolean getExtensionlessUrls() {
        return PropertiesUtil
            .toBoolean(settings.get(SystemSettingsService.SYSTEM_EXTENSIONLESS_URLS), PROP_DEFAULT_RPGM_EXTENSIONLESS_URLS);
    }

    @Override
    public boolean setBlogName(final String name) {
        return setProperties(new HashMap<String, Object>() {{
            put(SYSTEM_BLOG_NAME, name);
        }});
    }

    @Override
    public boolean setExtensionlessUrls(final boolean value) {
        return setProperties(new HashMap<String, Object>() {{
            put(SYSTEM_EXTENSIONLESS_URLS, value);
        }});
    }

    @Activate
    protected void activate(ComponentContext componentContext) {
        ResourceResolver resolver = null;
        try {
            resolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
            Resource settingsResource = resolver.getResource(SETTINGS_PATH);
            if (settingsResource != null) {
                settings.clear();
                settings.putAll(settingsResource.adaptTo(ValueMap.class));
            }
        } catch (LoginException e) {
            LOGGER.error("Unable to read RPGM settings.", e);
        } finally {
            if (resolver != null) {
                resolver.close();
            }
        }
    }

    private Resource getConfigurationResource() {
        ResourceResolver resolver;
        Resource settingsResource = null;
        try {
            resolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
            settingsResource = resolver.getResource(SETTINGS_PATH);
        } catch (LoginException e) {
            LOGGER.error("Unable to read RPGM settings.", e);
        }
        return settingsResource;
    }

}