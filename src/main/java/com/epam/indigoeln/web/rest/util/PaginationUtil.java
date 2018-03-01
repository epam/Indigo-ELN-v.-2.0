/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.web.rest.util;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

/**
 * Utility class for handling pagination.
 * <p>
 * <p>
 * Pagination uses the same principles as the <a href="https://developer.github.com/v3/#pagination">Github API</a>,
 * and follow <a href="http://tools.ietf.org/html/rfc5988">RFC 5988 (Link header)</a>.
 * </p>
 */
public final class PaginationUtil {

    private static final String URI_PATTERN = "{0}?page={1,number,#}&size={2,number,#}";
    private static final String LINK_PATTERN = "<{0}>; rel=\"{1}\"";

    private PaginationUtil() {
    }

    public static HttpHeaders generatePaginationHttpHeaders(Page<?> page, String baseUrl)
            throws URISyntaxException {

        HttpHeaders headers = new HttpHeaders();
        headers.add(HeaderUtil.TOTAL_COUNT, Long.toString(page.getTotalElements()));
        StringBuilder link = new StringBuilder();
        if ((page.getNumber() + 1) < page.getTotalPages()) {
            link.append(getLink(getURI(baseUrl, page.getNumber() + 1, page.getSize()), "next")).append(",");
        }
        // prev link
        if ((page.getNumber()) > 0) {
            link.append(getLink(getURI(baseUrl, page.getNumber() - 1, page.getSize()), "prev")).append(",");
        }
        // last and first link
        int lastPage = 0;
        if (page.getTotalPages() > 0) {
            lastPage = page.getTotalPages() - 1;
        }
        link.append(getLink(getURI(baseUrl, lastPage, page.getSize()), "last")).append(",")
                .append(getLink(getURI(baseUrl, 0, page.getSize()), "first"));
        headers.add(HttpHeaders.LINK, link.toString());
        return headers;
    }

    private static String getLink(String url, String rel) {
        return MessageFormat.format(LINK_PATTERN, url, rel);
    }

    private static String getURI(String baseUrl, int page, int size) throws URISyntaxException {
        return new URI(MessageFormat.format(URI_PATTERN, baseUrl, page, size)).toString();
    }
}
