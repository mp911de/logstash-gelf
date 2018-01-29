#!/bin/bash
set -e

JDK_FEATURE=10
JDK_BUILD=41
JDK_ARCHIVE=openjdk-${JDK_FEATURE}-ea+${JDK_BUILD}_linux-x64_bin.tar.gz

cd ~
wget https://download.java.net/java/jdk${JDK_FEATURE}/archive/${JDK_BUILD}/GPL/${JDK_ARCHIVE}
tar -xzf ${JDK_ARCHIVE}
export JAVA_HOME=~/jdk-${JDK_FEATURE}
export PATH=${JAVA_HOME}/bin:$PATH
cd -
java --version

