#!/usr/bin/env bash

source build.cfg

VERSION=$(git describe --tags --exact-match 2>/dev/null)

if [[ $? != 0 ]] ; then
    echo "Can't publish image from untagged commit"
    exit 1
elif [[ "${VERSION}" == *dirty ]] ; then
    echo "Can't publish image with uncommited changes"
fi

./build.sh

docker tag "${USERNAME}"/"${IMAGE}":latest "${USERNAME}"/"${IMAGE}":"${VERSION}"
docker push "${USERNAME}"/"${IMAGE}":latest
docker push "${USERNAME}"/"${IMAGE}":"${VERSION}"
