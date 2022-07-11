package io.cdap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

@Mojo(name = "create-pipeline-json")
public class PipelineJSON extends AbstractMojo {

  //Location of exported pipeline.json
  @Parameter(required = true)
  private String pipelineJsonFile;

  @Parameter(property = "buildDirectory", defaultValue = "${project.build.directory}")
  private String buildDirectory;

  @Parameter(property = "artifactId", defaultValue = "${project.artifactId}")
  private String artifactId;

  @Parameter(property = "version", defaultValue = "${project.version}")
  private String version;

  public void execute() throws MojoExecutionException {

    // Pipeline Modules may not have the build dir created by the time this goal runs
    File outputDir = new File(buildDirectory);
    outputDir.mkdirs();

    try {
      System.out.println("PATH : " + Paths.get(pipelineJsonFile));
      JSONTokener jsonTokener = new JSONTokener(Files.newInputStream(Paths.get(pipelineJsonFile)));
      JSONObject output = new JSONObject(jsonTokener);

      if (!output.has("config")) {
        getLog().error("Incorrect PipelineJsonFile");
        throw new MojoExecutionException("Incorrect PipelineJsonFile");
      }
      File outputFile = new File(outputDir, artifactId + "-" + version + ".json");

      if (outputFile.exists()) {
        outputFile.delete();
      }

      outputFile.createNewFile();
      FileUtils.fileWrite(outputFile.getAbsolutePath(), output.get("config").toString());
      getLog().info("Successfully created: " + pipelineJsonFile);

    } catch (IOException e) {
      throw new MojoExecutionException(e.getMessage());
    }
  }
}
