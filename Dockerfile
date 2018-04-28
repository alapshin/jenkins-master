FROM jenkins/jenkins:2.107.2

USER root
RUN apt-get update \
    && apt-get --no-install-recommends --yes install gosu

# Skip initial setup
ENV JAVA_OPTS "-Djava.awt.headless=true -Djenkins.install.runSetupWizard=false"

# Copy configuration scripts
COPY init.groovy.d/ /usr/share/jenkins/ref/init.groovy.d/
# https://github.com/jenkinsci/docker/issues/618
# Make a .override file for each file so it copied over upon Jenkins startup.
RUN find /usr/share/jenkins/ref/init.groovy.d -type f -exec ln -s '{}' '{}'.override \;

# Pre-install plugins
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

COPY entrypoint.sh /
ENTRYPOINT ["/sbin/tini", "--", "/entrypoint.sh"]
