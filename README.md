# ScalaIde

Is a web-based IDE for the Scala Language. For the frontend html5 is used with especially websockets; in the backend the play framework is used, which is implemented in Scala.

It is originated as a teamproject at the department of computer science at [University of Applied Sciences, Constance, Germany](http://www.htwg-konstanz.de/).

Thanks to [Dirk](https://github.com/dirkmc/ph), he had previously implemented the compiler-binding. We ported it to Play 2.0.

![ScalaIde Screenshot](https://lh3.googleusercontent.com/-WKTbRBZ009k/T98xE8BcA-I/AAAAAAAAAHY/bWuPr5fQxjE/s800/scalaide.png)

ScalaIde with a bigger Project opened:

![ScalaIde Screenshot bigger Project](https://lh4.googleusercontent.com/-xD0TbYWRNcw/UHAXc0w0fMI/AAAAAAAAAIU/LVTjYQsUT8w/s720/scalaide.jpg)

**Features**:

  * Multiple users
  * Each users has a project
  * Automatic compiling for error detection and notice
  * File tree
  * Editor
  * Terminal-Shell (ssh connection)
  
**Browser Support (Tested on)**:

  * Firefox 13.0.1 (Windows)
  * Safari 5.1.7 (Mac OS X)

**Used technologies**:

  * [HTML5 websocket](http://www.w3.org/TR/websockets/)
  * [AceEditor](http://ace.ajax.org/)
  * [jsTree](http://www.jstree.com/)
  * [Typesafe Stack](http://typesafe.com/stack/download)
  * [Play Framework 2.0](http://www.playframework.org/documentation/2.0/Installing)

This project is optimized for running on UNIX and UNIXlike systems -- especially for the Terminal feature. If you run it on Windows, the Terminal will be deactivated.

## Installation

Steps to do, before you can run ScalaIde.

### Mac OS X

Instructions for installing [Homebrew](http://mxcl.github.com/homebrew/).

Instructions for installing [Scala](http://typesafe.com/stack/download).

```
brew install scala sbt maven giter8
brew install play
```

### Debian or Ubuntu

Instructions for installing [Scala](http://typesafe.com/stack/download).

```
wget http://apt.typesafe.com/repo-deb-build-0002.deb
dpkg --install repo-deb-build-0002.deb 
apt-get update
apt-get install typesafe-stack
apt-get install openjdk-6-jdk  # we need also javac!
```
[Play Framkework Download](http://www.playframework.org/download)

```
cd ~
wget http://download.playframework.org/releases/play-2.0.1.zip
unzip play-2.0.1.zip
# add play to your PATH, via default $HOME/bin is already in your PATH
mkdir bin
ln -s $HOME/play-2.0.1/play bin/play
```

### Windows

Click something here and click something there…

## Configuration

### Basic Configuration (without terminal support)

If you want to run ScalaIde on *Windows* there is **no terminal support** yet and on *UNIX* if you disable the terminal, you can save much configuration effort.

In `conf/application.conf` set the flag `terminal.support=true` if you want to *enable* terminal support, else let the default value `terminal.support=false`.

Via default a `projectspaces` folder is created, where every user has his own folder (relative path in `conf/evolutions/2.sql` file) with source code files and maybe additional jar-files fetched automatically via `sbt update` at user login. Therefore the **login may take some seconds!**

If you want run the server on Windows you must provide in `conf/application.conf` the path to the bat-file, which stats sbt. Change this flag: `sbt.windows.path="C:/path/to/sbt.bat"`.

To compile code the scala compiler is needed in form of jar-files. The play framework delivers this. In `conf/application.conf` you must set a flag to the main folder of your downloaded play installation: `framework.directory="/path/to/play-2.0"`. In this example the play-executable would be in /path/to/play-2.0/play.

### Advanced Configuration

If you want terminal support, there may be more configuration effort. Especially terminal support is only on **UNIX** (like Debian, Mac OS X) available yet.

If you enable the terminal (set in `conf/application.conf` the flag `terminal.support=true`, default ist `false`) we *assume*, you have a real operating-system user for every user in the database. The OS-user must be available via ssh. Users email is also ssh loginname and users password is also the ssh password. (Users marked as public have *never* terminal access/support.)
Further we assume the home of the user is his project path, and only one project per user is allowed yet!
Set an absolute path (path to the user's home) and set the flag, that's an absolute path is set.

You can set the users in `conf/evolutions/2.sql`.

Because of the OS-useres you have more configuration, with groups and rights. Example: One OS-user (the master user) starts the ScalaIde and the client user can login through the Ide and have their own scope and can not kill the ScalaIde-Server. But the master user must have access (read, write) to the client user files!

## Run It

Reminder: Change in `conf/application.conf`

  * the variable `framework.directory` to the play-main-folder,
  * windows users must set `sbt.windows.path`,
  * if you want terminal support set `terminal.support` -> more configuration effort!

Lets begin, type in your shell:

```
git clone git://github.com/themerius/ScalaIde.git
cd ScalaIde
play run
```

Open your websocket-ready browser at `http://localhost:9000`.

You will be asked if you want to create the bootstrap database, with three default users:

  * lars@test.de, password = 123
  * sven@test.de, password = 123
  * simon@test.de, password = 123
  * public@test.de, password = 123

If you want to create the database direcly, instead of `play run`, call play like this:

```
play -DapplyEvolutions.default=true start
```
_To manage the databse_:

```
play h2-browser run
```

## Limits

  * One project per user
