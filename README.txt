This is a BETA version of the FITS GUI tester/Runner Tool

It is released as is and no warranty is implied.

Use at your own risk.

-------------------------------------------------
I. - To Build:
-------------------------------------------------

1) Have Java Installed
2) Have Gradle Installed or Use the gradle wrapper
gradlew or gradlew.bat

-------------------------------------------------
II. - To Install:
-------------------------------------------------

1) unzip the FitsTester-1.0-bin.zip (located in build/distributions)

2) make sure you have execution writes to

    runTester.sh (Mac/LINUX)
or
    runTester.bat (Windows)


-------------------------------------------------
III. - To Configure:
-------------------------------------------------

Edit the configuration file:
fits_tester.properties

1) Make sure

test.fits.runner

points to the correct FITS start script or batch.

It defaults to Mac/LINUX

2) Make sure

    test.fits.install.dir 

points to where you FITS installation resides

3) TODO: Describe the below

This is used for Testing of Expected Output against Actual Output:

# The location of the expected files for use with XmlUnit
test.fits.expected.root.dir=/Users/dab980/documents/FITS_Test/FITS_Test_Expected
test.fits.expected.Fits.folder=Fits
test.fits.expected.Standard.folder=Standard
test.fits.expected.Combo.folder=Combo

-------------------------------------------------
IV - To start:
-------------------------------------------------

1)
    runTester.sh (Mac/LINUX)
or
    runTester.bat (Windows)

NOTE: There are 2 log files:

FITSTester.log - Run information

FITSTesterERROR.log - Used by the XMLUnit Testing Comparison

-------------------------------------------------
V - Usage:
-------------------------------------------------

1) Select Files or a Folder to Test

Selection Dialog - Slects Multiple Files or a Folder.

* NOTE: All files in a folder will be acted upon if a folder is selected,
but currently, folder recusion is NOT supported

(like the -i command line folder or files)

2) FITS Run Output Directory

Folder Selection Dialog - Where the outputted FITS XML files get generated
(the -o command line folder location)

3) Enable output to file Checkbox - Select this if you wish the output to
go to XML files. Otherwise the output goes to the JAVA window

TODO - capture the output when the checkbox is not selected/checked.

4) Compare to Actual Output XML Expected files Checkbox

TODO - describe this

* For now DO NOT check

5) Select Run button (or Exit Button)

6) FITS Output Type Dropdown

The output type:
    Standard - The Standard Metadata output for the file, like -x param
    FITS - Generic FITS output, the default FITS output 
    Combines - A combined format, like -xc