package com.kentcdodds.featurebuilder.controller;

import au.com.bytecode.opencsv.CSVReader;
import com.kentcdodds.featurebuilder.endpoints.Endpoint;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author kentdodds
 */
public class EndpointController {

  private static EndpointController instance;
  private HttpClient client = new DefaultHttpClient();
  private CookieStore cookieStore = new BasicCookieStore();
  private HttpContext httpContext = new BasicHttpContext();
  public final String SCHEME = "https";
  public final String HOST = "qastaging-manual.domo.com";
  public final String CHARSET = "UTF-8";
  public final String methodsToTest = ""
          + "PUT"
          + "POST"
          + "DELETE"
          + "GET"
          + "";
  /**
   * Setting the limit to less than 0 will effectively make no limit.
   */
  public final int limit = 4, offset = 15;

  private EndpointController() {
  }

  public static EndpointController getInstance() {
    if (instance == null) {
      instance = new EndpointController();
    }
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
    while ((next = reader.readNext()) != null) {

      final String method = next[0].toUpperCase();
      String path = next[1];
      boolean ignore = !next[2].isEmpty();

      if (ignore || path.contains("{") || !methodsToTest.contains(method)) {
        skipped++;
        continue;
      }

      HttpRequestBase requestBase = new HttpRequestBase() {
        @Override
        public String getMethod() {
          return method;
        }
      };
      try {
        requestBase.setURI(buildURI(path));

        endpoints.add(new Endpoint(path, requestBase));
      } catch (URISyntaxException ex) {
        System.out.println("Problem with the URI for endpoint: " + path);
        System.out.println(ex.getMessage());
        System.out.println(ex.getReason());
      }
    }

    System.out.println("Total Endpoints Skipped: " + skipped);
    System.out.println("Total Endpoints: " + endpoints.size());

    return endpoints;
  }

  /**
   * Reads the endpoints located at a hard coded resourceLocation, then runs through the HttpRequestBases and executes
   * them on the client and prints the responses.
   *
   * @throws ClientProtocolException
   * @throws IOException
   * @throws URISyntaxException
   */
  public void runEndpoints(List<Endpoint> endpoints) {
    Map<Integer, Integer> statusCodeCount = new HashMap<Integer, Integer>();
    for (int i = 0; i < endpoints.size(); i++) {
      if (limit > 0 && (i <= offset || i > (offset + limit))) {
        continue;
      }
      Endpoint endpoint = endpoints.get(i);
      try {
        runEndpoint(endpoint);
        addResponseCodeToCount(endpoint, statusCodeCount);
      } catch (IOException ex) {
        Logger.getLogger(EndpointController.class.getName()).log(Level.SEVERE, null, ex);
      }

    }
    System.out.println("Status Code Count: " + statusCodeCount);
  }

  private void runEndpoint(Endpoint endpoint) throws IOException {
    endpoint.runRequestSetVariablesAndConsumeEntity();
  }

  private void addResponseCodeToCount(Endpoint endpoint, Map<Integer, Integer> statusCodeCount) {
    int statusCode = endpoint.getResponse().getStatusLine().getStatusCode();
    Integer count = statusCodeCount.get(statusCode);
    if (count == null) {
      statusCodeCount.put(statusCode, 1);
    } else {
      statusCodeCount.put(statusCode, count + 1);
    }
  }

  public void setup() {
    CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
    httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
  }

  public URI buildURI(String path, String[]... params) throws URISyntaxException {
    URIBuilder builder = new URIBuilder();
    builder.setScheme(SCHEME).setHost(HOST).setPath(path);
    for (String[] param : params) {
      builder.addParameter(param[0], param[1]);
    }
    return builder.build();
  }

  /**
   * This is used to execute all requests with the same httpContext.
   *
   * @param request
   * @return
   * @throws IOException
   */
  public HttpResponse executeOnClient(HttpRequestBase request) throws IOException {
    System.out.println("Executing " + request.getURI());
    HttpResponse response = client.execute(request, httpContext);
    return response;
  }
}