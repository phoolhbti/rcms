package com.rpgm.online.site;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import com.rpgm.online.sightly.WCMUse;

public class TitleLink extends WCMUse {

	public String title;
	public boolean social;

	// public List<>
	private Resource resource;

	private List<HashMap<String, Object>> links;

	public List<HashMap<String, Object>> getLinks() {
		return links;
	}

	@Override
	public void activate() {
		resource = getResource();
		ValueMap properties = resource.adaptTo(ValueMap.class);
		title = properties.get("title", String.class);
		if(properties.containsKey("social")){
			social=true;
		}else {
			social=false;
		}
		links = getLink(resource);
	}

	private List<HashMap<String, Object>> getLink(Resource resource) {
		List<HashMap<String, Object>> linkProperties = new ArrayList<HashMap<String, Object>>();
		if (resource != null && resource.hasChildren()) {
			Iterator<Resource> iterator = resource.listChildren();

			while (iterator.hasNext()) {
				HashMap<String, Object> linkProperty = new HashMap<String, Object>();
				Resource link = iterator.next();
				ValueMap properties = link.adaptTo(ValueMap.class);

				linkProperty.put("name", properties.get("name", String.class));
				linkProperty.put("url", properties.get("url", String.class));
				linkProperty.put("blank", properties.get("blank", String.class));
				if(social){
					linkProperty.put("cssClass", properties.get("cssClass", String.class));
					linkProperty.put("dataPlacement", properties.get("dataPlacement", String.class));
					linkProperty.put("socialCssClass", properties.get("socialCssClass", String.class));
				}
				linkProperties.add(linkProperty);
			}

		}
		return linkProperties;
	}

	public String getTitle() {
		return title;
	}
	public boolean getSocial(){
		return social;
	}
}