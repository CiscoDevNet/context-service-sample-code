#!/bin/bash

export CATALINA_HOME=`pwd`/shared-tomcat
export CATALINA_BASE=`pwd`/rest-tomcat
$CATALINA_HOME/bin/startup.sh

export CATALINA_BASE=`pwd`/management-tomcat
$CATALINA_HOME/bin/startup.sh

