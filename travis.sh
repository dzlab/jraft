#!/bin/bash

set -e

INITIAL_VERSION=1.0-SNAPSHOT

export MAVEN_OPTS="-Xmx1G -Xms128m"

mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent package sonar:sonar $MAVEN_ARGS
