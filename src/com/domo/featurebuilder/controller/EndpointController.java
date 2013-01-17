package com.domo.featurebuilder.controller;

import au.com.bytecode.opencsv.CSVReader;
import com.domo.featurebuilder.model.Endpoint;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
    /**
     * Setting the limit to less than 0 will effectively make no limit.
     */
    public final int limit = -1, offset = 15;

    private EndpointController() {
    }

    public static EndpointController getInstance() {
        if (instance == null)
            instance = new EndpointController();
        return instance;
    }

    /**
     * @param csvResourceLocation (expected to be a resource file within the package, not a file)
     * @return a list of the HttpRequestBases read from the CSV file
     * @throws IOException
     * @throws URISyntaxException
     */
    public List<Endpoint> readEndpointsFromCSVFile(String csvResourceLocation) throws IOException {
        CSVReader reader = new CSVReader(new InputStreamReader(getClass().getResourceAsStream(csvResourceLocation)));
        @SuppressWarnings("UnusedAssignment") // To skip the header
                String[] next = reader.readNext();
        int skipped = 0;
        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        int i = -1;
        while ((next = reader.readNext()) != null) {
            i++;
            if (limit > 0 && i < offset)
                continue;
            if ((limit + offset) < i && limit > 0)
                break;
            Endpoint endpoint = createEndpoint(next);
            if (endpoint == null) {
                skipped++;
                continue;
            }
            endpoints.add(endpoint);

        }
        System.out.println("Total Endpoints Skipped: " + skipped);
        System.out.println("Total Endpoints: " + endpoints.size());
        return endpoints;
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
