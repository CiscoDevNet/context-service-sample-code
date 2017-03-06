#!/bin/bash
TMP_DIR="tmp_sdk"
SDK_VERSION="2.0.1"

mvn -U install:install-file -Dfile=$TMP_DIR/context-service-sdk-$SDK_VERSION.jar -DgroupId=com.cisco.thunderhead -DartifactId=context-service-sdk -Dversion=$SDK_VERSION -Dpackaging=jar -DpomFile=$TMP_DIR/context-service-sdk-pom.xml

if [ $? -eq 0 ]; then
    rm -rf $TMP_DIR
fi