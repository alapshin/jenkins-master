#!/bin/bash

if [ -S ${DOCKER_SOCKET} ]; then
    DOCKER_GID=$(stat -c '%g' ${DOCKER_SOCKET})
    groupadd --force --non-unique --gid ${DOCKER_GID} ${DOCKER_GROUP} \
        && usermod --append --groups ${DOCKER_GROUP} ${JENKINS_USER}
fi

exec gosu ${JENKINS_USER} bash -c /usr/local/bin/jenkins.sh
