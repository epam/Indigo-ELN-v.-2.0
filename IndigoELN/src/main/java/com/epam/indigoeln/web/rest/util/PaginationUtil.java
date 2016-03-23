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
 * Pagination uses the same principles as the <a href="https://developer.github.com/v3/#pagination">Github API</api>,
 * and follow <a href="http://tools.ietf.org/html/rfc5988">RFC 5988 (Link header)</a>.
 * </p>
 */
public class PaginationUtil {

    private static final String URI_PATTERN = "{0}?page={1,number,#}&size={2,number,#}";
    private static final String LINK_PATTERN = "<{0}>; rel=\"{1}\"";

    private PaginationUtil() {
    }

    public static HttpHeaders generatePaginationHttpHeaders(Page<?> page, String baseUrl)
            throws URISyntaxException {

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", Long.toString(page.getTotalElements()));
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

    private static String getLink(String url, String rel) throws URISyntaxException {
        return MessageFormat.format(LINK_PATTERN, url, rel);
    }

    private static String getURI(String baseUrl, int page, int size) throws URISyntaxException {
        return new URI(MessageFormat.format(URI_PATTERN, baseUrl, page, size)).toString();
    }
}
