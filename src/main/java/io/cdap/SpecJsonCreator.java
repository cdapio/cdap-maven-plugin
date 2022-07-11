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

import com.google.common.base.Strings;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This mojo is used for creating the CDAP Plugin JSON file.
 */
@Mojo(name = "create-plugin-spec-json")
public class SpecJsonCreator extends AbstractMojo {

  public static final String ONE_STEP_DEPLOY_PLUGIN = "one_step_deploy_plugin";
  public static final String CREATE_PIPELINE_DRAFT = "create_pipeline_draft";
  private static final String SPEC_VERSION = "specVersion";
  private static final String LABEL = "label";
  private static final String DESCRIPTION = "description";
  private static final String AUTHOR = "author";
  private static final String ORG = "org";
  private static final String CREATED = "created";
  private static final String CATEGORIES = "categories";
  private static final String CDAP_VERSION = "cdapVersion";
  private static final String ACTIONS = "actions";
  private static final String SPEC_JSON = "spec.json";
  public static final String LIVERAMP = "Liveramp";

  @Parameter(property = "isPipeline", defaultValue = "false")
  private boolean isPipeline;

  @Parameter(property = "cdapArtifacts", required = true)
  private String[] cdapArtifacts;

  @Parameter(property = "version", defaultValue = "${project.version}")
  private String version;

  @Parameter(property = "label", defaultValue = "${project.name}")
  private String label;

  @Parameter(property = "description", defaultValue = "${project.description}")
  private String description;

  @Parameter(property = "author")
  private String author;

  @Parameter(property = "org")
  private String org;

  @Parameter(property = "categories")
  private String[] categories;

  @Parameter(property = "additionalActionArguments")
  private String[][] additionalActionArguments;

  /**
   * plugin action type, defaulted to
   */
  @Parameter(property = "actionType", defaultValue = ONE_STEP_DEPLOY_PLUGIN)
  private String actionType;

  @Parameter(property = "scope", defaultValue = "user")
  private String scope;

  @Parameter(property = "buildDirectory", defaultValue = "${project.build.directory}")
  private String buildDirectory;

  @Parameter(property = "artifactId", defaultValue = "${project.artifactId}")
  private String artifactId;

  private long created;
  private String cdapVersion;
  /**
   * Name of the configuration json file
   */
  private String configFilename;

  public SpecJsonCreator() {
    super();
  }

  /**
   * This constructor is used for testing
   */
  SpecJsonCreator(
    String actionType,
    String scope,
    String[] cdapArtifacts,
    String artifactId,
    String version,
    String label,
    String description,
    String author,
    String org,
    String[] categories,
    String buildDirectory,
    boolean isPipeline,
    String[][] additionalActionArguments) {

    this.actionType = actionType;
    this.scope = scope;
    this.cdapArtifacts = cdapArtifacts;
    this.artifactId = artifactId;
    this.version = version;
    this.label = label;
    this.description = description;
    this.author = author;
    this.org = org;
    this.categories = categories;
    this.isPipeline = isPipeline;
    this.additionalActionArguments = additionalActionArguments;
    this.buildDirectory = buildDirectory;
  }

  public void execute() throws MojoExecutionException {
    // Read in all the configurations.
    initialize();

    // Print header.
    printHeader();

    // We iterate through widgets and documentation directories creating the output configurations.
    JSONObject output = new JSONObject();

    output.put(SPEC_VERSION, version);
    output.put(LABEL, label);
    output.put(DESCRIPTION, description);
    if (!Strings.isNullOrEmpty(author)) {
      output.put(AUTHOR, author);
    }
    if (!Strings.isNullOrEmpty(org)) {
      output.put(ORG, org);
    }
    output.put(CREATED, created);
    output.put(CATEGORIES, categories);
    output.put(CDAP_VERSION, cdapVersion);

    JSONArray actions = new JSONArray();

    JSONArray additionalArguments = new JSONArray();
    if (additionalActionArguments != null) {
      for (String[] pair : additionalActionArguments) {
        String key = pair[0];
        String value = pair[1];
        JSONObject argument = createActionArgument(key, value);
        additionalArguments.put(argument);
      }
    }

    JSONObject actionJson = createAction(
      actionType,
      label,
      version,
      scope,
      configFilename,
      isPipeline,
      additionalArguments);

    actions.put(actionJson);
    output.put(ACTIONS, actions);

    try {
      File outputFile = new File(buildDirectory, SPEC_JSON);
      if (outputFile.exists()) {
        outputFile.delete();
      }
      outputFile.createNewFile();
      FileUtils.fileWrite(outputFile.getAbsolutePath(), output.toString(2));
      getLog().info("Successfully created: " + SPEC_JSON);
    } catch (IOException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }

    printFooter();
  }

