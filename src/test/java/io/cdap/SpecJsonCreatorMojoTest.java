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
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.PlexusTestCase;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Rule;
import org.junit.Test;

import static io.cdap.Utils.newMavenSession;
import static org.junit.Assert.*;

public class SpecJsonCreatorMojoTest {

  @Rule
  public MojoRule rule = new MojoRule();

  @Test
  public void execute_pluginIsConfiguredWithMinimumValues_specJsonIsGeneratedWithDefaultValuesInjected() throws Exception {
    // GIVEN
    File testPom = new File(PlexusTestCase.getBasedir(), "src/test/resources/test_poms/default-values-pom.xml");

    ProjectBuildingRequest buildingRequest = newMavenSession(rule).getProjectBuildingRequest();
    ProjectBuilder projectBuilder = rule.lookup(ProjectBuilder.class);
    MavenProject project = projectBuilder.build(testPom, buildingRequest).getProject();

    // WHEN
    SpecJsonCreator mojo = (SpecJsonCreator) rule.lookupConfiguredMojo(project, "create-plugin-spec-json");
    mojo.execute();

    // THEN
    JSONTokener jsonTokener = new JSONTokener(Files.newInputStream(Paths.get("src/test/resources/spec.json")));
    JSONObject object = new JSONObject(jsonTokener);

    assertEquals("0.1.0", object.get("specVersion"));
    assertEquals("Adhoc SQL Plugin", object.get("label"));
    assertFalse(object.has("org"));
    assertFalse(object.has("author"));
    assertEquals("Optional description", object.get("description"));
    assertEquals("one_step_deploy_plugin", object.getJSONArray("actions").getJSONObject(0).get("type"));
    assertEquals("Adhoc SQL Plugin", object.getJSONArray("actions").getJSONObject(0).get("label"));
  }

  @Test
  public void execute_pluginIsConfiguredWithMinimumValues_specJsonIsGeneratedWithDefaultAuthorOrgValuesInjected() throws Exception {
    // GIVEN
    File testPom = new File(PlexusTestCase.getBasedir(), "src/test/resources/test_poms/default-values-with-author-org-pom.xml");

    ProjectBuildingRequest buildingRequest = newMavenSession(rule).getProjectBuildingRequest();
    ProjectBuilder projectBuilder = rule.lookup(ProjectBuilder.class);
    MavenProject project = projectBuilder.build(testPom, buildingRequest).getProject();

    // WHEN
    SpecJsonCreator mojo = (SpecJsonCreator) rule.lookupConfiguredMojo(project, "create-plugin-spec-json");
    mojo.execute();

    // THEN
    JSONTokener jsonTokener = new JSONTokener(Files.newInputStream(Paths.get("src/test/resources/spec.json")));
    JSONObject object = new JSONObject(jsonTokener);

    assertEquals("0.1.0", object.get("specVersion"));
    assertEquals("Adhoc SQL Plugin", object.get("label"));
    assertEquals("org1", object.get("org"));
    assertEquals("author1", object.get("author"));
    assertEquals("Optional description", object.get("description"));
    assertEquals("one_step_deploy_plugin", object.getJSONArray("actions").getJSONObject(0).get("type"));
    assertEquals("Adhoc SQL Plugin", object.getJSONArray("actions").getJSONObject(0).get("label"));
  }

  @Test
  public void execute_pluginConfigurationWithNoDescription_specJsonIsGeneratedWithEmptyDescription() throws Exception {
    // GIVEN
    File testPom = new File(PlexusTestCase.getBasedir(), "src/test/resources/test_poms/no-default-values-pom.xml");

    ProjectBuildingRequest buildingRequest = newMavenSession(rule).getProjectBuildingRequest();
    ProjectBuilder projectBuilder = rule.lookup(ProjectBuilder.class);
    MavenProject project = projectBuilder.build(testPom, buildingRequest).getProject();

    // WHEN
    SpecJsonCreator mojo = (SpecJsonCreator) rule.lookupConfiguredMojo(project, "create-plugin-spec-json");
    mojo.execute();

    // THEN
    JSONTokener jsonTokener = new JSONTokener(Files.newInputStream(Paths.get("src/test/resources/spec.json")));
    JSONObject object = new JSONObject(jsonTokener);

    assertFalse(object.has("description"));
  }

  @Test
  public void execute_pluginConfigurationWithIsPipeline_specJsonGeneratedWithArtifactSection() throws Exception {
    // GIVEN
    File testPom = new File(PlexusTestCase.getBasedir(), "src/test/resources/test_poms/specJson-pipeline-pom.xml");
    final int ARTIFACT_INDEX = 4;

    ProjectBuildingRequest buildingRequest = newMavenSession(rule).getProjectBuildingRequest();
    ProjectBuilder projectBuilder = rule.lookup(ProjectBuilder.class);
    MavenProject project = projectBuilder.build(testPom, buildingRequest).getProject();

    // WHEN
    SpecJsonCreator mojo = (SpecJsonCreator) rule.lookupConfiguredMojo(project, "create-plugin-spec-json");
    mojo.execute();

    // THEN
    JSONTokener jsonTokener = new JSONTokener(Files.newInputStream(Paths.get("src/test/resources/spec.json")));
    JSONObject object = new JSONObject(jsonTokener);

    JSONObject artifactObject = object.getJSONArray("actions").getJSONObject(0)
        .getJSONArray("arguments").getJSONObject(ARTIFACT_INDEX).getJSONObject("value");

    assertEquals(expectedPipelineSpec(), artifactObject.toString());
  }

  private String expectedPipelineSpec() {
    return "{\"scope\":\"SYSTEM\",\"name\":\"cdap-data-pipeline\",\"version\":\"[6.1.1, 7.0.0-SNAPSHOT)\"}";
  }

}
