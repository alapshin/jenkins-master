package com.alapshin.jenkins.init

import hudson.model.Node
import jenkins.model.Jenkins
import org.yaml.snakeyaml.Yaml


config = new File(Jenkins.instance.getRootDir(), "config.yml").withInputStream {
    return new Yaml().load(it)
}

Jenkins.instance.mode = Node.Mode."${config.jenkins.mode}"
Jenkins.instance.save()
