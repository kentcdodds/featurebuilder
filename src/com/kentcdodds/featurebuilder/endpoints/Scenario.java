package com.kentcdodds.featurebuilder.endpoints;

import com.kentcdodds.featurebuilder.controller.TemplateController;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author kentdodds
 */
public class Scenario {

  private String name;
  private Endpoint endpoint;

  public Scenario(String name, Endpoint endpoint) {
    this.name = name;
    this.endpoint = endpoint;
  }

  public String getTemplateInfo() throws IOException, TemplateException {
    StringWriter out = new StringWriter();
    
    Template scenarioTemplate = TemplateController.getInstance().getScenarioTemplate();
    Map rootMap = getTemplateMap();
    scenarioTemplate.process(rootMap, out);
    
    String output = new String(out.getBuffer());
    out.close();
    return output;
  }
  
  private Map getTemplateMap() {
    Map root = new HashMap();    
    root.put("name", name);
    root.put("endpoint_path", endpoint.getPath());
    root.put("endpoint_method", endpoint.getRequest().getMethod());
//    root.put("response_code", endpoint.getResponse().getStatusLine().getStatusCode());
    return root;
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
   * @return the endpoint
   */
  public Endpoint getEndpoint() {
    return endpoint;
  }

  /**
   * @param endpoint the endpoint to set
   */
  public void setEndpoint(Endpoint endpoint) {
    this.endpoint = endpoint;
  }

}
