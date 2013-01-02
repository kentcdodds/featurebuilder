package com.kentcdodds.endpoints;

import java.util.List;

/**
 *
 * @author kentdodds
 */
public class Scenario {

  private List<Endpoint> endpoints;

  public Scenario(List<Endpoint> endpoints) {
    this.endpoints = endpoints;
  }

  /**
   * @return the endpoints
   */
  public List<Endpoint> getEndpoints() {
    return endpoints;
  }

  /**
   * @param endpoints the endpoints to set
   */
  public void setEndpoints(List<Endpoint> endpoints) {
    this.endpoints = endpoints;
  }
}
