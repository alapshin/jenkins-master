# Jenkins Master

Docker image with preconfigured Jenkins master node

## What it provides on top of base Jenkins image
1. Number of preinstalled plugins (see `plugins.txt` for full list)
2. Custom entrypoint script that automatically creates docker group inside container
to allow access to host docker's socket
3. Number of groovy scripts that configure Jenkins instance on first run using
configuration stored in YAML file.

## Automated configuration using groovy scripts
Jenkins can execute initialization scripts written in Groovy if they are present
during start up. See [Groovy Hook Script][1] for details. The hook name for this
event is `init`.
So every script placed into `/usr/share/jenkins/ref/init.groovy.d` will be
executed in alphabetical order.

This project uses this feature and provides number of scripts that automate
Jenkins configuration. For full list of scripts see subproject `scripts`

## How to run
```
docker-compose up
```
or for development
```
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
```

Second command will mount init scripts directory into container. This way you
don't have to rebuild image every time there is change in init scripts and could
iterate faster when writing them.

[1]: https://wiki.jenkins.io/display/JENKINS/Groovy+Hook+Script
