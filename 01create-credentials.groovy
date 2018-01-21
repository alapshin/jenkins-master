import jenkins.model.*
import hudson.security.*
import org.yaml.snakeyaml.Yaml
import java.util.logging.Logger

// Import local util class
// For some reason evaluate doesn't work
File utilFile = new File(Jenkins.instance.getRootDir(), 
            "init.groovy.d/utils/CredentialsUtils.groovy")
Class utilClass = new GroovyClassLoader(getClass().getClassLoader())
    .parseClass(utilFile);
GroovyObject credentialsUtils = (GroovyObject) utilClass.newInstance();

logger = Logger.getLogger("")
credentials = new File("/var/run/secrets/credentials.yml").withInputStream { 
        return new Yaml().load(it)
}

// Iterate over all credentials and create them
for (cred in credentials) {
    if (cred.type == "secret_text") {
        credentialsUtils.addStringCredentials([
            'credentials_id': cred.id,
            'secret': cred.secret, 
            'description': cred.description
        ])
    } else if (cred.type == "username_password") {
        credentialsUtils.addUsernamePasswordCredentials([
            'credentials_id': cred.id,
            'user': cred.username,
            'password': cred.password,
            'description': cred.description
        ])
    }
}
