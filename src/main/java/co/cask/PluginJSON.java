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

import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.IOUtils;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * This mojo is used for creating the CDAP Plugin JSON file.
 */
@Mojo(name = "create-plugin-json")
public class PluginJSON extends AbstractMojo {

  @Parameter(alias = "cdap-artifacts", required = true)
  private String[] cdapArtifacts;

  @Parameter(defaultValue = "docs")
  private String docsDirectory;

  @Parameter(defaultValue = "widgets")
  private String widgetsDirectory;

  @Parameter(defaultValue = "icons")
  private String iconsDirectory;

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
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
   * Directory where plugin icon(s) are stored
   */
  private File iconDirectory;

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
  public void execute() throws MojoExecutionException {
    // Read in all the configurations.
    initialize();

    // Print header.
    printHeader();

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
      artifacts.put(artifact);
    }
    output.put("parents", artifacts);

    try {
      File outputFile = new File(buildDirectory, artifactId + "-" + version + ".json");
      if (outputFile.exists()) {
        outputFile.delete();
      }
      outputFile.createNewFile();
      FileUtils.fileWrite(outputFile.getAbsolutePath(), output.toString(2));
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

    Map<String, File> iconFiles = getFileNameMap(iconDirectory.listFiles());

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
      if (!ext.equalsIgnoreCase("json")) {
        getLog().warn(
          String.format("Skipping non JSON file '%s'", file.getName())
        );
        continue;
      }

      String name = file.getName();
      try {
        JSONTokener tokener = new JSONTokener(FileUtils.fileRead(file.getAbsoluteFile(), "UTF-8"));
        JSONObject object = new JSONObject(tokener);

        String fileName = FileUtils.removeExtension(name);
        addIcon(object, iconFiles, fileName);
        properties.put(String.format("%s.%s", "widgets", fileName), object.toString(2));
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
   * Inspects all the Icon files to add properties to related widget.
   */
  private void addIcon(JSONObject widgetsJson, Map<String, File> iconFiles, String fileName) throws IOException {
    if (iconFiles.containsKey(fileName)) {
      JSONObject iconObject = new JSONObject();
      iconObject.put("type", "inline");
      Map<String, String> arguments = new TreeMap<>();
      File file = iconFiles.get(fileName);
      String mediaType = URLConnection.guessContentTypeFromName(file.getName());
      arguments.put("data", getDataURISchemaAsString(file, mediaType));
      iconObject.put("arguments", arguments);
      widgetsJson.put("icon", iconObject);
    }
  }

  /**
   * Returns the Data URI Scheme of File
   */
  private String getDataURISchemaAsString(File file, String mediaType) throws IOException {
    try (FileInputStream inStream = new FileInputStream(file);
         ByteArrayOutputStream outStream = new ByteArrayOutputStream();
         Base64OutputStream outStreamBase64 = new Base64OutputStream(outStream)) {
      IOUtils.copy(inStream, outStreamBase64);
      outStreamBase64.flush();
      return "data:" + mediaType + ";base64," + outStream.toString();
    }
  }

  /**
   * Remove extensions from files in directory and return a map of names to files.
   */
  private Map<String, File> getFileNameMap(File[] listFiles) {
    Map<String, File> names = new HashMap<>();

    if (listFiles != null) {
      for (File file : listFiles) {
        String fileName = FileUtils.removeExtension(file.getName());
        if (names.containsKey(fileName)) {
          getLog().warn(String.format("'%s' is being ignored. '%s' will be used.", file.getName(),
                                      names.get(fileName).getName()));
        } else {
          names.put(fileName, file);
        }
      }
    }

    return names;
  }

  /**
   * Inspects all the Documentation files to generate properties to be included in plugin JSON.
   *
   * @param directory where documentations are stored.
   * @return Map of property and correspoinding Markdown documentation.
   * @throws MojoExecutionException thrown in case of any execution error.
   */
  private Map<String, String> getDocumentation(File directory) throws MojoExecutionException {
    Map<String, String> properties = new TreeMap<>();
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
      if (!ext.equalsIgnoreCase("md")) {
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
    widgetDirectory = getAndValidate(baseDirectory, widgetsDirectory);
    iconDirectory = getAndValidate(baseDirectory, iconsDirectory);
    docDirectory = getAndValidate(baseDirectory, docsDirectory);
  }

  /**
   * Validate directory based on project
   */
  private File getAndValidate(File baseDirectory, String directoryPath) {
    File directory;
    if (directoryPath.startsWith("/")) {
      directory = new File(directoryPath);
    } else {
      directory = new File(baseDirectory, directoryPath);
    }

    if (!directory.exists()) {
      getLog().warn(String.format("'%s' does not exist.", directoryPath));
    }
    if (!directory.isDirectory()) {
      getLog().warn(String.format("'%s' is not a directory.", directoryPath));
    }
    return directory;
  }

  /**
   * Prints the header for this mojo with all the information it needs to execute correctly.
   */
  private void printHeader() {
    getLog().info(repeat("-", 72));
    getLog().info("CDAP Plugin JSON");
    getLog().info(repeat("-", 72));
    getLog().info("Project              : " + project.getName());
    getLog().info("Group ID             : " + project.getGroupId());
    getLog().info("Artifact ID          : " + project.getArtifactId());
    getLog().info("Version              : " + project.getVersion());
    getLog().info("Base Directory       : " + project.getBasedir());
    getLog().info("Build Directory      : " + project.getBuild().getDirectory());
    getLog().info("Widgets Directory    : " + widgetDirectory.getPath());
    getLog().info("Icons Directory      : " + iconDirectory.getPath());
    getLog().info("Docs Directory       : " + docDirectory.getPath());
    getLog().info("CDAP Artifacts");
    for (String artifact : cdapArtifacts) {
      getLog().info(" " + artifact);
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
