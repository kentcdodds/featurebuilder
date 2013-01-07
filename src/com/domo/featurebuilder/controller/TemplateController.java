package com.domo.featurebuilder.controller;

import com.domo.featurebuilder.model.Feature;
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

public class TemplateController {

  private static TemplateController instance;
  private final String TEMPLATE_DIRECTORY = "/com/domo/featurebuilder/resources/templates";
  private final String FEATURE_TEMPLATE_FILENAME = "feature_template.feature";
  private final String SCENARIO_TEMPLATE_FILENAME = "scenario_template.feature";
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
    return cfg.getTemplate(FEATURE_TEMPLATE_FILENAME);
  }

  public Template getScenarioTemplate() throws IOException {
    return cfg.getTemplate(SCENARIO_TEMPLATE_FILENAME);
  }
  
  private void setup() throws URISyntaxException, IOException, TemplateModelException {
    if (!outputDirectory.exists()) {
      outputDirectory.mkdir();
    }
    cfg.setDirectoryForTemplateLoading(new File(TemplateController.class.getResource(TEMPLATE_DIRECTORY).toURI()));
    cfg.setObjectWrapper(new DefaultObjectWrapper());
    cfg.setSharedVariable("intro_comments",
            "# Authors:" + Main.newline
            + "#   Kent Dodds (kent.dodds@domo.com)" + Main.newline
            + "#   Mack Cope (mack.cope@domo.com)" + Main.newline
            + "# Manager: Doug Reid (doug.reid@domo.com)");
    cfg.setSharedVariable("global_tag", "@featureBuilder");
  }
}