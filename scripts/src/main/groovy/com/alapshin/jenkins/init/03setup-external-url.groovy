package com.alapshin.jenkins.init

import jenkins.model.*

def env = System.getenv()
def locationConfig =
    JenkinsLocationConfiguration.get()
locationConfig.url = String.format("https://%s", env.JENKINS_HOST)
locationConfig.save()
