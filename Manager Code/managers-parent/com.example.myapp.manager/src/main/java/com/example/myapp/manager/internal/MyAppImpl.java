package com.example.myapp.manager.internal;

import com.example.myapp.manager.IMyApp;
import com.example.myapp.manager.MyAppException;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.IDockerContainer;
import dev.galasa.docker.spi.IDockerManagerSpi;

public class MyAppImpl implements IMyApp {
    private String tag;
    private String version;
    private IDockerContainer container;

    public MyAppImpl(MyAppManagerImpl myAppManager ,String tag, String version) throws MyAppException {
        this.tag = tag;
        this.version = version;

        try {
            IDockerManagerSpi dockerManager = myAppManager.getDockerManager();
            this.container = dockerManager.provisionContainer(tag, "library/httpd:"+version, true, "PRIMARY");
        } catch (DockerManagerException e) {
            throw new MyAppException("Failed to create my app");
        }
    }

    @Override
    public String getTag() {
        return this.tag;
    }

    @Override
    public String getVersion() {
        return this.version;
    }
    
}