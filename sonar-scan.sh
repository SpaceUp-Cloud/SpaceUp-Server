#!/usr/bin/env bash
echo "Gradle requires Java 11"
./gradlew sonarqube  -D"sonar.projectKey=SpaceUp-Server" -D"sonar.host.url=https://sonar.iatlas.dev" -D"sonar.login=2cbf3a38f3f7e3a580a3a83e18bb574df8793ffc"