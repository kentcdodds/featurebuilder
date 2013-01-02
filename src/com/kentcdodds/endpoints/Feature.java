package com.kentcdodds.endpoints;

import java.util.List;

/**
 *
 * @author kentdodds
 */
public class Feature {

  private List<Scenario> scenarios;

  public Feature(List<Scenario> scenarios) {
    this.scenarios = scenarios;
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
}
