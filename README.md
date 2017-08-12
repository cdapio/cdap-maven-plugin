# CDAP Maven Plugin

<a href="https://cdap-users.herokuapp.com/"><img alt="Join CDAP community" src="https://cdap-users.herokuapp.com/badge.svg?t=1"/></a> [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This repository has collection of CDAP Maven Plugins. 

## CDAP Plugin JSON

This Maven plugin allows one to create plugin JSON. 

### Configurations

| Configuration | Required | Default | Description |
| :------------ | :------: | :----- | :---------- |
| **cdapArtifacts** | **Y** | N/A | Specifies all the parent CDAP artifacts and scope this plugin is applicable for.|
| **widgetsDirectory** | **N** | ```${project.dir}/widgets``` | Specifies alternate widgets directory.|
| **docsDirectory** | **N** | ```${project.dir}/docs``` | Specifies alternate documentation directory.|
| **iconsDirectory** | **N** | ```${project.dir}/icons``` | Specifies alternate icons directory.|

### Maven Goal

This plugin allows you to specify a maven goal that would generate the plugin json file ```create-plugin-json```. The result of create will be placed in ```${project.build}``` directory. 

To run only the goal 

```mvn cdap:create-plugin-json```

### Example POM

```
<plugin>
  <groupId>co.cask</groupId>
  <artifactId>cdap-maven-plugin</artifactId>
  <version>1.0-SNAPSHOT</version>
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

### Output 
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
