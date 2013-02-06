package com.domo.featurebuilder.controller;

import com.domo.featurebuilder.helper.Helper;
import com.domo.featurebuilder.model.Endpoint;
import com.domo.featurebuilder.model.Scenario;
import com.domo.featurebuilder.model.Feature;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
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
        String path = endpointData[0];
        final String method = endpointData[1];
        String urlToUse = endpointData[3];
        if (urlToUse == null || urlToUse.isEmpty()) {
            urlToUse = path;
        }
        final String requestContent = endpointData[4];
        String[] keyValuePairs = endpointData[6].split("&");
        String[][] params = new String[keyValuePairs.length][2];
        for (int i = 0; i < keyValuePairs.length; i++) {
            String keyValue = keyValuePairs[i];
            String[] kv = keyValue.split("=");
            params[i] = kv;
        }

        if (ignore || path.contains("{") || !methodsToTest.contains(method))
            return null;

        HttpEntityEnclosingRequestBase requestBase = new HttpEntityEnclosingRequestBase() {
            @Override
            public String getMethod() {
                return method;
            }

            @Override
            public HttpEntity getEntity() {
                try{
                    return new StringEntity(requestContent, ContentType.APPLICATION_JSON);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            
        };
        try {

            if(method.equals(Helper.METHOD_POST) || method.equals(Helper.METHOD_PUT) || method.equals(Helper.METHOD_PATCH)){
                requestBase.setURI(HttpController.getInstance().buildURI(urlToUse, params));
                System.out.println(requestBase.getURI());
            }
            else
                requestBase.setURI(HttpController.getInstance().buildURI(urlToUse));
            String parentDirectory;
            String featureName;

            if(!endpointData[5].isEmpty()) {
                String [] pathValues = endpointData[5].split("/");
                System.out.println("Paths >>> "+pathValues[1] + " > " + pathValues[2]);

                parentDirectory = Helper.outputDirectory + Helper.fileSep + pathValues[1];
                featureName = pathValues[2];
            }else{
                return null;
            }



            List<Feature> features = createFeatures(parentDirectory, featureName);

            return new Endpoint(requestBase, features);
        } catch (URISyntaxException ex) {
            System.err.println("Problem with the URI for endpoint: " + path);
            System.err.println(ex.getMessage());
            System.err.println(ex.getReason());
        }
        return null;
    }

    private List<Feature> createFeatures(String parentDirectory, String featureName) {

        List<Feature> features = new ArrayList<Feature>();
        List<Scenario> scenarios = createScenarios(featureName, true);
        features.add(new Feature(featureName, parentDirectory, scenarios, featureName));
        
        return features;
    }
    
    private List<Scenario> createScenarios(String featureName, boolean makeFailPath){
        List<Scenario> scenarios = new ArrayList<Scenario>();
        scenarios.add(createHappyPathScenario(featureName));
        if (makeFailPath)
            scenarios.add(createFailPathScenario(featureName));
        return scenarios;
    }

    private Scenario createHappyPathScenario(String featureName) {
        return new Scenario(featureName + " (happy path)", true);
    }
    private Scenario createFailPathScenario(String featureName) {
        return new Scenario(featureName + " (fail path)", false);
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
