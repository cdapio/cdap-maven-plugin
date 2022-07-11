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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import static io.cdap.Utils.newMavenSession;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PipelineJsonMojoTest {

  private static final String CREATE_PIPELINE_JSON_GOAL = "create-pipeline-json";
  private static final String EXPECTED_PIPELINE_JSON_FILE_PATH = "src/test/resources/expected_pipeline.json";
  private static final String EXPORTED_PIPELINE_JSON_FILE_PATH = "src/test/resources/target/test_exported_pipeline-1.0.0.json";
  private static final String INVALID_PIPELINE_JSON_POM_XML_FILE_PATH = "src/test/resources/test_poms/invalid-pipelineJson-pom.xml";
  public static final String VALID_PIPELINE_JSON_POM_XML_FILE_PATH = "src/test/resources/test_poms/pipelineJson-pom.xml";

  @Rule
  public MojoRule rule = new MojoRule();

  @After
  public void cleanUp() throws IOException {
    File output = new File(EXPORTED_PIPELINE_JSON_FILE_PATH);
    Files.deleteIfExists(output.toPath());
  }

  @Test
  public void execute_pipelineJsonFileProvided_configIsExtracted() throws Exception {
    //GIVEN
    File testPom = new File(VALID_PIPELINE_JSON_POM_XML_FILE_PATH);

    ProjectBuildingRequest buildingRequest = newMavenSession(rule).getProjectBuildingRequest();
    ProjectBuilder projectBuilder = rule.lookup(ProjectBuilder.class);
    MavenProject project = projectBuilder.build(testPom, buildingRequest).getProject();

    // WHEN
    PipelineJson mojo = (PipelineJson)rule.lookupConfiguredMojo(project, CREATE_PIPELINE_JSON_GOAL);
    mojo.execute();

    ObjectMapper mapper = new ObjectMapper();

    // THEN
    JsonNode result = mapper.readTree(Files.newInputStream(
        Paths.get(EXPORTED_PIPELINE_JSON_FILE_PATH)));

    JsonNode expected = mapper.readTree(Files.newInputStream(
        Paths.get(EXPECTED_PIPELINE_JSON_FILE_PATH)));

    assertEquals(expected, result);
  }

  @Test(expected = MojoExecutionException.class)
  public void execute_invalidPipelineJsonFileProvided_fileNotExtracted() throws Exception {
    //GIVEN
    File testPom = new File(PlexusTestCase.getBasedir(), INVALID_PIPELINE_JSON_POM_XML_FILE_PATH);

    ProjectBuildingRequest buildingRequest = newMavenSession(rule).getProjectBuildingRequest();
    ProjectBuilder projectBuilder = rule.lookup(ProjectBuilder.class);
    MavenProject project = projectBuilder.build(testPom, buildingRequest).getProject();

    // WHEN
    PipelineJson mojo = (PipelineJson)rule.lookupConfiguredMojo(project, CREATE_PIPELINE_JSON_GOAL);
    mojo.execute();

    // THEN
    File output = new File(EXPORTED_PIPELINE_JSON_FILE_PATH);

    assertFalse(output.exists());
  }
}
