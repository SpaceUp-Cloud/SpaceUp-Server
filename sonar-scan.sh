***REMOVED***!/usr/bin/env bash
***REMOVED***
***REMOVED*** Copyright (c) 2022 Gino Atlas.
***REMOVED***
***REMOVED*** Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
***REMOVED***
***REMOVED*** The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
***REMOVED***
***REMOVED*** THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
***REMOVED***

echo "Gradle requires Java 11"
./gradlew sonarqube  -D"sonar.projectKey=SpaceUp-Server" -D"sonar.host.url=https://sonar.iatlas.dev" -D"sonar.login=2cbf3a38f3f7e3a580a3a83e18bb574df8793ffc"