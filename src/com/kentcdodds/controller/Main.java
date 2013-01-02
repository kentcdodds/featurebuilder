package com.kentcdodds.controller;

import au.com.bytecode.opencsv.CSVReader;
import java.io.*;
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
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
public class Main {

  public static final String SCHEME = "https";
  private HttpClient client = new DefaultHttpClient();
  private CookieStore cookieStore = new BasicCookieStore();
  private HttpContext httpContext = new BasicHttpContext();
  public static final String HOST = "qastaging-manual.domo.com";
  public static final String CHARSET = "UTF-8";
  private String methodsToTest = ""
          + "PUT"
          + "POST"
          + "DELETE"
          + "GET"
          + "";
  /**
   * Setting the limit to less than 0 will effectively make no limit.
   */
  public static final int limit = 4, offset = 15;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
    Main mc = new Main();
    mc.setup();
    if (!mc.signin()) {
      return;
    }
    mc.runEndpoints();
    mc.signout();
  }

  public void setup() {
    httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
  }

  public void printHeaders(HttpResponse response) {
    for (Header header : response.getAllHeaders()) {
      System.out.println(header.getName() + ": " + header.getValue());
      for (HeaderElement headerElement : header.getElements()) {
        System.out.println("\t" + headerElement.getName() + ": " + headerElement.getValue());
      }
    }
  }

  public void printContent(InputStream inputStream) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
    String readLine;
    while ((readLine = reader.readLine()) != null) {
      System.out.println(readLine);
    }
    reader.close();
  }

  public boolean signin() throws UnsupportedEncodingException, ClientProtocolException, IOException, URISyntaxException {
    URI uri = buildURI("/domoweb/auth/signin",
            new String[]{"username", "qa6.tester@domosoftware.net"},
            new String[]{"password", "enduserPassword"});
    HttpGet httpGet = new HttpGet(uri);
    HttpResponse response = executeOnClient(httpGet);
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
    URI uri = buildURI("/domoweb/auth/signout");
    HttpGet httpget = new HttpGet(uri);
    HttpEntity entity = executeOnClient(httpget).getEntity();
    EntityUtils.consume(entity);
    System.out.println("Signed Out");
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
   * @param csvResourceLocation (expected to be a resource file within the package, not a file)
   * @return a list of the HttpRequestBases read from the CSV file
   * @throws IOException
   * @throws URISyntaxException
   */
  private List<HttpRequestBase> readEndpointsFromCSVFile(String csvResourceLocation) throws IOException {
    CSVReader reader = new CSVReader(new InputStreamReader(getClass().getResourceAsStream(csvResourceLocation)));
    @SuppressWarnings("UnusedAssignment") // To skip the header
    String[] next = reader.readNext();
    int skipped = 0;
    List<HttpRequestBase> endpoints = new ArrayList<HttpRequestBase>();
    while ((next = reader.readNext()) != null) {

      final String method = next[0].toUpperCase();
      String endpoint = next[1];
      boolean ignore = !next[2].isEmpty();

      if (ignore || endpoint.contains("{") || !methodsToTest.contains(method)) {
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
        requestBase.setURI(buildURI(endpoint));

        endpoints.add(requestBase);
      } catch (URISyntaxException ex) {
        System.out.println("Problem with the URI for endpoint: " + endpoint);
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
  private void runEndpoints() throws IOException {
    String resourceLocation = "/com/kentcdodds/resources/domoWebEndpoints.csv";
    List<HttpRequestBase> allUrls = readEndpointsFromCSVFile(resourceLocation);
    Map<Integer, Integer> statusCodeCount = new HashMap<Integer, Integer>();

    for (int i = 0; i < allUrls.size(); i++) {
      if (limit > 0 && (i <= offset || i > (offset + limit))) {
        continue;
      }
      HttpRequestBase httpRequest = allUrls.get(i);

      HttpResponse response;
      String requestId = httpRequest.getMethod() + ": " + httpRequest.getURI();
      String errorMessage = ">>>> Problem %s for request: " + requestId;

      try {
        response = executeOnClient(httpRequest);
      } catch (IOException ex) {
        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        System.err.println(String.format(errorMessage, "executing on client"));
        continue;
      }

      printInfo(response, requestId, errorMessage);

      try {
        EntityUtils.consume(response.getEntity());
      } catch (IOException ex) {
        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        System.err.println(String.format(errorMessage, "consuming the entity"));
      }

      int statusCode = response.getStatusLine().getStatusCode();
      Integer count = statusCodeCount.get(statusCode);
      if (count == null) {
        statusCodeCount.put(statusCode, 1);
      } else {
        statusCodeCount.put(statusCode, count + 1);
      }
    }

    System.out.println("Status Code Count: " + statusCodeCount);

  }

  /**
   * Prints pertinent information about the HttpResponse.
   *
   * @param response
   * @param requestId
   * @param errorMessage
   */
  private void printInfo(HttpResponse response, String requestId, String errorMessage) {
    System.out.println();
    System.out.println("------------- " + requestId + " (Status Code: " + response.getStatusLine().getStatusCode() + ") -------------------");

    printHeaders(response);

    System.out.println(System.getProperty("line.separator") + "******** content ********" + System.getProperty("line.separator"));

    try {
      printContent(response.getEntity().getContent());
    } catch (IOException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
      System.err.println(String.format(errorMessage, "printing content"));
    }

    System.out.println("------------------------------------------------------");
    System.out.println();
  }

  /**
   * This is used to execute all requests with the same httpContext.
   *
   * @param request
   * @return
   * @throws IOException
   */
  private HttpResponse executeOnClient(HttpRequestBase request) throws IOException {
    System.out.println("Executing " + request.getURI());
    HttpResponse response = client.execute(request, httpContext);
    return response;
  }
}
