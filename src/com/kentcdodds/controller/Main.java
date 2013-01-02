package com.kentcdodds.controller;

import com.kentcdodds.endpoints.Endpoint;
import java.io.*;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author kentdodds
 */
public class Main {

  public static final String newline = System.getProperty("line.separator");
  private static final String resourceLocation = "/com/kentcdodds/resources/domoWebEndpoints.csv";

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
    Main mc = new Main();
    EndpointController.getInstance().setup();
    if (!mc.signin()) {
      return;
    }
    List<Endpoint> endpoints = EndpointController.getInstance().readEndpointsFromCSVFile(resourceLocation);
    EndpointController.getInstance().runEndpoints(endpoints);
    TemplateController.getInstance().generateEndpointFeatures(endpoints);
    
    mc.signout();
  }

  public boolean signin() throws UnsupportedEncodingException, ClientProtocolException, IOException, URISyntaxException {
    URI uri = EndpointController.getInstance().buildURI("/domoweb/auth/signin",
            new String[]{"username", "qa6.tester@domosoftware.net"},
            new String[]{"password", "enduserPassword"});
    HttpGet httpGet = new HttpGet(uri);
    HttpResponse response = EndpointController.getInstance().executeOnClient(httpGet);
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode != 200) {
      System.out.println("Failed signing in (status code: " + statusCode + ")");
      return false;
    }
    HttpEntity entity = response.getEntity();
    EntityUtils.consume(entity);
    System.out.println("Signed In");
    return true;
  }

  public void signout() throws ClientProtocolException, IOException, URISyntaxException {
    URI uri = EndpointController.getInstance().buildURI("/domoweb/auth/signout");
    HttpGet httpGet = new HttpGet(uri);
    HttpEntity entity = EndpointController.getInstance().executeOnClient(httpGet).getEntity();
    EntityUtils.consume(entity);
    System.out.println("Signed Out");
  }

}
