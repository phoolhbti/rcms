package com.rpgm.online.site;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import com.rpgm.online.sightly.WCMUse;

@Model(adaptables = Resource.class)
public class TitleText extends WCMUse {

	public String title;
	public String description;
	
	private Resource resource;

	@Override
	public void activate() {
		resource = getResource();
		getCopyRight(resource);
	}

	private void getCopyRight(Resource copynode) {
		copynode=copynode.getChild("jcr:content/mainContent/footer/4column/column1");
		ValueMap properties = copynode.adaptTo(ValueMap.class);
		title = properties.get("jcr:title", String.class);
		description = properties.get("jcr:description", String.class);		
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	
}