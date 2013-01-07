package com.domo.featurebuilder.test;

import com.domo.featurebuilder.controller.TemplateController;
import com.domo.featurebuilder.endpoints.Endpoint;
import com.domo.featurebuilder.endpoints.Feature;
import com.domo.featurebuilder.endpoints.Scenario;
import java.util.Arrays;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

public class Tester {

  public static void main(String[] args) throws Throwable {
    Tester t = new Tester();
    t.testFeatures();
  }
  
  private void testFeatures() {
    Feature feature = makeTestFeature("Test Feature", "tag1", "tag2");
    TemplateController tc = TemplateController.getInstance();
    tc.generateEndpointFeatures(Arrays.asList(feature));
    String featureText = feature.getFeatureText();
    System.out.println(featureText);
  }
  
  private Feature makeTestFeature(String name, String... tags) {
    Scenario s1 = makeTestScenario("Scenario 1");
    Scenario s2 = makeTestScenario("Scenario 1");
    Scenario s3 = makeTestScenario("Scenario 1");
    return new Feature(name, Arrays.asList(s1, s2, s3), tags);
  }
  
  private Scenario makeTestScenario(String name) {
    return new Scenario(name, makeTestEndpoint());
  }
  
  private Endpoint makeTestEndpoint() {
    return new Endpoint(makeTestRequest());
  }
  
  private HttpRequestBase makeTestRequest() {
    return new HttpGet("http://www.google.com/");
  }
  
}
