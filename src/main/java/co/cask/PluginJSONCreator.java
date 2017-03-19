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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * This mojo is used for creating the JSON file required for the plugin.
 */
@Mojo(name = "create-plugin-json")
public class PluginJSONCreator extends AbstractMojo {

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

  private File baseDirectory;
  private File widgetDirectory;
  private File docDirectory;
  private File buildDirectory;
  private String artifactId;
  private String groupId;
  private String version;

  public void execute()  throws MojoExecutionException {
    initialize();
    printHeader();

    File outputFile = new File(project.getBuild().getDirectory(),
                               project.getArtifactId() + "-" +
                               project.getVersion() + ".json");

    if (outputFile.exists()) {
      outputFile.delete();
    }

    try {
      outputFile.createNewFile();
    } catch (IOException e) {
      throw new MojoExecutionException(
        String.format("Failed to create file '%s'. %s", outputFile.getName(), e.getMessage())
      );
    }

    // Checks if the widget path specified by the user is a directory or not. If the path specified is not
    // not directory, then we would bail from further processing.
    if (! widgetDirectory.isDirectory()) {
      throw new MojoExecutionException(
        String.format("Widgets path '%s' specified is not a directory or directory not present",
                      widgetDirectory.getPath())
      );
    }

    // Now, we iterate through each of the files in the widget path. We process each widget file.

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
      artifacts.add(String.format("%s%s", artifact, versionRange));
    }
    output.put("parents", artifacts);

    try {
      FileUtils.fileWrite(outputFile, output.toJSONString());
      getLog().info("Successfully created  : " + project.getArtifactId() + "-" +
                      project.getVersion() + ".json");
    } catch (IOException e) {
      throw new MojoExecutionException(e.getMessage());
    }

    printFooter();
  }


  private Map<String, String> getWidgets(File directory) throws MojoExecutionException {
    JSONParser parser = new JSONParser();
    Map<String, String> properties = new TreeMap<String, String>();
    File[] files = directory.listFiles();
    for (File file : files) {
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
      FileReader reader = null;
      try {
        reader = new FileReader(file.getAbsoluteFile());
        JSONObject object = (JSONObject) parser.parse(reader);
        properties.put(String.format("%s.%s", "widgets", FileUtils.removeExtension(name)), object.toJSONString());
      } catch (FileNotFoundException e) {
        throw new MojoExecutionException(e.getMessage());
      } catch (ParseException e) {
        throw new MojoExecutionException(e.getMessage());
      } catch (IOException e) {
        throw new MojoExecutionException(e.getMessage());
      }
    }
    return properties;
  }

  private Map<String, String> getDocumentation(File directory) throws MojoExecutionException {
    Map<String, String> properties = new TreeMap<String, String>();
    File[] files = directory.listFiles();
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
      FileReader reader = null;
      try {
        properties.put(String.format("%s.%s", "doc", FileUtils.removeExtension(name)),
                                     FileUtils.fileRead(file.getAbsoluteFile(), "UTF-8"));
      } catch (FileNotFoundException e) {
        throw new MojoExecutionException(e.getMessage());
      } catch (IOException e) {
        throw new MojoExecutionException(e.getMessage());
      }
    }
    return properties;
  }

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

  private void printFooter() {
    getLog().info(StringUtils.repeat('-', 72));
  }
}
