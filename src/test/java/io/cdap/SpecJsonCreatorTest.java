package io.cdap;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SpecJsonCreatorTest{

  private static final String SNAPSHOT_6_0_0_9_0_0 = "[6.0.0-SNAPSHOT,9.0.0-SNAPSHOT)";
  private static final String BUILD_DIRECTORY = "./src/test/resources/";
  private static final String SPEC_JSON_PATHNAME = BUILD_DIRECTORY + "spec.json";

  private String actionType;
  private String scope;
  private String[] cdapArtifacts;
  private String artifactId;
  private String version;
  private String label;
  private String description;
  private String author;
  private String org;
  private String[] categories;
  private boolean isPipeline;
  private String[][] additionalActionArguments;

  @Before
  public void initVariables() {

    /**
     * Default values used in the tests. Those values could be overwritten in specific tests.
     */
    actionType = "one_step_deploy_plugin";
    scope = "scopeTest";
    cdapArtifacts = new String[]{
        "system:cdap-data-pipeline[6.0.0-SNAPSHOT,9.0.0-SNAPSHOT)",
        "system:cdap-data-pipeline[7.0.0-SNAPSHOT,8.0.0-SNAPSHOT)"};
    artifactId = "artifactIdTest";
    version = "versionTest";
    label = "labelTest";
    description = "descriptionTest";
    author = "authorTest";
    org = "orgTest";
    categories = new String[]{"categoryTest1", "categoryTest2"};
    additionalActionArguments = new String[][]{
        {"arg1key", "arg1value"},
        {"arg2key", "arg2value"}
    };
    isPipeline = false;
  }

  @Test
  public void createSpec_succeed_allFieldsPopulated() {
    /*
     * GIVEN
     */

    SpecJsonCreator specJsonCreator = getSpecJsonCreator();

    /*
     * WHEN
     */

    SpecJsonTestBean bean = null;
    try {
      specJsonCreator.execute();
      ObjectMapper objectMapper = new ObjectMapper();
      bean = objectMapper.readValue(new File(SPEC_JSON_PATHNAME), SpecJsonTestBean.class);

    } catch (MojoExecutionException | IOException e) {
      fail(e.getMessage());
    }

    /*
     * THEN
     */

    // Main bean
    assertEquals(author, bean.getAuthor());
    assertEquals(version, bean.getSpecVersion());
    assertEquals(org, bean.getOrg());
    assertTrue(bean.getCreated() > 1608706620012L);
    assertEquals(SNAPSHOT_6_0_0_9_0_0, bean.getCdapVersion());
    assertEquals(description, bean.getDescription());
    assertEquals(label, bean.getLabel());

    //Categories
    assertTrue(bean.getCategories().size() == 2);
    assertEquals(categories[0], bean.getCategories().get(0));
    assertEquals(categories[1], bean.getCategories().get(1));

    //Actions
    assertTrue(bean.getActions().size() == 1);
    assertEquals(label, bean.getActions().get(0).getLabel());
    assertEquals(SpecJsonCreator.ONE_STEP_DEPLOY_PLUGIN, bean.getActions().get(0).getType());

    //Action's arguments
    assertTrue(bean.getActions().get(0).getArguments().size() == 6);
    assertEquals("name", bean.getActions().get(0).getArguments().get(0).getName());
    assertEquals(artifactId, bean.getActions().get(0).getArguments().get(0).getValue());
    assertEquals("version", bean.getActions().get(0).getArguments().get(1).getName());
    assertEquals(version, bean.getActions().get(0).getArguments().get(1).getValue());
    assertEquals("scope", bean.getActions().get(0).getArguments().get(2).getName());
    assertEquals(scope, bean.getActions().get(0).getArguments().get(2).getValue());
    assertEquals("config", bean.getActions().get(0).getArguments().get(3).getName());
    assertEquals(String.join("",artifactId,"-",version,".json"), bean.getActions().get(0).getArguments().get(3).getValue());

    //Action's additional arguments
    assertEquals(additionalActionArguments[0][0], bean.getActions().get(0).getArguments().get(4).getName());
    assertEquals(additionalActionArguments[0][1], bean.getActions().get(0).getArguments().get(4).getValue());
    assertEquals(additionalActionArguments[1][0], bean.getActions().get(0).getArguments().get(5).getName());
    assertEquals(additionalActionArguments[1][1], bean.getActions().get(0).getArguments().get(5).getValue());
  }

  @Test
  public void createSpec_succeed_noCategories() {
    /*
     * GIVEN
     */

    categories = null;
    SpecJsonCreator specJsonCreator = getSpecJsonCreator();

    /*
     * WHEN
     */

    SpecJsonTestBean bean = null;
    try {
      specJsonCreator.execute();
      ObjectMapper objectMapper = new ObjectMapper();
      bean = objectMapper.readValue(new File(SPEC_JSON_PATHNAME), SpecJsonTestBean.class);

    } catch (MojoExecutionException | IOException e) {
      fail(e.getMessage());
    }

    /*
     * THEN
     */

    // Main bean
    assertEquals(author, bean.getAuthor());
    assertEquals(version, bean.getSpecVersion());
    assertEquals(org, bean.getOrg());
    assertTrue(bean.getCreated() > 1608706620012L);
    assertEquals(SNAPSHOT_6_0_0_9_0_0, bean.getCdapVersion());
    assertEquals(description, bean.getDescription());
    assertEquals(label, bean.getLabel());

    //Categories
    assertNull(bean.getCategories());

    //Actions
    assertTrue(bean.getActions().size() == 1);
    assertEquals(label, bean.getActions().get(0).getLabel());
    assertEquals(SpecJsonCreator.ONE_STEP_DEPLOY_PLUGIN, bean.getActions().get(0).getType());

    //Action's arguments
    assertTrue(bean.getActions().get(0).getArguments().size() == 6);
    assertEquals("name", bean.getActions().get(0).getArguments().get(0).getName());
    assertEquals(artifactId, bean.getActions().get(0).getArguments().get(0).getValue());
    assertEquals("version", bean.getActions().get(0).getArguments().get(1).getName());
    assertEquals(version, bean.getActions().get(0).getArguments().get(1).getValue());
    assertEquals("scope", bean.getActions().get(0).getArguments().get(2).getName());
    assertEquals(scope, bean.getActions().get(0).getArguments().get(2).getValue());
    assertEquals("config", bean.getActions().get(0).getArguments().get(3).getName());
    assertEquals(String.join("",artifactId,"-",version,".json"), bean.getActions().get(0).getArguments().get(3).getValue());

    //Action's additional arguments
    assertEquals(additionalActionArguments[0][0], bean.getActions().get(0).getArguments().get(4).getName());
    assertEquals(additionalActionArguments[0][1], bean.getActions().get(0).getArguments().get(4).getValue());
    assertEquals(additionalActionArguments[1][0], bean.getActions().get(0).getArguments().get(5).getName());
    assertEquals(additionalActionArguments[1][1], bean.getActions().get(0).getArguments().get(5).getValue());
  }

  @Test
  public void createSpec_succeed_noAdditionalArguments() {
    /*
     * GIVEN
     */

    additionalActionArguments = null;
    SpecJsonCreator specJsonCreator = getSpecJsonCreator();

    /*
     * WHEN
     */

    SpecJsonTestBean bean = null;
    try {
      specJsonCreator.execute();
      ObjectMapper objectMapper = new ObjectMapper();
      bean = objectMapper.readValue(new File(SPEC_JSON_PATHNAME), SpecJsonTestBean.class);

    } catch (MojoExecutionException | IOException e) {
      fail(e.getMessage());
    }

    /*
     * THEN
     */

    // Main bean
    assertEquals(author, bean.getAuthor());
    assertEquals(version, bean.getSpecVersion());
    assertEquals(org, bean.getOrg());
    assertTrue(bean.getCreated() > 1608706620012L);
    assertEquals(SNAPSHOT_6_0_0_9_0_0, bean.getCdapVersion());
    assertEquals(description, bean.getDescription());
    assertEquals(label, bean.getLabel());

    //Categories
    assertTrue(bean.getCategories().size() == 2);
    assertEquals(categories[0], bean.getCategories().get(0));
    assertEquals(categories[1], bean.getCategories().get(1));

    //Actions
    assertTrue(bean.getActions().size() == 1);
    assertEquals(label, bean.getActions().get(0).getLabel());
    assertEquals(SpecJsonCreator.ONE_STEP_DEPLOY_PLUGIN, bean.getActions().get(0).getType());

    //Action's arguments
    assertTrue(bean.getActions().get(0).getArguments().size() == 4);
    assertEquals("name", bean.getActions().get(0).getArguments().get(0).getName());
    assertEquals(artifactId, bean.getActions().get(0).getArguments().get(0).getValue());
    assertEquals("version", bean.getActions().get(0).getArguments().get(1).getName());
    assertEquals(version, bean.getActions().get(0).getArguments().get(1).getValue());
    assertEquals("scope", bean.getActions().get(0).getArguments().get(2).getName());
    assertEquals(scope, bean.getActions().get(0).getArguments().get(2).getValue());
    assertEquals("config", bean.getActions().get(0).getArguments().get(3).getName());
    assertEquals(String.join("",artifactId,"-",version,".json"), bean.getActions().get(0).getArguments().get(3).getValue());

  }

  @Test
  public void filterVersion_succeedEmpty_noLeftBracket() {
    /*
     * GIVEN
     */

    cdapArtifacts = new String[]{"system:cdap-data-pipeline-6.0.0-SNAPSHOT,9.0.0-SNAPSHOT)"};
    SpecJsonCreator specJsonCreator = getSpecJsonCreator();

    /*
     * WHEN
     */

    SpecJsonTestBean bean = null;
    try {
      specJsonCreator.execute();
      ObjectMapper objectMapper = new ObjectMapper();
      bean = objectMapper.readValue(new File(SPEC_JSON_PATHNAME), SpecJsonTestBean.class);

    } catch (MojoExecutionException | IOException e) {
      fail(e.getMessage());
    }

    /*
     * THEN
     */

    // Main bean
    assertEquals("", bean.getCdapVersion());
  }

  @Test
  public void filterVersion_succeedEmpty_noRightBracket() {
    /*
     * GIVEN
     */

    cdapArtifacts = new String[]{"system:cdap-data-pipeline(6.0.0-SNAPSHOT,9.0.0-SNAPSHOT"};
    SpecJsonCreator specJsonCreator = getSpecJsonCreator();

    /*
     * WHEN
     */

    SpecJsonTestBean bean = null;
    try {
      specJsonCreator.execute();
      ObjectMapper objectMapper = new ObjectMapper();
      bean = objectMapper.readValue(new File(SPEC_JSON_PATHNAME), SpecJsonTestBean.class);

    } catch (MojoExecutionException | IOException e) {
      fail(e.getMessage());
    }

    /*
     * THEN
     */

    // Main bean
    assertEquals("", bean.getCdapVersion());
  }

  @Test
  public void filterVersion_succeedEmpty_noBrackets() {
    /*
     * GIVEN
     */

    cdapArtifacts = new String[]{"system:cdap-data-pipeline-6.0.0-SNAPSHOT,9.0.0-SNAPSHOT"};
    SpecJsonCreator specJsonCreator = getSpecJsonCreator();

    /*
     * WHEN
     */

    SpecJsonTestBean bean = null;
    try {
      specJsonCreator.execute();
      ObjectMapper objectMapper = new ObjectMapper();
      bean = objectMapper.readValue(new File(SPEC_JSON_PATHNAME), SpecJsonTestBean.class);

    } catch (MojoExecutionException | IOException e) {
      fail(e.getMessage());
    }

    /*
     * THEN
     */

    // Main bean
    assertEquals("", bean.getCdapVersion());
  }

  private SpecJsonCreator getSpecJsonCreator() {
    SpecJsonCreator specJsonCreator = new SpecJsonCreator(
        actionType,
        scope,
        cdapArtifacts,
        artifactId,
        version,
        label,
        description,
        author,
        org,
        categories,
        BUILD_DIRECTORY,
        isPipeline,
        additionalActionArguments
    );
    return specJsonCreator;
  }
}