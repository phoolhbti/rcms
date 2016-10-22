package com.rpgm.online.site;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;

@Model(adaptables=Resource.class)
public class Footer {
    // Our 3 columns and exposed list of columns
    @Inject @Optional
    private Resource column1;

    @Inject @Optional
    private Resource column2;

    @Inject @Optional
    private Resource column3;

   /* @Inject @Optional
    private Resource column4;
   */ 
    public List<FooterColumn> columns;

    // Our copyright text
    @Inject
    public String copyright;

    // Our list of social media links
    @Inject @Optional
    private Resource social;
    
    public List<Link> socialLinks;

    @PostConstruct
    protected void init() throws Exception {
        columns = new ArrayList<FooterColumn>();

        if (column1 != null)
            columns.add(column1.adaptTo(FooterColumn.class));

        if (column2 != null)
            columns.add(column2.adaptTo(FooterColumn.class));

        if (column3 != null)
            columns.add(column3.adaptTo(FooterColumn.class));
        /*if (column4 != null)
            columns.add(column4.adaptTo(FooterColumn.class));*/
        /*if(social != null) {
            socialColumn = social.adaptTo(FooterColumn.class);
            if(socialColumn != null)
                socialLinks = socialColumn.links;
        }*/
    }   
}