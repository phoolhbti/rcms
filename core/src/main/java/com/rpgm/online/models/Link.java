package com.rpgm.online.models;

import javax.inject.Inject;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;

@Model(adaptables=Resource.class)
public class Link {
    @Inject
    public String name;

    @Inject
    public String link;
}