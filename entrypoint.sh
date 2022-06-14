#!/usr/bin/env bash

set -x

# configure script to call original entrypoint
set -- tini -- /usr/local/bin/jenkins.sh "$@"

# In Prod, this may be configured with a GID already matching the container
# allowing the container to be run directly as Jenkins. In Dev, or on unknown
# environments, run the container as root to automatically correct docker
# group in container to match the docker.sock GID mounted from the host.
if [ "$(id -u)" = "0" ]; then
    # Get gid of docker socket file
    DOCKER_SOCKET_GID=$(stat -c '%g' "${DOCKER_SOCKET}")
    # Get group of docker inside container
    DOCKER_IMAGE_GID=$(getent group docker | cut -f3 -d: || true)

    # If they don't match, adjust
    if [ -n "$DOCKER_SOCKET_GID" ] && [ "$DOCKER_SOCKET_GID" != "$DOCKER_IMAGE_GID" ]; then
        groupmod --gid "${DOCKER_SOCKET_GID}" --non-unique docker
    fi
    if ! groups jenkins | grep -q docker; then
        usermod --append --groups docker jenkins
    fi
    # Add call to gosu to drop from root user to jenkins user
    # when running original entrypoint
    set -- gosu jenkins "$@"
fi

# Replace the current pid 1 with original entrypoint
exec "$@"
