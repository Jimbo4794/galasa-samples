package com.example.myapp.manager.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.example.myapp.manager.IMyApp;
import com.example.myapp.manager.IMyAppManager;
import com.example.myapp.manager.MyApp;
import com.example.myapp.manager.MyAppException;
import com.example.myapp.manager.internal.properties.MyAppPropertiesSingleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.docker.IDockerManager;
import dev.galasa.docker.spi.IDockerManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;

@Component(service = { IManager.class })
public class MyAppManagerImpl extends AbstractManager implements IMyAppManager{
    private static final Log logger = LogFactory.getLog(MyAppManagerField.class);

    public static final String NAMESPACE = "myapp";
    private IConfigurationPropertyStoreService cps;
    private IDynamicStatusStoreService dss;

    // Dependancy on the docker manager
    private IDockerManagerSpi dockerManager;

    /**
     * This method allows the manager to scan the test class for any annotations that belong to it.
     * If it find a annotation it owns, the manager will mark itself as required to the framework, whilst
     * also marking any dependant managers in the process
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);

        /**
         * Because galasa has support for the gherkin language, we can peform differnt actions per 
         * language
         */
        if(galasaTest.isJava()) {
            List<AnnotatedField> ourFields = findAnnotatedFields(MyAppManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers);
            }
        }

        /**
         * My App manager specific properties for CPS and DSS
         */
        try {
            this.cps = framework.getConfigurationPropertyService(NAMESPACE);
            this.dss = framework.getDynamicStatusStoreService(NAMESPACE);
            MyAppPropertiesSingleton.setCps(cps);
        } catch (Exception e) {
            throw new MyAppException("Unable to request framework services", e);
        }
    }

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
        throws ManagerException { 
    
        // Check to the another manager hasnt already added this manager into the required managers list.
        if (activeManagers.contains(this)) {
            return;
        }
        activeManagers.add(this);
        
        // We then add out dependancy to this method, stating we have a dependancy on this manager also being active
        dockerManager = this.addDependentManager(allManagers, activeManagers, IDockerManagerSpi.class);
        if (dockerManager == null) {
            throw new MyAppException("The docker manager is not available, unable to initialise MyAppManager");
        }
    }

    /**
     * This method is then overriden to state that we have a provisional dependancy on the docker manager, aka
     * we need the manager to peform provision generate before this one.
     */
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        if(otherManager instanceof IDockerManager) {
            return true;
        }
        return super.areYouProvisionalDependentOn(otherManager);
    }

    /**
     * A getter method for the docker manager, as other classes in my manager will need access to it.
     * @return
     */
    public IDockerManagerSpi getDockerManager() {
        return dockerManager;
    }

    /**
     * The method called to generate all the objects behind our annotations
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        List<AnnotatedField> foundAnnotatedFields = findAnnotatedFields(MyAppManagerField.class);
        for (AnnotatedField annotatedField : foundAnnotatedFields) {
            Field field = annotatedField.getField();
            List<Annotation> annotations = annotatedField.getAnnotations();

            if (field.getType() == IMyApp.class) {
                MyApp annotation = field.getAnnotation(MyApp.class);
                if (annotation != null) {
                    IMyApp myApp = generateMyApp(field, annotations);
                    registerAnnotatedField(field, myApp);
                }
            }
        }
    }

    /**
     * This allows for the generation of a object for aspecific annotation to be completed.
     */
    @GenerateAnnotatedField(annotation = MyApp.class)
    public IMyApp generateMyApp(Field field, List<Annotation> annotations) throws MyAppException {
        MyApp myAppAnnotation = field.getAnnotation(MyApp.class);

        return new MyAppImpl(myAppAnnotation.tag(), myAppAnnotation.version());
    }
}