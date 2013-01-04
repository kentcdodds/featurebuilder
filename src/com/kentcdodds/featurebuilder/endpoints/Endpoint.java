package com.kentcdodds.featurebuilder.endpoints;

import com.kentcdodds.featurebuilder.controller.HttpController;
import com.kentcdodds.featurebuilder.controller.Main;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JSONString;
import net.sf.json.util.JSONUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

public class Endpoint {

  private HttpRequestBase request;
  private HttpResponse response;
  private String responseContent;

  public Endpoint(HttpRequestBase request) {
    this.request = request;
  }

  public void processEndpoint() throws IOException {
    runRequest();
    generateResponseContent();
//    formatResponseContentIfIsJSON();
    consumeResponseEntity();
  }

  private void runRequest() throws IOException {
    response = HttpController.getInstance().executeRequestOnClientWithContext(request);
  }

  private void generateResponseContent() throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
    String readLine;
    StringBuilder sb = new StringBuilder();
    while ((readLine = reader.readLine()) != null)
      sb.append(readLine);
    reader.close();
    responseContent = sb.toString();
  }

  private void consumeResponseEntity() throws IOException {
    EntityUtils.consume(response.getEntity());
  }

  public boolean responseContentIsJSON() {
    return JSONUtils.mayBeJSON(responseContent);
  }

  public String getRequestMethod() {
    return request.getMethod();
  }

  public int getResponseCode() {
    return response.getStatusLine().getStatusCode();
  }

  public boolean isProcessed() {
    return response != null;
  }

  public String getRequestPath() {
    return request.getURI().toString();
  }

  public void formatResponseContentIfIsJSON() {
    if (responseContentIsJSON()) {
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(responseContent);
      int indentSpaces = 2;
      responseContent = jsonObject.toString(indentSpaces);
    }
  }

  public Map getTemplateMap() {
    Map root = new HashMap();
    root.put("endpoint_path", getRequestPath());
    root.put("endpoint_method", getRequestMethod());
    if (isProcessed()) {
      root.put("response_code", getResponseCode());
      root.put("response_content", responseContent);
    }
    return root;
  }

  @Override
  public String toString() {
    return "Path: " + getRequestPath() + Main.newline
            + "Request:" + Main.newline + "\t" + getRequestAsString().replace(Main.newline, Main.newline + "\t") + Main.newline
            + "Response:" + Main.newline + "\t" + getResponseAsString().replace(Main.newline, Main.newline + "\t");
  }

  private String getResponseAsString() {
    if (response == null)
      return "[null response]";
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
    if (request == null)
      return "[null request]";
    return "URI: " + request.getURI() + Main.newline
            + "Method: " + request.getRequestLine().getMethod() + Main.newline
            + "Headers: " + Main.newline
            + "\t" + getHeadersAsString(request.getAllHeaders()).replace(Main.newline, Main.newline + "\t");
  }

  public String getHeadersAsString(Header... headers) {
    StringBuilder sb = new StringBuilder();
    for (Header header : headers)
      sb.append(header.getName()).append(": ").append(header.getValue()).append(Main.newline);
    if (headers.length == 0)
      sb.append("[no headers]");
    return sb.toString();
  }
}
