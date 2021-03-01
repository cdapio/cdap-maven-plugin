package io.cdap;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.PlexusTestCase;
import org.junit.Rule;
import org.junit.Test;

import static io.cdap.Utils.newMavenSession;
import static org.junit.Assert.assertTrue;

public class ArtifactPackagerMojoTest {

  @Rule
  public MojoRule rule = new MojoRule();

  @Test
  public void execute_relativeOutputDirSpecified_andFilesExistInBuildDir_ArtifactsAreMovedToOutputDir() throws Exception{
    //Given: a standard pom where relativeOutputDir = "/../packages/"
    String expectedOutputDir = "src/test/resources/packages/artifact/1.0.0/";
    FileUtils.deleteDirectory(new File(expectedOutputDir));

    String baseDir = PlexusTestCase.getBasedir();
    File testPom = new File(baseDir, "src/test/resources/test_poms/artifactPackager-pom.xml");

    ProjectBuildingRequest buildingRequest = newMavenSession(rule).getProjectBuildingRequest();
    ProjectBuilder projectBuilder = rule.lookup(ProjectBuilder.class);
    MavenProject project = projectBuilder.build(testPom, buildingRequest).getProject();

    //When: Goal is executed
    ArtifactPackager mojo = (ArtifactPackager) rule.lookupConfiguredMojo(project, "package-artifacts-for-hub");
    mojo.execute();

    //Then: Files are moved to expectedOutputDir
    File jarFile = new File(baseDir,expectedOutputDir+"artifact-1.0.0.jar");
    File configFile = new File(baseDir,expectedOutputDir+"artifact-1.0.0.json");
    File specFile = new File(baseDir,expectedOutputDir+"spec.json");

    assertTrue(jarFile.exists());
    assertTrue(configFile.exists());
    assertTrue(specFile.exists());

    FileUtils.deleteDirectory(new File(expectedOutputDir));
  }

  @Test
  public void execute_filesExistInBuildDir_artifactsAreMovedToDefaultOutputDir() throws Exception{
    //Given: a standard pom where relativeOutputDir not specified
    String expectedOutputDir = "src/test/resources/test_build_dir/packages/artifact/1.0.0/";
    FileUtils.deleteDirectory(new File(expectedOutputDir));

    String basedir = PlexusTestCase.getBasedir();
    File testPom = new File(basedir, "src/test/resources/test_poms/artifactPackager-no-relativePath-pom.xml");

    ProjectBuildingRequest buildingRequest = newMavenSession(rule).getProjectBuildingRequest();
    ProjectBuilder projectBuilder = rule.lookup(ProjectBuilder.class);
    MavenProject project = projectBuilder.build(testPom, buildingRequest).getProject();

    //When: Goal is executed
    ArtifactPackager mojo = (ArtifactPackager) rule.lookupConfiguredMojo(project, "package-artifacts-for-hub");
    mojo.execute();

    //Then: Files are moved to expectedOutputDir
    File jarFile = new File(basedir,expectedOutputDir+"artifact-1.0.0.jar");
    File configFile = new File(basedir,expectedOutputDir+"artifact-1.0.0.json");
    File specFile = new File(basedir,expectedOutputDir+"spec.json");

    assertTrue(jarFile.exists());
    assertTrue(configFile.exists());
    assertTrue(specFile.exists());

    FileUtils.deleteDirectory(new File(expectedOutputDir));
  }

  @Test
  public void execute_iconFilesExist_iconsAreMovedToOutputDir() throws Exception {
    //Given: relativeOutputDir = "/../packages/" && iconsDirectory = src/test/resources/test_icons_dir
    String expectedOutputDir = "src/test/resources/packages/artifact/1.0.0/";
    FileUtils.deleteDirectory(new File(expectedOutputDir));

    String basedir = PlexusTestCase.getBasedir();
    File testPom = new File(basedir, "src/test/resources/test_poms/artifactPackager-icons-pom.xml");

    ProjectBuildingRequest buildingRequest = newMavenSession(rule).getProjectBuildingRequest();
    ProjectBuilder projectBuilder = rule.lookup(ProjectBuilder.class);
    MavenProject project = projectBuilder.build(testPom, buildingRequest).getProject();

    //When: Goal is executed
    ArtifactPackager mojo = (ArtifactPackager) rule.lookupConfiguredMojo(project, "package-artifacts-for-hub");
    mojo.execute();

    //Then: Files are moved to expectedOutputDir
    File jarFile = new File(basedir,expectedOutputDir+"artifact-1.0.0.jar");
    File configFile = new File(basedir,expectedOutputDir+"artifact-1.0.0.json");
    File specFile = new File(basedir,expectedOutputDir+"spec.json");
    File iconFile = new File(basedir,expectedOutputDir+"icon.png");

    assertTrue(jarFile.exists());
    assertTrue(configFile.exists());
    assertTrue(specFile.exists());
    assertTrue(iconFile.exists());

    FileUtils.deleteDirectory(new File(expectedOutputDir));
  }
}
