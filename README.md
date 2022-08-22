# Writing a Manager

This document is to cover the creation of a very basic manager and some of the concepts. For this manager I am pretending I have an application which I am trying to encapsulate for simple use for my testers.

### My App

For this example I want to create a manager that will help testers interact and provision tests of "My App". This is going to do several things:
1. Configure my test to locate a LPAR my test is running on
2. Configure a 3270 Terminal to connect to that LPAR
3. Perform some basic 3270 interactions to sign me into my application (handle credentials and navigation)

The idea here is to demonstrate that we can do more with a manager that just interact with a technology. We can code in context knowledge into our manager to reduce repeated steps in tests. This also means that if a application ever changes something like the login process, we only have to resolve this in 1 manager, rather than 1000's of tests.

### Setting Up the Basic Project
To start, we need a maven project to build in:
 ![](./images/Image1.png)

There are two empty packages in place:
- `com.example.application.manager` - This is going to house all of the Classes that a tester will be interacting with. (The annotations themselves, the Object the Annotation provides and the JavaException for this manager)
- `com.example.application.manager.internal` - This will contain all of the actually implementation code that will perform the work.

I have also setup the pom.xml with a few dependencies that looks like:

 ```
 <project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>com.example</groupId>
	<artifactId>com.example.application.manager</artifactId>
	<version>0.1.0</version>
	<packaging>bundle</packaging>
	
	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>11</java.version>
		<maven.compiler.source>11</maven.compiler.source>
		<maven.compiler.target>11</maven.compiler.target>
		<maven.build.timestamp.format>yyyyMMddHHmm</maven.build.timestamp.format>
		<unpackBundle>true</unpackBundle>
	</properties>

	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>dev.galasa</groupId>
				<artifactId>galasa-bom</artifactId>
				<version>0.24.0</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

	<dependencies>
		<dependency>
			<groupId>dev.galasa</groupId>
			<artifactId>dev.galasa.framework</artifactId>
		</dependency>
		<dependency>
			<groupId>dev.galasa</groupId>
			<artifactId>dev.galasa</artifactId>
		</dependency>
		<dependency>
			<groupId>dev.galasa</groupId>
			<artifactId>dev.galasa.zos.manager</artifactId>
		</dependency>
		<dependency>
			<groupId>dev.galasa</groupId>
			<artifactId>dev.galasa.zos3270.manager</artifactId>
		</dependency>
		<dependency>
			<groupId>commons-logging</groupId>
			<artifactId>commons-logging</artifactId>
		</dependency>
		<dependency>
			<groupId>javax.validation</groupId>
			<artifactId>validation-api</artifactId>
		</dependency>
		<dependency>
			<groupId>org.osgi</groupId>
			<artifactId>org.osgi.service.component.annotations</artifactId>
		</dependency>
	</dependencies>

	<build>
		<pluginManagement>
			<plugins>
				<plugin>
					<groupId>org.eclipse.m2e</groupId>
					<artifactId>lifecycle-mapping</artifactId>
					<version>1.0.0</version>
					<configuration>
						<lifecycleMappingMetadata>
							<pluginExecutions>
								<pluginExecution>
									<pluginExecutionFilter>
										<groupId>org.apache.felix</groupId>
										<artifactId>maven-bundle-plugin</artifactId>
										<versionRange>[5.1.1,)</versionRange>
										<goals>
											<goal>bundle</goal>
										</goals>
									</pluginExecutionFilter>
									<action>
										<execute>
											<runOnConfiguration>true</runOnConfiguration>
											<runOnIncremental>true</runOnIncremental>
										</execute>
									</action>
								</pluginExecution>
							</pluginExecutions>
						</lifecycleMappingMetadata>
					</configuration>
				</plugin>
			</plugins>
		</pluginManagement>

		<plugins>
			<plugin>
				<groupId>org.apache.felix</groupId>
				<artifactId>maven-bundle-plugin</artifactId>
				<version>5.1.1</version>
				<extensions>true</extensions>
			</plugin>
		</plugins>
	</build>

</project>
```
### Creating an annotation
To create an Galasa annotation we need three things, the Class to define the interface the user will interact with, the class to define the annotation itself, a class to define a grouping annotation. So starting with the interface for the user object (we call this a TPI in Galasa)

### `IMyAppTerminal.java`
```
package com.example.application.manager;

import dev.galasa.zos3270.ITerminal;

public interface IMyAppTerminal extends ITerminal{
	void goToMainScreen() throws MyAppException;
}
```
This is a very simple class as we are extending the default 3270 Terminal built into Galasa to allow us to add some initial navigation to my terminal, as well as some addition functionality like the `goToMainScreen()`.

For a tester to be able to use this object and for Galasa to reflect a real implementation into this object, we need to attach it to an annotation.
### `MyAppTerminal.java`

