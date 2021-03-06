// Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.config.server.session;

import com.yahoo.component.Version;
import com.yahoo.config.provision.ApplicationId;
import com.yahoo.config.provision.Rotation;
import com.yahoo.config.provision.TenantName;
import com.yahoo.container.jdisc.HttpRequest;
import com.yahoo.config.model.api.ContainerEndpoint;
import org.junit.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author hmusum
 */
public class PrepareParamsTest {

    private static final String rotation = "rotation-042.vespa.a02.yahoodns.net";
    private static final String vespaVersion = "6.37.49";
    private static final String request = "http://foo:19071/application/v2/tenant/foo/application/bar?" +
                                          PrepareParams.DRY_RUN_PARAM_NAME + "=true&" +
                                          PrepareParams.VERBOSE_PARAM_NAME+ "=true&" +
                                          PrepareParams.IGNORE_VALIDATION_PARAM_NAME + "=false&" +
                                          PrepareParams.APPLICATION_NAME_PARAM_NAME + "=baz&" +
                                          PrepareParams.VESPA_VERSION_PARAM_NAME + "=" + vespaVersion;

    @Test
    public void testCorrectParsing() {
        PrepareParams prepareParams = createParams("http://foo:19071/application/v2/", TenantName.defaultName());

        assertThat(prepareParams.getApplicationId(), is(ApplicationId.defaultId()));
        assertFalse(prepareParams.isDryRun());
        assertFalse(prepareParams.isVerbose());
        assertFalse(prepareParams.ignoreValidationErrors());
        assertThat(prepareParams.vespaVersion(), is(Optional.<String>empty()));
        assertTrue(prepareParams.getTimeoutBudget().hasTimeLeft());
        assertThat(prepareParams.rotations().size(), is(0));
    }
   
    @Test
    public void testCorrectParsingWithRotation() {
        PrepareParams prepareParams = createParams(request + "&" +
                                                   PrepareParams.ROTATIONS_PARAM_NAME + "=" + rotation,
                                                   TenantName.from("foo"));

        assertThat(prepareParams.getApplicationId().serializedForm(), is("foo:baz:default"));
        assertTrue(prepareParams.isDryRun());
        assertTrue(prepareParams.isVerbose());
        assertFalse(prepareParams.ignoreValidationErrors());
        Version expectedVersion = Version.fromString(vespaVersion);
        assertThat(prepareParams.vespaVersion().get(), is(expectedVersion));
        assertTrue(prepareParams.getTimeoutBudget().hasTimeLeft());
        Set<Rotation> rotations = prepareParams.rotations();
        assertThat(rotations.size(), is(1));
        assertThat(rotations, contains(equalTo(new Rotation(rotation))));
    }

    @Test
    public void testCorrectParsingWithSeveralRotations() {
        String rotationTwo = "rotation-043.vespa.a02.yahoodns.net";
        String twoRotations = rotation + "," + rotationTwo;
        PrepareParams prepareParams = createParams(request + "&" +
                                      PrepareParams.ROTATIONS_PARAM_NAME + "=" + twoRotations,
                                      TenantName.from("foo"));
        Set<Rotation> rotations = prepareParams.rotations();
        assertThat(rotations, containsInAnyOrder(new Rotation(rotation), new Rotation(rotationTwo)));
    }

    @Test
    public void testCorrectParsingWithContainerEndpoints() {
        var endpoints = List.of(new ContainerEndpoint("qrs1",
                                                      List.of("c1.example.com",
                                                              "c2.example.com")),
                                new ContainerEndpoint("qrs2",
                                                      List.of("c3.example.com",
                                                              "c4.example.com")));
        var param = "[\n" +
                    "  {\n" +
                    "    \"clusterId\": \"qrs1\",\n" +
                    "    \"names\": [\"c1.example.com\", \"c2.example.com\"]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"clusterId\": \"qrs2\",\n" +
                    "    \"names\": [\"c3.example.com\", \"c4.example.com\"]\n" +
                    "  }\n" +
                    "]";

        var encoded = URLEncoder.encode(param, StandardCharsets.UTF_8);
        var prepareParams = createParams(request + "&" + PrepareParams.CONTAINER_ENDPOINTS_PARAM_NAME +
                                                   "=" + encoded, TenantName.from("foo"));
        assertEquals(endpoints, prepareParams.containerEndpoints());
    }

    // Create PrepareParams from a request (based on uri and tenant name)
    private static PrepareParams createParams(String uri, TenantName tenantName) {
        return PrepareParams.fromHttpRequest(
                HttpRequest.createTestRequest(uri, com.yahoo.jdisc.http.HttpRequest.Method.PUT),
                tenantName,
                Duration.ofSeconds(60));
    }

}
