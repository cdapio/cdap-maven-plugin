/*
 * Copyright © 2022 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SpecJsonTestBean {

  private final String specVersion;
  private final String org;
  private final String author;
  private final long created;
  private final String cdapVersion;
  private final String description;
  private final String label;
  private final List<String> categories;
  private final List<Actions> actions;

  @JsonCreator
  public SpecJsonTestBean(
    @JsonProperty("specVersion") String specVersion,
    @JsonProperty("org") String org,
    @JsonProperty("author") String author,
    @JsonProperty("created") long created,
    @JsonProperty("cdapVersion") String cdapVersion,
    @JsonProperty("description") String description,
    @JsonProperty("label") String label,
    @JsonProperty("categories") List<String> categories,
    @JsonProperty("actions") List<Actions> actions) {

    this.specVersion = specVersion;
    this.org = org;
    this.author = author;
    this.created = created;
    this.cdapVersion = cdapVersion;
    this.description = description;
    this.label = label;
    this.categories = categories;
    this.actions = actions;
  }

  public String getSpecVersion() {
    return specVersion;
  }

  public String getOrg() {
    return org;
  }

  public String getAuthor() {
    return author;
  }

  public long getCreated() {
    return created;
  }

  public String getCdapVersion() {
    return cdapVersion;
  }

  public String getDescription() {
    return description;
  }

  public String getLabel() {
    return label;
  }

  public List<String> getCategories() {
    return categories;
  }

  public List<Actions> getActions() {
    return actions;
  }
}
