package com.alapshin.jenkins.init

/*
   Copyright (c) 2015-2017 Sam Gleske
   https://github.com/samrocketman/jenkins-bootstrap-shared

   Changes to support version 1.* of the Docker Plugin by Andrei Lapshin

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */


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
   Configure Credentials for docker cloud stack in Jenkins.
   Automatically configure the docker cloud stack in Jenkins.
   Docker plugin v1.1.2
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

