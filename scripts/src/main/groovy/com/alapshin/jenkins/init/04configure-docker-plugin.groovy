package com.alapshin.jenkins.init

import org.yaml.snakeyaml.Yaml
import java.util.logging.Logger

import jenkins.model.*
import com.nirima.jenkins.plugins.docker.DockerCloud

// Import local util class
File classFile = new File(Jenkins.instance.getRootDir(),
        "init.groovy.d/utils/DockerCloudFactory.groovy")
Class utilClass = new GroovyClassLoader(this.class.classLoader)
        .parseClass(classFile);
GroovyObject dockerCloudFactory = (GroovyObject) utilClass.newInstance();

/*
   Configure Docker cloud plugin
 */

logger = Logger.getLogger("")

config = new File(Jenkins.instance.getRootDir(), "config.yml").withInputStream {
    return new Yaml().load(it)
}

if (!config.jenkins.clouds.docker) {
    logger.info("Nothing changed. No Docker clouds to configure.")
    return
}

if (!Jenkins.instance.isQuietingDown()) {
    logger.info("Start configuring Docker clouds")
    clouds = dockerCloudFactory.bindObjectToList(DockerCloud.class, config.jenkins.clouds.docker)
    clouds.each { cloud ->
        // If cloud if such name is present then override it
        Jenkins.instance.clouds.removeAll { it.name == cloud.name }
        Jenkins.instance.clouds.add(cloud)
        logger.info("Configured Docker cloud ${cloud.name}")
    }
    Jenkins.instance.save()
} else {
    logger.info("Shutdown mode enabled. Configure Docker clouds SKIPPED.")
}

