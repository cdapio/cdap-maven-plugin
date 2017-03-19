# CDAP Maven Plugin

<a href="https://cdap-users.herokuapp.com/"><img alt="Join CDAP community" src="https://cdap-users.herokuapp.com/badge.svg?t=1"/></a> [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This repository has collection of CDAP Maven Plugins. 

## CDAP Plugin JSON

This Maven plugin allows one to create plugin JSON. 

### Configurations

| Configuration | Required | Default | Description |
| :------------ | :------: | :----- | :---------- |
| **cdapArtifacts** | **Y** | N/A | Specifies all the parent CDAP artifacts and scope this plugin is applicable for.|
| **versionRange** | **N** | [4.0.0,10.0.0-SNAPSHOT) | Specifies the parent pipeline version range.|
| **widgetsDirectory** | **N** | ```${project.dir}/widgets``` | Specifies alternate widgets directory.|
| **docsDirectory** | **N** | ```${project.dir}/docs``` | Specifies alternate documentation directory.|

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
    <versionRange>[4.0.0,9.0.0-SNAPSHOT)</versionRange>
    <cdapArtifacts>
       <param>system:cdap-data-pipeline</param>
       <param>system:cdap-data-streams</param>
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
