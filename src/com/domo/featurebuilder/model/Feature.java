package com.domo.featurebuilder.model;

import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Feature {

  private String fileName;
  private List<Scenario> scenarios;
  private List<String> tags;
  private String featureText;

  public Feature(String fileName, List<Scenario> scenarios, String... tags) {
    this.fileName = fileName;
    this.scenarios = scenarios;
    this.tags = Arrays.asList(tags);
  }

  public void generateFeatureText(Template template) throws TemplateException, IOException {
    StringWriter out = new StringWriter();
    template.process(getTemplateMap(), out);
    featureText = new String(out.getBuffer());
    out.close();
  }

  private Map getTemplateMap() throws IOException, TemplateException {
    Map root = new HashMap();
    root.put("name", fileName);
    root.put("tags", tags);
    root.put("scenarios", getScenarioInfo());
    return root;
  }

  private List<String> getScenarioInfo() throws IOException, TemplateException {
    List<String> scenarioInfo = new ArrayList<String>(scenarios.size());
    for (Scenario scenario : scenarios)
      scenarioInfo.add(scenario.getTemplateInfo());
    return scenarioInfo;
  }


    public void save(File outputDirectory) throws IOException{
        File featureFile = new File(outputDirectory, fileName);
        PrintWriter printWriter = new PrintWriter(featureFile);
        printWriter.write(featureText);
        printWriter.close();
    }

  /**
   * @return the featureText
   */
  public String getFeatureText() {
    return featureText;
  }

   /**
   * @return the fileName
   */
  public String getFilename() {
    return fileName;
  }

}
