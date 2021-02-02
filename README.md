# Sample 2 Guide - Creating a Simple Manager

This objective of this second sampele is to show how to create a manager which can then be used from your tests. A design choice for this sample is to have this manager in its own project away from test material.

## Example Test

If you look in the Test Code directory in this repo there is a sample test which we will be looking to replace with an application manager. In this tests we perform some setup. We start up our docker container that contains our applicaiton, mounting a known volume which contains a script that we commonly use for setup. We might not want to always use this, but it could be a common procedure that my tests regularly need to do. It requires them to know the docker setup, so where the images are hosted, the image names, and where to mounts the volumes for every test they create. Where this might be part of my test in some cases, a lot of the time this could be just "boilerplate" repeated code.

This is why we want to create a manager to extract this application setup, and pass it back to the tester as a single annotation. So aims of this is to create:

```
@MyApp
public IMyApp myApp;
```

Where this annoation goes away, runs a docker container with our application in, configures it, and passes the tests useful utility methods (aka, getPorts(), getXYZLog(), setApplicationProperties())

This brings several other useful things. If i ever changed any implementation surrounding my application and how it is setup; rather than needing to change what could be 100's of tests, I can just change it in the manager, which will ripple out to my tests. It also allows for great control over the test resources in context of your application.

## Manager Lifecyle

It should be noted that test code and manager code have completely different lifecycles within the galasa framework. If we have a quick look at an overview of how the test runs:

1. We start a test by launching the galasa framework inside a OSGi runtime.
1. This also loads up the test material that we are trying to run.
1. The test code is then scanned for annoations, as these are used to identify which managers are going to be required for this test.
1. The appropaite managers are then also loaded into the runtime.
1. The test runs, using the managers it needs to interact with any resources.
1. There are then clean up steps to ensure we left any evironment as we found it.

In step 3, we see the managers are loaded, but this is the start of what is called the manager lifecycle:

1. Initalise
1. Provision Generate
1. Provision Build
1. Provision Start
1. Start of test Class
1. Start of test method
1. End of test method
1. End of test class
1. Provision Stop
1. Provision Discard
1. Perform Failure Analysis
1. End of test run

Not all steps in the lifecycle are required. And they should be used to suite your needs. The basic example we are going to use today is:
1. Initalise - intialise the manager, stating any dependancies
1. Provision Generate - generate the annotations and any requried resources
1. Provision Discard  - do any clean up of resources controlled by this manager
