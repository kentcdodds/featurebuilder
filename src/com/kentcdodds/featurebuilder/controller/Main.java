package com.kentcdodds.featurebuilder.controller;

import com.kentcdodds.featurebuilder.endpoints.Endpoint;
import com.kentcdodds.featurebuilder.endpoints.Feature;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

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
    processEndpoints();
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
    URI uri = EndpointController.getInstance().buildURI("/domoweb/auth/signin",
            new String[]{"username", domoUsername},
            new String[]{"password", domoPassword});
    executeGet(uri);
    System.out.println("Signin successful");
  }

  private static void processEndpoints() throws Exception {
    List<Endpoint> endpoints = EndpointController.getInstance().readEndpointsFromCSVFile(endpointsCSVLocation);
    EndpointController.getInstance().runEndpoints(endpoints);

    List<Feature> features = FeatureController.getInstance().createFeatures(endpoints);
    TemplateController.getInstance().generateEndpointFeatures(features);
    FeatureController.getInstance().printFeatures(features);
 }


  /**
   * For testing purposes.
   * @param endpoints 
   */
  private static void printEndpoints(List<Endpoint> endpoints) {
    for (Endpoint endpoint : endpoints) {
      System.out.println("---------------------------------------------" + newline);
      System.out.println(endpoint);
      System.out.println(newline + newline);
    }
  }
  
  private static void exit() throws Exception {
    try {
      signout();
    } catch (Exception ex) {
      throw new Exception("Failed signing out", ex);
    }
  }

  public static void signout() throws Exception {
    URI uri = EndpointController.getInstance().buildURI("/domoweb/auth/signout");
    executeGet(uri);
    System.out.println("Sign Out successful");
  }

  private static void executeGet(URI uri) throws Exception {
    HttpGet httpGet = new HttpGet(uri);
    HttpResponse response;
    try {
      response = EndpointController.getInstance().executeOnClient(httpGet);
      checkStatusOk(response);
      consumeResponse(response);
    } catch (Exception ex) {
      throw new Exception("Error attempting to execute get for URI: " + uri, ex);
    }
  }

  private static void checkStatusOk(HttpResponse response) throws Exception {
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode != 200) {
      throw new Exception("Status code not OK (200). Status code: " + statusCode);
    }
  }

  private static void consumeResponse(HttpResponse response) throws Exception {
    HttpEntity entity = response.getEntity();
    try {
      EntityUtils.consume(entity);
    } catch (IOException ex) {
      throw new Exception("Failed consuming response", ex);
    }
  }
}
