#!/bin/bash

set -e

INITIAL_VERSION=1.0-SNAPSHOT

export MAVEN_OPTS="-Xmx1G -Xms128m"

mvn package sonar:sonar \
  $MAVEN_ARGS \
  -Dsonar.host.url=$SONAR_HOST_URL \
  -Dsonar.login=$SONAR_TOKEN \
  -Dsonar.projectVersion=$INITIAL_VERSION
