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

import groovy.swing.SwingBuilder

import java.awt.BorderLayout
import java.io.File
import javax.swing.filechooser.FileFilter
import javax.swing.text.DefaultCaret
import javax.swing.JFrame
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

import org.apache.log4j.Logger
import org.apache.log4j.PropertyConfigurator
import org.custommonkey.xmlunit.*

class FitsTester_MainGui {
	
	static Logger log = Logger.getLogger(FitsTester_MainGui.class.getName())

	private SwingBuilder swing
	
	String testOutputDir
	def filesToTest = new ArrayList<File>()
	def textArea
	def newline = "\n"
	def tab = "\t"
	
	def STANDARD_ARG = "-x"
	def COMBO_ARG = "-xc"
	
	// Read Data from the config file
	// Get an instance of the configReaderSingleton class
	def configReader = new ConfigReader()
	def config = configReader.getConfig()

	public FitsTester_MainGui() {

		def BL = new BorderLayout()

		def initialPath = System.getProperty("user.dir")

		swing = new SwingBuilder()
		
		// edt method makes sure UI is build on Event Dispatch Thread.
		swing.edt {

			lookAndFeel 'nimbus'  // Simple change in look and feel.
			frame = swing.frame(title:'FITS Tester', size:[800, 400],
			defaultCloseOperation: JFrame.DISPOSE_ON_CLOSE,			
			show:true, layout:new

			BorderLayout()) {

				menuBar {
					menu(text:'Help') {
						menuItem() {
							action(name:'About', closure:{ showAbout() })
						}
						separator()
						menuItem() {
							action( name:'Exit', mnemonic:'E', closure:{ System.exit(0) } )
						}
					}
				}	// menuBar
							
				panel(constraints:BL.NORTH) {
					
					tableLayout {
						tr {
							td { // text property is default, so it is implicit.
								label 'Select Files or a Folder to Test:'
							}
							td{
								button("...", actionPerformed: this.&selectFilesForTest)
							}
						}

						tr {
							td {
								checkBox(id: 'isRecursive', text: 'Recurse Selected Directory', visible: false)
							}
						}
												
						tr {
							td {
								label 'FITS Run Output Directory:'
							}
							td {
								textField testOutputDir, id: 'testOutputDirField', columns: 40
							}
							td{
								button("...", actionPerformed: this.&selectOutputDir)
							}
						}
						
						tr {
							td {
								checkBox(id: 'fileOutputOn', text: 'Enable output to file')
							}
						}
						
						tr {
							td {
								checkBox(id: 'fileOutputCompareOn', text: 'Compare Actual Output XML to Expected files')
							}
						}

						tr {
							td {
								label 'FITS Output Type:'
							}
							td {
								//comboBox(id: 'outputType', items:["FITS", "Standard", "Combo"], 
								//	selectedIndex:1)
								comboBox(id: 'outputType', items:OutputType.values(),
									selectedIndex:1)
							}
						}
						
						
					} // tablelayout				
					
				} // panel
				
				scrollPane(constraints:BL.CENTER) {
					textArea = swing.textArea()
					// Set scroll bar to automatically scroll when text is added
					DefaultCaret caret = (DefaultCaret)textArea.getCaret()
					caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE)
				}
				panel(constraints:BL.SOUTH){
					button("Run", actionPerformed: this.&runTest)
					button(text:"Exit", actionPerformed:{ frame.dispose() })
				}

			} // BorderLayout

		}  // swing.edt

	}
	
	private void selectFilesForTest( event = null) {
		filesToTest.clear()
		File[] files = null
		
		swing.with {
			JFileChooser chooser = new JFileChooser(
				dialogTitle: "Choose files or folder to pass to FITS",
				fileSelectionMode: JFileChooser.FILES_AND_DIRECTORIES)
			chooser.setMultiSelectionEnabled(true)
			chooser.showOpenDialog(frame)

			// Clear the text area, as this is a new run
			textArea.setText(null)
			textArea.append("${newline}Selected files or folder to process: ${newline}")
			
			// If we select a directory track it and show checkbox
			if(chooser.getSelectedFile().isDirectory()) {
				//System.out.println("Got Directory")
				isRecursive.visible = true
			}
			else {
				isRecursive.visible = false
				isRecursive.selected = false
			}
				
			chooser.getSelectedFiles().each {
				filesToTest.add(it)
				// DEBUG
				String text = it.getName()
				println text
				
				doOutside {
					doLater {
						textArea.append(text + newline)
					}
				}
				
			} // chooser.getSelected

		} // swing.with
		
	}
	
	private void selectOutputDir( event = null ) {
		
		swing.with {
			JFileChooser dirChooser = new JFileChooser(
				dialogTitle: "Choose a Directory for Test Output",
				fileSelectionMode: JFileChooser.DIRECTORIES_ONLY,
				// the file filter must show directories, in order to be able to look into them
				fileFilter: [getDescription: {-> "Directories Only"}, accept:{file-> file.isDirectory()}] as FileFilter)
			
			int answer = dirChooser.showOpenDialog(frame)
			if( answer == JFileChooser.APPROVE_OPTION ) {
				doOutside {
					doLater {
						testOutputDirField.setText(dirChooser.getSelectedFile().getPath())
					}
				}
			} // answer
			
		} // swing.with
	}
	
	private void runTest( event = null ) {
		swing.with {
			
			def fileTypeArg = ""
			String selected = outputType.selectedItem
			if (selected.equals("Standard")) {
				fileTypeArg = STANDARD_ARG
		    }
			else if (selected.equals("Combined")) {
				fileTypeArg = COMBO_ARG
			}
			
			// Initialize these once
			def expectedDirPath = config.test.fits.expected.root.dir
			def standardFolder = config.test.fits.expected.Standard.folder
			def fitsFolder = config.test.fits.expected.Fits.folder
			def comboFolder = config.test.fits.expected.Combo.folder
			
			// Verify the folder location of the "Expected" files
			if(fileOutputOn.selected && fileOutputCompareOn.selected) {
				
				// TODO: only initialize these once
				//def standardFolder = config.test.fits.expected.Standard.folder
				//def fitsFolder = config.test.fits.expected.Fits.folder
				//def comboFolder = config.test.fits.expected.Combo.folder
				String fileTypePrefix = standardFolder
				if (selected.equalsIgnoreCase("Fits")) {
					fileTypePrefix = fitsFolder
				}
				else if (selected.equalsIgnoreCase("Combined")) {
					fileTypePrefix = comboFolder
				}
				
				def expectedPath = "${expectedDirPath}/${fileTypePrefix}"
				File expectedFolder = new File(expectedPath)
				if( !expectedFolder.exists() ) {
					JOptionPane.showMessageDialog(null,
						"The Test Expected input Directory ${expectedPath} does not exist.\n " +
						"Please revise the location of the expected test files in fits_tester.properties and try again.",
						"Directory Not Found",
						JOptionPane.ERROR_MESSAGE)
					return
				}
			}
			
			//println filesToTest.size
			if (filesToTest.size < 1) {
				JOptionPane.showMessageDialog(null,
					"Please select some files to test.",
					"Test Files Specified",
					JOptionPane.WARNING_MESSAGE)
				return
			}
			
			if (testOutputDirField.text.length() == 0 ) {
				JOptionPane.showMessageDialog(null,
					"Please specify the Test Output Directory.",
					"Directory Not Specified",
					JOptionPane.WARNING_MESSAGE)
				return
			}

			File f1 = new File(testOutputDirField.text)
			if(!f1.exists()) {
				JOptionPane.showMessageDialog(null,
						"The Test Output Directory does not exist.",
						"Directory Not Found",
						JOptionPane.WARNING_MESSAGE)
				return
			}
			
			// If input directory is a folder, the output location must be
			// Set and a directory
			if(!fileOutputOn.selected) {
				int numFolders = 0
				filesToTest.each { file ->
					if (file.isDirectory() ) {
						numFolders++
					}
				}
				if(numFolders > 0) {
					JOptionPane.showMessageDialog(null,
						"When FITS is run in directory processing mode. " +
						"\"Enable output to file\" must be selected.",
						"Invalid selection",
						JOptionPane.ERROR_MESSAGE)
					return
				}
			} // not file output


			// Path to FITS script or batch file
			def FITS_DIR = config.test.fits.install.dir
			def FITS_PROG = config.test.fits.runner
			def fitsScriptFile = new File("${FITS_DIR}/${FITS_PROG}")
			
			
			// Verify the FITS directory exists before going any further
			def runDir = new File(FITS_DIR)
			if( !runDir.exists() ) {
				JOptionPane.showMessageDialog(null,
					"FITS directory ${runDir.absolutePath} does not exist. ",
					"FITS Installation missing",
					JOptionPane.ERROR_MESSAGE)
				return
			}
		
			//try {
					
				// Separate thread outside the EDT
				doOutside {
						
					filesToTest.each { file ->
						callFits(fitsScriptFile, file, runDir, fileTypeArg,
							fileOutputOn.selected, testOutputDirField.text, isRecursive)
					} // filesToTest.each
	
					// Now do the XML comparison, but only if
					// BOTH of the following are checked
					// 1) 'Enable output to file' is checked
					//   and
					// 2) 'Compare Actual Output XML to Expected files'	
					if(fileOutputOn.selected && fileOutputCompareOn.selected) {				

						// Select the output subfolder based on the output type
						// TODO: verify the below config settings BEFORE using them
						//def standardFolder = config.test.fits.expected.Standard.folder
						//def fitsFolder = config.test.fits.expected.Fits.folder
						//def comboFolder = config.test.fits.expected.Combo.folder
						String fileTypePrefix = standardFolder
						if (selected.equalsIgnoreCase("Fits")) {
							fileTypePrefix = fitsFolder
						}
						else if (selected.equalsIgnoreCase("Combined")) {
							fileTypePrefix = comboFolder
						}
						
						textArea.append(newline + "Beginning the XMLUnit file comparison${newline}")
						
						filesToTest.each { file ->								
							compareResults(file, fileTypePrefix, testOutputDirField.text)	
						}
							

					} // if(fileOutputCompareOn.selected)

					textArea.append( "COMPLETED all processing!!!${newline}")
					log.info ("COMPLETED all processing!!!")
						
				} // doOutside

				
			//} catch(Exception e){
				//Font font = new Font("Serif", Font.BOLD, 20)
				//textArea.setFont(font)
				//textArea.setForeground(Color.red)
				//textArea.setText(e.toString())
			//}
						
		} // swing.with
	}
	
	void callFits(File fitsFile, File fileToProcess, def runDir, String outputType, 
		boolean outputToFile, String outputDirPath, def isRecursive) {
		
		// --------------------------------------------------------------------
		// Create a Process Builder object
		// --------------------------------------------------------------------
		// DEBUG
		// println "Output Type " + outputType
		
		// Default output file params to empty strings
		def outputFileName = ""
		def outputToFileSwitch = ""
		def isRecursiveSwitch = ""
		
		def fileOrDirMsg = "File"
		if(outputToFile) {
			outputToFileSwitch = "-o"
			
			// If the file to process is a directory, the output must be 
			// the output directory, not a file
			//
			// Ex call:
			// ./fits.sh -i /Users/dab980/downloads/remade-video-files
			// -o /Users/dab980/documents/FITS_TEST/FITS_Test_Output
			//
			// TODO: Add validation for this
			if (fileToProcess.isDirectory()) {
				outputFileName =  outputDirPath
				fileOrDirMsg = "Folder"
				
				if(isRecursive.selected) {
					isRecursiveSwitch = "-r"
				}
			}
			else {
				outputFileName =  outputDirPath + "/" +
					fileToProcess.name + ".xml"
			}
		}

		// DEBUG
		// println "Arg for output file: ${outputToFileSwitch} ${outputFileName}"
		
		println "${newline}${fileOrDirMsg} to process is: ${fileToProcess}"
		textArea.append("${newline}${fileOrDirMsg} to process by FITS is: ${fileToProcess}${newline}")
		log.info ("${fileOrDirMsg} to process by FITS is: ${fileToProcess}")
		
		if(isRecursive.selected) {
			textArea.append("Folder will be processed Recursively${newline}")
			log.info("Folder will be processed Recursively${newline}")
		}
		
		textArea.append("Processing ... Please wait ...${newline}")
		
		ProcessBuilder pb = new ProcessBuilder(fitsFile.getAbsolutePath(),
			"-i", fileToProcess.getAbsolutePath(), outputType, isRecursiveSwitch,
			outputToFileSwitch, outputFileName)

		// Set the working directory. The program will run as if you are in this
		// directory.
		pb.directory(runDir)

		// Redirect the error stream (merging both both std out and error stream)
		pb.redirectErrorStream(true)
		
		//
		// TODO: Redirect the output to the GUI's TextArea
		//

		// Add an environment variable to the process
		//pb.environment().put("-i", fileToProcess.getAbsolutePath())
		//pb.environment().put("isProduction", IS_PRODUCTION)
		//pb.environment().put("isTestData", IS_TEST)

		// Start the process and wait for it to finish.
		final Process process = pb.start()

		// --------------------------------------------------------------------
		// Handle the output stream (both std out and error stream are merged)
		// ONLY If we are not outputting to a file
		// --------------------------------------------------------------------
		if(!outputToFile) {
			InputStreamReader isr = new  InputStreamReader(process.getInputStream())
			BufferedReader br = new BufferedReader(isr)
			String lineRead
	
			while ((lineRead = br.readLine()) != null) {
				log.info "FITS_OUT_STREAM >>> " + lineRead
				println "FITS_OUT_STREAM >>> " + lineRead
				textArea.append("FITS_OUT_STREAM >>> ${lineRead}${newline}")
			}
		}

		// --------------------------------------------------------------------

		// 0 indicates normal termination
		boolean processSuccess = (process.waitFor() == 0)


		println "Processed ${fileOrDirMsg} ${fileToProcess} finished with status: " +
			"${processSuccess ? "SUCCESS" :  "FAILURE"}${newline}"

		textArea.append("Processed ${fileOrDirMsg} ${fileToProcess} finished with status: " +
			"${processSuccess ? "SUCCESS" :  "FAILURE"}" +
			"${newline}${newline}")
		
		log.info ("Processed ${fileOrDirMsg} ${fileToProcess} finished with status: " +
			"${processSuccess ? "SUCCESS" :  "FAILURE"}")

		
	} // callFits()
		
	void compareResults(File file, String fileTypePrefix, String testOutputDirField) {
		def fileOrDirMsg = "File"
		if (file.isDirectory()) {
			fileOrDirMsg = "Folder"
		}
		textArea.append("Comparing ${fileOrDirMsg} ${file.name}${newline}")
		log.info("Comparing ${fileOrDirMsg} ${file.name}")
		
		TestUtil app = new TestUtil(textArea)
		
		def expectedDirPath = config.test.fits.expected.root.dir

		List<NonMatchingResult> errResults =
			app.compareXmlInFileOrFolder(
				file, fileTypePrefix,
				testOutputDirField,
				expectedDirPath)
		
		if (errResults.size() == 0) {
			textArea.append("${tab}Success for XML Results comparison ${fileOrDirMsg} ${file.name}${newline}")
			log.info("Success for XML Results comparison ${fileOrDirMsg}")
		}
		else {
			textArea.append("${tab}Number of Errors for ${fileOrDirMsg} ${file.name}: " + errResults.size() +
				"${newline}")
			log.info("Number of Errors for ${fileOrDirMsg} ${file.name}: " + errResults.size())
			log.error("Number of Errors for ${fileOrDirMsg} ${file.name}: " + errResults.size())
		}
		
		// Handle Differences if they exist
		// Interate Each Error and report
		errResults.each () { diff ->

			DetailedDiff detailedDiff = diff.detailedDiff

			// Display any Differences
			List<Difference> diffs = detailedDiff.getAllDifferences()
			StringBuffer differenceDescription = new StringBuffer()
			differenceDescription.append(diffs.size()).append(" differences")

			System.out.println(differenceDescription.toString())
			for(Difference difference : diffs) {
				System.out.println(difference.toString())
				log.error (difference.toString())
			}
		}
		
	} // compareResults

	void showAbout() {
		JOptionPane.showMessageDialog(null,
			'This is the FITS Testing Application.',
			'About',
			JOptionPane.INFORMATION_MESSAGE)
	}

	static main(args) {
		Properties props = new Properties()
		props.load(new FileInputStream("log4j.properties"))
		PropertyConfigurator.configure(props)
		
		log.info ("STARTING - the FITS Tester Test Application")
		log.info ("Using Groovy: ${GroovySystem.version}")
		println "Using Groovy: ${GroovySystem.version}"
		
		FitsTester_MainGui viewer = new FitsTester_MainGui()
		
		log.info ("SHUTDOWN - the FITS Tester Test Application")
	}
}
