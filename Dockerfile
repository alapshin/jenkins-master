FROM jenkins/jenkins:2.263.1-jdk11

USER root
RUN apt-get update \
    && apt-get --no-install-recommends --yes install gosu

# Disable setup wizard
# See https://github.com/jenkinsci/docker#script-usage
RUN echo $JENKINS_VERSION > /usr/share/jenkins/ref/jenkins.install.UpgradeWizard.state
RUN echo $JENKINS_VERSION > /usr/share/jenkins/ref/jenkins.install.InstallUtil.lastExecVersion

# Pre-install plugins
COPY --chown=jenkins plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

COPY entrypoint.sh /
ENTRYPOINT ["/sbin/tini", "--", "/entrypoint.sh"]
