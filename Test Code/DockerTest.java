package com.example.docker.tests;

import dev.galasa.Test;
import dev.galasa.docker.DockerContainer;
import dev.galasa.docker.DockerContainerConfig;
import dev.galasa.docker.DockerVolume;
import dev.galasa.docker.IDockerContainer;
import dev.galasa.docker.IDockerContainerConfig;
import dev.galasa.http.HttpClient;
import dev.galasa.http.IHttpClient;

public class DockerTest {
	
	@DockerContainer(image = "my-application:latest", dockerContainerTag = "a", start = false)
    public IDockerContainer container;

    @DockerContainerConfig(
        dockerVolumes =  {
            @DockerVolume(volumeTag = "setupVolume", mountPath = "/tmp/testvol"),
        }
    )
    public IDockerContainerConfig config;
    
    @Test
    public void testContainerFunctionX() throws Exception {
        container.startWithConfig(this.config);
        container.exec("/tmp/testvol/setupScript.sh");
        
        /**
         * Rest of Test ...
         */   
    }

}