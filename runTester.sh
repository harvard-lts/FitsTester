#!/usr/bin/env bash
##
## This is the shell script to run the FITS Test Application
##
TEMP_CLASSPATH=./FitsTester-0.2.jar
TEMP_CLASSPATH="$TEMP_CLASSPATH":./lib/groovy-all-2.3.10.jar
TEMP_CLASSPATH="$TEMP_CLASSPATH":./lib/commons-io-2.4.jar
TEMP_CLASSPATH="$TEMP_CLASSPATH":./lib/log4j-1.2.11.jar
TEMP_CLASSPATH="$TEMP_CLASSPATH":./lib/xmlunit-1.4.jar

ECHO "$TEMP_CLASSPATH"

cmd="java -classpath \"$TEMP_CLASSPATH\" edu.harvard.hul.ois.fits.FitsTester_MainGui $args"
eval "exec $cmd"