  private JSONObject createAction(
      String type,
      String label,
      String version,
      String scope,
      String config,
      boolean isPipeline,
      JSONArray additionalArguments) {
    JSONObject actionJson = new JSONObject();
    actionJson.put("type", type);
    actionJson.put("label", label);

    JSONArray actionArguments = new JSONArray();

    JSONObject nameArg = createActionArgument("name", artifactId);
    JSONObject versionArg = createActionArgument("version", version);
    JSONObject scopeArg = createActionArgument("scope", scope);
    JSONObject configArg = createActionArgument("config", config);

    actionArguments.put(nameArg);
    actionArguments.put(versionArg);
    actionArguments.put(scopeArg);
    actionArguments.put(configArg);

    for (Object addArg : additionalArguments) {
      actionArguments.put(addArg);
    }

    if (isPipeline) {
      actionArguments.put(createPipelineArtifactArgument());
    }
    actionJson.put("arguments", actionArguments);
    return actionJson;
  }

  private JSONObject createActionArgument(String name, String value) {
    JSONObject actionArgument = new JSONObject();
    actionArgument.put("name", name);
    actionArgument.put("value", value);
    return actionArgument;
  }

  private JSONObject createPipelineArtifactArgument() {
    JSONObject actionArgument = new JSONObject();
    JSONObject valueObject = new JSONObject();
    valueObject.put("scope", "SYSTEM");
    valueObject.put("name", "cdap-data-pipeline");
    valueObject.put("version", "[6.1.1, 7.0.0-SNAPSHOT)");
    actionArgument.put("name", "artifact");
    actionArgument.put("value", valueObject);

    return actionArgument;
  }

  private String filterVersions(String cdapArtifact) {
    String empty = "";
    if (cdapArtifact == null || cdapArtifact.isEmpty()) {
      return empty;
    }
    int leftBracket = cdapArtifact.indexOf("[");
    if (leftBracket < 0) {
      leftBracket = cdapArtifact.indexOf("(");
    }
    if (leftBracket < 0) {
      return empty;
    }

    int rightBracket = cdapArtifact.indexOf("]");
    if (rightBracket < 0) {
      rightBracket = cdapArtifact.indexOf(")");
    }
    if (rightBracket < 0) {
      return empty;
    }

    return cdapArtifact.substring(leftBracket, rightBracket + 1);
  }

  /**
   * Initializes this Mojo, extracts all necessary paths and configurations.
   */
  private void initialize() {
    created = Instant.now().toEpochMilli();
    cdapVersion = filterVersions(cdapArtifacts[0]);
    configFilename = artifactId + "-" + version + ".json";
    if (isPipeline && ONE_STEP_DEPLOY_PLUGIN.equals(actionType)) {
      //Default value of pipeline is create_pipeline_draft, developer also can define it in pom.xml
      actionType = CREATE_PIPELINE_DRAFT;
    }

    File buildDirectoryDir = new File(buildDirectory);
    buildDirectoryDir.mkdirs();
  }

  /**
   * Prints the header for this mojo with all the information it needs to execute correctly.
   */
  private void printHeader() {
    getLog().info(repeat("-", 72));
    getLog().info("CDAP Plugin JSON - Spec Json creator");
    getLog().info(repeat("-", 72));
    getLog().info("specVersion          : " + version);
    getLog().info("label                : " + label);
    getLog().info("description          : " + description);
    if (!Strings.isNullOrEmpty(author)) {
      getLog().info("author               : " + author);
    }
    if (!Strings.isNullOrEmpty(org)) {
      getLog().info("org                  : " + org);
    }
    getLog().info("created              : " + created);
    getLog().info("cdapVersion          : " + cdapVersion);
    getLog().info("action type          : " + actionType);
    getLog().info("action arg name      : " + artifactId);
    getLog().info("action arg version   : " + version);
    getLog().info("action arg scope     : " + scope);
    getLog().info("action arg config    : " + configFilename);
    if (categories != null) {
      getLog().info("categories");
      for (String category : categories) {
        getLog().info(" " + category);
      }
    }
    if (additionalActionArguments != null) {
      getLog().info("additionalActionArguments");
      for (String pair[] : additionalActionArguments) {
        getLog().info(" " + pair[0] + " : " + pair[1]);
      }
    }
    getLog().info(repeat("-", 72));
  }

  /**
   * Prints footer.
   */
  private void printFooter() {
    getLog().info(repeat("-", 72));
  }

  /**
   * Repeats text for specified number of times
   */
  private String repeat(String text, int numRepetitions) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < numRepetitions; i++) {
      builder.append(text);
    }

    return builder.toString();
  }

}
