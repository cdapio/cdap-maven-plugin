package io.cdap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.PlexusTestCase;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import static io.cdap.Utils.newMavenSession;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PipelineJsonMojoTest {

  private final String EXPECTED_PIPELINE_JSON_FILE = "{\"comments\":[],\"processTimingEnabled\":true,\"resources\":{\"memoryMB\":2048,\"virtualCores\":1},\"stageLoggingEnabled\":false,\"schedule\":\"0 * * * *\",\"engine\":\"spark\",\"numOfRecordsPreview\":100,\"postActions\":[],\"stages\":[{\"outputSchema\":\"{\\\"name\\\":\\\"fileRecord\\\",\\\"type\\\":\\\"record\\\",\\\"fields\\\":[{\\\"name\\\":\\\"offset\\\",\\\"type\\\":\\\"long\\\"},{\\\"name\\\":\\\"body\\\",\\\"type\\\":\\\"string\\\"}]}\",\"plugin\":{\"artifact\":{\"scope\":\"SYSTEM\",\"name\":\"core-plugins\",\"version\":\"2.6.0-SNAPSHOT\"},\"name\":\"File\",\"label\":\"File\",\"type\":\"batchsource\",\"properties\":{\"filenameOnly\":\"false\",\"schema\":\"{\\\"name\\\":\\\"fileRecord\\\",\\\"type\\\":\\\"record\\\",\\\"fields\\\":[{\\\"name\\\":\\\"offset\\\",\\\"type\\\":\\\"long\\\"},{\\\"name\\\":\\\"body\\\",\\\"type\\\":\\\"string\\\"}]}\",\"path\":\"test\",\"fileEncoding\":\"UTF-8\",\"format\":\"csv\",\"ignoreNonExistingFolders\":\"false\",\"skipHeader\":\"false\",\"recursive\":\"false\",\"referenceName\":\"test\"}},\"name\":\"File\",\"id\":\"File\"},{\"outputSchema\":[{\"schema\":\"{\\\"name\\\":\\\"fileRecord\\\",\\\"type\\\":\\\"record\\\",\\\"fields\\\":[{\\\"name\\\":\\\"offset\\\",\\\"type\\\":\\\"long\\\"},{\\\"name\\\":\\\"body\\\",\\\"type\\\":\\\"string\\\"}]}\",\"name\":\"etlSchemaBody\"}],\"plugin\":{\"artifact\":{\"scope\":\"SYSTEM\",\"name\":\"transform-plugins\",\"version\":\"2.6.0-SNAPSHOT\"},\"name\":\"CloneRecord\",\"label\":\"Record Duplicator\",\"type\":\"transform\",\"properties\":{\"copies\":\"1\"}},\"inputSchema\":[{\"schema\":\"{\\\"name\\\":\\\"fileRecord\\\",\\\"type\\\":\\\"record\\\",\\\"fields\\\":[{\\\"name\\\":\\\"offset\\\",\\\"type\\\":\\\"long\\\"},{\\\"name\\\":\\\"body\\\",\\\"type\\\":\\\"string\\\"}]}\",\"name\":\"File\"}],\"name\":\"Record Duplicator\",\"id\":\"Record-Duplicator\"},{\"outputSchema\":[{\"schema\":\"{\\\"name\\\":\\\"fileRecord\\\",\\\"type\\\":\\\"record\\\",\\\"fields\\\":[{\\\"name\\\":\\\"offset\\\",\\\"type\\\":\\\"long\\\"},{\\\"name\\\":\\\"body\\\",\\\"type\\\":\\\"string\\\"}]}\",\"name\":\"etlSchemaBody\"}],\"plugin\":{\"artifact\":{\"scope\":\"SYSTEM\",\"name\":\"core-plugins\",\"version\":\"2.6.0-SNAPSHOT\"},\"name\":\"File\",\"label\":\"File2\",\"type\":\"batchsink\",\"properties\":{\"schema\":\"{\\\"name\\\":\\\"fileRecord\\\",\\\"type\\\":\\\"record\\\",\\\"fields\\\":[{\\\"name\\\":\\\"offset\\\",\\\"type\\\":\\\"long\\\"},{\\\"name\\\":\\\"body\\\",\\\"type\\\":\\\"string\\\"}]}\",\"path\":\"test2\",\"format\":\"csv\",\"suffix\":\"yyyy-MM-dd-HH-mm\",\"referenceName\":\"test2\"}},\"inputSchema\":[{\"schema\":\"{\\\"name\\\":\\\"fileRecord\\\",\\\"type\\\":\\\"record\\\",\\\"fields\\\":[{\\\"name\\\":\\\"offset\\\",\\\"type\\\":\\\"long\\\"},{\\\"name\\\":\\\"body\\\",\\\"type\\\":\\\"string\\\"}]}\",\"name\":\"Record Duplicator\"}],\"name\":\"File2\",\"id\":\"File2\"}],\"driverResources\":{\"memoryMB\":2048,\"virtualCores\":1},\"maxConcurrentRuns\":1,\"connections\":[{\"from\":\"File\",\"to\":\"Record Duplicator\"},{\"from\":\"Record Duplicator\",\"to\":\"File2\"}],\"properties\":{}}";

  @Rule
  public MojoRule rule = new MojoRule();

  @After
  public void cleanUp() throws IOException {
    File output = new File("src/test/resources/target", "test_exported_pipeline-1.0.0.json");
    Files.deleteIfExists(output.toPath());
  }

  @Test
  public void execute_pipelineJsonFileProvided_configIsExtracted() throws Exception {
    //GIVEN
    File testPom = new File(PlexusTestCase.getBasedir(), "src/test/resources/test_poms/pipelineJson-pom.xml");

    ProjectBuildingRequest buildingRequest = newMavenSession(rule).getProjectBuildingRequest();
    ProjectBuilder projectBuilder = rule.lookup(ProjectBuilder.class);
    MavenProject project = projectBuilder.build(testPom, buildingRequest).getProject();

    // WHEN
    PipelineJSON mojo = (PipelineJSON)rule.lookupConfiguredMojo(project, "create-pipeline-json");
    mojo.execute();

    // THEN
    JSONTokener jsonTokener = new JSONTokener(Files.newInputStream(
        Paths.get("src/test/resources/target/test_exported_pipeline-1.0.0.json")));
    JSONObject object = new JSONObject(jsonTokener);

    assertEquals(EXPECTED_PIPELINE_JSON_FILE, object.toString());
  }

  @Test(expected = MojoExecutionException.class)
  public void execute_invalidPipelineJsonFileProvided_fileNotExtracted() throws Exception {
    //GIVEN
    File testPom = new File(PlexusTestCase.getBasedir(), "src/test/resources/test_poms/invalid-pipelineJson-pom.xml");

    ProjectBuildingRequest buildingRequest = newMavenSession(rule).getProjectBuildingRequest();
    ProjectBuilder projectBuilder = rule.lookup(ProjectBuilder.class);
    MavenProject project = projectBuilder.build(testPom, buildingRequest).getProject();

    // WHEN
    PipelineJSON mojo = (PipelineJSON)rule.lookupConfiguredMojo(project, "create-pipeline-json");
    mojo.execute();

    // THEN
    File output = new File("src/test/resources/target/test_exported_pipeline-1.0.0.json");

    assertFalse(output.exists());
  }
}
