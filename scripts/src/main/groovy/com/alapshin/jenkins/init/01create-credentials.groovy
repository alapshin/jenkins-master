package com.alapshin.jenkins.init

import jenkins.model.*
import org.yaml.snakeyaml.Yaml
import java.util.logging.Logger

// Import local util class
File classFile = new File(Jenkins.instance.getRootDir(),
            "init.groovy.d/utils/CredentialsUtils.groovy")
Class utilClass = new GroovyClassLoader(this.class.classLoader)
    .parseClass(classFile);
GroovyObject credentialsUtils = (GroovyObject) utilClass.newInstance();

logger = Logger.getLogger("")
credentials = new File("/var/run/secrets/credentials.yml").withInputStream { 
        return new Yaml().load(it)
}

// Iterate over all credentials and create them
for (cred in credentials) {
    if (cred.type == "STRING") {
        credentialsUtils.addStringCredentials([
            'credentials_id': cred.id,
            'secret': cred.secret, 
            'description': cred.description
        ])
    } else if (cred.type == "USERNAME_PASSWORD") {
        credentialsUtils.addUsernamePasswordCredentials([
            'credentials_id': cred.id,
            'user': cred.username,
            'password': cred.password,
            'description': cred.description
        ])
    }
}
