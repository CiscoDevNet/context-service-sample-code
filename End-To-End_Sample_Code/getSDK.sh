#!/bin/bash

CUR_PATH=$(pwd)
TMP_DIR="tmp_sdk"
echo "Your java application should run from $CUR_PATH"
SDK_VERSION="2.0.1"
SDK_EXT_VERSION="2.0.2"
PROP_FILE="connector.property"
SDK_FILE="$TMP_DIR/context-service-sdk-$SDK_VERSION.jar"
POM_FILE="$TMP_DIR/context-service-sdk-pom.xml"
PLUGIN_FILE="plugin/context-service-sdk-extension-$SDK_EXT_VERSION.jar"
mkdir -p $TMP_DIR
mkdir -p plugin
rm $PROP_FILE
rm -rf plugin/*.jar
echo "Dowloading JARs..."

curl -o $SDK_FILE http://engci-maven-master.cisco.com/artifactory/context-service-release/com/cisco/thunderhead/context-service-sdk/$SDK_VERSION/context-service-sdk-$SDK_VERSION.jar 1> /dev/null 2>&1
curl -u partner1:partner1 -o $PLUGIN_FILE https://context-service-downloads.produs1.ciscoccservice.com/files/latest/context-service-sdk-extension-$SDK_EXT_VERSION.jar 1> /dev/null 2>&1
curl -o $POM_FILE http://engci-maven-master.cisco.com/artifactory/context-service-release/com/cisco/thunderhead/context-service-sdk/$SDK_VERSION/context-service-sdk-$SDK_VERSION.pom 1> /dev/null 2>&1
echo "path=plugin" > $PROP_FILE
echo "jar-name=context-service-sdk-extension-$SDK_EXT_VERSION.jar" >> $PROP_FILE
echo "You are all set to launch your application"
