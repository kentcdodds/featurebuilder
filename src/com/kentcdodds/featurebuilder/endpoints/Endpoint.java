package com.kentcdodds.featurebuilder.endpoints;

import com.kentcdodds.featurebuilder.controller.EndpointController;
import com.kentcdodds.featurebuilder.controller.Main;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

public class Endpoint {

  private String path;
  private HttpRequestBase request;
  private HttpResponse response;
  private String responseContent;

  public Endpoint(String path, HttpRequestBase request) {
    this.path = path;
    this.request = request;
  }

  public void runRequestSetVariablesAndConsumeEntity() throws IOException {
    response = runRequest();
    responseContent = generateResponseContent();
    consumeResponseEntity();
  }

  private HttpResponse runRequest() throws IOException {
    return EndpointController.getInstance().executeOnClient(request);
  }

  private String generateResponseContent() throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
    String readLine;
    StringBuilder sb = new StringBuilder();
    while ((readLine = reader.readLine()) != null) {
      sb.append(readLine);
    }
    reader.close();
    return sb.toString();
  }

  private void consumeResponseEntity() throws IOException {
    EntityUtils.consume(response.getEntity());
  }

  /**
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * @param path the path to set
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * @return the request
   */
  public HttpRequestBase getRequest() {
    return request;
  }

  /**
   * @param request the request to set
   */
  public void setRequest(HttpRequestBase request) {
    this.request = request;
  }

  /**
   * @return the response
   */
  public HttpResponse getResponse() {
    return response;
  }

  /**
   * @param response the response to set
   */
  public void setResponse(HttpResponse response) {
    this.response = response;
  }

  /**
   * This is null until runRequestSetVariablesAndConsumeEntity is called.
   *
   * @return the responseContent
   */
  public String getResponseContent() {
    return responseContent;
  }

  /**
   * @param responseContent the responseContent to set
   */
  public void setResponseContent(String responseContent) {
    this.responseContent = responseContent;
  }

  @Override
  public String toString() {
    return "Path: " + path + Main.newline
            + "Request:" + Main.newline + "\t" + getRequestAsString().replace(Main.newline, Main.newline + "\t") + Main.newline
            + "Response:" + Main.newline + "\t" + getResponseAsString().replace(Main.newline, Main.newline + "\t");
  }

  private String getResponseAsString() {
    if (response == null) {
      return "[null response]";
    }
    HttpEntity entity = response.getEntity();
    StatusLine statusLine = response.getStatusLine();
    return "Code: " + statusLine.getStatusCode() + Main.newline
            + "Phrase: " + statusLine.getReasonPhrase() + Main.newline
            + "Content Type: " + entity.getContentType().getName() + ": " + entity.getContentType().getValue() + Main.newline
            + "Content Length: " + entity.getContentLength() + Main.newline
            + "Headers: " + Main.newline
            + "\t" + getHeadersAsString(response.getAllHeaders()).replace(Main.newline, Main.newline + "\t");
  }

  private String getRequestAsString() {
    if (request == null) {
      return "[null request]";
    }
    return "URI: " + request.getURI() + Main.newline
            + "Method: " + request.getRequestLine().getMethod() + Main.newline
            + "Headers: " + Main.newline
            + "\t" + getHeadersAsString(request.getAllHeaders()).replace(Main.newline, Main.newline + "\t");
  }

  public String getHeadersAsString(Header... headers) {
    StringBuilder sb = new StringBuilder();
    for (Header header : headers) {
      sb.append(header.getName()).append(": ").append(header.getValue()).append(Main.newline);
    }
    if (headers.length == 0)
      sb.append("[no headers]");
    return sb.toString();
  }
}
