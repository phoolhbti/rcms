package com.rpgm.online.impl.servlets;

import com.rpgm.online.RPGMConstants;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import javax.servlet.ServletException;

import java.io.IOException;

/**
 * Get servlet to return the Asset List component HTML markup which
 * allows the user to upload assets such as images and navigate folders.
 */
@SlingServlet(paths = RPGMConstants.SERVLET_PATH_ADMIN + "/getassetlist")
public class GetAssetListServlet extends SlingAllMethodsServlet {

    /**
     * The path to the asset list component which allows the
     * pickup and upload of assets such as images.
     */
    private static final String ASSET_LIST_COMPONENT_PATH = "/apps/rpgm/components/admin/assetList/assetList.html";

    /**
     * Return AssetList component HTML in the response.
     *
     * @param request The Sling HTTP servlet request.
     * @param response The Sling HTTP servlet response.
     */
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        Resource resource = request.getResourceResolver().getResource(ASSET_LIST_COMPONENT_PATH);
        request.getRequestDispatcher(resource).include(request, response);
    }
}