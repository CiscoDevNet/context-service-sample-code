#!/bin/bash

if [ $# -lt 3 ]; then
    echo Parameters: [targz-filename] [project-dir] [static-sdk-version] [extension-sdk-version]
    echo i.e. context-service-sdk-2.0.1.tar.gz /Users/tweissin/dev/cs-java-sdk-swing-ui 2.0.1 2.0.3
    exit
fi

TAR_GZ=$1
PROJECT_DIR=$2
STATIC_SDK_VERSION=$3
EXT_SDK_VERSION=${4:-$3}
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
echo Using static SDK version: $STATIC_SDK_VERSION
echo Using extension SDK version: $EXT_SDK_VERSION
echo

TMPDIR=/tmp
TMPPATH=$TMPDIR/cs-java-sdk

# Extract the context service SDK tar gz
mkdir $TMPPATH
cd $TMPPATH
tar xvf $TAR_GZ

if [ ! -d context-service-sdk-$STATIC_SDK_VERSION ] ; then
    echo Check SDK version.  Directory context-service-sdk-$STATIC_SDK_VERSION does not exist
    exit
fi

JAR=context-service-sdk-$STATIC_SDK_VERSION.jar
POM=context-service-sdk-$STATIC_SDK_VERSION-pom.xml

for f in $JAR $POM ; do
    if [ ! -e context-service-sdk-$STATIC_SDK_VERSION/$f ] ; then
        echo Check SDK version.  $f does not exist
        exit
    fi
done

# Install the SDK into the local Maven repository
cd context-service-sdk-$STATIC_SDK_VERSION
mvn -U install:install-file -Dfile=$JAR -DgroupId=com.cisco.thunderhead -DartifactId=context-service-sdk -Dversion=$STATIC_SDK_VERSION -Dpackaging=jar -DpomFile=$POM
rm -rf $TMPPATH

EXT_JAR=context-service-sdk-extension-$EXT_SDK_VERSION.jar

# Get the latest SDK extension JAR
cd $PROJECT_DIR
mkdir -p $PROJECT_DIR/plugin
rm $EXT_JAR
wget -O $PROJECT_DIR/plugin/$EXT_JAR https://context-service-downloads.rciad.ciscoccservice.com/files/latest/$EXT_JAR
if [ $? -ne 0 ] ; then
    echo Problem downloading extension SDK.  Is $EXT_SDK_VERSION the correct version?
    exit
fi

# Create the connector.property file
echo path=plugin > $PROP_FILE
echo jar-name=context-service-sdk-extension-$EXT_SDK_VERSION.jar >> $PROP_FILE

echo Done!