```
package com.example.application.manager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.example.application.manager.internal.MyAppField;

import dev.galasa.framework.spi.ValidAnnotatedFields;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@MyAppField
@ValidAnnotatedFields({ IMyAppTerminal.class })
public @interface MyAppTerminal {
}
```
Which at first glance is an empty class. I could be adding fields to this class, but for this example we dont need to. But for an example look at the Account annotation in the Simbank examples for a reference.

The annotations in the class are the important bits, so here is a breakdown of what they all do:
- `@Retention(RetentionPolicy.RUNTIME)` - indicates to the VM that our annotation is used at both compile and runtime.
- `@Target({ ElementType.FIELD })` - indicates at the context at which our annotation is applicable.
- `@MyAppField` -  is another annotation we are about to define inside our internal package, we will come back to this.
- `@ValidAnnotatedFields({ IMyAppTerminal.class })` - Is used to both define and validate what object interface we are expecting the object that will populate our interface once instantiated by out manager.

But it is also possible for a manager to "own" multiple annotations. For this reason we need to be able to group annotations, which we do with another annotation (you will see why later):
### `MyAppField.java`
```
package com.example.application.manager.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface MyAppField {
}
```
This second annotation is completely empty except our defining annotations.

I have also included a custom Exception we will use:
### `MyAppException.java`
```
package com.example.application.manager;

import dev.galasa.ManagerException;

public class MyAppException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public MyAppException() {
    }

    public MyAppException(String message) {
        super(message);
    }

    public MyAppException(Throwable cause) {
        super(cause);
    }

    public MyAppException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyAppException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}

```
At this point our project should look like this:
![](./images/Image2.png)

With these in place, we have defined out TPI. 

### Fitting into the Test Lifecycle
We now need to move onto some of the Galasa specific components that will allow our manager to fit into the test lifecycle, as well as actually get picked up at runtime. For this simple manager we are only going to interact with 3 components within the test lifecycle: `Initialise, ProvisionGenerate, and ProvisionDiscard`

But before we do that, we need to setup a class which will get recognized by the framework as a manager, which we will do in a file called `MyAppManagerImpl.java`. As a best practice, this class will be used for all the Galasa components, and call out to other classes to perform the actually functionality of this manager.
### `MyAppManagerImpl.java`
```
package com.example.application.manager.internal;

import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.IManager;

@Component(service = { IManager.class})
public class MyAppManagerImpl extends AbstractManager{
}
```
Before we add any code here, it is important to add both the `extends` to the class and the OSGi service component. The service component is what we used in Galasa to identify what classes are managers (which will have all the methods we are expecting a manager to have). The AbstractManager implements `IManager.class` and provides all the components required to be a manager. We will be overriding methods to customise our manager for purpose however.

The first method to override here is the `initalise()` method, as this is what tells Galasa if this manager is needed for a certain test run. For context, the framework will loop through every manager, and ask if the test we are trying to run has any annotations owned by that manager. If the answer is yes then we initialise the manager (and any dependant managers.)
```
@Component(service = { IManager.class})
public class MyAppManagerImpl extends AbstractManager{
	private static final Log logger = LogFactory.getLog(MyAppManagerImpl.class);

    private IZosManagerSpi                     zosManager;
    private IZos3270ManagerSpi                 z3270manager;
	
	@Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);

        if(galasaTest.isJava()) {
            List<AnnotatedField> ourFields = findAnnotatedFields(MyAppField.class);
            if (!ourFields.isEmpty()) {
            	logger.info("My App annotation found, initalising");
                youAreRequired(allManagers, activeManagers, galasaTest);
            }
        }
	}

    @Override
	public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        zosManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosManagerSpi.class);
        if (zosManager == null) {
            throw new Zos3270ManagerException("The zOS Manager is not available");
        }
        z3270manager = addDependentManager(allManagers, activeManagers, galasaTest, IZos3270ManagerSpi.class);
        if (z3270manager == null) {
            throw new Zos3270ManagerException("The zOS 3270 Manager is not available");
        }
    }
}
```
We need to override two methods to make this possible. `initialise()` and `youAreRequired()`.

The function of these methods respectively:
1. To search for any annotations owned by this manager (`findAnnotatedFields()` is provided from the abstract class)
2. Add this manager to a list of managers to activate, and add any dependancies on other managers we are going to use.

Another method we need to override is:
```
    @Override
	public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        if (otherManager instanceof IZosManager || otherManager instanceof IZos3270Manager) {
            return true;
        }

        return super.areYouProvisionalDependentOn(otherManager);
    }
```
This isn't obvious from the code, but the framework itself will call this method when running a tests to build up a ordered list of managers which it needs to provision in a specific order. Here we want to make sure both the ZosManager and the Zos3270Manager are provisioned before this manager (as we plan to use them)