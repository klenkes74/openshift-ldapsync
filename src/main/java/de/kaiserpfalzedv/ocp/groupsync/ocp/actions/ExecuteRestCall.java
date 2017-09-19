/*
 *    Copyright 2017 Kaiserpfalz EDV-Service, Roland T. Lichti
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.kaiserpfalzedv.ocp.groupsync.ocp.actions;

import java.net.URI;
import java.util.Optional;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import de.kaiserpfalzedv.ocp.groupsync.ocp.providers.ServerCredentials;

import de.kaiserpfalzedv.ocp.groupsync.ExecuterException;
import de.kaiserpfalzedv.ocp.groupsync.ocp.providers.OcpToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @author klenkes {@literal <rlichti@kaiserpfalz-edv.de>}
 * @version 1.0.0
 * @since 2017-09-10
 */
@Service
public class ExecuteRestCall {
    private static final Logger LOG = LoggerFactory.getLogger(ExecuteRestCall.class);

    @Inject
    private RestTemplate sender;

    @Value("${ocpServer:-}")
    private String masterUrl;

    @Inject
    private OcpToken token;


    @PostConstruct
    public void init() {
        if ("-".equals(masterUrl)) {
            masterUrl = "https://"
                + System.getenv("OCP_MASTER_SERVICE_HOST");
        }
    }

    public Optional<String> get(final String localPart) {
        HttpEntity<String> request = generateRequest();
        String requestUrl = masterUrl + localPart;

        return doRequest(request, requestUrl, HttpMethod.GET);
    }

    private Optional<String> doRequest(final HttpEntity<String> request, final String requestUrl, final HttpMethod method) {
        try {
            LOG.debug("Connecting to: server={}, method={}", requestUrl, method);
            LOG.trace("Request: {}", request);
            String result = sender.exchange(requestUrl, method, request, String.class).getBody();

            LOG.debug("Response: {}", result);
            return Optional.ofNullable(result);
        } catch (RestClientException e) {
            LOG.error(e.getClass().getSimpleName() + " caught: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    public Optional<String> post(final String localPart, String body) {
        HttpEntity<String> request = generateRequest(body);
        String requestUrl = masterUrl + localPart;

        return doRequest(request, requestUrl, HttpMethod.POST);
    }

    public Optional<String> put(final String localPart, final String body) throws ExecuterException {
        HttpEntity<String> request = generateRequest(body);
        String requestUrl = masterUrl + localPart;

        return doRequest(request, requestUrl, HttpMethod.PUT);
    }


    public Optional<String> delete(final String localPart) throws ExecuterException {
        HttpEntity<String> request = generateRequest();
        String requestUrl = masterUrl + localPart;

        return doRequest(request, requestUrl, HttpMethod.DELETE);
    }

    private HttpEntity<String> generateRequest() {
        HttpHeaders headers = generateHeaders();
        return new HttpEntity<>(headers);
    }

    private HttpEntity<String> generateRequest(@NotNull final String body) {
        HttpHeaders headers = generateHeaders();
        return new HttpEntity<>(body, headers);
    }

    private HttpHeaders generateHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token.getToken());
        headers.add("Content-Type", "application/yaml");

        return headers;
    }
}
