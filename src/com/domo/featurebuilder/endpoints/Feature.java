package com.domo.featurebuilder.endpoints;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Feature {

  private String name;
  private List<Scenario> scenarios;
  private List<String> tags;
  private String featureText;

  public Feature(String name, List<Scenario> scenarios, List<String> tags) {
    this.name = name;
    this.scenarios = scenarios;
    this.tags = tags;
  }

  public Feature(String name, List<Scenario> scenarios, String... tags) {
    this.name = name;
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
    root.put("name", name);
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
  
  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the scenarios
   */
  public List<Scenario> getScenarios() {
    return scenarios;
  }

  /**
   * @param scenarios the scenarios to set
   */
  public void setScenarios(List<Scenario> scenarios) {
    this.scenarios = scenarios;
  }

  /**
   * @return the tags
   */
  public List<String> getTags() {
    return tags;
  }

  /**
   * @param tags the tags to set
   */
  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  /**
   * @return the featureText
   */
  public String getFeatureText() {
    return featureText;
  }

  /**
   * @param featureText the featureText to set
   */
  public void setFeatureText(String featureText) {
    this.featureText = featureText;
  }
}
