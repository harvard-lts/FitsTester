package edu.harvard.hul.ois.fits

import groovy.io.FileType

import java.io.File;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.*

class TestUtil {
	
	static Logger log = Logger.getLogger(TestUtil.class.getName())
	
	public List<NonMatchingResult> compareXmlInFileOrFolder(File fileToProcess, String outputType,
		String outputDirPath, String expectedDirPath) {
		
		String actualXmlFileName
		String expectedXmlFileName
		List<NonMatchingResult> diffList = new ArrayList<DetailedDiff>()
		
		// If the file to process is a directory, the output must be
		// the output directory, not a file
		println "File or folder to process is: ${fileToProcess}"

		if (fileToProcess.isDirectory()) {

			fileToProcess.eachFileRecurse (FileType.FILES) { file ->
				
				if(!file.name.equalsIgnoreCase(".DS_Store")) {
					// list << file
					expectedXmlFileName = "${expectedDirPath}/${outputType}/${file.name}" + ".xml"
					actualXmlFileName = "${outputDirPath}/${file.name}" + ".fits.xml"
					
					println "Comparing ${expectedXmlFileName} to ${actualXmlFileName}"
					
					Diff diff = compareXmlFiles(new File(expectedXmlFileName), new File(actualXmlFileName))
					
					println "Is Identical: " + diff.identical()
					if (!diff.identical()) {	// add to diffList

						DetailedDiff detailedDiff = new DetailedDiff(diff);
						
						//NonMatchingResult errResult = new NonMatchingResult()
						//errResult.actualXmlFileName = actualXmlFileName
						//errResult.expectedXmlFileName = expectedXmlFileName
						//errResult.detailedDiff = detailedDiff
						//
						//NonMatchingResult errResult = new NonMatchingResult(actualXmlFileName:actualXmlFileName,
						//	expectedXmlFileName:expectedXmlFileName,detailedDiff:detailedDiff)
						//
						//diffList.add(errResult)
						
						diffList.add(new NonMatchingResult(actualXmlFileName:actualXmlFileName,
							expectedXmlFileName:expectedXmlFileName,detailedDiff:detailedDiff))

					} // !diff.identical()
		
				}
				

			}
		}
		else {
			expectedXmlFileName = "${expectedDirPath}/${outputType}/${fileToProcess.name}" + ".xml"
			actualXmlFileName = "${outputDirPath}/${fileToProcess.name}" + ".xml"
			Diff diff = compareXmlFiles(new File(expectedXmlFileName), new File(actualXmlFileName))
			
			println "Is Identical: " + diff.identical()
			if (!diff.identical()) {	// add to diffList

				DetailedDiff detailedDiff = new DetailedDiff(diff);
				
				//NonMatchingResult errResult = new NonMatchingResult()
				//errResult.actualXmlFileName = actualXmlFileName
				//errResult.expectedXmlFileName = expectedXmlFileName
				//errResult.detailedDiff = detailedDiff
				//
				//NonMatchingResult errResult = new NonMatchingResult(actualXmlFileName:actualXmlFileName,
				//	expectedXmlFileName:expectedXmlFileName,detailedDiff:detailedDiff)
				//
				//diffList.add(errResult)
				
				diffList.add(new NonMatchingResult(actualXmlFileName:actualXmlFileName,
					expectedXmlFileName:expectedXmlFileName,detailedDiff:detailedDiff))

			} // !diff.identical()
		}
		
		return diffList
	}

	public Diff compareXmlFiles(File expectedXmlFile, File actualXmlFile) {
		return compareXmlStrings(readFile(expectedXmlFile), readFile(actualXmlFile))
	}
	
	public String readFile(File file) {
		Scanner scan = new Scanner(file)
		String xmlStr = scan.
				useDelimiter("\\Z").next()
		scan.close()
		return xmlStr
	}
	
	public Diff compareXmlStrings(String expectedXmlStr, String actualXmlStr) {
		// Set up XMLUnit
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setNormalizeWhitespace(true);

		Diff diff = new Diff(expectedXmlStr,actualXmlStr);

		// Initialize attributes or elements to ignore for difference checking
		diff.overrideDifferenceListener(new IgnoreNamedElementsDifferenceListener(
				"toolversion",
				"dateModified",
				"fslastmodified",
				"startDate",
				"startTime",
				"timestamp",
				"fitsExecutionTime",
				"executionTime",
				"filepath",
				"location"));
			
		return diff
	}
	
//	static main(args) {
//		
//		TestUtil app = new TestUtil()
//		
////		File actual = new File(
////			"/Users/dab980/documents/fits_Test/FITS_Test_Output/FITS-SAMPLE-44_1_1_4_4_4_6_1_1_2_3_1.mp4.xml")
////		
////		File expected = new File(
////			"/Users/dab980/documents/fits_Test/FITS_Test_Expected/FITS-SAMPLE-44_1_1_4_4_4_6_1_1_2_3_1_mp4_FITS.xml")
////		
////		Diff diff = app.compareXmlFiles(expected, actual)
////		println "Is Identical: " + diff.identical()
////		
////		if (!diff.identical()) {
////
////			DetailedDiff detailedDiff = new DetailedDiff(diff);
////
////			// Display any Differences
////			List<Difference> diffs = detailedDiff.getAllDifferences();
////			if (!diff.identical()) {
////				StringBuffer differenceDescription = new StringBuffer();
////				differenceDescription.append(diffs.size()).append(" differences");
////
////				System.out.println(differenceDescription.toString());
////				for(Difference difference : diffs) {
////					System.out.println(difference.toString());
////				}
////
////			}
////
////		} // !diff.identical()
//		
//
//		String outputDirPath = "/Users/dab980/documents/fits_Test/FITS_Test_Output"
//		String expectedDirPath = "/Users/dab980/documents/fits_Test/FITS_Test_Expected"
//
//		File toProcess = new File(
//				"/Users/dab980/documents/fits_Test/FITS_Test_Output")
//		List<NonMatchingResult> errResults = app.compareXmlInFileOrFolder(toProcess, "FITS",
//				outputDirPath, expectedDirPath)
//
//		// Interate Each Error and report
//		errResults.each () { diff ->
//
//			DetailedDiff detailedDiff = diff.detailedDiff
//
//			// Display any Differences
//			List<Difference> diffs = detailedDiff.getAllDifferences();
//			StringBuffer differenceDescription = new StringBuffer();
//			differenceDescription.append(diffs.size()).append(" differences");
//
//			System.out.println(differenceDescription.toString());
//			for(Difference difference : diffs) {
//				System.out.println(difference.toString());
//				log.error (difference.toString())
//			}
//
//		}
//		
//	}

}
