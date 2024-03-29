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

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "package-artifacts-for-hub")
public class ArtifactPackager extends AbstractMojo {

  @Parameter(defaultValue = "${project.build.directory}/packages/", required = true)
  private String relativeOutputDir;

  @Parameter(property = "buildDirectory", defaultValue = "${project.build.directory}")
  private String buildDirectory;

  @Parameter(property = "baseDir", defaultValue = "${project.baseDir}")
  private String baseDirectory;

  @Parameter(property = "artifactId", defaultValue = "${project.artifactId}")
  private String artifactId;

  @Parameter(property = "version", defaultValue = "${project.version}")
  private String version;

  @Parameter(property = "isPipeline", defaultValue = "false")
  private boolean isPipeline;

  @Parameter(defaultValue = "icons")
  private String iconsDirectory;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    String currentPath = null;
    try {
      currentPath = new File(".").getCanonicalPath();
      getLog().info("Working Directory = " + System.getProperty("user.dir"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    getLog().info("Current dir:" + currentPath);

    File outputDirectory = Paths.get(relativeOutputDir, artifactId, version).toFile();
    File iconDirectory = new File(baseDirectory, iconsDirectory);

    getLog().info("Creating artifact output dir at: " + outputDirectory.getAbsolutePath());
    outputDirectory.mkdirs();

    //jar
    moveFile(outputDirectory, artifactId + "-" + version + ".jar");

    //config json
    moveFile(outputDirectory, artifactId + "-" + version + ".json");

    //spec json
    moveFile(outputDirectory, "spec.json");

    //icon file
    if (iconDirectory.exists()) {
      File[] iconFiles = iconDirectory.listFiles();
      if (iconFiles != null) {
        for (File icon : iconFiles) {
          moveFile(outputDirectory, icon, icon.getName());
        }
      }
    }
  }

  private void moveFile(File outputDirectory, String fileName) {
    File artifactSource = new File(buildDirectory, fileName);
    if (artifactSource.exists()) {
      moveFile(outputDirectory, artifactSource, fileName);
    } else {
      // Warning as jar may not exist for pipeline based artifacts
      getLog().warn(artifactSource.getName() + " does not exist!");
    }

  }

  private void moveFile(File outputDirectory, File fileSource, String fileName) {
    File fileDestination = new File(outputDirectory, fileName);
    try {
      if (!isPipeline || !fileName.endsWith(".jar")) {
        // The pipeline only copy spec.json and pipeline.json to destination path
        getLog().info("Copying " + fileName + " to " + fileDestination.getCanonicalPath());
        FileUtils.copyFile(fileSource, fileDestination);
      }
    } catch (IOException e) {
      getLog().error(e);
    }
  }
}
