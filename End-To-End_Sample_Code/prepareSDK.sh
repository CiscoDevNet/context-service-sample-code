#!/bin/bash

if [ $# -ne 1 ]; then
    echo Parameters: [targz-filename]
    echo i.e. context-service-sdk-2.0.1.tar.gz
    exit
fi

TAR_GZ=$1

SDK_VERSION=`echo $TAR_GZ | sed -n 's/.*context-service-sdk-\([0-9]\.[0-9]\.[0-9]\)\.tar\.gz/\1/p'`
PROJECT_DIR=$PWD
PROP_FILE="connector.property"

if [ ! -e $TAR_GZ ] ; then
    echo Tar file does not exist: $TAR_GZ
    exit
fi

if [ ! -d $PROJECT_DIR ] ; then
    echo Project directory does not exist: $PROJECT_DIR
    exit
fi

echo Using tar file: $TAR_GZ
echo Using project directory: $PROJECT_DIR
echo Using static SDK version: $SDK_VERSION
echo

TMPDIR=/tmp
TMPPATH=$TMPDIR/cs-java-sdk

# Extract the context service SDK tar gz
mkdir $TMPPATH
cd $TMPPATH
tar xvf $TAR_GZ

if [ ! -d context-service-sdk-$SDK_VERSION ] ; then
    echo Check SDK version.  Directory context-service-sdk-$SDK_VERSION does not exist
    exit
fi

JAR=context-service-sdk-$SDK_VERSION.jar
POM=context-service-sdk-$SDK_VERSION-pom.xml

for f in $JAR $POM ; do
    if [ ! -e context-service-sdk-$SDK_VERSION/$f ] ; then
        echo Check SDK version.  $f does not exist
        exit
    fi
done

# Install the SDK into the local Maven repository
cd context-service-sdk-$SDK_VERSION
mvn -U install:install-file -Dfile=$JAR -DgroupId=com.cisco.thunderhead -DartifactId=context-service-sdk -Dversion=$SDK_VERSION -Dpackaging=jar -DpomFile=$POM

EXT_JAR=context-service-sdk-extension-$SDK_VERSION.jar

# Create the connector.property file
cd $PROJECT_DIR
mkdir -p $PROJECT_DIR/plugin
cp $TMPPATH/context-service-sdk-$SDK_VERSION/$EXT_JAR plugin
rm -rf $TMPPATH

echo path=plugin > $PROP_FILE
echo jar-name=$EXT_JAR >> $PROP_FILE

echo Done!