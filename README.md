# Jenkins Master

Docker image with preconfigured Jenkins master node

## What it provides on top of base Jenkins image
1. Number of preinstalled plugins (see `plugins.txt` for full list) including
experimental [Configuration as Code][1] plugin
2. Custom entrypoint script that automatically creates docker group inside
container to allow access to host docker's socket

## Automated configuration using Configuration as Code plugin
There is work in progress on [Configuration as Code][1] plugin that allows
Jenkins to be configured using YAML config file instead of manual configuration
using web interface.

This image uses this plugin to automate Jenkins configuration and provides
comprehensive initial config in `jenkins.yaml` that could be used as is or as
basis for custom configurations. Default config provides:

1. Sane global security settings
    1. Disabled CLI over Remoting
    2. Disabled legacy agent protocols
    3. Enabled agent -> master access control
    4. Enabled CRSF protection
2. Role-based authorization with admin user
3. Configured Docker cloud
3. Configured GitHub plugin
4. Example how to provide credentials
5. Different default theme using Simple Theme Plugin

## How to run
Build image
```
./build.sh
```
Run container
```
docker-compose up
```

[1]: https://github.com/jenkinsci/configuration-as-code-plugin
