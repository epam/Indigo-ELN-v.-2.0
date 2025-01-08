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
package com.epam.indigoeln.core.repository.signature;

import com.epam.indigoeln.config.signature.SignatureProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Repository
public class SignatureRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignatureRepository.class);
    private static final String USERNAME = "username";

    @Autowired
    private SignatureProperties signatureProperties;

    private final Object signatureSessionIdLock = new Object();
    private final Object csrfTokenLock = new Object();
    private volatile String signatureSessionId;
    private volatile String csrfToken;

    private RestTemplate restTemplate = new RestTemplate();

    public String getReasons() {
        return exchange(signatureProperties.getUrl() + "/api/getReasons", HttpMethod.GET, null,
                String.class, new HashMap<>()).getBody();
    }

    public String getStatuses() {
        return exchange(signatureProperties.getUrl() + "/api/getStatuses", HttpMethod.GET, null,
                String.class, new HashMap<>()).getBody();
    }

    public String getFinalStatus() {
        return exchange(signatureProperties.getUrl() + "/api/getFinalStatus", HttpMethod.GET, null,
                String.class, new HashMap<>()).getBody();
    }

    public String getSignatureTemplates(String username) {
        return exchange(signatureProperties.getUrl() + "/api/getTemplates?username={username}",
                HttpMethod.GET, null,
                String.class, Collections.singletonMap(USERNAME, username)).getBody();
    }

    public String uploadDocument(String username, String templateId, final String fileName, byte[] file) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(templateId)) {
            return StringUtils.EMPTY;
        }

        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add(USERNAME, username);
        map.add("templateId", templateId);
        ByteArrayResource fileResource = new ByteArrayResourceImpl(file, fileName);
        map.add("file", fileResource);

        return exchange(signatureProperties.getUrl() + "/api/uploadDocument", HttpMethod.POST, map,
                String.class, new HashMap<>()).getBody();
    }

    public String getDocumentInfo(String documentId) {
        if (StringUtils.isBlank(documentId)) {
            return StringUtils.EMPTY;
        }

        try {
            return exchange(signatureProperties.getUrl() + "/api/getDocumentInfo?id={id}", HttpMethod.GET,
                    null, String.class, Collections.singletonMap("id", documentId)).getBody();
        } catch (Exception e) {
            LOGGER.error("Couldn't get document info, document id = " + documentId, e);
            return StringUtils.EMPTY;
        }
    }

    public String getDocumentsInfo(Collection<String> documentIds) {
        if (CollectionUtils.isEmpty(documentIds)) {
            return org.apache.commons.lang3.StringUtils.EMPTY;
        }

        try {
            return exchange(signatureProperties.getUrl() + "/api/getDocumentsByIds", HttpMethod.POST,
                    Collections.singletonMap("documentsIds", documentIds), String.class, new HashMap<>()).getBody();
        } catch (Exception e) {
            LOGGER.error("Couldn't get documents info, document ids = " + documentIds.stream()
                    .reduce("", (s1, s2) -> s1 + ", " + s2), e);
            return StringUtils.EMPTY;
        }
    }

    public String getDocuments(String username) {
        if (StringUtils.isBlank(username)) {
            return StringUtils.EMPTY;
        }

        try {
            return exchange(signatureProperties.getUrl() + "/api/getDocuments?username={username}",
                    HttpMethod.GET, null,
                    String.class, Collections.singletonMap(USERNAME, username)).getBody();
        } catch (Exception e) {
            LOGGER.error("Couldn't get documents, username = " + username, e);
            return StringUtils.EMPTY;
        }
    }

    public byte[] downloadDocument(String documentId) {
        if (StringUtils.isBlank(documentId)) {
            return new byte[0];
        }

        try {
            return exchange(signatureProperties.getUrl() + "/api/downloadDocument?id={id}",
                    HttpMethod.GET, null,
                    byte[].class, Collections.singletonMap("id", documentId)).getBody();
        } catch (Exception e) {
            LOGGER.error("Couldn't download document, document id = " + documentId, e);
            return new byte[0];
        }
    }

    private <E> ResponseEntity<E> exchange(String url, HttpMethod method, Object body, Class<E> clazz,
                                           Map<String, Object> args) {
        try {
            return restTemplate.exchange(
                    url,
                    method,
                    new HttpEntity<>(body, headers()),
                    clazz,
                    args);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                Cookie cookie = login(signatureProperties.getUsername(), signatureProperties.getPassword());
                setSignatureSessionId(cookie.getSessionID());
                setCsrfToken(cookie.getCsrfToken());

                return restTemplate.exchange(
                        url,
                        method,
                        new HttpEntity<>(body, headers()),
                        clazz,
                        args);
            } else {
                LOGGER.warn("Error occurred while exchanging with signature service:"
                        + e.getResponseBodyAsString(), e);
                throw e;
            }
        }
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-CSRF-TOKEN", getCsrfToken());
        headers.add(HttpHeaders.COOKIE, "JSESSIONID=" + getSignatureSessionId());
        return headers;
    }

    private Cookie login(String username, String password) {
        LinkedMultiValueMap<String, String> o = new LinkedMultiValueMap<>();
        o.add(USERNAME, username);
        o.add("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(o, headers);

        ResponseEntity<Object> responseEntity = restTemplate
                .postForEntity(signatureProperties.getUrl() + "/login", request, Object.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            List<String> cookies = responseEntity.getHeaders().get(HttpHeaders.SET_COOKIE);
            String jsessionid = cookies.stream()
                    .filter(c -> c.contains("JSESSIONID"))
                    .flatMap(s -> Arrays.stream(s.split(";")))
                    .flatMap(s -> Arrays.stream(s.split(",")))
                    .findAny()
                    .map(s -> s.split("=")[1])
                    .orElse(null);

            String token = cookies.stream()
                    .filter(c -> c.contains("CSRF-TOKEN"))
                    .flatMap(s -> Arrays.stream(s.split(";")))
                    .flatMap(s -> Arrays.stream(s.split(",")))
                    .findAny()
                    .map(s -> s.split("=")[1])
                    .orElse(null);

            return new Cookie(jsessionid, token);
        }

        return new Cookie(null, null);
    }

    private String getSignatureSessionId() {
        return signatureSessionId;
    }

    private void setSignatureSessionId(String signatureSessionId) {
        synchronized (signatureSessionIdLock) {
            this.signatureSessionId = signatureSessionId;
        }
    }

    private String getCsrfToken() {
        return csrfToken;
    }

    private void setCsrfToken(String csrfToken) {
        synchronized (csrfTokenLock) {
            this.csrfToken = csrfToken;
        }
    }

    @EqualsAndHashCode
    private static class ByteArrayResourceImpl extends ByteArrayResource {
        private final String fileName;

        ByteArrayResourceImpl(byte[] byteArray, String fileName) {
            super(byteArray);
            this.fileName = fileName;
        }

        @Override
        public String getFilename() {
            return fileName;
        }
    }

    @Data
    private static class Cookie {
        private final String sessionID;
        private final String csrfToken;
    }
}
