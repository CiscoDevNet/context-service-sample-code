#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

export CLASSPATH=`ls target/*.jar`
for i in $DIR/target/dependency/*.jar; do
    export CLASSPATH=$CLASSPATH\:$i
done

java com.cisco.thunderhead.sample.importexport.Export $*
