package com.rpgm.online.impl;

import java.security.Principal;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.security.Privilege;

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rpgm.online.RPGMConstants;

/**
 * Setup application by creating user groups and setting privileges.
 * This class runs on activation of the core bundle.
 */
public class Activator implements BundleActivator {

    /**
     * Logger instance to log and debug errors.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    /**
     * Display Name property for user groups.
     */
    private static final String GROUP_DISPLAY_NAME = "displayName";


    @Override
    public void start(BundleContext bundleContext) throws Exception {
        LOGGER.info(bundleContext.getBundle().getSymbolicName() + " started");
        ServiceReference resourceResolverFactoryReference = null;
        ResourceResolver resolver = null;
        try {
            resourceResolverFactoryReference = bundleContext.getServiceReference(ResourceResolverFactory.class.getName());
            ResourceResolverFactory resolverFactory = (ResourceResolverFactory) bundleContext.getService(resourceResolverFactoryReference);

            if (resolverFactory != null) {
                resolver = resolverFactory.getAdministrativeResourceResolver(null);
                createGroups(resolver);
                setPermissions(resolver);
            }
        } catch (LoginException e) {
            LOGGER.error("Could not login to repository", e);
        } finally {
            if (resolver != null && resolver.isLive()) {
                resolver.close();
            }
            if (resourceResolverFactoryReference != null) {
                bundleContext.ungetService(resourceResolverFactoryReference);
            }
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        LOGGER.info(bundleContext.getBundle().getSymbolicName() + " stopped");
    }

    private void setPermissions(ResourceResolver resolver) {
        JackrabbitSession session = (JackrabbitSession) resolver.adaptTo(Session.class);
        if (session != null) {
            try {
                Principal everyonePrincipal = session.getPrincipalManager().getEveryone();

                AccessControlUtils.clear(session, RPGMConstants.ADMIN_PATH);
                AccessControlUtils.denyAllToEveryone(session, RPGMConstants.ADMIN_PATH);
                AccessControlUtils.allow(session.getNode(RPGMConstants.ADMIN_PATH), RPGMConstants.GROUP_ID_AUTHORS, Privilege
                    .JCR_ALL);


                AccessControlUtils.clear(session, RPGMConstants.BLOG_PATH);
                AccessControlUtils.denyAllToEveryone(session, RPGMConstants.BLOG_PATH);
                Node blogPath = session.getNode(RPGMConstants.BLOG_PATH);
                AccessControlUtils.allow(blogPath, everyonePrincipal.getName(), Privilege.JCR_READ);
                AccessControlUtils.allow(blogPath, RPGMConstants.GROUP_ID_AUTHORS, Privilege.JCR_ALL);

                AccessControlUtils.clear(session, RPGMConstants.ASSET_PATH);
                AccessControlUtils.denyAllToEveryone(session, RPGMConstants.ASSET_PATH);
                Node assetPath = session.getNode(RPGMConstants.ASSET_PATH);
                AccessControlUtils.allow(assetPath, everyonePrincipal.getName(), Privilege.JCR_READ);
                AccessControlUtils.allow(assetPath, RPGMConstants.GROUP_ID_AUTHORS, Privilege.JCR_ALL);

                session.save();
            } catch (Exception e) {
                LOGGER.error("Unable to modify ACLs.", e);
            }
        }
    }

    private void createGroups(ResourceResolver resolver) {
        try {
            Session session = resolver.adaptTo(Session.class);

            if (session != null && session instanceof JackrabbitSession) {
                UserManager userManager = ((JackrabbitSession) session).getUserManager();
                ValueFactory valueFactory = session.getValueFactory();

                Authorizable authors = userManager.getAuthorizable(RPGMConstants.GROUP_ID_AUTHORS);

                if (authors == null) {
                    authors = userManager.createGroup(RPGMConstants.GROUP_ID_AUTHORS);
                    authors.setProperty(GROUP_DISPLAY_NAME, valueFactory.createValue(RPGMConstants.GROUP_DISPLAY_AUTHORS));
                }

                Authorizable testers = userManager.getAuthorizable(RPGMConstants.GROUP_ID_TESTERS);

                if (testers == null) {
                    testers = userManager.createGroup(RPGMConstants.GROUP_ID_TESTERS);
                    testers.setProperty(GROUP_DISPLAY_NAME, valueFactory.createValue(RPGMConstants.GROUP_DISPLAY_TESTERS));
                }

                session.save();
            }
        } catch (RepositoryException e) {
            LOGGER.error("Could not get session", e);
        }
    }
}