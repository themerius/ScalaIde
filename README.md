# ScalaIde

Is a web-based IDE for the Scala Language. For the frontend html5 is used, especially websockets in the backend the play framework is used, which is implemented in Scala.

It is originated as a teamproject at the department of computer science at [University of Applied Sciences, Constance, Germany](http://www.htwg-konstanz.de/).

Thanks to [Dirk](https://github.com/dirkmc/ph), he had previously implemented the compiler-thing. We ported it to Play 2.0.

![ScalaIde Screenshot](https://lh3.googleusercontent.com/-WKTbRBZ009k/T98xE8BcA-I/AAAAAAAAAHY/bWuPr5fQxjE/s800/scalaide.png)

**Features**:

  * Multiple users
  * Each users has a project
  * Automatic compiling for error detection and notice
  * File tree
  * Editor
  * Terminal-Shell (ssh connection)

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

## Run It

Change in `conf/application.conf` the variable `framework.directory` to the play-main-folder.

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

If you want to create the database direcly, call play like this:

```
play -DapplyEvolutions.default=true start
```
_To manage the databse_:

```
play h2-browser run
```

## Work to do

There is also work to do...
