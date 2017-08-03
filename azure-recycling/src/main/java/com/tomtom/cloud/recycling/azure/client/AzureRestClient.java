/**
 * Copyright (C) 2017, TomTom International BV (http://www.tomtom.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tomtom.cloud.recycling.azure.client;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.oauth2.OAuth2ClientSupport;


/**
 * Class used for executing azure cli commands over http.
 * When the java sdk will offer the possibility to perform this operation,
 * this class should be removed.
 */
public class AzureRestClient {

    private static final String GRANT_TYPE = "client_credentials";

    private static final String ACCESS_TOKEN_KEY = "access_token";

    private static final String GET_HEALTH_CHECK_URL = "https://management.azure.com/subscriptions" +
            "/%s/resourceGroups" +
            "/%s/providers/Microsoft.Network/applicationGateways" +
            "/%s/backendhealth";

    private final AzureRestConfig config;

    private final Client client;

    public AzureRestClient(final AzureRestConfig config) {
        this(config, ClientBuilder.newClient());
    }

    private AzureRestClient(final AzureRestConfig config, final Client client) {
        this.config = config;
        this.client = client;
    }

    public AzureRestClient authenticate() {
        MultivaluedMap formData = new MultivaluedHashMap();
        formData.add("client_id", config.getClientId());
        formData.add("client_secret", config.getClientSecret());
        formData.add("grant_type", GRANT_TYPE);
        formData.add("resource", config.getResource());
        Response response = client.target(config.getAuthority())
                .request()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .post(Entity.form(formData));
        Map<String, String> responseMap = response.readEntity(HashMap.class);
        Feature feature = OAuth2ClientSupport.feature(responseMap.get(ACCESS_TOKEN_KEY));
        return new AzureRestClient(config, client.register(feature));
    }

    public URI readHealthCheckLocation() {
        WebTarget webTarget = client.target(String.format(
                GET_HEALTH_CHECK_URL,
                config.getSubscriptionId(),
                config.getResourceGroupName(),
                config.getAppGatewayName()))
                .queryParam("api-version", config.getApiVersion());
        Response response = webTarget
                .request()
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .post(Entity.json(""));
        return response.getLocation();
    }

    public boolean areAllInstancesHealthy(final URI location) throws ExecutionException, InterruptedException {
        Invocation.Builder webTarget = client.target(location).request()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        Response response = webTarget.get();
        while (response.getStatus() == 202) {
            Thread.sleep(1000 * Integer.valueOf(response.getHeaderString(HttpHeaders.RETRY_AFTER)));
            response = webTarget.get();
        }
        Map map = response.readEntity(HashMap.class);

        long counter = ((List<Map>) map.get("backendAddressPools"))
                .stream()
                .filter(p -> ((List<Map>) p.get("backendHttpSettingsCollection"))
                        .stream()
                        .filter(c -> ((List<Map>) c.get("servers"))
                                .stream()
                                .filter(s -> !s.get("health").equals(config.getHealth())).count() != 0).count() != 0)
                .count();
        return counter == 0;
    }

}
