#!/bin/bash

set -x
if [ $# != 2 ] ; then
	echo Arguments: tomcat.tar.gz connection.properties
	exit
fi	

# Used to get the full path of a relative path name.
function abspath {
    if [[ -d "$1" ]]
    then
        pushd "$1" >/dev/null
        pwd
        popd >/dev/null
    elif [[ -e $1 ]]
    then
        pushd "$(dirname "$1")" >/dev/null
        echo "$(pwd)/$(basename "$1")"
        popd >/dev/null
    else
        echo "$1" does not exist! >&2
        return 127
    fi
}

TOMCAT_TAR_GZ=`abspath $1`
CONNECTION_PROPERTIES=`abspath $2`
TOMCAT_VERSION=`echo $TOMCAT_TAR_GZ | sed -n "s/.*apache-tomcat-\([0-9][^;]*\).tar.gz/\1/p"`

# Extract the Tomcat distribution
tar xvf $TOMCAT_TAR_GZ

# This sets up 2 Tomcat instances, one for Management and one for the REST API.
# See "Multiple Tomcat Instances" section in https://tomcat.apache.org/tomcat-8.0-doc/RUNNING.txt
rm -rf management-tomcat
rm -rf rest-tomcat
mkdir -p management-tomcat/bin
mkdir -p rest-tomcat/bin
mkdir shared-tomcat

for f in conf logs webapps work temp ; do 
   cp -r apache-tomcat-${TOMCAT_VERSION}/$f management-tomcat
   cp -r apache-tomcat-${TOMCAT_VERSION}/$f rest-tomcat
done

for f in bin lib endorsed ; do 
   cp -r apache-tomcat-${TOMCAT_VERSION}/$f shared-tomcat
done

rm -rf apache-tomcat-*

# For Management Tomcat, use different ports so they don't conflict with REST API.
sed -i -e 's/8080/8082/g' management-tomcat/conf/server.xml
sed -i -e 's/8005/8006/g' management-tomcat/conf/server.xml
sed -i -e 's/8009/8010/g' management-tomcat/conf/server.xml
sed -i -e 's/8443/8444/g' management-tomcat/conf/server.xml

# Copy the Management and REST API webapps into each Tomcat instance.
cp ../REST_API_Sample/target/rest.war rest-tomcat/webapps
cp ../Mgmt_Connector_Sample/target/management.war management-tomcat/webapps

# Copy the Tomcat "setenv.sh" script and configure each to use the
# connectiondata.properties file passed into the script.
cp setenv.sh rest-tomcat/bin
cp setenv.sh management-tomcat/bin

sed -i -e "s:PROPS:$CONNECTION_PROPERTIES:g" rest-tomcat/bin/setenv.sh
sed -i -e "s:PROPS:$CONNECTION_PROPERTIES:g" management-tomcat/bin/setenv.sh
