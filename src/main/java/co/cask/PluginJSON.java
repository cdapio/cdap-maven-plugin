/*
 * Copyright © 2017 Cask Data, Inc.
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

package co.cask;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * This mojo is used for creating the CDAP Plugin JSON file.
 */
@Mojo(name = "create-plugin-json")
public class PluginJSON extends AbstractMojo {

  @Parameter(alias="version-range", defaultValue = "[4.0.0,10.0.0-SNAPSHOT)", required = false)
  private String versionRange;

  @Parameter(alias="cdap-artifacts", required = true)
  private String[] cdapArtifacts;

  @Parameter(defaultValue = "docs")
  private String docsDirectory;

  @Parameter(defaultValue = "widgets")
  private String widgetsDirectory;

  @Parameter(defaultValue="${project}", readonly=true, required=true)
  private MavenProject project;

  /**
   * The base project directory.
   */
  private File baseDirectory;

  /**
   * Directory where plugin widget(s) are stored.
   */
  private File widgetDirectory;

  /**
   * Directory where plugin documentation(s) is stored.
   */
  private File docDirectory;

  /**
   * Target directory where all the build artifacts are stored.
   */
  private File buildDirectory;

  /**
   * Project artifact Id.
   */
  private String artifactId;

  /**
   * Project group Id.
   */
  private String groupId;

  /**
   * Version of the project.
   */
  private String version;

  /**
   *
   * @throws MojoExecutionException
   */
  public void execute()  throws MojoExecutionException {
    // Read in all the configurations.
    initialize();

    // Print header.
    printHeader();

    // Checks if the widget path specified by the user is a directory or not. If the path specified is not
    // not directory, then we would bail from further processing.
    if (! widgetDirectory.isDirectory()) {
      throw new MojoExecutionException(
        String.format("Widgets path '%s' specified is not a directory or directory not present",
                      widgetDirectory.getPath())
      );
    }

    // We iterate through widgets and documentation directories creating the output configurations.
    JSONObject output = new JSONObject();
    JSONObject properties = new JSONObject();
    for (Map.Entry<String, String> entry : getDocumentation(docDirectory).entrySet()) {
      properties.put(entry.getKey(), entry.getValue());
    }
    for (Map.Entry<String, String> entry : getWidgets(widgetDirectory).entrySet()) {
      properties.put(entry.getKey(), entry.getValue());
    }
    output.put("properties", properties);
    JSONArray artifacts = new JSONArray();
    for (String artifact : cdapArtifacts) {
      artifacts.put(String.format("%s%s", artifact, versionRange));
    }
    output.put("parents", artifacts);

    try {
      File outputFile = new File(buildDirectory, artifactId + "-" + version + ".json");
      if (outputFile.exists()) {
        outputFile.delete();
      }
      outputFile.createNewFile();
      FileUtils.fileWrite(outputFile, output.toString(2));
      getLog().info("Successfully created  : " + project.getArtifactId() + "-" +
                      project.getVersion() + ".json");
    } catch (IOException e) {
      throw new MojoExecutionException(e.getMessage());
    }

    printFooter();
  }


  /**
   * Inspects all the Widget files to generate properties to be included in plugin JSON.
   *
   * @param directory where widgets are stored.
   * @return Map of property and correspoinding widget JSON.
   * @throws MojoExecutionException thrown in case of any execution error.
   */
  private Map<String, String> getWidgets(File directory) throws MojoExecutionException {
    Map<String, String> properties = new TreeMap<String, String>();
    File[] files = directory.listFiles();

    // Iterate through all widget files.
    for (File file : files) {

      // If it's not a file, skip the file.
      if (!file.isFile()) {
        getLog().warn(
          String.format("Widget path '%s' is not a file. Skipping", file.getPath())
        );
        continue;
      }

      String ext = FileUtils.extension(file.getName());
      if (! ext.equalsIgnoreCase("json")) {
        getLog().warn(
          String.format("Skipping non JSON file '%s'", file.getName())
        );
        continue;
      }

      String name = file.getName();
      try {
        JSONTokener tokener = new JSONTokener(FileUtils.fileRead(file.getAbsoluteFile(), "UTF-8"));
        JSONObject object = new JSONObject(tokener);
        properties.put(String.format("%s.%s", "widgets", FileUtils.removeExtension(name)), object.toString(2));
      } catch (FileNotFoundException e) {
        throw new MojoExecutionException(
          String.format("Unable to access Widget file '%s' or not found. %s", file.getName(), e.getMessage())
        );
      } catch (JSONException e) {
        throw new MojoExecutionException(
          String.format("Widget file '%s' has incorrect JSON. %s", file.getName(), e.getMessage())
        );
      } catch (IOException e) {
        throw new MojoExecutionException(
          String.format("Issue reading Widget file '%s'. %s", file.getName(), e.getMessage())
        );
      }
    }
    return properties;
  }

