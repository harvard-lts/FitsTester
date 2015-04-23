REM
REM This is the Batch file to run the FITS Test Application
REM
ECHO OFF
set TEMP_CLASSPATH=./FitsTester-1.0.jar
set TEMP_CLASSPATH=%TEMP_CLASSPATH%;test_libs/groovy-all-2.3.10.jar
set TEMP_CLASSPATH=%TEMP_CLASSPATH%;test_libs/commons-io-2.4.jar
set TEMP_CLASSPATH=%TEMP_CLASSPATH%;test_libs/log4j-1.2.11.jar
set TEMP_CLASSPATH=%TEMP_CLASSPATH%;test_libs/xmlunit-1.4.jar
ECHO ON
ECHO %TEMP_CLASSPATH%

java -cp %TEMP_CLASSPATH%;. edu.harvard.hul.ois.fits.FitsTester_MainGui
