/*
 * Copyright 2015 Harvard University Library
 *
 * This file is part of FITS (File Information Tool Set).
 *
 * FITS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FITS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FITS.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.harvard.hul.ois.fits

import groovy.io.FileType

import java.io.File;
import java.util.Scanner
import javax.swing.JTextArea

import org.apache.log4j.Logger
import org.custommonkey.xmlunit.*

class TestUtil {
	
	static Logger log = Logger.getLogger(TestUtil.class.getName())
	
	JTextArea textArea
	
	// Constructor to allow writing to the Swing GUI
	TestUtil (JTextArea textArea) {
		this.textArea = textArea	
	}
	
	public List<NonMatchingResult> compareXmlInFileOrFolder(File fileToProcess, String outputType,
		String outputDirPath, String expectedDirPath) {
		
		String actualXmlFileName
		String expectedXmlFileName
		List<NonMatchingResult> diffList = new ArrayList<DetailedDiff>()

		def fileOrDirMsg = "File"
		if (fileToProcess.isDirectory()) {
			fileOrDirMsg = "Folder"
		}
		println "${fileOrDirMsg} to process is: ${fileToProcess}"
		log.info("${fileOrDirMsg} to process is: ${fileToProcess}")

		// If the file to process is a directory, the output must be
		// the output directory, not a file
		if (fileToProcess.isDirectory()) {
			fileToProcess.eachFileRecurse (FileType.FILES) { file ->
				if(!file.name.equalsIgnoreCase(".DS_Store")) {
					// list << file
					expectedXmlFileName = "${expectedDirPath}/${outputType}/${file.name}" + ".xml"
					actualXmlFileName = "${outputDirPath}/${file.name}" + ".fits.xml"
					doDiff(actualXmlFileName, expectedXmlFileName, diffList)
				} // !".DS_Store"				
			} // fileToProcess.eachFileRecurse 
		}
		else {
			expectedXmlFileName = "${expectedDirPath}/${outputType}/${fileToProcess.name}" + ".xml"
			actualXmlFileName = "${outputDirPath}/${fileToProcess.name}" + ".xml"
			doDiff(actualXmlFileName, expectedXmlFileName, diffList)
		}
		return diffList
	}

	public Diff compareXmlFiles(File expectedXmlFile, File actualXmlFile) {
		return compareXmlStrings(readFile(expectedXmlFile), readFile(actualXmlFile))
	}
	
	public String readFile(File file) {
		
		// TODO:
		// Should we check the file up higher, or just let it fail here	
		if (!file.exists()) {
			println "ERROR: ${file} does not exist"
			throw new Exception("File does not exist")
			//System(-1)
		}
		
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
	
	private void doDiff(String actualXmlFileName, String expectedXmlFileName, List<NonMatchingResult> diffList) {
		println "Comparing ${expectedXmlFileName} to ${actualXmlFileName}"
		log.info("Comparing ${expectedXmlFileName} to ${actualXmlFileName}")
		if(textArea != null)
			textArea.append("Comparing ${expectedXmlFileName} to ${actualXmlFileName}")
		else
			println "Comparing ${expectedXmlFileName} to ${actualXmlFileName}"
		
		Diff diff = compareXmlFiles(new File(expectedXmlFileName), new File(actualXmlFileName))
		println "Is Identical: " + diff.identical()
		log.info("Is Identical: " + diff.identical())
		if(textArea != null)
			textArea.append("Is Identical: " + diff.identical())
		else
			println "Is Identical: " + diff.identical()

		if (!diff.identical()) {	// add to diffList

			DetailedDiff detailedDiff = new DetailedDiff(diff);
			diffList.add(new NonMatchingResult(actualXmlFileName:actualXmlFileName,
				expectedXmlFileName:expectedXmlFileName,detailedDiff:detailedDiff))

		}
	}

}
