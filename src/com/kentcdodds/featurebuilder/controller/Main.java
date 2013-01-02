package com.kentcdodds.featurebuilder.controller;

import com.kentcdodds.featurebuilder.endpoints.Endpoint;
import java.net.URI;
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

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    startup();
    processEndpoints();
    exit();
  }
  
  private static void startup() throws Exception {
    EndpointController.getInstance().setup();
    boolean succeededSigningIn = signin();
    if (!succeededSigningIn)
      throw new Exception("Failed logging in");
  }
  
  public static boolean signin() throws Exception {
    URI uri = EndpointController.getInstance().buildURI("/domoweb/auth/signin",
            new String[]{"username", domoUsername},
            new String[]{"password", domoPassword});
    HttpGet httpGet = new HttpGet(uri);
    HttpResponse response = EndpointController.getInstance().executeOnClient(httpGet);
    
    checkStatusOk(response);
    
    HttpEntity entity = response.getEntity();
    EntityUtils.consume(entity);
    
    System.out.println("Signed In");
    return true;
  }
  
  private static void checkStatusOk(HttpResponse response) throws Exception {
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode != 200) {
      throw new Exception("Failed signing in (status code: " + statusCode + ")");
    }
  }
  
  private static void processEndpoints() throws Exception {
    java.util.List<Endpoint> endpoints = EndpointController.getInstance().readEndpointsFromCSVFile(endpointsCSVLocation);
    EndpointController.getInstance().runEndpoints(endpoints);
    TemplateController.getInstance().generateEndpointFeatures(endpoints);
  }
  
  private static void exit() throws Exception {
    signout();
  }

  public static void signout() throws Exception {
    URI uri = EndpointController.getInstance().buildURI("/domoweb/auth/signout");
    HttpGet httpGet = new HttpGet(uri);
    HttpEntity entity = EndpointController.getInstance().executeOnClient(httpGet).getEntity();
    EntityUtils.consume(entity);
    System.out.println("Signed Out");
  }

}
