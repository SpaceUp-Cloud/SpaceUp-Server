## SpaceUp-Server
  
SpaceUp-Server:  
![TeamCity build status](https://tc.iatlas.dev/app/rest/builds/buildType:id:SpaceUpServer_Build/statusIcon.svg)  
SWS:  
![TeamCity build status](https://tc.iatlas.dev/app/rest/builds/buildType:id:SpaceUpServer_Sws_Build/statusIcon.svg)

## Description

SpaceUp-Server is a webservice, which helps provide or do actions on your webhost on *Uberspace*.  
Or just simplify things without using ssh/console.
The server application is written in kotlin with micronaut as web framework. The client is written in Dart with Flutter as framework.

## Features

* Adding/Deleting domains
* Handle web backends
* Start/Stop/Restart services
* Inspecting logfiles of the corresponding service
* Have a look on the disk space
* Write your own (automation) scripts (SWS - Server Web Scripts) to even more simplify jobs and stuff.
  Like updating a piece of software.
* JVM 11 based
* Other features which are planned:
  * Handle mail (uberspace mail)
  * Show web traffic (uberspace traffik)
  * Add/Delete services with an internal editor
  * Support GraalVM as native application for smaller footprint
  * Self-Updating JAR (Never download it by yourself again!)
  And this script will be automatically available as rest service and visible in the app! Cool, right? 😁
  * ... and so on.

If any other feature is wished, or you want to *help me*, write me a small email.  
So I will register you for youtrack to open an issue, or we have a discussion how you can support the project:    
<spaceup-support@iatlas.technology>

Little big bonus: There is a multiplatform app (written in Flutter) which already consumes the APIs. 😁  
You should know it's not tested with ios/osx as I am not an apple user.  
But it works for Web, Android, Windows and Linux.

## Code Repo / Bug tracker / etc.

I've of course my own tool infrastructure where I keep my code and bugs in place.

Git: https://git.iatlas.dev/SpaceUp  
Youtrack: https://yt.iatlas.dev/issues/SU  
Artifactory: https://artifactory.iatlas.dev/

## Build & Run

### Server application

Requirements:

* Java 11
* MongoDb 5.x [see Uberspace Lab how to enable it. It's straightforward 😁](https://lab.uberspace.de/guide_mongodb/)

The application itself is a fatjar which can be directly executed.

---

All configuration properties to can be overriden.  
Above in the configuration file are the Micronaut specifics. **Be carefully!!**
[Micronaut-Properties](https://git.iatlas.dev/SpaceUp/SpaceUp-Server/src/branch/master/src/main/resources/application.properties#L45)  
Here are the SpaceUp specific ones.
[SpaceUp-Properties](https://git.iatlas.dev/SpaceUp/SpaceUp-Server/src/branch/master/src/main/resources/application.properties#L122)

```
# Build the jar file
./gradlew assemble

#
# TODO: Add parameter to override token-secret! Show how to generate it.
#

# Normal prod usecase
java -jar target/lib/SpaceUp-{Version}-all.jar -micronaut.server.host=0.0.0.0 -micronaut.host.port=<Your Port> -mongodb.uri=mongodb://xxx:xxx@localhost:27017/admin

# In dev you can directly supply SSH configuration when you pass "-spaceup.dev.ssh.db-credentials=true"
# Default is '-spaceup.dev.ssh.db-credentials=false'
# If you've got troubles, increase loglevel with '-spaceup.logging.level=DEBUG|TRACE'
java -jar target/lib/SpaceUp-{Version}-all.jar -micronaut.server.host=127.0.0.1 -micronaut.host.port=<Your Port> spaceup.dev.ssh.db-credentials=false -spaceup.ssh.host=<Your server to connect> -spaceup.ssh.username=<User> -spaceup.ssh.password=<Password> -mongodb.uri=mongodb://xxx:xxx@localhost:27017/admin

# TODO: Show how to pass arguments to native image
```

Don't forget to create the uberspace web backend to make it external visible for the client. 😉

*After the first run, you have run the installation procedure* in the app.  

You'll find the APIKey in the application logs.
In **dev** mode there are Swagger-ui, Redoc and Rapidoc.

See:

* {server}/swagger-ui
* {server}/redoc
* {server}/rapidoc

**After the first run, you have run the installation procedure** in the app or via REST.  
You need the API key from the logs to proceed the installation.  
*TODO show how to pass the API key via curl*

All endpoints are secured. Means you have to be logged in and pass the JWT in the HTTP Header.  
*TODO show how to pass the JWT via curl*

*The swagger/redoc site will help you to send those requests.*

After first startup you will be greeted with this console output:

```
 __  __ _                                  _   
|  \/  (_) ___ _ __ ___  _ __   __ _ _   _| |_ 
| |\/| | |/ __| '__/ _ \| '_ \ / _` | | | | __|
| |  | | | (__| | | (_) | | | | (_| | |_| | |_ 
|_|  |_|_|\___|_|  \___/|_| |_|\__,_|\__,_|\__|
  Micronaut (v3.6.1)

14:26:16.672 [main] INFO  i.m.context.env.DefaultEnvironment - Established active environments: [dev]
14:26:17.873 [main] INFO  t.iatlas.spaceup.services.DbService - Created DB Connection to mongodb://[hidden]:[hidden]@localhost:27017/admin
14:26:17.887 [main] INFO  t.iatlas.spaceup.services.DbService - Get development DB
   ▄████████    ▄███████▄    ▄████████  ▄████████    ▄████████ ███    █▄     ▄███████▄ 
  ███    ███   ███    ███   ███    ███ ███    ███   ███    ███ ███    ███   ███    ███ 
  ███    █▀    ███    ███   ███    ███ ███    █▀    ███    █▀  ███    ███   ███    ███ 
  ███          ███    ███   ███    ███ ███         ▄███▄▄▄     ███    ███   ███    ███ 
▀███████████ ▀█████████▀  ▀███████████ ███        ▀▀███▀▀▀     ███    ███ ▀█████████▀  
         ███   ███          ███    ███ ███    █▄    ███    █▄  ███    ███   ███        
   ▄█    ███   ███          ███    ███ ███    ███   ███    ███ ███    ███   ███        
 ▄████████▀   ▄████▀        ███    █▀  ████████▀    ██████████ ████████▀   ▄████▀      
	SpaceUp Server (0.26.0-SNAPSHOT)
14:26:18.467 [main] INFO  t.i.s.c.startup.StartupEventListener - Running SpaceUp startup
14:26:18.468 [main] WARN  t.i.s.c.startup.StartupEventListener - 
                    You are running in DEV mode!
                    If property 'spaceup.dev.ssh.db-credentials' is set to false
                    then supply all necessary SSH configuration as parameters to ensure SpaceUp can run as expected!
14:26:18.468 [main] INFO  t.i.s.c.startup.StartupEventListener - Create local directories
14:26:18.474 [main] INFO  t.i.s.c.startup.StartupEventListener - Create C:\Users\[hidden]\.spaceup
14:26:18.479 [main] INFO  t.i.s.c.startup.StartupEventListener - Create C:\Users\[hidden]\.spaceup\tmp
14:26:18.506 [main] INFO  t.i.s.c.startup.StartupEventListener - Create external directories
14:26:19.282 [main] INFO  t.iatlas.spaceup.services.SshService - Authenticate SSH via password!
14:26:20.041 [main] INFO  t.i.s.c.startup.StartupEventListener - Update SWS cache
14:26:20.099 [main] INFO  t.i.s.c.startup.StartupEventListener - Finished SpaceUp startup
14:26:20.843 [main] INFO  io.micronaut.runtime.Micronaut - Startup completed in 4865ms. Server Running: http://0.0.0.0:9090
```

### Client application

For GNU/Linux, it's as snap available here:

[![Get it from the Snap Store](https://snapcraft.io/static/images/badges/en/snap-store-black.svg)](https://snapcraft.io/spaceup-ui)

For Android I plan to release it later on F-Droid.

But if you like to build it by your own, go ahead with the following!

Requirements:

* Flutter (current version)
* ADB (if you want to install it directly on your Android phone)

```
# platform: apk, web, windows and etc.
flutter build <platform>
```

Afterwards you can find it here:
```
<your-directory>\SpaceUp-UI\spaceup_ui\build\windows\runner\Release
```
