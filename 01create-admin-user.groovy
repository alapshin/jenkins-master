import java.util.logging.Logger
import jenkins.model.*
import hudson.security.*
import java.util.Properties

// Create admin user with name and password specified via docker's secret file

Logger logger = Logger.getLogger("")
Jenkins instance = Jenkins.getInstance()

instance.setSecurityRealm(new HudsonPrivateSecurityRealm(false))
instance.setAuthorizationStrategy(new GlobalMatrixAuthorizationStrategy())

Properties properties = new Properties()
new File('/var/run/secrets/admin_credentials.properties').withInputStream {
    properties.load(it)
}

instance.getSecurityRealm().createAccount(properties['username'], 
    properties['password']).save()
instance.getAuthorizationStrategy().add(Jenkins.ADMINISTER, properties['username'])

instance.save()
logger.info("Created admin user")
