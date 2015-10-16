!/usr/bin/env bash
##
## This is the shell script to run the COMMAND LINE FITS Test Application
##
TEMP_CLASSPATH=./FitsTester-0.3.jar
TEMP_CLASSPATH="$TEMP_CLASSPATH":./lib/groovy-all-2.3.10.jar
TEMP_CLASSPATH="$TEMP_CLASSPATH":./lib/commons-cli-1.3.1.jar
TEMP_CLASSPATH="$TEMP_CLASSPATH":./lib/commons-io-2.4.jar
TEMP_CLASSPATH="$TEMP_CLASSPATH":./lib/log4j-1.2.11.jar
TEMP_CLASSPATH="$TEMP_CLASSPATH":./lib/xmlunit-1.4.jar

ECHO "$TEMP_CLASSPATH"

# concatenate args and use eval/exec to preserve spaces in paths, options and args
args=""
for arg in "$@" ; do
	args="$args \"$arg\""
done

cmd="java -classpath \"$TEMP_CLASSPATH\" edu.harvard.hul.ois.fits.FitsCommandProcessor $args"
eval "exec $cmd"