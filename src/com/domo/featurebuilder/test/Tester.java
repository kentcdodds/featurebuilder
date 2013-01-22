package com.domo.featurebuilder.test;

import com.domo.featurebuilder.controller.TemplateController;
import com.domo.featurebuilder.model.Endpoint;
import com.domo.featurebuilder.model.Feature;
import com.domo.featurebuilder.model.Scenario;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

import java.util.Arrays;

public class Tester {

    public static void main(String[] args) throws Throwable {
        Tester t = new Tester();
        t.testFeatures();
    }

    private void testFeatures() {
        Feature feature = makeTestFeature("Test Feature", "tag1", "tag2");
        TemplateController tc = TemplateController.getInstance();
        //tc.generateEndpointFeatures(Arrays.asList(feature));
        String featureText = feature.getFeatureText();
        System.out.println(featureText);
    }

    private Feature makeTestFeature(String name, String... tags) {
        Scenario s1 = makeTestScenario("Scenario 1");
        Scenario s2 = makeTestScenario("Scenario 1");
        Scenario s3 = makeTestScenario("Scenario 1");
        return null; //new Feature(name, Arrays.asList(s1, s2, s3), tags);
    }

    private Scenario makeTestScenario(String name) {
        return null;// new Scenario(name, makeTestEndpoint());
    }

    private Endpoint makeTestEndpoint() {
        return null;// new Endpoint(makeTestRequest());
    }

    private HttpRequestBase makeTestRequest() {
        return null;// new HttpGet("http://www.google.com/");
    }
}
