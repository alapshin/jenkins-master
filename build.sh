#!/usr/bin/env bash
set -euo pipefail

source build.cfg

docker build --tag "${USERNAME}"/"${IMAGE}":latest .
