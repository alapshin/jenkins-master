import jenkins.model.*

config = evaluate(new File(Jenkins.instance.getRootDir(), 
    "init.groovy.d/config.groovy"))

def locationConfig = 
    JenkinsLocationConfiguration.get()
locationConfig.url = config.jenkins.url
locationConfig.save()
