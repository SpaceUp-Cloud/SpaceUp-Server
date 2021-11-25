#!/usr/bin/env bash
echo "Gradle requires Java 11"
./gradlew sonarqube  -D"sonar.projectKey=SpaceUp-Server" -D"sonar.host.url=https://sonar.iatlas.dev" -D"sonar.login=***REMOVED***"