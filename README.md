# Sample 2 Guide - Dependancies on other managers

For MyApp manager, I want to put a dependancy on the docker manager. Its the docker manager I will be using to instantiate my application at test time. 

To do this we override some of the methods provided in the superclass. Starting with `youAreRequired()`:
```
@Override
public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
    throws ManagerException { 

    if (activeManagers.contains(this)) {
        return;
    }
    activeManagers.add(this);

    dockerManager = this.addDependentManager(allManagers, activeManagers, IDockerManagerSpi.class);
    if (dockerManager == null) {
        throw new MyAppException("The docker manager is not available, unable to initialise MyAppManager");
    }
}
```

This method was previously calling the one in our superclass which just added this manager to the activeManagers list. This informed galasa that the manager is required and should be provisioned in the next step of the lifecycle.

We have now overriden this method to add some further dependancies to this list. We want to have the situation in out test class where a tester can use the annotation that is owned by this manager WITHOUT the need to also use the docker manager annotations to make use of the docker manager. So in this method we add the dockerManager as a dependant manager. If you looked at the code new code in the `MyAppManagerImpl.java` you may also noticed that the object is actually the DockerManagerSpi. This is another important distinction. Where managers can have dependancies on each other, a manager is still in control of exactly what functionality is provided to another manager through the SPI. I wont be creating an SPI for this manager in this example, but there are examples in out other repos.

You may have also noticed that we check to see if our manager is already in the list of managers to be activated. This is again due to dependancies. Another manager else where could have a dependancy on this manager, previously intialising it before we got the the annotations in the test class that would of done so.

The next thing we have to do is add a provisional dependancy on the docker manager:
```
@Override
public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
    if(otherManager instanceof IDockerManager) {
        return true;
    }
    return super.areYouProvisionalDependentOn(otherManager);
}
```
What this means, is in the next lifecycle step of provision generate, we allow galasa to correctly order in which managers to be provisioned. This ensures the docker manager will be ready to perform any provision activties that we need it too in this manager.

Last method to add right now is a quick getter to the ManagerImpl.
```
public IDockerManagerSpi getDockerManager() {
    return dockerManager;
}
```
The `MyAppManagerImpl.java` object is not really going to be perform much of the manager specific work, more just a communication and setup piece between your code and the galasa framework. This means other classes are likely to want access to any of the other dependancy managers.

In the next branch we will look at creating and provisioning the annotations.