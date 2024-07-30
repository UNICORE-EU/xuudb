# UNICORE XUUDB

This repository contains the source code for the UNICORE XUUDB,
a service acting as an 'attribute source' as part
of a UNICORE installation. It is an optional service, that is best
suited as a per-site service, providing attributes for multiple
UNICORE/X-like services at a site.

The XUUDB maps a UNICORE user identity (which is formally an X.500
distinguished name (DN)) to a set of attributes. The attributes are
typically used to provide local account details (uid, gid(s)) and
commonly also to provide authorization information, i.e. the
user's role.

## Download

The XUUDB is distributed as part of the "Core Server" bundle and can be
[downloaded from GitHub](https://github.com/UNICORE-EU/server-bundle/releases)

## Documentation

See the [XUUDB manual](https://unicore-docs.readthedocs.io/en/latest/admin-docs/xuudb/index.html)


## Building from source

You need Java and Apache Maven.

The Java code is built and unit tested using

    mvn install

To skip unit testing

    mvn install -DskipTests

The following commands create distribution packages
in tgz, deb and rpm formats


 * tgz

    cd xuudb-all; mvn package -DskipTests -Ppackman -Dpackage.type=bin.tar.gz

 * deb

    cd xuudb-all; mvn package -DskipTests -Ppackman -Dpackage.type=deb -Ddistribution=Debian

 * rpm

    cd xuudb-all; mvn package -DskipTests -Ppackman -Dpackage.type=rpm -Ddistribution=RedHat
