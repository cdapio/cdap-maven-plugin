# CDAP Maven Plugin

<a href="https://cdap-users.herokuapp.com/"><img alt="Join CDAP community" src="https://cdap-users.herokuapp.com/badge.svg?t=1"/></a> [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This Maven Plugin has 4 goals:
* `mvn cdap:create-plugin-json`
  * Generates a CDAP Plugin JSON file
* `mvn cdap:create-plugin-spec-json`
  * Generates a `spec.json` file
* `mvn cdap:package-artifacts-for-hub`
  * Moves artifacts to a user specified output dir
  * Note: cannot be run alone
* `mvn cdap:create-pipeline-json`
  * Extracts configuration from exported pipelines
  * Suitable for Hub

## Maven Goals

1. [Create Plugin Json](#create-plugin-json)
2. [Create Plugin Spec JSON](#create-plugin-spec-json)
3. [Move Artifact Files](#move-artifact-files)
4. [Extract Configuration From Exported Pipelines ](#extract-configuration-from-exported-pipelines)

### Create Plugin Json

This goal generates the cdap plugin json file. The maven goal is `create-plugin-json`.

To run only the goal use:

```mvn cdap:create-plugin-json```

The generated file will be placed in the `${project.build}` directory.

#### Configurations

| Configuration | Required | Default | Description |
| :------------ | :------: | :----- | :---------- |
| **cdapArtifacts** | **Y** | N/A | Specifies all the parent CDAP artifacts and scope this plugin is applicable for.|
| **widgetsDirectory** | **N** | ```${project.dir}/widgets``` | Specifies alternate widgets directory.|
| **docsDirectory** | **N** | ```${project.dir}/docs``` | Specifies alternate documentation directory.|
| **iconsDirectory** | **N** | ```${project.dir}/icons``` | Specifies alternate icons directory.|

#### Example POM

```
<plugin>
  <groupId>co.cask</groupId>
  <version>1.1.3</version>
  <artifactId>cdap-maven-plugin</artifactId>
  <configuration>
    <cdapArtifacts>
      <parent>system:cdap-data-pipeline[4.0.0,9.0.0-SNAPSHOT)</parent>
      <parent>system:cdap-data-streams[4.0.0,9.0.0-SNAPSHOT)</parent>
    </cdapArtifacts>
  </configuration>
  <executions>
    <execution>
       <id>create-artifact-config</id>
       <phase>prepare-package</phase>
       <goals>
         <goal>create-plugin-json</goal>
       </goals>
    </execution>
  </executions>
</plugin>
```

#### Output
```
[INFO] ------------------------------------------------------------------------
[INFO] Building Trash Sink 1.1.0-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- cdap-maven-plugin:1.0-SNAPSHOT:create-plugin-json (default-cli) @ trash-plugin ---
[INFO] ------------------------------------------------------------------------
[INFO] CDAP Plugin JSON
[INFO] ------------------------------------------------------------------------
[INFO] Project              : Trash Sink
[INFO] Group ID             : co.cask
[INFO] Artifact ID          : trash-plugin
[INFO] Version              : 1.1.0-SNAPSHOT
[INFO] Base Directory       : /Users/nitin/Work/Devel/plugin-json-test
[INFO] Build Directory      : /Users/nitin/Work/Devel/plugin-json-test/target
[INFO] Widgets Directory    : /Users/nitin/Work/Devel/plugin-json-test/widgets
[INFO] Docs Directory       : /Users/nitin/Work/Devel/plugin-json-test/docs
[INFO] CDAP Artifacts
[INFO]  system:cdap-data-pipeline[4.0.0,9.0.0-SNAPSHOT)
[INFO]  system:cdap-data-streams[4.0.0,9.0.0-SNAPSHOT)
[INFO] ------------------------------------------------------------------------
[INFO] Successfully created  : trash-plugin-1.1.0-SNAPSHOT.json
[INFO] ------------------------------------------------------------------------
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 1.026 s
[INFO] Finished at: 2017-03-19T03:21:38-07:00
[INFO] Final Memory: 13M/309M
[INFO] ------------------------------------------------------------------------
```

### Create Plugin Spec JSON

This goal generates the `spec.json` file. The maven goal is `create-plugin-spec-json`

To run only the goal use:

```mvn cdap:create-plugin-spec-json```

The generated file will be placed in the ```${project.build}``` directory.

#### Configuration

| Configuration | Required | Default | Description |
| :------------ | :------: | :----- | :---------- |
| **isPipeline** | **N** | ```false``` | Specifies if Spec.json is for a pipeline.|
| **actionType** | **N** | ```one_step_deploy_plugin``` | Specifies actionType - needs to be `create_pipeline_draft` if isPipeline true.|
| **scope** | **N** | ```user``` | Artifact scope.|
| **version** | **N** | ```${project.version}``` | Artifact version.|
| **cdapArtifacts** | **Y** | N/A | Specifies all the parent CDAP artifacts and scope this plugin is applicable for.|
| **label** | **N** | ```${project.name}``` | Short description that will be displayed to users during the install process.|
| **actionType** | **Y** | ```one_step_deploy_plugin``` | Action specification type.|
| **description** | **N** | ```${project.description}``` | Plugin description.|
| **author** | **N** | ```${project.author}``` | Plugin author.|
| **org** | **N** | ```${project.org}``` | Plugin author organisation.|
| **categories** | **N** | N/A | List of Categories.|
| **buildDirectory** | **N** | ```${project.build.directory}``` | Target directory for the `spec.json` file.|
| **artifactId** | **N** | ```${project.artifactId}``` | Used to reference the config (json) file name.|
| **additionalActionArguments** | **N** | N/A | List of key/value parameters to include in the Action.|

**NOTE: Categories affect the display of the HUB - please include at the bare minimum the team and type of item**

#### Example POM

Including both `create-plugin-json` and `create-plugin-spec-json` goals

```
<plugin>
  <groupId>co.cask</groupId>
  <version>1.1.3</version>
  <artifactId>cdap-maven-plugin</artifactId>
  <configuration>
    <cdapArtifacts>
      <parent>system:cdap-data-pipeline[6.0.0-SNAPSHOT,9.0.0-SNAPSHOT)</parent>
      <parent>system:cdap-data-streams[6.0.0-SNAPSHOT,9.0.0-SNAPSHOT)</parent>
    </cdapArtifacts>
    <author>LiveRamp</author>
    <org>LiveRamp</org>
    <description>optional plugin description</description>
    <categories>
      <category>cat1</category>
      <category>cat2</category>
    </categories>
    <additionalActionArguments>
      <argument>
        <key>jar</key>
        <value>${project.artifactId}-${project.version}.jar</value>
      </argument>
    </additionalActionArguments>
    <scope>user</scope>
  </configuration>
  <executions>
    <execution>
      <id>create-artifact-config</id>
      <phase>prepare-package</phase>
      <goals>
        <goal>create-plugin-json</goal>
        <goal>create-plugin-spec-json</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

Including only `create-plugin-spec-json` goal

```
<plugins>
 <plugin>
   <groupId>co.cask</groupId>
   <artifactId>cdap-maven-plugin</artifactId>
   <version>1.1.3</version>
   <configuration>
     <cdapArtifacts>
       <parent>system:cdap-data-pipeline[6.0.0-SNAPSHOT,9.0.0-SNAPSHOT)</parent>
       <parent>system:cdap-data-streams[6.0.0-SNAPSHOT,9.0.0-SNAPSHOT)</parent>
     </cdapArtifacts>
     <categories>
         <param>cat1</param>
     </categories>
     <actionType>create_pipeline_draft</actionType>
     <isPipeline>true</isPipeline>
   </configuration>
   <executions>
     <execution>
       <id>create-artifact-config</id>
       <phase>prepare-package</phase>
       <goals>
         <goal>create-plugin-spec-json</goal>
       </goals>
     </execution>
   </executions>
 </plugin>
</plugins>
</build>

</project>
```

#### Output

```
[INFO] ------------------------------------------------------------------------
[INFO] CDAP Plugin JSON
[INFO] ------------------------------------------------------------------------
[INFO] Project              : Adhoc SQL Plugin
[INFO] Group ID             : co.cask
[INFO] Artifact ID          : adhoc_sql
[INFO] Version              : 0.1.0
[INFO] Base Directory       : /Users/user1/code/cdap-plugins/lsh/misc/adhoc_sql
[INFO] Build Directory      : /Users/user1/code/cdap-plugins/lsh/misc/adhoc_sql/target
[INFO] Widgets Directory    : widgets
[INFO] Icons Directory      : icons
[INFO] Docs Directory       : docs
[INFO] CDAP Artifacts
[INFO]  system:cdap-data-pipeline[6.0.0-SNAPSHOT,9.0.0-SNAPSHOT)
[INFO]  system:cdap-data-streams[6.0.0-SNAPSHOT,9.0.0-SNAPSHOT)
[INFO] ------------------------------------------------------------------------
[INFO] Successfully created  : adhoc_sql-0.1.0.json
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] --- cdap-maven-plugin:1.1.1-SNAPSHOT:create-plugin-spec-json (create-artifact-config) @ adhoc_sql ---
[INFO] ------------------------------------------------------------------------
[INFO] CDAP Plugin JSON - Spec Json creator
[INFO] ------------------------------------------------------------------------
[INFO] specVersion          : 0.1.0
[INFO] label                : Adhoc SQL Plugin
[INFO] description          : optional plugin description
[INFO] author               : LiveRamp
[INFO] org                  : LiveRamp
[INFO] created              : 1608720834834
[INFO] cdapVersion          : [6.0.0-SNAPSHOT,9.0.0-SNAPSHOT)
[INFO] action type          : one_step_deploy_plugin
[INFO] action label         : Adhoc SQL Plugin
[INFO] action arg name      : Adhoc SQL Plugin
[INFO] action arg version   : 0.1.0
[INFO] action arg scope     : user
[INFO] action arg config    : adhoc_sql-0.1.0.json
[INFO] action arg jar       : adhoc_sql-0.1.0.jar
[INFO] categories
[INFO]  cat1
[INFO]  cat2
[INFO] ------------------------------------------------------------------------
[INFO] Successfully created: spec.json
[INFO] ------------------------------------------------------------------------
```

### Move Artifact Files

This plugin aims to move built artifacts packages to a common user specified output directory in the format expected
by a CDAP HUB and HUB packaging/publishing tools.
The goal is useful in case you'd want to run CDAP publisher from that output directory.

The maven goal is `package-artifacts-for-hub`

#### Configuration

| Configuration | Required | Default | Description |
| :------------ | :------: | :----- | :---------- |
| **relativeOutputDir** | **Y** | "${project.build.directory}/packages/" | Specifies the Output Directory **Relative** to the module build directory|

Example `relativeOutputDir` value : `/../../../../packages/`

#### Using Directory Maven Plugin

To simplify the `relativeOutputDir` value the `directory-maven-plugin` should be used in the parent Pom

**Example of `directory-maven-plugin` plugin configuration to have in parent pom.xml**
```
<plugin>
  <groupId>org.commonjava.maven.plugins</groupId>
  <artifactId>directory-maven-plugin</artifactId>
  <version>0.3.1</version>
  <executions>
    <execution>
      <id>directories</id>
      <goals>
        <goal>directory-of</goal>
      </goals>
      <phase>initialize</phase>
      <configuration>
        <property>parent_module.basedir</property>
        <project>
          <groupId>com.org.groupId</groupId>
          <artifactId>artifactId</artifactId>
        </project>
      </configuration>
    </execution>
  </executions>
</plugin>
```

Using this simplifies `relativeOutputDir` value to `${parent_module.basedir}/packages`.<br>
If left to default value the output will be placed under the module build dir

#### Execution settings

The goal must be run after any pre-package goals specified. In order to do this the following execution settings need
to be applied.

```
<execution>
  <goals>
    <goal>package-artifacts-for-hub</goal>
  </goals>
  <id>move-artifacts</id>
  <phase>package</phase>
</execution>
```

#### Example POM

```
<plugin>
  <groupId>co.cask</groupId>
  <version>1.1.3</version>
  <artifactId>cdap-maven-plugin</artifactId>
  <configuration>
    <cdapArtifacts>
      <parent>system:cdap-data-pipeline[6.1.0-SNAPSHOT,7.0.0-SNAPSHOT)</parent>
      <parent>system:cdap-data-streams[6.1.0-SNAPSHOT,7.0.0-SNAPSHOT)</parent>
    </cdapArtifacts>
    <additionalActionArguments>
      <argument>
        <key>jar</key>
        <value>${project.artifactId}-${project.version}.jar</value>
      </argument>
    </additionalActionArguments>
    <scope>user</scope>
    <author>LiveRamp</author>
    <org>LiveRamp</org>
    <description>optional plugin description</description>
    <!-- Using directory maven plugin -->
    <relativeOutputDir>${parent_module.basedir}/packages</relativeOutputDir>
  </configuration>
  <executions>
    <execution>
      <goals>
        <goal>create-plugin-json</goal>
        <goal>create-plugin-spec-json</goal>
      </goals>
      <id>create-artifact-config</id>
      <phase>prepare-package</phase>
    </execution>
    <execution>
      <goals>
        <goal>package-artifacts-for-hub</goal>
      </goals>
      <id>move-artifacts</id>
      <phase>package</phase>
    </execution>
  </executions>
</plugin>
```

#### Output

```
[INFO] 
[INFO] --- cdap-maven-plugin:1.1.2-SNAPSHOT:package-artifacts-for-hub (move-artifacts) @ adhoc_sql ---
[INFO] Creating artifact output dir at: /Users/user1/code/cdap-plugins/lsh/misc/adhoc_sql/target/../../../../packages/adhoc_sql/0.1.0
[INFO] Copying adhoc_sql-0.1.0.jar to /Users/user1/code/cdap-plugins/lsh/misc/adhoc_sql/target/../../../../packages/adhoc_sql/0.1.0/adhoc_sql-0.1.0.jar
[INFO] Copying adhoc_sql-0.1.0.json to /Users/user1/code/cdap-plugins/lsh/misc/adhoc_sql/target/../../../../packages/adhoc_sql/0.1.0/adhoc_sql-0.1.0.json
[INFO] Copying spec.json to /Users/user1/code/cdap-plugins/lsh/misc/adhoc_sql/target/../../../../packages/adhoc_sql/0.1.0/spec.json
```

Packages moved to output directory:
```
- packages
  - artifact-A
    - 1.0.0
      - artifact-A-1.0.0.jar 
      - artifact-A-1.0.0.json
      - spec.json
  - artifact-B
    - 1.1.0
      - artifact-B-1.1.0.jar 
      - artifact-B-1.1.0.json
      - spec.json
      - icon.json [Optional]
```

### Extract Configuration From Exported Pipelines

Maven goal to extract and rename an exported Pipeline JSON so that it is able to be published and consumed by a HUB

The goal is `create-pipeline-json`

#### Configuration
| Configuration | Required | Default | Description |
| :------------ | :------: | :----- | :---------- |
| **pipelineJsonFile** | **Y** | "" | Specifies the exported Json file|

Note: The pipelineJsonFile value needs to be the full path hence using `${project.baseDir}` will be needed.

E.g: `${project.basedir}/exported_jsonFile.json`

If the path is not correct you will see a

```
[ERROR] exported_jsonFile.json
```

#### Example POM

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/POM/4.0.0"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

  <name>Test Exported Pipeline</name>
  <groupId>co.cask</groupId>
  <artifactId>test_exported_pipeline</artifactId>
  <version>1.0.0</version>
  <modelVersion>4.0.0</modelVersion>

  <properties>
    <main.basedir>${project.basedir}</main.basedir>
    <!-- properties for script build step that creates the config files for the artifacts -->
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <build>
    <plugins>
      <plugin>
        <groupId>co.cask</groupId>
        <version>1.1.3</version>
        <artifactId>cdap-maven-plugin</artifactId>
        <configuration>
          <pipelineJsonFile>${basedir}/../exported_pipeline.json</pipelineJsonFile>
        </configuration>
        <executions>
          <execution>
            <id>create-artifact-config</id>
            <phase>prepare-package</phase>
            <goals>
              <goal>create-pipeline-json</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

#### Output

A `exported_pipeline.json` file should be created in the specified path in `pipelineJsonFile` property.