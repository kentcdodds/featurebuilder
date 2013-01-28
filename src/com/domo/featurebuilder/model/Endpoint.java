package com.domo.featurebuilder.model;

import com.domo.featurebuilder.controller.HttpController;
import com.domo.featurebuilder.helper.Helper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Endpoint {

    private List<Feature> features;
    private HttpEntityEnclosingRequestBase request;
    private String requestContent;
    private HttpResponse response;
    private String responseContent;
    private String responseContentType;

    public Endpoint(HttpEntityEnclosingRequestBase request, List<Feature> features) {
        this.request = request;
        this.features = features;
    }

    public void processEndpoint() throws IOException {
        runRequest();
        generateRequestContent();
        if(requestContent != null || !requestContent.isEmpty()){
            requestContent = formatContentIfIsJSON(requestContent);
        }
        generateResponseContent();
        if(responseContent != null || !responseContent.isEmpty()){
            responseContent = formatContentIfIsJSON(responseContent);
        }
        consumeResponseEntity();
    }

    private void runRequest() throws IOException {
        response = HttpController.getInstance().executeRequestOnClientWithContext(request);
    }

    private void generateRequestContent() throws IOException {
        StringEntity requestStringEntity = (StringEntity)request.getEntity();
        BufferedReader reader = new BufferedReader(new InputStreamReader(requestStringEntity.getContent(), "UTF-8"));
        String lineIn;
        StringBuilder sb = new StringBuilder();
        while ((lineIn = reader.readLine()) != null){
            sb.append(lineIn);
        }
        reader.close();
        requestContent = sb.toString();
    }

    private void generateResponseContent() throws IOException {
        for (Header header : response.getAllHeaders()) {
            if (header.getName().equalsIgnoreCase("Content-Type"))
                responseContentType = header.getValue();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
        String readLine;
        StringBuilder sb = new StringBuilder();
        while ((readLine = reader.readLine()) != null)
            sb.append(readLine);
        reader.close();
        responseContent = sb.toString();
        if (responseContent.length() < 1 || responseContent.isEmpty()) {
            System.out.println("This has responseContent less length of 1 (or it's empty)!! " + request.getURI());
        }
    }

    private void consumeResponseEntity() throws IOException {
        EntityUtils.consume(response.getEntity());
    }

    /**
     * @return a JSONObject representing the responseContent. Returns null if responseContent is not a valid JSONObject
     */
    private JSONObject getContentAsJSONObject(String content) {
        try {
            return new JSONObject(content);
        } catch (JSONException ex) {
            return null;
        }
    }

    /**
     * @return a JSONArray representing the responseContent. Returns null if responseContent is not a valid JSONArray
     */
    private JSONArray getContentAsJSONArray(String content) {
        try {
            return new JSONArray(content);
        } catch (JSONException ex) {
            return null;
        }
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
        return request.getURI().getPath();
    }

    private String formatContentIfIsJSON(String content) {
        String formattedContent;
        int indentFactor = 4;
        JSONObject jsonObject = getContentAsJSONObject(content);
        JSONArray jsonArray;
        try {
            if (jsonObject == null) {
                jsonArray = getContentAsJSONArray(content);
                if (jsonArray == null)
                    return null;
                formattedContent = jsonArray.toString(indentFactor);
            } else{
                formattedContent = jsonObject.toString(indentFactor);
            }
            return formattedContent;
        } catch (JSONException ex) {
            Logger.getLogger(Endpoint.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public Map getTemplateMap() {
        Map root = new HashMap();
        root.put("endpoint_path", getRequestPath());
        root.put("endpoint_method", getRequestMethod());
        if (isProcessed()) {
            root.put("response_code", getResponseCode());
            if (requestContent != null && requestContent.length() > 1 && !requestContent.isEmpty()) {
                root.put("request_body", requestContent.replace(Helper.newline, Helper.newline + "            "));
            }
            if (responseContent != null && responseContent.length() > 1 && !responseContent.isEmpty()) {
                root.put("response_content", responseContent.replace(Helper.newline, Helper.newline + "            "));
            }
        }
        return root;
    }

    public boolean contentTypeIsJson() {
        return responseContentType.matches("(?i).*application/json.*");
    }

    @Override
    public String toString() {
        return "Path: " + getRequestPath() + Helper.newline
                + "Request:" + Helper.newline + "\t" + getRequestAsString().replace(Helper.newline, Helper.newline + "\t") + Helper.newline
                + "Response:" + Helper.newline + "\t" + getResponseAsString().replace(Helper.newline, Helper.newline + "\t");
    }

    private String getResponseAsString() {
        if (response == null)
            return "[null response]";
        HttpEntity entity = response.getEntity();
        StatusLine statusLine = response.getStatusLine();
        return "Code: " + statusLine.getStatusCode() + Helper.newline
                + "Phrase: " + statusLine.getReasonPhrase() + Helper.newline
                + "Content Type: " + entity.getContentType().getName() + ": " + entity.getContentType().getValue() + Helper.newline
                + "Content Length: " + entity.getContentLength() + Helper.newline
                + "Headers: " + Helper.newline
                + "\t" + getHeadersAsString(response.getAllHeaders()).replace(Helper.newline, Helper.newline + "\t");
    }

    private String getRequestAsString() {
        if (request == null)
            return "[null request]";
        return "URI: " + request.getURI() + Helper.newline
                + "Method: " + request.getRequestLine().getMethod() + Helper.newline
                + "Headers: " + Helper.newline
                + "\t" + getHeadersAsString(request.getAllHeaders()).replace(Helper.newline, Helper.newline + "\t");
    }

    public String getHeadersAsString(Header... headers) {
        StringBuilder sb = new StringBuilder();
        for (Header header : headers)
            sb.append(header.getName()).append(": ").append(header.getValue()).append(Helper.newline);
        if (headers.length == 0)
            sb.append("[no headers]");
        return sb.toString();
    }

    public void generateFeatureText() throws IOException, TemplateException {
        for (Feature feature : features) {
            feature.generateFeatureText(getTemplateMap());
        }
    }

    public List<Feature> getFeature() {
        return features;
    }

    public void saveFeatures() {
        for (Feature feature : features) {
            try {
                feature.save();
            } catch (IOException e) {
                System.err.println("Error saving feature: " + feature.getFilename());
                System.err.println(e);
                System.err.println(e.getStackTrace());
            }
        }
    }
}
