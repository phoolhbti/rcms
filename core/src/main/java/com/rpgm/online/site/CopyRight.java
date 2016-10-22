package com.rpgm.online.site;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import com.rpgm.online.sightly.WCMUse;

@Model(adaptables = Resource.class)
public class CopyRight extends WCMUse {

	public String name;

	public String url;

	public String copyrightText;

	public String initalText;
	private Resource resource;

	@Override
	public void activate() {
		resource = getResource();
		getCopyRight(resource);
	}

	private void getCopyRight(Resource copynode) {
		copynode=copynode.getChild("jcr:content/mainContent/footer/copyright");
		ValueMap properties = copynode.adaptTo(ValueMap.class);
		name = properties.get("name", String.class);
		url = properties.get("url", String.class);
		copyrightText = properties.get("copyrightText", String.class);
		initalText = properties.get("initalText", String.class);
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public String getCopyrightText() {
		return copyrightText;
	}

	public String getInitalText() {
		return initalText;
	}
}