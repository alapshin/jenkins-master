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

import java.util.logging.Logger
import hudson.*;
import hudson.model.*;
import hudson.slaves.*;
import jenkins.*;
import jenkins.model.*;

import com.nirima.jenkins.plugins.docker.DockerCloud
import com.nirima.jenkins.plugins.docker.DockerImagePullStrategy
import com.nirima.jenkins.plugins.docker.DockerTemplate
import com.nirima.jenkins.plugins.docker.DockerTemplateBase
import com.nirima.jenkins.plugins.docker.launcher.DockerComputerLauncher
import com.nirima.jenkins.plugins.docker.launcher.DockerComputerJNLPLauncher
import com.nirima.jenkins.plugins.docker.strategy.DockerOnceRetentionStrategy
import io.jenkins.docker.connector.DockerComputerJNLPConnector

/*
   Configure Credentials for docker cloud stack in Jenkins.
   Automatically configure the docker cloud stack in Jenkins.
   Docker plugin v1.1.2
 */

logger = Logger.getLogger("")
config = evaluate(new File(Jenkins.instance.getRootDir(), 
    "init.groovy.d/config.groovy"))

if (!Jenkins.instance.isQuietingDown()) {
    ArrayList<DockerCloud> clouds = 
        new ArrayList<DockerCloud>()
    logger.info("Start configuring Docker clouds")
    clouds = Factory.bindObjectToList(DockerCloud.class, config.jenkins.clouds.docker)
    if (clouds.size() > 0) {
        clouds.each { cloud ->
            if (Jenkins.instance.getCloud(cloud.name) == null) {
                Jenkins.instance.clouds.add(cloud)
                logger.info("Configured Docker cloud ${cloud.name}")
            }
        }
        Jenkins.instance.save()
    } else {
        logger.info("Nothing changed. No docker clouds to configure.")
    }
} else {
    logger.info("Shutdown mode enabled. Configure Docker clouds SKIPPED.")
}

class Factory {
    def static bindObjectToList(Class type, Object src) {
        if (!(type == DockerCloud) && !(type == DockerTemplate)) {
            throw new Exception("Must use DockerCloud or DockerTemplate class.")
        }
        // dockerArray should be a DockerCloud or DockerTemplate
        ArrayList<?> dockerArray
        if (type == DockerCloud){
            dockerArray = new ArrayList<DockerCloud>()
        } else {
            dockerArray = new ArrayList<DockerTemplate>()
        }
        // Cast the configuration object to a Docker instance which Jenkins will use in configuration
        if (src instanceof Map) {
            // Uses string interpallation to call a method
            // e.g instead of newDockerCloud(src) we use instead...
            dockerArray.add("new${type.getSimpleName()}"(src))
        } else if (src instanceof List) {
            for (Object o : src) {
                if (o instanceof Map) {
                    dockerArray.add("new${type.getSimpleName()}"(o))
                }
            }
        }
        return dockerArray
    }

    // Factory method to create a new instance of the DockerCloud class from a map
    def static newDockerCloud(Map obj) {
        new DockerCloud(
                obj['name'],
                bindObjectToList(DockerTemplate.class, obj['templates']),
                obj.host.uri,
                obj.get('container_cap', 100),
                obj['connection_timeout'],
                obj['read_timeout'],
                obj['credentials_id'],
                obj['version'],
                obj["docker_hostname"]
                )
    }

    // Factory method to create a new instance of the DockerTemplate class from a map
    def static newDockerTemplate(Map obj) {
        DockerTemplateBase dockerTemplateBase = new DockerTemplateBase(
                obj['image'],
                obj['pull_credentials_id'],
                obj['dns'],
                obj["network"],
                obj['docker_command'],
                obj['volumes'],
                obj['volumes_from'],
                obj['environment'],
                obj['hostname'],
                obj.get('memory_limit', null),
                obj.get('memory_swap_limit', null),
                obj.get('cpu_shares', null),
                obj['port_bindings'],
                obj.get('bind_all_ports', false),
                obj.get('privileged', false),
                obj.get('allocate_pseudo_tty', false),
                obj['mac_address'],
                obj.get('extra_host', '')
        )
        // For now the launcher_method will always be "jnlp"
        JNLPLauncher launcher = new JNLPLauncher(true)
        DockerComputerJNLPConnector connector = 
            new DockerComputerJNLPConnector(launcher)
        connector.setUser(obj["connector"]["user"])
        // This availability_strategy is for "run_once".  We can customize it later
        RetentionStrategy retentionStrategy = new DockerOnceRetentionStrategy(
            obj.get('availability_idle_timeout', 10))
        String node_usage = (obj.get('usage', 'NORMAL')
            .toUpperCase().equals('EXCLUSIVE'))? 'EXCLUSIVE' : 'NORMAL'
        DockerImagePullStrategy pullStrategy
        switch (obj.get('image_pull_strategy', 'PULL_LATEST').toUpperCase()) {
            case 'PULL_ALWAYS':
                pullStrategy = DockerImagePullStrategy.PULL_ALWAYS
                break
            case 'PULL_NEVER':
                pullStrategy = DockerImagePullStrategy.PULL_NEVER
                break
            default:
                pullStrategy = DockerImagePullStrategy.PULL_LATEST
        }
        def dockerTemplate = new DockerTemplate(
                dockerTemplateBase,
                connector,
                obj['labels'],
                obj['remote_fs', '/home/jenkins'],
                obj.get('instance_cap', '1')
        )

        return dockerTemplate
    }
}
