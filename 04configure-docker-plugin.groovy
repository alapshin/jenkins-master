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

/*
   Configure Credentials for docker cloud stack in Jenkins.
   Automatically configure the docker cloud stack in Jenkins.
   Docker plugin v1.1.2
 */

import hudson.*;
import hudson.model.*;
import hudson.slaves.*;
import jenkins.*;
import jenkins.model.*;

import net.sf.json.JSONArray
import net.sf.json.JSONObject
import org.kohsuke.stapler.Stapler
import org.kohsuke.stapler.StaplerRequest

import com.nirima.jenkins.plugins.docker.DockerCloud
import com.nirima.jenkins.plugins.docker.DockerImagePullStrategy
import com.nirima.jenkins.plugins.docker.DockerTemplate
import com.nirima.jenkins.plugins.docker.DockerTemplateBase
import com.nirima.jenkins.plugins.docker.launcher.DockerComputerLauncher
import com.nirima.jenkins.plugins.docker.launcher.DockerComputerJNLPLauncher
import com.nirima.jenkins.plugins.docker.strategy.DockerOnceRetentionStrategy
import io.jenkins.docker.connector.DockerComputerJNLPConnector


/*
   Docker Clouds defined
*/
JSONArray DOCKER_CLOUDS = [
    [
        name: "docker",
        server_url: "unix:///var/run/docker.sock",
        container_cap: 10,
        connection_timeout: 5,
        read_timeout: 15,
        credentials_id: "",
        templates: [
            image: "alapshin/android-build-env:latest",
            executors: 1,
            instance_cap: "",
            remote_fs: "/home/jenkins",
            labels: "android",
            // valid values: exclusive or normal
            usage: "normal",
            // valid values: pull_latest, pull_always, pull_never
            image_pull_strategy: "pull_latest",
            remove_volumes: false,
            // Docker settings
            dns: "",
            docker_command: "",
            volumes: "",
            volumes_from: "",
            environment: "",
            hostname: "",
            // cpu_shares: 0,
            // memory_limit: -1,
            // memory_swap_limit: -1,
            port_bindings: "",
            bind_all_ports: false,
            priviliged: false,
            allocate_pseudo_tty: false,
            mac_address: "",
            remote_fs_root_mapping: "",
            // Availability settings
            availability_strategy: "run_once",
            availability_idle_timeout: 5,
            connector: [
                user: "jenkins",
            ]
        ]
    ]
] as JSONArray

if (!Jenkins.instance.isQuietingDown()) {
    ArrayList<DockerCloud> clouds = 
        new ArrayList<DockerCloud>()
    println "Start configuring Docker clouds"
    clouds = Factory.bindJSONToList(DockerCloud.class, DOCKER_CLOUDS)
    if (clouds.size() > 0) {
        clouds.each { cloud ->
            if (Jenkins.instance.getCloud(cloud.name) == null) {
                Jenkins.instance.clouds.add(cloud)
                println "Configured Docker cloud ${cloud.name}"
            }
        }
        Jenkins.instance.save()
    } else {
        println 'Nothing changed. No docker clouds to configure.'
    }
} else {
    println 'Shutdown mode enabled. Configure Docker clouds SKIPPED.'
}

class Factory {
    def static bindJSONToList(Class type, Object src) {
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
        if (src instanceof JSONObject) {
            // Uses string interpallation to call a method
            // e.g instead of newDockerCloud(src) we use instead...
            dockerArray.add("new${type.getSimpleName()}"(src))
        } else if (src instanceof JSONArray) {
            for (Object o : src) {
                if (o instanceof JSONObject) {
                    dockerArray.add("new${type.getSimpleName()}"(o))
                }
            }
        }
        return dockerArray
    }

    // Factory method to create a new instance of the DockerCloud class from a JSONObject
    def static newDockerCloud(JSONObject obj) {
        new DockerCloud(
                obj.optString('name'),
                bindJSONToList(DockerTemplate.class, obj.opt('templates')),
                obj.optString('server_url'),
                obj.optInt('container_cap', 100),
                obj.optInt('connection_timeout'),
                obj.optInt('read_timeout'),
                obj.optString('credentials_id'),
                obj.optString("version"),
                obj.optString("docker_hostname")
                )
    }

    // Factory method to create a new instance of the DockerTemplate class from a JSONObject
    def static newDockerTemplate(JSONObject obj) {
        def staplerRequest = Stapler.getCurrentRequest()
        DockerTemplateBase dockerTemplateBase = new DockerTemplateBase(
                obj.optString('image'),
                obj.optString('pull_credentials_id'),
                obj.optString('dns'),
                obj.optString("network"),
                obj.optString('docker_command'),
                obj.optString('volumes'),
                obj.optString('volumes_from'),
                obj.optString('environment'),
                obj.optString('hostname'),
                obj.optInt('memory_limit') ? obj.optInt('memory_limit') : null,
                obj.optInt('memory_swap_limit') ? obj.optInt('memory_swap_limit') : null,
                obj.optInt('cpu_shares') ? obj.optInt('cpu_shares') : null,
                obj.optString('port_bindings'),
                obj.optBoolean('bind_all_ports', false),
                obj.optBoolean('privileged', false),
                obj.optBoolean('allocate_pseudo_tty', false),
                obj.optString('mac_address'),
                obj.optString('extra_host')
                )
        // For now the launcher_method will always be "jnlp"
        JNLPLauncher launcher = new JNLPLauncher(true)
        DockerComputerJNLPConnector connector = 
            new DockerComputerJNLPConnector(launcher)
        connector.setUser(obj.optJSONObject("connector").optString("user"))
        // This availability_strategy is for "run_once".  We can customize it later
        RetentionStrategy retentionStrategy = new DockerOnceRetentionStrategy(
            obj.optInt('availability_idle_timeout', 10))
        String node_usage = (obj.optString('usage', 'NORMAL')
            .toUpperCase().equals('EXCLUSIVE'))? 'EXCLUSIVE' : 'NORMAL'
        DockerImagePullStrategy pullStrategy
        switch (obj.optString('image_pull_strategy', 'PULL_LATEST').toUpperCase()) {
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
                obj.optString('labels'),
                obj.optString('remote_fs', '/home/jenkins'),
                obj.optString('instance_cap', '1'))

        return dockerTemplate
    }
}



