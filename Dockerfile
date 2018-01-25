# Preconfigured Jenkins image

FROM jenkins/jenkins:lts-slim

USER root
RUN apt-get update && apt-get install gosu
COPY entrypoint.sh /

# Skip initial setup
ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false

# Copy configuration files
COPY *.xml /usr/share/jenkins/ref/

# Copy configuration scripts
COPY init.groovy.d/ /usr/share/jenkins/ref/init.groovy.d/

# Pre-install plugins
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

ENTRYPOINT /entrypoint.sh
