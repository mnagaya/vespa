// Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.hosted.controller.integration;

import com.yahoo.config.provision.ClusterSpec;
import com.yahoo.vespa.hosted.controller.api.identifiers.DeploymentId;
import com.yahoo.vespa.hosted.controller.api.integration.routing.RoutingEndpoint;
import com.yahoo.vespa.hosted.controller.api.integration.routing.RoutingGenerator;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Returns a default set of endpoints on every query if it has no mappings, or those added by the user, otherwise.
 *
 * @author bratseth
 * @author jonmv
 */
public class RoutingGeneratorMock implements RoutingGenerator {

    private final Map<DeploymentId, List<RoutingEndpoint>> routingTable = new ConcurrentHashMap<>();

    private static final List<RoutingEndpoint> defaultEndpoints =
            List.of(new RoutingEndpoint("http://old-endpoint.vespa.yahooapis.com:4080", "host1", false, "upstream3"),
                    new RoutingEndpoint("http://qrs-endpoint.vespa.yahooapis.com:4080", "host1", false, "upstream1"),
                    new RoutingEndpoint("http://feeding-endpoint.vespa.yahooapis.com:4080", "host2", false, "upstream2"),
                    new RoutingEndpoint("http://global-endpoint.vespa.yahooapis.com:4080", "host1", true, "upstream1"),
                    new RoutingEndpoint("http://alias-endpoint.vespa.yahooapis.com:4080", "host1", true, "upstream1"));

    @Override
    public List<RoutingEndpoint> endpoints(DeploymentId deployment) {
        return routingTable.isEmpty()
                ? defaultEndpoints
                : routingTable.getOrDefault(deployment, Collections.emptyList());
    }

    @Override
    public Map<ClusterSpec.Id, URI> clusterEndpoints(DeploymentId deployment) {
        return endpoints(deployment).stream()
                                    .limit(1)
                                    .collect(Collectors.toMap(__ -> ClusterSpec.Id.from("default"),
                                                              endpoint -> URI.create(endpoint.endpoint())));
    }

    public void putEndpoints(DeploymentId deployment, List<RoutingEndpoint> endpoints) {
        routingTable.put(deployment, endpoints);
    }

    public void removeEndpoints(DeploymentId deployment) {
        routingTable.remove(deployment);
    }

}
