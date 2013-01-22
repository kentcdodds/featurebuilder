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

    private FeatureController() {
    }

    public static FeatureController getInstance() {
        if (instance == null)
            instance = new FeatureController();
        return instance;
    }

    public void printFeatures(List<Feature> features) {
        for (Feature feature : features) {
            System.out.println("---------------------------------------------" + Helper.newline);
            System.out.println(feature.getFeatureText());
            System.out.println(Helper.newline + Helper.newline);
        }
    }

}
