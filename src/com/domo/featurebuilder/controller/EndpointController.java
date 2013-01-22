package com.domo.featurebuilder.controller;

import com.domo.featurebuilder.helper.Helper;
import com.domo.featurebuilder.model.Endpoint;
import com.domo.featurebuilder.model.Scenario;
import com.domo.featurebuilder.model.Feature;
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
            Endpoint endpoint = createEndpoint(endpointToCreate);
            if (endpoint != null)
                endpointList.add(endpoint);
        }
        return endpointList;
    }

    private Endpoint createEndpoint(String[] endpointData) {
        boolean ignore = !endpointData[2].isEmpty();
        String path = endpointData[1];
        final String method = endpointData[0];

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

            String parentDirectory = Helper.outputDirectory + Helper.fileSep + endpointData[3];
            String featureName = endpointData[4];
            String crud = endpointData[5];

            List<Feature> features = createFeatures(parentDirectory, featureName, crud);

            return new Endpoint(requestBase, features);
        } catch (URISyntaxException ex) {
            System.err.println("Problem with the URI for endpoint: " + path);
            System.err.println(ex.getMessage());
            System.err.println(ex.getReason());
        }
        return null;
    }

    private List<Feature> createFeatures(String parentDirectory, String featureName, String crud) {

        List<Feature> features = new ArrayList<Feature>();
        
        if (crud.contains("C")) {
            List<Scenario> scenarios = createScenarios(featureName);
            features.add(new Feature("create_" + featureName, parentDirectory, scenarios, featureName));
        }
        if (crud.contains("R")) {
            List<Scenario> scenarios = createScenarios(featureName);
            features.add(new Feature("read_" + featureName, parentDirectory, scenarios, featureName));
        }
        if (crud.contains("U")) {
            List<Scenario> scenarios = createScenarios(featureName);
            features.add(new Feature("update_" + featureName, parentDirectory, scenarios, featureName));
        }
        if (crud.contains("D")) {
            List<Scenario> scenarios = createScenarios(featureName);
            features.add(new Feature("delete_" + featureName, parentDirectory, scenarios, featureName));
        }
        
        return features;
    }
    
    private List<Scenario> createScenarios(String featureName){
        List<Scenario> scenarios = new ArrayList<Scenario>();
        Scenario happyPath = new Scenario(featureName + " (happy path)");
        Scenario failPath = new Scenario(featureName + " (fail path)");
        scenarios.add(happyPath);
        scenarios.add(failPath);
        return scenarios;
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
            System.out.println("---------------------------------------------" + Helper.newline);
            System.out.println(endpoint);
            System.out.println(Helper.newline + Helper.newline);
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

    public void saveEndpoints(List<Endpoint> endpoints) {
        File outputDirectory = new File(Helper.outputDirectory);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdir();
        }
        for (Endpoint endpoint : endpoints) {
            endpoint.saveFeatures();
        }
    }
}
