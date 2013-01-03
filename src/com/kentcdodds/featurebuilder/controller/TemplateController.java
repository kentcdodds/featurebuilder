package com.kentcdodds.featurebuilder.controller;

import com.kentcdodds.featurebuilder.endpoints.Feature;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kentdodds
 */
public class TemplateController {

  private static TemplateController instance;
  public final String TEMPLATE_DIRECTORY = "/com/kentcdodds/featurebuilder/resources/";
  public final String FEATURE_TEMPLATE_FILENAME = "feature_template.feature";
  public final String SCENARIO_TEMPLATE_FILENAME = "scenario_template.feature";
  private final File outputDirectory = new File(System.getProperty("user.home") + "/Desktop/Test Feature Output/");
  private Configuration cfg = new Configuration();

  private TemplateController() {
    try {
      setup();
    } catch (URISyntaxException ex) {
      Logger.getLogger(TemplateController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(TemplateController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (TemplateModelException ex) {
      Logger.getLogger(TemplateController.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public static TemplateController getInstance() {
    if (instance == null) {
      instance = new TemplateController();
    }
    return instance;
  }

  public void generateEndpointFeatures(List<Feature> features) {
    for (Feature feature : features) {
      try {
        Template template = getFeatureTemplate();
        feature.generateFeatureText(template);
      } catch (IOException ex) {
        Logger.getLogger(TemplateController.class.getName()).log(Level.SEVERE, null, ex);
      } catch (TemplateException ex) {
        Logger.getLogger(TemplateController.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  public Template getFeatureTemplate() throws IOException {
    return getCfg().getTemplate(FEATURE_TEMPLATE_FILENAME);
  }

  public Template getScenarioTemplate() throws IOException {
    return getCfg().getTemplate(SCENARIO_TEMPLATE_FILENAME);
  }
  
  private void setup() throws URISyntaxException, IOException, TemplateModelException {
    if (!outputDirectory.exists()) {
      getOutputDirectory().mkdir();
    }
    getCfg().setDirectoryForTemplateLoading(new File(TemplateController.class.getResource(TEMPLATE_DIRECTORY).toURI()));
    getCfg().setObjectWrapper(new DefaultObjectWrapper());
    getCfg().setSharedVariable("intro_comments",
            "# Author: Kent Dodds (kent.dodds@domo.com)" + Main.newline
            + "# Manager: Doug Reid (doug.reid@domo.com)");
    getCfg().setSharedVariable("global_tag", "@kentsTest");
  }

  /**
   * @return the outputDirectory
   */
  public File getOutputDirectory() {
    return outputDirectory;
  }

  /**
   * @return the cfg
   */
  public Configuration getCfg() {
    return cfg;
  }
}