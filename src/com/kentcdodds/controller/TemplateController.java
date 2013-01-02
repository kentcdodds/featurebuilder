package com.kentcdodds.controller;

import com.kentcdodds.endpoints.Endpoint;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kentdodds
 */
public class TemplateController {
  
  private static TemplateController instance;

  private final String templateDirectory = "/com/kentcdodds/resources/";
  private final String featureTemplateFilename = "feature_template.feature";
  private final File outputDirectory = new File(System.getProperty("user.home") + "/Desktop/Test Feature Output/");
  private Configuration cfg = new Configuration();
  
  private TemplateController()  {
    try {
      setupConfiguration();
    } catch (URISyntaxException ex) {
      Logger.getLogger(TemplateController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(TemplateController.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  public static TemplateController getInstance() {
    if (instance == null) {
      instance = new TemplateController();
    }
    return instance;
  }
  
  public void generateEndpointFeatures(List<Endpoint> endpoints) {
    for (Endpoint endpoint : endpoints) {
      try {
        writeToTemplate(endpoint);
      } catch (URISyntaxException ex) {
        Logger.getLogger(TemplateController.class.getName()).log(Level.SEVERE, null, ex);
      } catch (IOException ex) {
        Logger.getLogger(TemplateController.class.getName()).log(Level.SEVERE, null, ex);
      } catch (TemplateException ex) {
        Logger.getLogger(TemplateController.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
  
  private void writeToTemplate(Endpoint endpoint) throws URISyntaxException, IOException, TemplateException {
    Template template = cfg.getTemplate(featureTemplateFilename);
    
    Map root = new HashMap();
    setEndpointTemplateMap(endpoint, root);
    
    PrintWriter out = new PrintWriter(outputDirectory + endpoint.getPath() + ".feature");
    template.process(root, out);
    out.close();
  }
  
  private void setEndpointTemplateMap(Endpoint endpoint, Map root) {
    root.put("intro_comments",
            "# Author: Kent Dodds (kent.dodds@domo.com)" + Main.newline
            + "# Manager: Doug Reid (doug.reid@domo.com)");
    
  }

  private void setupConfiguration() throws URISyntaxException, IOException {
    if (!outputDirectory.exists())
      outputDirectory.mkdir();
    cfg.setDirectoryForTemplateLoading(new File(TemplateController.class.getResource(templateDirectory).toURI()));
    cfg.setObjectWrapper(new DefaultObjectWrapper());
  }

}