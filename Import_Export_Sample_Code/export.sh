#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

CLASSPATH=`ls target/*.jar`
for i in $DIR/target/dependency/*.jar; do
    CLASSPATH=$CLASSPATH\:$i
done

java com.cisco.thunderhead.sample.importexport.Export $*
