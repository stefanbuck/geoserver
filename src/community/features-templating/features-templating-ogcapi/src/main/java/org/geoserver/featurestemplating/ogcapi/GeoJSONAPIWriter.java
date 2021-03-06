/* (c) 2021 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.featurestemplating.ogcapi;

import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import org.geoserver.featurestemplating.writers.GeoJSONWriter;
import org.geoserver.ogcapi.APIRequestInfo;
import org.geoserver.ogcapi.Link;
import org.geoserver.ogcapi.features.CollectionDocument;
import org.geoserver.ogcapi.features.FeaturesResponse;
import org.geoserver.ows.URLMangler;
import org.geoserver.ows.util.ResponseUtils;
import org.springframework.http.MediaType;

public class GeoJSONAPIWriter extends GeoJSONWriter {

    public GeoJSONAPIWriter(JsonGenerator generator) {
        super(generator);
    }

    public void writeLinks(
            String previous, String next, String prefixedName, String featureId, String mimeType)
            throws IOException {
        APIRequestInfo requestInfo = APIRequestInfo.get();
        writeElementName("links");
        startArray();
        // paging links
        if (previous != null) {
            writeLink("Previous page", mimeType, "prev", previous);
        }
        if (next != null) {
            writeLink("Next page", mimeType, "next", next);
        }
        // alternate/self links
        String basePath = "ogc/features/collections/" + ResponseUtils.urlEncode(prefixedName);
        Collection<MediaType> formats =
                requestInfo.getProducibleMediaTypes(FeaturesResponse.class, true);
        for (MediaType format : formats) {
            String path = basePath + "/items";
            if (featureId != null) {
                path += "/" + ResponseUtils.urlEncode(featureId);
            }
            String href =
                    ResponseUtils.buildURL(
                            requestInfo.getBaseURL(),
                            path,
                            Collections.singletonMap("f", format.toString()),
                            URLMangler.URLType.SERVICE);
            String linkType = Link.REL_ALTERNATE;
            String linkTitle = "This document as " + format;
            if (format.toString().equals(mimeType)) {
                linkType = Link.REL_SELF;
                linkTitle = "This document";
            }
            writeLink(linkTitle, format.toString(), linkType, href);
        }
        // backpointer to the collection
        for (MediaType format :
                requestInfo.getProducibleMediaTypes(CollectionDocument.class, true)) {
            String href =
                    ResponseUtils.buildURL(
                            requestInfo.getBaseURL(),
                            basePath,
                            Collections.singletonMap("f", format.toString()),
                            URLMangler.URLType.SERVICE);
            String linkType = Link.REL_COLLECTION;
            String linkTitle = "The collection description as " + format;
            writeLink(linkTitle, format.toString(), linkType, href);
        }
        endArray();
    }
}
