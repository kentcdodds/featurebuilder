package com.kentcdodds.controller;

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
  
  
  
}
