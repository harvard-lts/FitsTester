REM
REM This is the Batch file to run the FITS Test Application
REM
ECHO OFF
set TEMP_CLASSPATH=./FitsTester-1.0.jar
set TEMP_CLASSPATH=%TEMP_CLASSPATH%;lib/groovy-all-2.3.10.jar
set TEMP_CLASSPATH=%TEMP_CLASSPATH%;lib/commons-io-2.4.jar
set TEMP_CLASSPATH=%TEMP_CLASSPATH%;lib/log4j-1.2.11.jar
set TEMP_CLASSPATH=%TEMP_CLASSPATH%;lib/xmlunit-1.4.jar
ECHO ON
ECHO %TEMP_CLASSPATH%

java -cp %TEMP_CLASSPATH%;. edu.harvard.hul.ois.fits.FitsTester_MainGui
