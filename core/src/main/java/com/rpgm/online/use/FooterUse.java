package com.rpgm.online.use;

import org.apache.jackrabbit.spi.commons.name.NameConstants;
import org.apache.sling.api.resource.Resource;

import com.rpgm.online.sightly.WCMUse;
import com.rpgm.online.site.Footer;

public class FooterUse extends WCMUse{

	 public static final String HOME_PAGE_RESOURCE_PATH = "/apps/acme/templates/home";

	    public Footer footer;

	    /*@Override
	    public void activate() {
	        Page homePage = getHomePage(getCurrentPage());
	        if(homePage != null) {
	            Resource footerResource = homePage.getContentResource("footer");
	            if(footerResource != null) {
	                footer = footerResource.adaptTo(Footer.class);
	            }
	        }
	    }

	    private Page getHomePage(Page current) {
	        try {
	            while (current != null) {
	                String pageTemplate = current.getProperties()
	                        .get(NameConstants.PN_TEMPLATE, "");
	                if (HOME_PAGE_TEMPLATE_PATH.equals(pageTemplate)) {
	                    return current;
	                }
	                // else keep going up
	                current = current.getParent();
	            }
	        } catch(Exception e) {
	            // log the error
	        }

	        return null;
	    }*/
}
