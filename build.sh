#!/bin/bash
set -euo pipefail

IMAGE=jenkins-master
USERNAME=alapshin

docker build --tag "${USERNAME}"/"${IMAGE}":latest .
