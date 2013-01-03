package com.kentcdodds.featurebuilder.test;

import com.kentcdodds.featurebuilder.controller.TemplateController;
import com.kentcdodds.featurebuilder.endpoints.Endpoint;
import com.kentcdodds.featurebuilder.endpoints.Feature;
import com.kentcdodds.featurebuilder.endpoints.Scenario;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.http.client.methods.HttpRequestBase;

/**
 *
 * @author kentdodds
 */
public class Tester {

  public static void main(String[] args) throws Throwable {
    List<Scenario> scenarios = new ArrayList<Scenario>(3);
    scenarios.add(new Scenario("Scenario 1", new Endpoint("testEndpoint1", new HttpRequestBase() {

      @Override
      public String getMethod() {
        return "GET";
      }
    })));
    scenarios.add(new Scenario("Scenario 2", new Endpoint("testEndpoint2", new HttpRequestBase() {

      @Override
      public String getMethod() {
        return "GET";
      }
    })));
    scenarios.add(new Scenario("Scenario 3", new Endpoint("testEndpoint3", new HttpRequestBase() {

      @Override
      public String getMethod() {
        return "GET";
      }
    })));
    Feature feature = new Feature("Test Feature", scenarios, "@tag1", "@tag2");
    TemplateController tc = TemplateController.getInstance();
    tc.generateEndpointFeatures(Arrays.asList(feature));
    String featureText = feature.getFeatureText();
    System.out.println(featureText);
  }
  
}
