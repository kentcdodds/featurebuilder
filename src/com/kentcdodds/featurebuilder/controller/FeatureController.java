package com.kentcdodds.featurebuilder.controller;

import com.kentcdodds.featurebuilder.endpoints.Endpoint;
import com.kentcdodds.featurebuilder.endpoints.Feature;
import com.kentcdodds.featurebuilder.endpoints.Scenario;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author kentdodds
 */
public class FeatureController {

  private static FeatureController instance;

  private FeatureController() {
  }
  
  public static FeatureController getInstance() {
    if (instance == null) {
      instance = new FeatureController();
    }
    return instance;
  }
  
  public List<Feature> createFeatures(List<Endpoint> endpoints) {
    List<Feature> features = new ArrayList<Feature>();
    for (Endpoint endpoint : endpoints) {
      if (endpoint.getResponse() != null) { //TODO: This is for testing only. Remove this in prod.
        Feature feature = new Feature("Feature " + features.size(), Arrays.asList(new Scenario("Scenario " + features.size(), endpoint)), "");
        features.add(feature);
      }
    }
    return features;
  }

  public void printFeatures(List<Feature> features) {
    for (Feature feature : features) {
      System.out.println("---------------------------------------------" + Main.newline);
      System.out.println(feature.getFeatureText());
      System.out.println(Main.newline + Main.newline);
    }
  }
  
  
}
