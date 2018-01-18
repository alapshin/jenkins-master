import java.util.logging.Logger
import jenkins.model.*
import hudson.security.*
import java.util.Properties

// Import local util class
// For some reason evaluate doesn't work
File utilFile = new File(Jenkins.instance.getRootDir(), 
            "init.groovy.d/utils/CredentialsUtils.groovy")
Class utilClass = new GroovyClassLoader(getClass().getClassLoader())
    .parseClass(utilFile);
GroovyObject credentialsUtils = (GroovyObject) utilClass.newInstance();

// Create GitHub crediatials specified via docker's secret file
// Two type of credentials are created
// 1. Secret text
// 2. Username with password

logger = Logger.getLogger("")
File propFile = new File('/var/run/secrets/github_credentials.properties')
if (!propFile.exists()) {
    logger.info('Nothing changed. No GitHub credential found.')
} else {
    Properties properties = new Properties()
    propFile.withInputStream {
        properties.load(it)
        println "Loaded GitHub credentials"
    }

    credentialsUtils.addStringCredentials([
        'credentials_id': 'github_access_token',
        'secret': properties['token'],
        'description': 'GitHub access token'
    ])
    credentialsUtils.addUsernamePasswordCredentials([
        'credentials_id': 'github_account_with_token',
        'user': properties['user'],
        'password': properties['token'],
        'description': 'GitHub account with access token'
    ])
}
