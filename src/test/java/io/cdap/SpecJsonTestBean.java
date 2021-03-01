package io.cdap;
import java.util.List;

public class SpecJsonTestBean {

  private String specVersion;
  private String org;
  private String author;
  private long created;
  private String cdapVersion;
  private String description;
  private String label;
  private List<String> categories;
  private List<Actions> actions;
  public void setSpecVersion(String specVersion) {
    this.specVersion = specVersion;
  }
  public String getSpecVersion() {
    return specVersion;
  }

  public void setOrg(String org) {
    this.org = org;
  }
  public String getOrg() {
    return org;
  }

  public void setAuthor(String author) {
    this.author = author;
  }
  public String getAuthor() {
    return author;
  }

  public void setCreated(long created) {
    this.created = created;
  }
  public long getCreated() {
    return created;
  }

  public void setCdapVersion(String cdapVersion) {
    this.cdapVersion = cdapVersion;
  }
  public String getCdapVersion() {
    return cdapVersion;
  }

  public void setDescription(String description) {
    this.description = description;
  }
  public String getDescription() {
    return description;
  }

  public void setLabel(String label) {
    this.label = label;
  }
  public String getLabel() {
    return label;
  }

  public void setCategories(List<String> categories) {
    this.categories = categories;
  }
  public List<String> getCategories() {
    return categories;
  }

  public void setActions(List<Actions> actions) {
    this.actions = actions;
  }
  public List<Actions> getActions() {
    return actions;
  }

}