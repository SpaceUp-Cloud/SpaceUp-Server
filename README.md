## SpaceUp-Server

Buildserver status:  
![TeamCity build status](https://tc.iatlas.dev/app/rest/builds/buildType:id:SpaceUpServer_Build/statusIcon.svg)

## Description

SpaceUp is a "simple" server / client construct, which helps provide or do actions on your webhost. Or just simplify things without using ssh/console.
It does as much possible without a database (only saving credentials / install process), to keep it easy as possible.

The server application is written in kotlin with micronaut as web framework. The client is written in Dart with Flutter as framework.

## Features

* Adding/Deleting domains
* Start/Stop/Restart services
* Inspecting logfiles of the corresponding service
* Have a look on the space

* Other features which are planned:
  * Handle web backends (uberspace web commands)
  * Handle mail (uberspace mail commands)
  * Add/Delete services with internal editor
  * Support GraalVM as native application for smaller footprint
  * Migrating to JVM 11 =< x
  * Self-Updating JAR (Never download it by yourself again!)
  * Write your own (automation) script (*.sus) to even more simplify jobs and stuff.
  Like updating a piece of software. And this script will be automatically available as REST service and visible in the app! Cool, right? 😁
  * ... and so on.

If any other feature is wished, write me a small email, so I will register you for youtrack to open an issue:
*thraax.session@iatlas.technology*

Little big bonus: There is a multiplatform app (written in Flutter) which already consumes the APIs. 😁
You should know it's not tested with ios/osx as I am not an apple user.
But it works for Web, Android, Windows and soon Linux.

## Code Repo / Bug tracker / etc.

I've of course my own tool infrastructure where I keep my code and bugs in place.

Git: https://git.iatlas.dev/SpaceUp

Youtrack: https://yt.iatlas.dev/

Artifactory:
https://artifactory.iatlas.dev/#browse/browse:technology.iatlas.spaceup:technology%2Fiatlas%2Fspaceup%2FSpaceUp

## Build & Run

### Server application

Requirements:

* Java 11
* MongoDb 5.x (see Uberspace Lab how to enable it. It's straightforward 😁)

The application itself is a fatjar which can be directly executed.

```
# Build the jar file
./gradlew assemble

#
# The parameters are necessary for now.
# Later they will be used from the installation process.
#

# Normal prod usecase
java -jar target/lib/SpaceUp-{Version}-all.jar -micronaut.server.host=0.0.0.0 -micronaut.host.port=<Your Port> -mongodb.uri=mongodb://xxx:xxx@localhost:41421/admin

# In dev you can directly supply SSH configuration when you pass "-spaceup.dev.ssh.db-credentials=true"
# Default is '-spaceup.dev.ssh.db-credentials=false'
# If you've got troubles, increase loglevel with '-spaceup.logging.level=DEBUG|TRACE'
java -jar target/lib/SpaceUp-{Version}-all.jar -micronaut.server.host=127.0.0.1 -micronaut.host.port=<Your Port> spaceup.dev.ssh.db-credentials=false -spaceup.ssh.host=<Your server to connect> -spaceup.ssh.username=<User> -spaceup.ssh.password=<Password> -mongodb.uri=mongodb://xxx:xxx@localhost:41421/admin
```

Don't forget to create the uberspace web backend to make it external visible for the client. 😉

*After the first run, you have run the installation procedure* in the app. You'll find the APIKey in the application logs.
In dev mode there are Swagger-ui, Redoc and Rapidoc.

See:

* {server}/swagger-ui
* {server}/redoc
* {server}/rapidoc

After first startup you will be greeted with this console output.
*Beware the output of they API Key, which you need to verify that it is you, when you finish the installation!*

```
 __  __ _                                  _   
|  \/  (_) ___ _ __ ___  _ __   __ _ _   _| |_ 
| |\/| | |/ __| '__/ _ \| '_ \ / _` | | | | __|
| |  | | | (__| | | (_) | | | | (_| | |_| | |_ 
|_|  |_|_|\___|_|  \___/|_| |_|\__,_|\__,_|\__|
  Micronaut (v3.4.1)

12:14:00.174 [main] INFO  i.m.context.env.DefaultEnvironment - Established active environments: [prod]
12:14:02.772 [main] INFO  t.iatlas.spaceup.services.DbService - Created DB Connection to mongodb://xxx:xxx@localhost:41421/admin
   ▄████████    ▄███████▄    ▄████████  ▄████████    ▄████████ ███    █▄     ▄███████▄ 
  ███    ███   ███    ███   ███    ███ ███    ███   ███    ███ ███    ███   ███    ███ 
  ███    █▀    ███    ███   ███    ███ ███    █▀    ███    █▀  ███    ███   ███    ███ 
  ███          ███    ███   ███    ███ ███         ▄███▄▄▄     ███    ███   ███    ███ 
▀███████████ ▀█████████▀  ▀███████████ ███        ▀▀███▀▀▀     ███    ███ ▀█████████▀  
         ███   ███          ███    ███ ███    █▄    ███    █▄  ███    ███   ███        
   ▄█    ███   ███          ███    ███ ███    ███   ███    ███ ███    ███   ███        
 ▄████████▀   ▄████▀        ███    █▀  ████████▀    ██████████ ████████▀   ▄████▀      
        SpaceUp Server (0.24.1-SNAPSHOT)
12:14:03.578 [main] INFO  t.i.s.c.startup.StartupEventListener - Running SpaceUp startup
12:14:03.579 [main] INFO  t.i.s.c.startup.StartupEventListener - Create remote directories
12:14:03.589 [main] INFO  t.i.s.c.startup.StartupEventListener - Create /home/thraax/.spaceup
12:14:03.608 [main] INFO  t.i.s.c.startup.StartupEventListener - Create /home/thraax/.spaceup/tmp
12:14:04.081 [main] INFO  t.i.s.c.startup.StartupEventListener - Finished SpaceUp startup
12:14:04.163 [main] INFO  io.micronaut.runtime.Micronaut - Startup completed in 4417ms. Server Running: http://0.0.0.0:xxx
12:14:06.530 [scheduled-executor-thread-1] INFO  t.i.s.services.SchedulerService - Update domain list
12:14:06.589 [scheduled-executor-thread-1] INFO  t.iatlas.spaceup.services.SshService - Take saved credentials
12:14:06.589 [scheduled-executor-thread-1] INFO  t.iatlas.spaceup.services.SshService - Assuming there is only one configuration
12:14:06.667 [scheduled-executor-thread-1] WARN  t.iatlas.spaceup.services.SshService - To authenticate with Privatekey supply '-spaceup.ssh.privatekey="your path to key"' to JAR.
12:14:06.672 [scheduled-executor-thread-1] INFO  t.iatlas.spaceup.services.SshService - Authenticate SSH via password!
12:14:08.781 [default-nioEventLoopGroup-1-6] INFO  t.i.s.c.a.AuthenticationProviderUserPassword - thraax is authenticated!
12:14:35.148 [default-nioEventLoopGroup-1-2] INFO  t.iatlas.spaceup.services.SshService - Upload script getLogs.sh to /home/thraax/.spaceup/tmp/getLogs.sh
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