  /**
   * Inspects all the Documentation files to generate properties to be included in plugin JSON.
   *
   * @param directory where documentations are stored.
   * @return Map of property and correspoinding Markdown documentation.
   * @throws MojoExecutionException thrown in case of any execution error.
   */
  private Map<String, String> getDocumentation(File directory) throws MojoExecutionException {
    Map<String, String> properties = new TreeMap<String, String>();
    File[] files = directory.listFiles();

    // Iterate through all markdown files.
    for (File file : files) {
      if (!file.isFile()) {
        getLog().warn(
          String.format("Documentation path '%s' is not a file. Skipping", file.getPath())
        );
        continue;
      }

      String ext = FileUtils.extension(file.getName());
      if (! ext.equalsIgnoreCase("md")) {
        getLog().warn(
          String.format("Skipping non JSON file '%s'", file.getName())
        );
        continue;
      }

      String name = file.getName();
      try {
        properties.put(String.format("%s.%s", "doc", FileUtils.removeExtension(name)),
                                     FileUtils.fileRead(file.getAbsoluteFile(), "UTF-8"));
      } catch (FileNotFoundException e) {
        throw new MojoExecutionException(
          String.format("Unable to access Documentation file '%s' or not found. %s",
                        file.getName(), e.getMessage())
        );
      } catch (IOException e) {
        throw new MojoExecutionException(
          String.format("Issue reading Documentation file '%s'. %s", file.getName(), e.getMessage())
        );
      }
    }
    return properties;
  }

  /**
   * Initializes this Mojo, extracts all necessary paths and configurations.
   */
  private void initialize() {
    groupId = project.getGroupId();
    artifactId = project.getArtifactId();
    version = project.getVersion();
    baseDirectory = project.getBasedir();
    buildDirectory = new File(project.getBuild().getDirectory());
    if (!widgetsDirectory.contains("/")) {
      widgetDirectory = new File(project.getBasedir() + "/" + widgetsDirectory);
    } else {
      widgetDirectory = new File(widgetsDirectory);
    }

    if (!docsDirectory.contains("/")) {
      docDirectory = new File(project.getBasedir() + "/" + docsDirectory);
    } else {
      docDirectory = new File(docsDirectory);
    }
  }

  /**
   * Prints the header for this mojo with all the information it needs to execute correctly.
   */
  private void printHeader() {
    getLog().info(StringUtils.repeat('-', 72));
    getLog().info("CDAP Plugin JSON");
    getLog().info(StringUtils.repeat('-', 72));
    getLog().info("Project              : " + project.getName());
    getLog().info("Group ID             : " + project.getGroupId());
    getLog().info("Artifact ID          : " + project.getArtifactId());
    getLog().info("Version              : " + project.getVersion());
    getLog().info("Base Directory       : " + project.getBasedir());
    getLog().info("Build Directory      : " + project.getBuild().getDirectory());
    getLog().info("Widgets Directory    : " + widgetDirectory.getPath());
    getLog().info("Docs Directory       : " + docDirectory.getPath());
    getLog().info("Plugin Version Range : " + versionRange);
    getLog().info("CDAP Artifacts");
    for (String artifact : cdapArtifacts) {
      getLog().info(" " + artifact);
    }
    getLog().info(StringUtils.repeat('-', 72));
  }

  /**
   * Prints footer. 
   */
  private void printFooter() {
    getLog().info(StringUtils.repeat('-', 72));
  }
}
