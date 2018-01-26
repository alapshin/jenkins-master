package main.groovy.com.alapshin.jenkins.init

import jenkins.model.*
import org.yaml.snakeyaml.Yaml

config = new File(Jenkins.instance.getRootDir(), "config.yml").withInputStream {
    return new Yaml().load(it)
}

def locationConfig = 
    JenkinsLocationConfiguration.get()
locationConfig.url = config.jenkins.url
locationConfig.save()
