[![Build Status](https://travis-ci.org/CoastalHacking/xcore-bndtools-example.svg?branch=master)](https://travis-ci.org/CoastalHacking/xcore-bndtools-example)

# Get Xcore to Work within bndtools

This example project shows how Xcore can work within bndtools. It's broken
up into two sub-goals:

* Get Xcore to work via the Eclipse UI with bndtools
* Get Xcore to work via Gradle on the command line

## Getting Xcore to Work via Eclipse UI

High level:

* Create and configure Xcore project
* Add repositories and dependencies using bndtools semantics
* Customize for Equinox runtime
* Compile and test the Xcore model

### Create and Configure Xcore Project

* Create a bnd workspace based on the GitHub "bndtools/workspace" template
* Create a "Bnd OSGi Project" &rarr; "OSGi Standard Templates" &rarr; "Component Development". This is known as the "Xcore project"
* Via context menu on above project, select "Configure" &rarr; "Convert to Xtext Project"
* Via context menu root project (which contains "cnf"), select "Configure" &rarr; "Convert to Gradle Project"
* Create the following basic Xcore model in the Xcore project, saved in the "model/Model.xcore" file. (Note: the "model" directory is not a source directory.)

```java
@GenModel(
  bundleManifest="false",
  updateClasspath="false",
  modelDirectory="/com.example.bndtools.xcore.model/src",
  oSGiCompatible="true",
  forceOverwrite="true"
)
package com.example.bndtools.xcore.model

class One
{
  refers Two two opposite one 
}

class Two
{
  refers One one opposite two
}
```

### Add repositories and dependencies using bndtools semantics

* Resolve the Xcore dependencies (i.e. "@GenModel" annotation) by adding the following repositories to "build.bnd" cnf project:

```
-plugin.5.Xtext: \
  aQute.bnd.repository.p2.provider.P2Repository; \
    url = http://download.eclipse.org/modeling/tmf/xtext/updates/releases/2.10.0/; \
    name = Xtext

-plugin.6.EmfCore: \
  aQute.bnd.repository.p2.provider.P2Repository; \
    url = http://download.eclipse.org/modeling/emf/emf/updates/2.10.x/core/S201501230452/; \
    name = EmfCore

-plugin.7.EmfBase: \
  aQute.bnd.repository.p2.provider.P2Repository; \
    url = http://download.eclipse.org/modeling/emf/emf/updates/2.10.x/base/S201501230348/; \
    name = EmfBase

-plugin.8.EmfXcore: \
  aQute.bnd.repository.p2.provider.P2Repository; \
    url = http://download.eclipse.org/modeling/emf/emf/updates/2.10.x/xcore/R201502020452/; \
    name = EmfXcore

# Downloads everything, caveat emptor
# Needed for JDT
-plugin.9.Neon: \
  aQute.bnd.repository.p2.provider.P2Repository; \
    url = http://download.eclipse.org/eclipse/updates/4.6/R-4.6.1-201609071200/; \
    name = Neon
```

(I tried using http://download.eclipse.org/modeling/tmf/xtext/updates/composite/releases/
as a p2 site but it seems the bnd p2 repository plugin doesn't support
composite p2 repositories. The above URLs were obtained via [compositeContent.xml][comp].

[comp]: http://download.eclipse.org/modeling/tmf/xtext/updates/composite/releases/compositeContent.xml

* Add the following build dependencies to the Xcore project's "bnd.bnd":
  * `org.eclipse.emf.ecore`
  * `org.eclipse.xtext.xbase.lib`
  * `org.eclipse.emf.ecore.xcore.lib`
  * `org.eclipse.emf.common`
  * `org.eclipse.xtext.ecore`
  * `org.eclipse.emf.codegen.ecore.xtext`
  * `org.eclipse.emf.ecore.xcore`
  * `org.eclipse.xtext`
  * `org.eclipse.xtext.xbase`
  * `org.eclipse.emf.codegen.ecore`
  * `org.eclipse.jdt.core`

### Customize For Equinox Runtime

* Modify the Xcore "bnd.bnd" to include the following:

```
# plugin.xml and its properties are needed
# To generate these, edit and save Xcore model
# Model not needed for runtime
Include-Resource: plugin.xml, plugin.properties, OSGI-OPT/model=model

# The bsn needs to specify the singleton attribute
Bundle-SymbolicName: ${p};singleton:=true
```

* Export all of the packages:

```
# Mirrors what the auto-generated plugin.xml declares
Export-Package: \
  com.example.bndtools.xcore.model,\
  com.example.bndtools.xcore.model.impl,\
  com.example.bndtools.xcore.model.util
```

### Compile and test the Xcore model

After saving "bnd.bnd", Xcore is able to resolve the dependencies and does its
magic. This involves creating the following:

* `src/com.example.bndtools.xcore.model.*` Java files
* `build.properties`
* `plugin.properties`
* `plugin.xml`

Note: the above did not add any additional libraries to the Xcore project build path.
With the above, bndtools via the UI can create a jar that contains the auto-generated Xcore classes.

To test:

* Add the following to `test/com.example.bndtools.xcore.model/ExampleTest.java`:

```java
  @Test
  public void testXcore() {
    One one = ModelFactory.eINSTANCE.createOne();
    Two two = ModelFactory.eINSTANCE.createTwo();
    one.setTwo(two);
    assertEquals(one, two.getOne());
  }
```

Running the above as a vanilla JUnit test successfully executes.

## Get Xcore to work via Gradle on the command line


* Add necessary repositories and dependencies
* Build jar
* Add plugin as dependency

### Add Necessary Repositories and Dependencies

The Itemis blog [Using Xtext with Xcore and Gradle][itemis] recommends the following:

```gradle
  dependencies {
      compile "org.eclipse.xtext:org.eclipse.xtext:${xtextVersion}"
      compile "org.eclipse.xtext:org.eclipse.xtext.xbase:${xtextVersion}"
      compile 'org.eclipse.emf:org.eclipse.emf.ecore.xcore.lib:+'

      xtextLanguages 'org.eclipse.emf:org.eclipse.emf.ecore.xcore:+'
      xtextLanguages 'org.eclipse.emf:org.eclipse.emf.ecore.xcore.lib:+'
      xtextLanguages 'org.eclipse.emf:org.eclipse.emf.codegen.ecore:+'
      xtextLanguages 'org.eclipse.emf:org.eclipse.emf.codegen.ecore.xtext:+'
      xtextLanguages "org.eclipse.xtext:org.eclipse.xtext.ecore:${xtextVersion}"

  }

  sourceSets {
      main {
          resources {
              exclude '**/*.xcore'
          }
      }
  }

  xtext {
      version = "${xtextVersion}"
      languages {
          ecore {
              setup = 'org.eclipse.xtext.ecore.EcoreSupport'
          }
          codegen {
              setup = 'org.eclipse.emf.codegen.ecore.xtext.GenModelSupport'
          }
          xcore {
              setup = 'org.eclipse.emf.ecore.xcore.XcoreStandaloneSetup'
              generator.outlet.producesJava = true
          }
      }
  }
```

The above uses the [xtext-builder-plugin][xtext-plugin] plugin:

```gradle
  plugins {
    id "org.xtext.builder" version "1.0.12"
  }
```

[itemis]: https://blogs.itemis.com/en/using-xtext-with-xcore-and-gradle
[xtext-plugin]: http://xtext.github.io/xtext-gradle-plugin/xtext-builder.html

The above "build.gradle" doesn't work as is. The following does:

```gradle
plugins {
    id 'org.xtext.builder' version '1.0.12'
}

// Needed for plugin repository
repositories {
    jcenter()
}

ext.xtextVersion = "2.10.0"

// Non-plugin dependencies managed via bnd.bnd
dependencies {
    compile "org.eclipse.xtext:org.eclipse.xtext:${xtextVersion}"
    compile "org.eclipse.xtext:org.eclipse.xtext.xbase:${xtextVersion}"
    compile 'org.eclipse.emf:org.eclipse.emf.ecore.xcore.lib:+'

    xtextLanguages 'org.eclipse.emf:org.eclipse.emf.ecore.xcore:+'
    xtextLanguages 'org.eclipse.emf:org.eclipse.emf.ecore.xcore.lib:+'
    xtextLanguages 'org.eclipse.emf:org.eclipse.emf.codegen.ecore:+'
    xtextLanguages 'org.eclipse.emf:org.eclipse.emf.codegen.ecore.xtext:+'
    xtextLanguages "org.eclipse.xtext:org.eclipse.xtext.ecore:${xtextVersion}"
    xtextLanguages 'org.eclipse.jdt:org.eclipse.jdt.core:+'
}

xtext {
    version = "${xtextVersion}"
    languages {
        ecore {
            setup = 'org.eclipse.xtext.ecore.EcoreSupport'
        }
        codegen {
            setup = 'org.eclipse.emf.codegen.ecore.xtext.GenModelSupport'
        }
        xcore {
            setup = 'org.eclipse.emf.ecore.xcore.XcoreStandaloneSetup'
            generator.outlet.producesJava = true
        }
    }
    sourceSets {
        main {
            srcDir 'model'
            // Move the generated Xcore output to the src directory
            output {
                dir(xtext.languages.xcore.generator.outlet, 'src')
            }
        }
    }
}
```

The above is a little different that the Itemis blog:

* The added repository is needed for plugin dependency resolution
* The `compile` and `xtextLanguages` dependencies, however, are managed via bndtools
* A missing dependency (JDT) is added
* The "sourceSets" directory is added to the "xtext" section, as pointed out
in the [Minimal Example][xtext-plugin] section.
* The generated source output is moved to bndtools default (and only allowed) source directory

### Build jar

* Open "Gradle Executions" and "Gradle Tasks" views
* In "Gradle Tasks", select the Xcore project, "build" folder, "jar" task, executed it, and observed the following:

```
:com.example.bndtools.xcore.model:generateXtext
:com.example.bndtools.xcore.model:compileJava
:com.example.bndtools.xcore.model:processResources UP-TO-DATE
:com.example.bndtools.xcore.model:classes
:com.example.bndtools.xcore.model:jar

BUILD SUCCESSFUL

Total time: 14.425 secs
```

