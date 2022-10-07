# Changelog

## Release 1.2

\* Fix SECURITY-294 - Rips out the ability to execute a Groovy script
with this plugin

## Release 0.33

\* Fix deployed item ID when there is multiple ArtifactDeployer step

## Release 0.32

\* Fix
[JENKINS-24140](https://issues.jenkins-ci.org/browse/JENKINS-24140) -
ArtifactDeployer migration breaks lazy-load on Jenkins initialization

## Release 0.31

\* Fix
[JENKINS-16130](https://issues.jenkins-ci.org/browse/JENKINS-16130) -
Provide folder links on build results page to condense long file paths  
\* Fix
[JENKINS-14681](https://issues.jenkins-ci.org/browse/JENKINS-14681) -
Constrain the list of artifacts deployed

## Release 0.30

\* Fix typo in help file.

## Release 0.29

\* Additional help text

## Release 0.28

\* Fix
[JENKINS-18135](https://issues.jenkins-ci.org/browse/JENKINS-18135) -
Conditional Build Step plugin crashes when using Artifact Deployer
plugin as build step

## Release 0.27

\* Fix NullPointerException

## Release 0.26

\* Fix
[JENKINS-16031](https://issues.jenkins-ci.org/browse/JENKINS-16031) -
Lost deployed artifacts after restart

## Release 0.25

\* Fix
[JENKINS-15709](https://issues.jenkins-ci.org/browse/JENKINS-15709) -
ArtifactDeployer does not appear in Flexible Publish

## Release 0.24

\* Fix
[JENKINS-15354](https://issues.jenkins-ci.org/browse/JENKINS-15354) -
Add option to fail the build if specified "Files to deploy" do not exist

## Release 0.23

\* Fix
[JENKINS-15059](https://issues.jenkins-ci.org/browse/JENKINS-15059) -
recursive deletion of deployment patches not working correctly

## Release 0.22

\* Fix
[JENKINS-15058](https://issues.jenkins-ci.org/browse/JENKINS-15058) -
Setting for GroovyScript is not permanent

## Release 0.21

\* Fix
[JENKINS-14547](https://issues.jenkins-ci.org/browse/JENKINS-14547) -
Null pointer exception when using groovy script

## Release 0.20

\* Fix
[JENKINS-14548](https://issues.jenkins-ci.org/browse/JENKINS-14548) -
help for groovy script usage is never displayed

## Release 0.19

\* Fix
[JENKINS-13841](https://issues.jenkins-ci.org/browse/JENKINS-13841) -
"Base folder" for deploying the artifact from source folder to remote
directory

## Release 0.18

\* Fix
[JENKINS-13937](https://issues.jenkins-ci.org/browse/JENKINS-13937) -
ArtifactDeployer 0.16 messes the filenames for Windows filesystems

## Release 0.17

\* Fix NullPointerException on artifact deletion

## Release 0.16

\* Fix
[JENKINS-12311](https://issues.jenkins-ci.org/browse/JENKINS-12311) -
Display the Deployed Artifacts in a tree structure similar to how they
are displayed under the Build Artifacts section

## Release 0.15

\* Fix
[JENKINS-11867](https://issues.jenkins-ci.org/browse/JENKINS-11867) -
Deployed files have a different time with original files.

## Release 0.14

\* Fix
[JENKINS-12522](https://issues.jenkins-ci.org/browse/JENKINS-12522) -
Deploy artifacts for failed builds, too

## Release 0.13

\* Fix
[JENKINS-11640](https://issues.jenkins-ci.org/browse/JENKINS-11640) -
Can't copy on remote windows slave

## Release 0.12

\* Fix partially
[JENKINS-9996](https://issues.jenkins-ci.org/browse/JENKINS-9996) - Have
the possibility to change the user and group ACLs on artifacts
(Conserve file permissions to copy).

## Release 0.11

\* Fixed NullPointerException when the remote directory value is not set
(for the ArtifactDeployer publisher and for the ArtifactDeployer
builder).

## Release 0.10

\* Make it compatible to LTS series (1.409.x)  
\* Complete fix
[JENKINS-10360](https://issues.jenkins-ci.org/browse/JENKINS-10360) -
Added support of Matrix project

## Release 0.9

\* Fix partially
[JENKINS-10360](https://issues.jenkins-ci.org/browse/JENKINS-10360) -
Added support of Maven project

## Release 0.8

\* Fix slave execution

## Release 0.7

\* Fix bug on deletion  
\* The deployed artifacts in the Jenkins dashboard are now sorted.

## Release 0.6

\* Fix a ClassCastException (for more than one entry) on save
configuration

## Release 0.5

\* Integrated a pull request - Fixed a NullPointerException

## Release 0.4

\* Add 'deployment artifacts' as a build step (builder item) in
addition to publishers.  
Use Case: In the same job as build steps: Build your artifacts, deploy
them in remote locations (as servers) and launch the integration tests.

## Release 0.3

\* Add a checkbox for deleting remote artifacts when the build is
deleted.

## Release 0.2

\* The plugins enables users to call a Groovy script when the builds are
deleted (for manual and automatic deletion).

## Release 0.1

\* Initial release  
Only the filesystem protocol is available