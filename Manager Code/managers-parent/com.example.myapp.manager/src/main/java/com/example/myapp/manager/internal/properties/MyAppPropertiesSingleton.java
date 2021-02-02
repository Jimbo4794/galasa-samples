package com.example.myapp.manager.internal.properties;

import com.example.myapp.manager.MyAppException;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

@Component(service = MyAppPropertiesSingleton.class, immediate = true)
public class MyAppPropertiesSingleton {

    private static MyAppPropertiesSingleton INSTANCE;

    private IConfigurationPropertyStoreService cps;

    @Activate
    public void activate() {
        INSTANCE = this;
    }

    @Deactivate
    public void deacivate() {
        INSTANCE = null;
    }

    public static IConfigurationPropertyStoreService cps() throws MyAppException {
        if (INSTANCE != null) {
            return INSTANCE.cps;
        }

        throw new MyAppException("Attempt to access manager CPS before it has been initialised");
    }

    public static void setCps(IConfigurationPropertyStoreService cps) throws MyAppException {
        if (INSTANCE != null) {
            INSTANCE.cps = cps;
            return;
        }

        throw new MyAppException("Attempt to set manager CPS before instance created");
    }
}
