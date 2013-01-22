package com.domo.featurebuilder.controller;

import com.domo.featurebuilder.helper.Helper;
import com.domo.featurebuilder.model.Endpoint;
import com.domo.featurebuilder.model.Feature;
import com.domo.featurebuilder.model.Scenario;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureController {

    private static FeatureController instance;
    private final File outputDirectory = new File(Helper.userHome + "/Desktop/Test Feature Output/");

    private FeatureController() {
    }

    public static FeatureController getInstance() {
        if (instance == null)
            instance = new FeatureController();
        return instance;
    }

    public List<Feature> createFeatures(List<Endpoint> endpoints) {
        List<Feature> features = new ArrayList<Feature>();
        for (int i = 0; i < endpoints.size(); i++) {
            Endpoint endpoint = endpoints.get(i);
            if (endpoint.isProcessed()) { //TODO: This is for testing only. Remove this in prod.
                Feature feature = new Feature("Feature " + i, Arrays.asList(new Scenario("Scenario " + i, endpoint)), "");
                features.add(feature);
            }
        }
        return features;
    }

    public void printFeatures(List<Feature> features) {
        for (Feature feature : features) {
            System.out.println("---------------------------------------------" + Helper.newline);
            System.out.println(feature.getFeatureText());
            System.out.println(Helper.newline + Helper.newline);
        }
    }

    public void saveFeaturesToOutputDirectory(List<Feature> features) {
        for (Feature feature : features) {
            try {
                feature.save(outputDirectory);
            } catch (IOException exception) {
                System.out.println("Failed to save feature : " + feature.getFilename());
            }
        }
    }
}
