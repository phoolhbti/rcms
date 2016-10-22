package com.rpgm.online.site;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;

import com.rpgm.online.sightly.WCMUse;


public class TitleLink extends WCMUse {

	public String title;

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
		links=getLink(resource);
	}

	
	 private List<HashMap<String, Object>> getLink(Resource resource){
		 List<HashMap<String, Object>> linkProperties = new ArrayList<HashMap<String, Object>>();
		 if (resource != null && resource.hasChildren()) {
				Iterator<Resource> iterator = resource.listChildren();
				
				while (iterator.hasNext()) {
					 HashMap<String, Object> linkProperty = new HashMap<String, Object>();
					Resource link = iterator.next();
					ValueMap properties = link.adaptTo(ValueMap.class);
					
					linkProperty.put("name",properties.get("name", String.class));
					linkProperties.add(linkProperty);
				}
				
			}
		 return linkProperties;
	 }
	public String getTitle() {
		return title;
	}

	

}