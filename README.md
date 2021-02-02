# Sample 2 Guide - Setting up and Initialise

Inside the `Managers Code` directory in this repo I have created the starts of a managers parent project. This is a place that can be used to store a all or a group of managers and the corresponding OBR required to use them.

Inside the parent there is a single Manager which will be used to control MyApp, and a OBR building project which will be used to create a OBR for any managers in this parent.

## My App Manager

Inside the com.example.myapp.manager package I have started to layout some of the components of the manager:

```
+-- managers_parent             
|   +-- com.example.managers.obr
|   +-- com.example.myapp.manager
|       +-- IMyAppManager.java
|       +-- MyAppException.java
|       +-- internal
|           +-- MyAppManagerField.java
|           +-- MyAppManagerImpl.java
|           +-- properties
|               +-- MyAppPropertiesSingleton.java
```
- `IMyAppManager.java` is an interface for our manager
- `MyAppException.java` it is good practise to have a manager specific expeption to help track any issues. This is especially useful when managers start using other managers.
- `internal/MyAppMangerImpl.java` is the implementation for the manager. We dont expose any of the code in the interal directory, so the manager will be refered to from other managers by the interface.
- `internal/MyAppManagerField.java` is a annotation interface that will be used to group all of the annotations owned by this manager.
- `internal/properties/MyAppPropertiesSingleton.java` is the managers method for interacting with galasa properties from both the CPS and the DSS. This makes it simplier for our manager to call to the framewrok service in a secure way (only a manager can interact with its own properties, and may not interact with others)

## MyAppManagerImpl.java

Currently this class is incomplete, but we have started with the first stage of the manager lifecycle, intialise.

In the class you can see: 
```
@Override
public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);

        if(galasaTest.isJava()) {
            List<AnnotatedField> ourFields = findAnnotatedFields(MyAppManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers);
            }
        }

        try {
            this.cps = framework.getConfigurationPropertyService(NAMESPACE);
            this.dss = framework.getDynamicStatusStoreService(NAMESPACE);
            MyAppPropertiesSingleton.setCps(cps);
        } catch (Exception e) {
            throw new MyAppException("Unable to request framework services", e);
        }
    }
```

Breaking this down:
1. Firstly we call the superclasses constructor which sets the test class within the parent object (Abstract Manager). This is important as we will be using several of the superclasses methods later.
1. We then ensure that the test code that we are looking at is Java. Galasa does have multilanguage support (Gherkin) and we need to do different steps in each case.
1. Once we have determined that this is Java code, we use the findAnnotatedFields() method from our superclass with the MyAppManagerField annotation class to search the passed test class for any annotations within that belong to this manager. All managers do this.
1. we then call a method called youAreRequired, (which we will be overriding later) to state that the manager has found an annotation that belongs to it, so the manager need to be initialised.
1. The last part of this init step is to get access to the CPS and DSS in the namespace that belongs to this manager. This step only needs to be done when you plan to have/retrieve properties as part of your manager.

In the next branch we will be looking how to add a dependancy from this manager to another.

