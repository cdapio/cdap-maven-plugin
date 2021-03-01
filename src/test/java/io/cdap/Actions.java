package io.cdap;

import java.util.List;

public class Actions {

  private List<Arguments> arguments;
  private String label;
  private String type;
  public void setArguments(List<Arguments> arguments) {
    this.arguments = arguments;
  }
  public List<Arguments> getArguments() {
    return arguments;
  }

  public void setLabel(String label) {
    this.label = label;
  }
  public String getLabel() {
    return label;
  }

  public void setType(String type) {
    this.type = type;
  }
  public String getType() {
    return type;
  }

}