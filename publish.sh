#!/bin/bash
set -euo pipefail

IMAGE=jenkins-master
USERNAME=alapshin
VERSION=$(cat VERSION)

./build.sh

git tag "${VERSION}" && git push --tags

docker tag "${USERNAME}"/"${IMAGE}":latest "${USERNAME}"/"${IMAGE}":"${VERSION}"
docker push "${USERNAME}"/"${IMAGE}":latest
docker push "${USERNAME}"/"${IMAGE}":"${VERSION}"
