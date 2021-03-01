package io.cdap;

import org.apache.maven.DefaultMaven;
import org.apache.maven.Maven;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.DefaultMavenExecutionResult;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequestPopulator;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.testing.MojoRule;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.internal.impl.SimpleLocalRepositoryManagerFactory;
import org.eclipse.aether.repository.LocalRepository;

public class Utils {

  public static MavenSession newMavenSession(MojoRule rule) {
    try {
      MavenExecutionRequest request = new DefaultMavenExecutionRequest();
      MavenExecutionResult result = new DefaultMavenExecutionResult();

      // populate sensible defaults, including repository basedir and remote repos
      MavenExecutionRequestPopulator populator;
      populator = rule.getContainer().lookup(MavenExecutionRequestPopulator.class);
      populator.populateDefaults(request);

      // this is needed to allow java profiles to get resolved; i.e. avoid during project builds:
      // [ERROR] Failed to determine Java version for profile java-1.5-detected @ org.apache.commons:commons-parent:22, /Users/alex/.m2/repository/org/apache/commons/commons-parent/22/commons-parent-22.pom, line 909, column 14
      request.setSystemProperties(System.getProperties());

      // and this is needed so that the repo session in the maven session
      // has a repo manager, and it points at the local repo
      // (cf MavenRepositorySystemUtils.newSession() which is what is otherwise done)
      DefaultMaven maven = (DefaultMaven)rule.getContainer().lookup(Maven.class);
      DefaultRepositorySystemSession repoSession =
          (DefaultRepositorySystemSession)maven.newRepositorySession(request);
      repoSession.setLocalRepositoryManager(
          new SimpleLocalRepositoryManagerFactory().newInstance(repoSession,
              new LocalRepository(request.getLocalRepository().getBasedir())));

      @SuppressWarnings("deprecation")
      MavenSession session = new MavenSession(rule.getContainer(),
          repoSession,
          request, result);
      return session;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
