REM
REM This is the Batch file to run the COMMAND LINE Version FITS Test Application
REM
ECHO OFF
set TEMP_CLASSPATH=./FitsTester-0.3.jar
set TEMP_CLASSPATH=%TEMP_CLASSPATH%;lib/groovy-all-2.3.10.jar
set TEMP_CLASSPATH=%TEMP_CLASSPATH%;lib/commons-cli-1.3.1.jar
set TEMP_CLASSPATH=%TEMP_CLASSPATH%;lib/commons-io-2.4.jar
set TEMP_CLASSPATH=%TEMP_CLASSPATH%;lib/log4j-1.2.11.jar
set TEMP_CLASSPATH=%TEMP_CLASSPATH%;lib/xmlunit-1.4.jar
ECHO ON
ECHO %TEMP_CLASSPATH%

java -cp %TEMP_CLASSPATH%;. edu.harvard.hul.ois.fits.FitsCommandProcessor %*