package com.domo.featurebuilder.controller;

import com.domo.featurebuilder.model.Endpoint;
import com.domo.featurebuilder.model.Feature;
import java.util.List;
import org.apache.http.HttpResponse;

public class Main {

  public static final String newline = System.getProperty("line.separator");
  private static final String endpointsCSVLocation = "/com/domo/featurebuilder/resources/domoWebEndpoints.csv";
  private static final String domoUsername = "qa6.tester@domosoftware.net";
  private static final String domoPassword = "enduserPassword";

  public static void main(String[] args) throws Exception {
    startup();
    buildFeatures();
    exit();
  }

  private static void startup() throws Exception {
    try {
      signin();
    } catch (Exception ex) {
      throw new Exception("Failed signing in.", ex);
    }
  }

  public static void signin() throws Exception {
    HttpResponse response = HttpController.getInstance().executePostOnClient("/domoweb/auth/signin",
            new String[]{"username", domoUsername},
            new String[]{"password", domoPassword});
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode != 302)
      throw new Exception("Status code != 302 >>> " + statusCode);
    HttpController.getInstance().consumeResponse(response);
    System.out.println("Signin successful");
  }

  private static void buildFeatures() throws Exception {
    List<Endpoint> endpoints = EndpointController.getInstance().readEndpointsFromCSVFile(endpointsCSVLocation);
    EndpointController.getInstance().runEndpoints(endpoints);
    EndpointController.getInstance().printEndpoints(endpoints);
    List<Feature> features = FeatureController.getInstance().createFeatures(endpoints);
    TemplateController.getInstance().generateEndpointFeatures(features);
    FeatureController.getInstance().printFeatures(features);
  }

  private static void exit() throws Exception {
    try {
      signout();
    } catch (Exception ex) {
      throw new Exception("Failed signing out", ex);
    }
  }

  public static void signout() throws Exception {
    HttpResponse response = HttpController.getInstance().executeGetOnClient("/domoweb/auth/signout");
    HttpController.getInstance().consumeResponse(response);
    System.out.println("Sign Out successful");
  }
}
