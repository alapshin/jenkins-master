import jenkins.model.*

def env = System.getenv()
def locationConfig = JenkinsLocationConfiguration.get()

locationConfig.url = env.JENKINS_EXTERNAL_URL
locationConfig.save()
