package com.kentcdodds.featurebuilder.controller;

import com.kentcdodds.featurebuilder.endpoints.Endpoint;
import com.kentcdodds.featurebuilder.endpoints.Feature;
import java.util.List;

/**
 *
 * @author kentdodds
 */
public class Main {

  public static final String newline = System.getProperty("line.separator");
  private static final String endpointsCSVLocation = "/com/kentcdodds/featurebuilder/resources/domoWebEndpoints.csv";
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
    HttpController.getInstance().executeGetOnClient("/domoweb/auth/signin",
            new String[]{"username", domoUsername},
            new String[]{"password", domoPassword});
    System.out.println("Signin successful");
  }

  private static void buildFeatures() throws Exception {
    List<Endpoint> endpoints = EndpointController.getInstance().readEndpointsFromCSVFile(endpointsCSVLocation);
    EndpointController.getInstance().runEndpoints(endpoints);

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
    HttpController.getInstance().executeGetOnClient("/domoweb/auth/signout");
    System.out.println("Sign Out successful");
  }

}
