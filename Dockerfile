# Preconfigured Jenkins image

FROM jenkins/jenkins:lts

USER root
RUN apt-get update \
    && apt-get  --no-install-recommends --yes install gosu
COPY entrypoint.sh /

# Skip initial setup
ENV JAVA_OPTS "-Djava.awt.headless=true -Djenkins.install.runSetupWizard=false"

# Copy configuration scripts
COPY init.groovy.d/ /usr/share/jenkins/ref/init.groovy.d/

# Pre-install plugins
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

ENTRYPOINT /entrypoint.sh
