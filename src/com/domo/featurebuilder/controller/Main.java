package com.domo.featurebuilder.controller;

import com.domo.featurebuilder.helper.CSVHandler;
import com.domo.featurebuilder.model.Endpoint;
import com.domo.featurebuilder.model.Feature;
import org.apache.http.HttpResponse;

import java.util.List;

public class Main {


    private static final String endpointsCSVLocation = "/com/domo/featurebuilder/resources/endpointSubset.csv";
    //private static final String endpointsCSVLocation = "/com/domo/featurebuilder/resources/domoWebEndpoints.csv";
    private static final String domoUsername = "qa6.tester@domosoftware.net";
    private static final String domoPassword = "enduserPassword";

    public static void main(String[] args) throws Exception {
        Main m = new Main();
        m.startup();
        m.buildFeatures();
        m.exit();
    }

    private void startup() throws Exception {
        try {
            signin();
        } catch (Exception ex) {
            throw new Exception("Failed signing in.", ex);
        }
    }

    private void signin() throws Exception {
        HttpResponse response = HttpController.getInstance().executePostOnClient("/domoweb/auth/signin",
                new String[]{"username", domoUsername},
                new String[]{"password", domoPassword});
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 302)
            throw new Exception("Status code != 302 >>> " + statusCode);
        HttpController.getInstance().consumeResponse(response);
        System.out.println("Signin successful");
    }

    private void buildFeatures() throws Exception {
        List<String[]> csvData = CSVHandler.getInstance().readEndpointsFromCSVFile(endpointsCSVLocation);
        List<Endpoint> endpoints = EndpointController.getInstance().createEndpointsFromCSVData(csvData);
        EndpointController.getInstance().runEndpoints(endpoints);
        TemplateController.getInstance().generateFeatureFileText(features);
        FeatureController.getInstance().printFeatures(features);
        FeatureController.getInstance().saveFeaturesToOutputDirectory(features);
    }

    private void exit() throws Exception {
        try {
            signout();
        } catch (Exception ex) {
            throw new Exception("Failed signing out", ex);
        }
    }

    private void signout() throws Exception {
        HttpResponse response = HttpController.getInstance().executeGetOnClient("/domoweb/auth/signout");
        HttpController.getInstance().consumeResponse(response);
        System.out.println("Sign Out successful");
    }
}
