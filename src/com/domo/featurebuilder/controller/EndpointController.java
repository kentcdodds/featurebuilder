package com.domo.featurebuilder.controller;

import com.domo.featurebuilder.model.Endpoint;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EndpointController {

    private static EndpointController instance;
    public final String methodsToTest = ""
            + "PUT"
            + "POST"
            + "DELETE"
            + "GET"
            + ""; //Just comment out the line that you don't want to test.

    private EndpointController() {
    }

    public static EndpointController getInstance() {
        if (instance == null)
            instance = new EndpointController();
        return instance;
    }

    public List<Endpoint> createEndpointsFromCSVData(List<String[]> endpointsToCreate) {
        List<Endpoint> endpointList = new ArrayList<Endpoint>();
        for (String[] endpointToCreate : endpointsToCreate) {
            endpointList.add(createEndpoint(endpointToCreate));
        }
        return endpointList;
    }

    private Endpoint createEndpoint(String[] next) {
        final String method = next[0].toUpperCase();
        String path = next[1];
        boolean ignore = !next[2].isEmpty();

        if (ignore || path.contains("{") || !methodsToTest.contains(method))
            return null;

        HttpRequestBase requestBase = new HttpRequestBase() {
            @Override
            public String getMethod() {
                return method;
            }
        };
        try {
            requestBase.setURI(HttpController.getInstance().buildURI(path));
            return new Endpoint(requestBase);
        } catch (URISyntaxException ex) {
            System.err.println("Problem with the URI for endpoint: " + path);
            System.err.println(ex.getMessage());
            System.err.println(ex.getReason());
        }
        return null;
    }

    public void runEndpoints(List<Endpoint> endpoints) {
        Map<Integer, Integer> statusCodeCount = new HashMap<Integer, Integer>();
        for (int i = 0; i < endpoints.size(); i++) {
//noinspection PointlessBooleanExpression, for readability
            Endpoint endpoint = endpoints.get(i);
            try {
                endpoint.processEndpoint();
                addResponseCodeToCount(endpoint, statusCodeCount);
            } catch (IOException ex) {
                Logger.getLogger(EndpointController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NullPointerException nex) {
                nex.printStackTrace();
            }

        }
        System.out.println("Status Code Count: " + statusCodeCount);
    }

    private void addResponseCodeToCount(Endpoint endpoint, Map<Integer, Integer> statusCodeCount) {
        int statusCode = endpoint.getResponseCode();
        Integer count = statusCodeCount.get(statusCode);
        if (count == null)
            statusCodeCount.put(statusCode, 1);
        else
            statusCodeCount.put(statusCode, count + 1);
    }

    /**
     * For testing purposes.
     *
     * @param endpoints
     */
    public void printEndpoints(List<Endpoint> endpoints) {
        for (Endpoint endpoint : endpoints) {
            System.out.println("---------------------------------------------" + Main.newline);
            System.out.println(endpoint);
            System.out.println(Main.newline + Main.newline);
        }
    }

    public void printEndpointsToCSV(List<Endpoint> endpoints, File destiation) {

    }

    public void printAllJsonReturningEndpoints(List<Endpoint> endpoints) {
        for (Endpoint endpoint : endpoints) {
            if (endpoint.contentTypeIsJson())
                System.out.println(endpoint.getRequestMethod() + "," + endpoint.getRequestPath());
        }
    }
}
